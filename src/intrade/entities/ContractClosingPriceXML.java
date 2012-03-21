package intrade.entities;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class ContractClosingPriceXML implements
		Comparable<ContractClosingPriceXML> {

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;

	private String contractid;

	private Date date;

	private Long dt;

	private Double high;

	private Double low;

	private Double price;

	public ContractClosingPriceXML(String contractid, Long dt, Double price,
			Double low, Double high) {
		this.contractid = contractid;
		this.date = new Date(dt);
		this.dt = dt;
		this.price = price;
		this.low = low;
		this.high = high;

		Key k = KeyFactory.createKey(
				ContractClosingPriceXML.class.getSimpleName(), "id"
						+ contractid + dt);
		this.setKey(k);

	}

	public int compareTo(ContractClosingPriceXML p) {

		if (!p.getContractid().equals(contractid)) {
			return contractid.compareTo(p.getContractid());
		}
		return date.compareTo(p.getDate());
	}

	public boolean equals(Object o) {
		if (o instanceof ContractClosingPriceXML) {
			ContractClosingPriceXML c = (ContractClosingPriceXML) o;
			if (c.getContractid().equals(contractid)
					&& c.getDate().equals(date)) {
				return true;
			}
			return false;
		}
		throw new ClassCastException();
	}

	public String getContractid() {
		return contractid;
	}

	public Date getDate() {
		return date;
	}

	public Long getDt() {
		return dt;
	}

	public Double getHigh() {
		return high;
	}

	public Double getLow() {
		return low;
	}

	public Double getPrice() {
		return price;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public String toString() {
		return "CP:(" + contractid + ',' + date + ',' + price + ',' + low + ','
				+ high + ',' + dt + ")";
	}

}
