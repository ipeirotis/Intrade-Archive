package intrade.entities;

import intrade.utils.Utilities;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class Contract {

	private HttpServletResponse	r;

	private void print(String message) {

		try {
			r.getWriter().println(message);
			r.getWriter().flush();
			r.flushBuffer();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void debug_setResponse(HttpServletResponse r) {

		this.r = r;
	}

	public static int getTime_threshold_minutes() {

		return time_threshold_minutes;
	}

	public static void setTime_threshold_minutes(int timeThresholdMinutes) {

		time_threshold_minutes = timeThresholdMinutes;
	}

	public static int getTrade_time_threshold_minutes() {

		return trade_time_threshold_minutes;
	}

	public static void setTrade_time_threshold_minutes(int tradeTimeThresholdMinutes) {

		trade_time_threshold_minutes = tradeTimeThresholdMinutes;
	}

	public Long getLastUTCInsertedTrades() {

		return lastUTCInsertedTrades;
	}

	public void setLastUTCInsertedTrades(Long lastUTCInsertedTrades) {

		this.lastUTCInsertedTrades = lastUTCInsertedTrades;
	}

	public Long getLastretrievedTrades() {

		return lastretrievedTrades;
	}

	public void setLastretrievedTrades(Long lastretrievedTrades) {

		this.lastretrievedTrades = lastretrievedTrades;
	}

	public Blob getFileTrades() {

		return fileTrades;
	}

	public void setFileTrades(Blob fileTrades) {

		this.fileTrades = fileTrades;
	}

	// We should not attempt to fetch daily prices more often than every 12
	// hours
	public static int	time_threshold_minutes				= 12 * 60;

	// We get the trades at most every three hours
	public static int	trade_time_threshold_minutes	= 3 * 60;

	public static Key generateKeyFromID(String id) {

		return KeyFactory.createKey(Contract.class.getSimpleName(), "id" + id);
	}

	public static int time_threshold() {

		return time_threshold_minutes * 60 * 1000;
	}

	public static int trade_time_threshold() {

		return trade_time_threshold_minutes * 60 * 1000;
	}

	@Persistent
	private Boolean	archived;

	@Persistent
	private String	ccy;

	@Persistent
	private Long		endDate;

	@Persistent
	private String	eventid;

	@Persistent
	private Long		expiryDate;

	@Persistent
	private Double	expiryPrice;

	@Persistent
	private Blob		fileCSV;

	@Persistent
	private Blob		fileXML;

	@Persistent
	private Blob		fileTrades;

	@Persistent
	private String	id;

	@Persistent
	private String	inRunning;

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key			key;

	@Persistent
	private Long		lastprocessed					= new Long(-1);

	@Persistent
	private Integer	lastLineInsertedXML		= new Integer(0);

	@Persistent
	private Integer	lastLineInsertedCSV		= new Integer(0);

	@Persistent
	private Long		lastUTCInsertedTrades	= new Long(-1);

	@Persistent
	private Long		lastretrievedCSV			= new Long(-1);

	@Persistent
	private Long		lastretrievedTrades		= new Long(-1);

	@Persistent
	private Long		lastretrievedXML			= new Long(-1);

	@Persistent
	private Long		laststoredCSV					= new Long(-1);

	@Persistent
	private Long		laststoredXML					= new Long(-1);

	@Persistent
	private String	name;

	@Persistent
	private Long		startDate;

	@Persistent
	private String	state;

	@Persistent
	private String	symbol;

	@Persistent
	private String	tickSize;

	@Persistent
	private String	tickValue;

	@Persistent
	private String	totalVolume;

	@Persistent
	private String	type;

	public Contract(String id, String eventid, String name, String symbol, String totalVolume, String ccy,
			String inRunning, String state, String tickSize, String tickValue, String type, Long startDate, Long endDate) {

		this.id = id;
		Key k = generateKeyFromID(id);
		this.setKey(k);

		this.eventid = eventid;
		this.symbol = symbol;
		this.name = name;
		this.totalVolume = totalVolume;
		this.ccy = ccy;
		this.inRunning = inRunning;
		this.state = state;
		this.tickSize = tickSize;
		this.tickValue = tickValue;
		this.type = type;

		this.startDate = startDate;
		this.endDate = endDate;

		this.archived = false;
		this.expiryDate = (long) 0;
		this.expiryPrice = -1.0;

	}

	public boolean fetchCSV() {

		String url = "http://data.intrade.com/graphing/jsp/downloadClosingPrice.jsp?contractId=" + this.id;
		Long now = (new Date()).getTime();

		// If more than it has been some time since last fetch, get it again.
		if (now - this.lastretrievedCSV > time_threshold()) {
			byte[] file = Utilities.getFile(url);
			if (file == null) {
				print("Cound not retrieve CSV file:" + url);
				return false;
			}

			this.fileCSV = new Blob(file);
			this.lastretrievedCSV = now;
		} else {
			print("Cached:" + this.id);

			print("Cached:" + DateFormat.getDateTimeInstance().format(this.lastretrievedCSV));
		}
		return true;
	}

	public boolean fetchXML() {

		String url = "http://api.intrade.com/jsp/XML/MarketData/ClosingPrice.jsp?conID=" + this.id;
		Long now = (new Date()).getTime();
		// If more than it has been some time since last fetch, get it again.
		if (now - this.lastretrievedCSV > time_threshold()) {
			byte[] file = Utilities.getFile(url);
			if (file == null) {
				print("Cound not retrieve XML file:" + url);
				return false;
			}

			this.fileXML = new Blob(file);
			this.lastretrievedXML = now;
		}
		return true;
	}

	public boolean fetchTrades() {

		// https://api.intrade.com/jsp/XML/TradeData/TimeAndSales.jsp?conID=683800&timezone=US/Eastern&timestamp=0

		// http://data.intrade.com/graphing/jsp/downloadTaS.jsp?contractId=683800&timezone=US/Eastern&timestamp=0

		String url = "https://api.intrade.com/jsp/XML/TradeData/TimeAndSales.jsp?conID=" + this.id;
		Long now = (new Date()).getTime();
		// If more than it has been some time since last fetch, get it again.
		if (this.lastretrievedTrades == null) {
			this.lastretrievedTrades = new Long(0);
		}

		if (now - this.lastretrievedTrades > trade_time_threshold()) {
			byte[] file = Utilities.getFile(url);
			if (file == null) {
				print("Cound not retrieve Trades file:" + url);
				return false;
			}

			this.fileTrades = new Blob(file);
			this.lastretrievedTrades = now;
		}
		return true;
	}

	public Boolean getArchived() {

		return archived;
	}

	public String getCcy() {

		return ccy;
	}

	public Long getEndDate() {

		return endDate;
	}

	public String getEventid() {

		return eventid;
	}

	public Long getExpiryDate() {

		return expiryDate;
	}

	public Double getExpiryPrice() {

		return expiryPrice;
	}

	public Blob getFileCSV() {

		return fileCSV;
	}

	public Blob getFileXML() {

		return fileXML;
	}

	public String getId() {

		return id;
	}

	public String getInRunning() {

		return inRunning;
	}

	public Key getKey() {

		return key;
	}

	public Integer getLastLineInsertedCSV() {

		return lastLineInsertedCSV;
	}

	public Long getLastprocessed() {

		return lastprocessed;
	}

	public Long getLaststoredCSV() {

		return laststoredCSV;
	}

	public Long getLaststoredXML() {

		return laststoredXML;
	}

	public String getName() {

		return name;
	}

	public Long getStartDate() {

		return startDate;
	}

	public String getState() {

		return state;
	}

	public String getSymbol() {

		return symbol;
	}

	public String getTickSize() {

		return tickSize;
	}

	public String getTickValue() {

		return tickValue;
	}

	public String getTotalVolume() {

		return totalVolume;
	}

	public String getType() {

		return type;
	}

	public void setArchived(Boolean archived) {

		this.archived = archived;
	}

	public void setCcy(String ccy) {

		this.ccy = ccy;
	}

	public void setEndDate(Long endDate) {

		this.endDate = endDate;
	}

	public void setEventid(String eventid) {

		this.eventid = eventid;
	}

	public void setExpiryDate(Long expiryDate) {

		this.expiryDate = expiryDate;
	}

	public void setExpiryPrice(Double expiryPrice) {

		this.expiryPrice = expiryPrice;
	}

	public void setFileCSV(Blob fileCSV) {

		this.fileCSV = fileCSV;
	}

	public void setFileXML(Blob fileXML) {

		this.fileXML = fileXML;
	}

	public void setId(String id) {

		this.id = id;
	}

	public void setInRunning(String inRunning) {

		this.inRunning = inRunning;
	}

	public void setKey(Key key) {

		this.key = key;
	}

	public void setLastLineInsertedCSV(Integer lastLineInsertedCSV) {

		this.lastLineInsertedCSV = lastLineInsertedCSV;
	}

	public void setLastprocessed(Long lastprocessed) {

		this.lastprocessed = lastprocessed;
	}

	public void setLaststoredCSV(Long laststoredCSV) {

		this.laststoredCSV = laststoredCSV;
	}

	public void setLaststoredXML(Long laststoredXML) {

		this.laststoredXML = laststoredXML;
	}

	public void setName(String name) {

		this.name = name;
	}

	public void setStartDate(Long startDate) {

		this.startDate = startDate;
	}

	public void setState(String state) {

		this.state = state;
	}

	public void setSymbol(String symbol) {

		this.symbol = symbol;
	}

	public void setTickSize(String tickSize) {

		this.tickSize = tickSize;
	}

	public void setTickValue(String tickValue) {

		this.tickValue = tickValue;
	}

	public void setTotalVolume(String totalVolume) {

		this.totalVolume = totalVolume;
	}

	public void setType(String type) {

		this.type = type;
	}

	public String toString() {

		return "C:(" + id + ',' + eventid + ',' + symbol + ',' + name + ',' + totalVolume + ',' + this.expiryDate + ','
				+ this.expiryPrice + ',' + ccy + ',' + inRunning + ',' + state + ',' + tickSize + ',' + tickValue + ',' + type
				+ ")";

	}

}
