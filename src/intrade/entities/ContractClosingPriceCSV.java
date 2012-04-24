package intrade.entities;

import java.text.DateFormat;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class ContractClosingPriceCSV implements Comparable<ContractClosingPriceCSV> {

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key			key;

	@Persistent
	private Double	close;

	@Persistent
	private String	contractid;

	@Persistent
	private Long		date;

	public Key getKey() {

		return key;
	}

	@Persistent
	private Double	high;

	@Persistent
	private Double	low;

	@Persistent
	private Double	open;

	@Persistent
	private Long		volume;

	public ContractClosingPriceCSV(String contractid, Long date, Double open, Double low, Double high, Double close,
			Long volume) {

		this.contractid = contractid;
		this.date = date;
		this.open = open;
		this.close = close;
		this.low = low;
		this.high = high;
		this.volume = volume;

		Key k = generateKeyFromID(contractid, date);
		this.setKey(k);

	}

	public static Key generateKeyFromID(String contractid, Long date) {

		return KeyFactory.createKey(ContractClosingPriceCSV.class.getSimpleName(), "id" + contractid + "-" + date);
	}

	public int compareTo(ContractClosingPriceCSV p) {

		if (!p.getContractid().equals(contractid)) {
			return contractid.compareTo(p.getContractid());
		}
		return date.compareTo(p.getDate());

	}

	public boolean equals(Object o) {

		if (o instanceof ContractClosingPriceCSV) {
			ContractClosingPriceCSV c = (ContractClosingPriceCSV) o;
			if (c.getContractid().equals(contractid) && c.getDate().equals(date)) {
				return true;
			}
			return false;
		}
		throw new ClassCastException();
	}

	public Double getClose() {

		return close;
	}

	public String getContractid() {

		return contractid;
	}

	public Long getDate() {

		return date;
	}

	public Double getHigh() {

		return high;
	}

	public Double getLow() {

		return low;
	}

	public Double getOpen() {

		return open;
	}

	public Long getVolume() {

		return volume;
	}

	public void setKey(Key key) {

		this.key = key;
	}

	public String toString() {

		return "CPCSV:(" + contractid + ',' + DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL).format(date)
				+ ',' + open + ',' + low + ',' + high + ',' + close + ',' + volume + ")";
	}

}
