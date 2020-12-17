package org.opensrp.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.jackson.annotate.JsonProperty;
import org.ektorp.support.TypeDiscriminator;
import org.joda.time.DateTime;
import org.opensrp.common.Gender;

@TypeDiscriminator("doc.type == 'Client'")
public class Client extends BaseEntity {
	
	@JsonProperty
	private String firstName;
	
	@JsonProperty
	private String middleName;
	
	@JsonProperty
	private String lastName;
	
	@JsonProperty
	private DateTime birthdate;
	
	@JsonProperty
	private DateTime deathdate;
	
	@JsonProperty
	private Boolean birthdateApprox;
	
	@JsonProperty
	private Boolean deathdateApprox;
	
	@JsonProperty
	private String gender;
	
	@JsonProperty
	private String clientType;
	
	@JsonProperty
	private Map<String, List<String>> relationships;
	
	@JsonProperty
	private String isSendToOpenMRS;
	
	@JsonProperty
	private String dataApprovalComments;
	
	@JsonProperty
	private String dataApprovalStatus;
	
	protected Client() {
		
	}
	
	public Client(String baseEntityId) {
		super(baseEntityId);
	}
	
	public Client(String baseEntityId, String firstName, String middleName, String lastName, DateTime birthdate,
	    DateTime deathdate, Boolean birthdateApprox, Boolean deathdateApprox, String gender) {
		super(baseEntityId);
		setFirstName(firstName);
		setMiddleName(middleName);
		setLastName(lastName);
		setBirthdate(birthdate);
		setDeathdate(deathdate);
		setBirthdateApprox(birthdateApprox);
		setDeathdateApprox(deathdateApprox);
		setGender(gender);
	}
	
	public Client(String baseEntityId, String firstName, String middleName, String lastName, DateTime birthdate,
	    DateTime deathdate, Boolean birthdateApprox, Boolean deathdateApprox, String gender, String identifierType,
	    String identifier) {
		super(baseEntityId);
		setFirstName(firstName);
		setMiddleName(middleName);
		setLastName(lastName);
		setBirthdate(birthdate);
		setDeathdate(deathdate);
		setBirthdateApprox(birthdateApprox);
		setDeathdateApprox(deathdateApprox);
		setGender(gender);
		addIdentifier(identifierType, identifier);
	}
	
	public Client(String baseEntityId, String firstName, String middleName, String lastName, DateTime birthdate,
	    DateTime deathdate, Boolean birthdateApprox, Boolean deathdateApprox, String gender, List<Address> addresses,
	    Map<String, String> identifiers, Map<String, Object> attributes) {
		super(baseEntityId);
		this.firstName = firstName;
		this.middleName = middleName;
		this.lastName = lastName;
		this.birthdate = birthdate;
		this.deathdate = deathdate;
		this.birthdateApprox = birthdateApprox;
		this.deathdateApprox = deathdateApprox;
		this.gender = gender;
		setIdentifiers(identifiers);
		setAddresses(addresses);
		setAttributes(attributes);
	}
	
	public Client(String baseEntityId, String firstName, String middleName, String lastName, DateTime birthdate,
	    DateTime deathdate, Boolean birthdateApprox, Boolean deathdateApprox, String gender, String clientType) {
		this(baseEntityId, firstName, middleName, lastName, birthdate, deathdate, birthdateApprox, deathdateApprox, gender);
		setClientType(clientType);
	}
	
	public Client(String baseEntityId, String firstName, String middleName, String lastName, DateTime birthdate,
	    DateTime deathdate, Boolean birthdateApprox, Boolean deathdateApprox, String gender, String identifierType,
	    String identifier, String clientType) {
		this(baseEntityId, firstName, middleName, lastName, birthdate, deathdate, birthdateApprox, deathdateApprox, gender,
		        identifierType, identifier);
		setClientType(clientType);
	}
	
	public Client(String baseEntityId, String firstName, String middleName, String lastName, DateTime birthdate,
	    DateTime deathdate, Boolean birthdateApprox, Boolean deathdateApprox, String gender, List<Address> addresses,
	    Map<String, String> identifiers, Map<String, Object> attributes, String clientType) {
		this(baseEntityId, firstName, middleName, lastName, birthdate, deathdate, birthdateApprox, deathdateApprox, gender,
		        addresses, identifiers, attributes);
		setClientType(clientType);
	}
	
