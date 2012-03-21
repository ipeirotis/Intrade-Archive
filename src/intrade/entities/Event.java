package intrade.entities;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Event {

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;

	@Persistent
	private String description;

	@Persistent
	private String displayOrder;

	@Persistent
	private Long endDate;

	@Persistent
	private String groupId;

	@Persistent
	private String id;

	@Persistent
	private Long lastretrieved = (long) 0;

	@Persistent
	private String name;

	@Persistent
	private Long startDate;

	public Event(String id, String groupId, String name, String displayOrder,
			String description, Long startDate, Long endDate) {
		this.id = id;
		this.groupId = groupId;
		this.name = name;
		this.displayOrder = displayOrder;
		this.description = description;
		this.startDate = startDate;
		this.endDate = endDate;

		Key k = generateKeyFromID(id);
		this.setKey(k);

	}

	public static Key generateKeyFromID(String id) {
		return KeyFactory.createKey(Event.class.getSimpleName(), "id" + id);
	}

	public String getDescription() {
		return description;
	}

	public String getDisplayOrder() {
		return displayOrder;
	}

	public Long getEndDate() {
		return endDate;
	}

	public String getGroupId() {
		return groupId;
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

	public Long getStartDate() {
		return startDate;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public void setLastretrieved(Long lastretrieved) {
		this.lastretrieved = lastretrieved;
	}

	public String toString() {
		return "E:(" + id + ',' + groupId + ',' + name + ',' + displayOrder
				+ ',' + description + ',' + startDate + ',' + endDate + ")";
	}

}
