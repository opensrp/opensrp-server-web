package org.opensrp.web.utils;

import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

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
		
		if (date != null) {
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			
			cal.set(Calendar.MONTH, 0);
			cal.set(Calendar.DAY_OF_MONTH, 1);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			
			return cal.getTime();
			
		}
		
		return null;
		
	}
	
	/**
	 * @param date joda LocalDate object
	 * @return Masked date e.g. 1980-01-01
	 */
	public Date maskDate(LocalDate date) {
		
		if (date != null) {
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(date.toDate());
			
			cal.set(Calendar.MONTH, 0);
			cal.set(Calendar.DAY_OF_MONTH, 1);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			
			return cal.getTime();
			
		}
		
		return null;
		
	}
	
	/**
	 * @param date java LocalDate object
	 * @return Masked date e.g. 1980-01-01
	 */
	public Date maskDate(java.time.LocalDate date) {
		
		if (date != null) {
			
			ZoneId defaultZoneId = ZoneId.systemDefault();
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(Date.from(date.atStartOfDay(defaultZoneId).toInstant()));
			
			cal.set(Calendar.MONTH, 0);
			cal.set(Calendar.DAY_OF_MONTH, 1);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			
			return cal.getTime();
			
		}
		
		return null;
		
	}
	
	/**
	 * @param date joda DateTime object
	 * @return Masked DateTime e.g. 1980-01-01
	 */
	public DateTime maskDate(DateTime date) {
		
		if (date != null) {
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(date.toDate());
			
			cal.set(Calendar.MONTH, 0);
			cal.set(Calendar.DAY_OF_MONTH, 1);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			
			return new DateTime(cal.getTime());
			
		}
		
		return null;
		
	}
	
	/**
	 * @param value String
	 * @return Masked value xxxxxxxxxx
	 */
	public String maskString(String value) {
		
		return "xxxxxxxxxx";
		
	}
	
}