	public String getFirstName() {
		return firstName;
	}
	
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	
	public String getMiddleName() {
		return middleName;
	}
	
	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}
	
	public String getLastName() {
		return lastName;
	}
	
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public String fullName() {
		String n = "";
		if (StringUtils.isNotBlank(firstName)) {
			n += firstName;
		}
		if (StringUtils.isNotBlank(middleName)) {
			n += " " + middleName;
		}
		if (StringUtils.isNotBlank(lastName)) {
			n += " " + lastName;
		}
		return n.trim();
	}
	
	public DateTime getBirthdate() {
		return birthdate;
	}
	
	public void setBirthdate(DateTime birthdate) {
		this.birthdate = birthdate;
	}
	
	public DateTime getDeathdate() {
		return deathdate;
	}
	
	public void setDeathdate(DateTime deathdate) {
		this.deathdate = deathdate;
	}
	
	public Boolean getBirthdateApprox() {
		return birthdateApprox;
	}
	
	public void setBirthdateApprox(Boolean birthdateApprox) {
		this.birthdateApprox = birthdateApprox;
	}
	
	public Boolean getDeathdateApprox() {
		return deathdateApprox;
	}
	
	public void setDeathdateApprox(Boolean deathdateApprox) {
		this.deathdateApprox = deathdateApprox;
	}
	
	public String getGender() {
		return gender;
	}
	
	public void setGender(String gender) {
		this.gender = gender;
	}
	
	public String getClientType() {
		return clientType;
	}
	
	public void setClientType(String clientType) {
		this.clientType = clientType;
	}
	
	public Map<String, List<String>> getRelationships() {
		return relationships;
	}
	
	public void setRelationships(Map<String, List<String>> relationships) {
		this.relationships = relationships;
	}
	
	public Client withFirstName(String firstName) {
		this.firstName = firstName;
		return this;
	}
	
	public Client withMiddleName(String middleName) {
		this.middleName = middleName;
		return this;
	}
	
	public Client withLastName(String lastName) {
		this.lastName = lastName;
		return this;
	}
	
	public Client withName(String firstName, String middleName, String lastName) {
		this.firstName = firstName;
		this.middleName = middleName;
		this.lastName = lastName;
		return this;
	}
	
	public Client withBirthdate(DateTime birthdate, Boolean isApproximate) {
		this.birthdate = birthdate;
		this.birthdateApprox = isApproximate;
		return this;
	}
	
	public Client withDeathdate(DateTime deathdate, Boolean isApproximate) {
		this.deathdate = deathdate;
		this.deathdateApprox = isApproximate;
		return this;
	}
	
	public Client withGender(String gender) {
		this.gender = gender;
		return this;
	}
	
	public Client withGender(Gender gender) {
		this.gender = gender.name();
		return this;
	}
	
	/**
	 * Overrides the existing data
	 */
	public Client withRelationships(Map<String, List<String>> relationships) {
		this.relationships = relationships;
		return this;
	}
	
	public List<String> findRelatives(String relationshipType) {
		if (relationships == null) {
			relationships = new HashMap<>();
		}
		
		return relationships.get(relationshipType);
	}
	
	public void addRelationship(String relationType, String relativeEntityId) {
		if (relationships == null) {
			relationships = new HashMap<>();
		}
		
		List<String> relatives = findRelatives(relationType);
		if (relatives == null) {
			relatives = new ArrayList<>();
		}
		relatives.add(relativeEntityId);
		relationships.put(relationType, relatives);
	}
	
	public List<String> getRelationships(String relativeEntityId) {
		List<String> relations = new ArrayList<String>();
		for (Entry<String, List<String>> rl : relationships.entrySet()) {
			List<String> relativeEntityIdList = rl.getValue();
			for (String entityId : relativeEntityIdList) {
				if (entityId.equalsIgnoreCase(relativeEntityId)) {
					relations.add(rl.getKey());
					break;
				}
			}
		}
		return relations;
	}
	
	public String getIsSendToOpenMRS() {
		return isSendToOpenMRS;
	}
	
	public Client withIsSendToOpenMRS(String isSendToOpenMRS) {
		this.isSendToOpenMRS = isSendToOpenMRS;
		return this;
	}
	
	public String getDataApprovalComments() {
		return dataApprovalComments;
	}
	
	public Client withDataApprovalComments(String dataApprovalComments) {
		this.dataApprovalComments = dataApprovalComments;
		return this;
	}
	
	public String getDataApprovalStatus() {
		return dataApprovalStatus;
	}
	
	public Client withDataApprovalStatus(String dataApprovalStatus) {
		this.dataApprovalStatus = dataApprovalStatus;
		return this;
	}
	
	@Override
	public final boolean equals(Object o) {
		return EqualsBuilder.reflectionEquals(this, o, "id", "revision");
	}
	
	@Override
	public final int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, "id", "revision");
	}
	
	@Override
	public String toString() {
		return "Client [firstName=" + firstName + ", middleName=" + middleName + ", lastName=" + lastName + ", birthdate="
		        + birthdate + ", deathdate=" + deathdate + ", birthdateApprox=" + birthdateApprox + ", deathdateApprox="
		        + deathdateApprox + ", gender=" + gender + ", clientType=" + clientType + ", relationships=" + relationships
		        + ", isSendToOpenMRS=" + isSendToOpenMRS + ", dataApprovalComments=" + dataApprovalComments
		        + ", dataApprovalStatus=" + dataApprovalStatus + "]";
	}
	
}