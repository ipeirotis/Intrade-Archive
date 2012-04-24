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
public class ContractTrade implements Comparable<ContractTrade> {

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key			key;

	@Persistent
	private String	contractid;

	@Persistent
	private Long		date;

	@Persistent
	private Double	price;

	@Persistent
	private Long		volume;

	public ContractTrade(String contractid, Long date, Double price, Long volume) {

		this.contractid = contractid;
		this.date = date;
		this.price = price;
		this.volume = volume;

		Key k = generateKeyFromID(contractid, date, price, volume);
		this.setKey(k);

	}

	public Double getPrice() {

		return price;
	}

	public static Key generateKeyFromID(String contractid, Long date, Double price, Long vol) {

		return KeyFactory.createKey(ContractTrade.class.getSimpleName(), "id" + contractid + "-" + date + "-" + price + "-"
				+ vol);
	}

	public int compareTo(ContractTrade p) {

		int cc = this.contractid.compareTo(p.getContractid());
		if (cc != 0) {
			return cc;
		}

		int cd = this.date.compareTo(p.getDate());
		if (cd != 0) {
			return cd;
		}

		int cp = this.price.compareTo(p.getPrice());
		if (cp != 0) {
			return cp;
		}

		return this.volume.compareTo(p.getVolume());
	}

	public boolean equals(Object o) {

		if (o instanceof ContractTrade) {
			ContractTrade c = (ContractTrade) o;
			if (c.getContractid().equals(contractid) && c.getDate().equals(date)) {
				return true;
			}
			return false;
		}
		throw new ClassCastException();
	}

	public String getContractid() {

		return contractid;
	}

	public Long getDate() {

		return date;
	}

	public Key getKey() {

		return key;
	}

	public Long getVolume() {

		return volume;
	}

	public void setKey(Key key) {

		this.key = key;
	}

	public String toString() {

		return "CPCSV:(" + contractid + ','
				+ DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL).format(this.date) + ',' + this.price + ','
				+ this.volume + ")";
	}

}
