package intrade.entities;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class EventGroup {

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;

	@Persistent
	private String displayOrder;

	@Persistent
	private String eventClassId;

	@Persistent
	private String id;

	@Persistent
	private Long lastretrieved = (long) 0;

	@Persistent
	private String name;

	public EventGroup(String id, String name, String displayOrder,
			String eventClassId) {
		this.id = id;
		this.name = name;
		this.displayOrder = displayOrder;
		this.eventClassId = eventClassId;

		Key key = generateKeyFromID(id);
		this.setKey(key);

	}

	public static Key generateKeyFromID(String id) {
		return KeyFactory
				.createKey(EventGroup.class.getSimpleName(), "id" + id);
	}

	public String getDisplayOrder() {
		return displayOrder;
	}

	public String getEventClassId() {
		return eventClassId;
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

	public void setDisplayOrder(String displayOrder) {
		this.displayOrder = displayOrder;
	}

	public void setEventClassId(String eventClassId) {
		this.eventClassId = eventClassId;
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
		return "G:(" + id + ',' + name + ',' + displayOrder + ','
				+ eventClassId + ")";
	}

}
