package intrade.entities;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class EventClass {

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key			key;

	@Persistent
	private int			displayOrder;

	@Persistent
	private String	id;

	@Persistent
	private Long		lastretrieved	= (long) 0;

	@Persistent
	private String	name;

	public EventClass(String id, String name, int displayOrder) {

		this.id = id;
		this.name = name;
		this.displayOrder = displayOrder;

		Key key = generateKeyFromID(id);
		this.setKey(key);
	}

	public static Key generateKeyFromID(String id) {

		return KeyFactory.createKey(EventClass.class.getSimpleName(), "id" + id);
	}

	public int getDisplayOrder() {

		return displayOrder;
	}

	public String getId() {

		return id;
	}

	public Key getKey() {

		return key;
	}

	public Long getLastretrieved() {

		return lastretrieved;
	}

	public String getName() {

		return name;
	}

	public void setDisplayOrder(int displayOrder) {

		this.displayOrder = displayOrder;
	}

	public void setId(String id) {

		this.id = id;
	}

	public void setKey(Key key) {

		this.key = key;
	}

	public void setLastretrieved(Long lastretrieved) {

		this.lastretrieved = lastretrieved;
	}

	public void setName(String name) {

		this.name = name;
	}

	public String toString() {

		return "C:(" + id + ',' + name + ',' + displayOrder + ")";
	}
}
