package org.opensrp.web.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.smartregister.domain.Address;
import org.smartregister.domain.Client;

public class MaskingUtils {
	
	/**
	 * Expects date in the ISO 8601 format e.g. 1980-11-02
	 * 
	 * @param date String
	 * @return Masked date e.g. 1980-01-01
	 */
	public String maskDate(String date) {
		
		if (StringUtils.isNotBlank(date) && StringUtils.contains(date, '-')) {
			
			return new StringBuilder(date.substring(0, date.indexOf('-'))).append("-01-01").toString();
			
		}
		
		return "";
		
	}
	
	/**
	 * @param date Date object
	 * @return Masked date e.g. 1980-01-01
	 */
	public Date maskDate(Date date) {
		
		return date != null ? maskDateCore(date) : null;
		
	}
	
	/**
	 * @param date joda LocalDate object
	 * @return Masked LocalDate e.g. 1980-01-01
	 */
	public LocalDate maskDate(LocalDate date) {
		
		return date != null ? new LocalDate(maskDateCore(date.toDate())) : null;
		
	}
	
	/**
	 * @param date joda DateTime object
	 * @return Masked DateTime e.g. 1980-01-01
	 */
	public DateTime maskDate(DateTime date) {
		
		return date != null ? new DateTime(maskDateCore(date.toDate())) : null;
		
	}
	
	/**
	 * @param date java LocalDate object
	 * @return Masked date e.g. 1980-01-01
	 */
	public java.time.LocalDate maskDate(java.time.LocalDate date) {
		
		if (date != null) {
			
			ZoneId defaultZoneId = ZoneId.systemDefault();
			Date newDate = maskDateCore(Date.from(date.atStartOfDay(defaultZoneId).toInstant()));
			
			return Instant.ofEpochMilli(newDate.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
			
		}
		
		return null;
		
	}
	
	/**
	 * @param T generic date object
	 * @return Masked Date object
	 */
	private <T> Date maskDateCore(T date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime((Date) date);
		
		cal.set(Calendar.MONTH, 0);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		Date newDate = cal.getTime();
		return newDate;
	}
	
	/**
	 * @param value String
	 * @return Masked value xxxxxxxxxx
	 */
	public String maskString(String value) {
		
		return "xxxxxxxxxx";
		
	}
	
	public List<Client> processDataMasking(List<Client> clients) {
		
		for (Client client : clients) {
			
			processDataMaskingForClient(client);
		}
		
		return clients;
		
	}
	
	/**
	 * Process PII data masking for a single client
	 * 
	 * @param Client
	 */
	public void processDataMaskingForClient(Client client) {
		//Mask Demographics
		client.setFirstName(maskString(client.getFirstName()));
		client.setMiddleName(maskString(client.getMiddleName()));
		client.setLastName(maskString(client.getLastName()));
		client.setBirthdate(maskDate(client.getBirthdate()));
		client.setBaseEntityId(maskString(client.getBaseEntityId()));
		
		//Mask Identifiers
		Map<String, String> clientIdentifiers = client.getIdentifiers();
		for (Map.Entry<String, String> entry : clientIdentifiers.entrySet()) {
			entry.setValue(maskString(entry.getValue()));
		}
		
		client.setIdentifiers(clientIdentifiers);
		
		//Mask Addresses
		List<Address> addresses = client.getAddresses();
		for (Address address : addresses) {
			Map<String, String> fields = address.getAddressFields();
			
			for (Map.Entry<String, String> entry : fields.entrySet()) {
				entry.setValue(maskString(entry.getValue()));
			}
		}
		
		client.setAddresses(addresses);
		
		//Mask Attributes
		Map<String, Object> attributes = client.getAttributes();
		for (Map.Entry<String, Object> entry : attributes.entrySet()) {
			entry.setValue(maskString(entry.getValue().toString()));
		}
		
		client.setAttributes(attributes);
	}
	
}
