package intrade.entities;

import intrade.utils.Utilities;

import java.text.DateFormat;
import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class MarketXML {

	public static int	time_threshold_minutes	= 15;

	private static int time_threshold() {

		return time_threshold_minutes * 60 * 1000;
	}

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key																			key;

	@Persistent
	private String																	URL;

	@Persistent
	private com.google.appengine.api.datastore.Blob	filecontent;

	@Persistent
	private Long																		timestamp			= new Long(-1);

	@Persistent
	private Long																		lastretrieved	= new Long(-1);

	public Long getLastretrieved() {

		return lastretrieved;
	}

	public void setLastretrieved(Long lastretrieved) {

		this.lastretrieved = lastretrieved;
	}

	public MarketXML(String URL) {

		this.URL = URL;

		Key k = generateKeyFromID(URL);
		this.setKey(k);

	}

	public static Key generateKeyFromID(String URL) {

		return KeyFactory.createKey(MarketXML.class.getSimpleName(), URL);
	}

	public void fetch() {

		byte[] content = Utilities.getFile(this.URL);
		if (content != null) {
			Document d = Utilities.getXMLFromString(content);
			Long t = getMarketTime(d);
			this.timestamp = t;
			this.filecontent = new Blob(content);
			this.lastretrieved = (new Date()).getTime();
		} else {
			this.filecontent = null;
			this.lastretrieved = (long) 0;
			this.timestamp = (long) 0;
		}

	}

	public Key getKey() {

		return key;
	}

	private Long getMarketTime(Document d) {

		NodeList nl_time = d.getElementsByTagName("MarketData");

		if (nl_time.getLength() != 1) {
			return (long) 0;
		}

		try {
			Node nd_marketdata = nl_time.item(0);
			Long time = Long.parseLong(nd_marketdata.getAttributes().getNamedItem("intrade.timestamp").getNodeValue());
			return time;
		} catch (Exception e) {
			return (long) 0;
		}

	}

	public Long getTimestamp() {

		return timestamp;
	}

	public String getURL() {

		return URL;
	}

	public Document getXML() {

		if (filecontent != null) {
			Document d = Utilities.getXMLFromString(filecontent.getBytes());
			return d;
		} else
			return null;
	}

	public void refresh() {

		Long now = (new Date()).getTime();

		System.out.println("Now:" + now);
		System.out.println("Now:" + DateFormat.getDateTimeInstance().format(now));

		System.out.println("Last retrieved:" + this.lastretrieved);
		System.out.println("Last retrieved:" + DateFormat.getDateTimeInstance().format(this.lastretrieved));

		System.out.println("File:" + this.timestamp);
		System.out.println("File:" + DateFormat.getDateTimeInstance().format(this.timestamp));

		System.out.println("Diff:" + (now - this.lastretrieved));

		// If more than it has been some time since last fetch, get it again.
		if (now - this.lastretrieved > time_threshold()) {
			fetch();
		} else {
			;
		}

	}

	public void setKey(Key key) {

		this.key = key;
	}

	public void setTimestamp(Long timestamp) {

		this.timestamp = timestamp;
	}

	public void setURL(String url) {

		URL = url;
	}

	public String toString() {

		return "URL:(" + URL + ',' + timestamp + ")";
	}

}
