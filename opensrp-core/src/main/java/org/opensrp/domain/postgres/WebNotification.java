package org.opensrp.domain.postgres;

public class WebNotification {
	
	private String notificationType;
	
	private Long id;
	
	private String title;
	
	private String details;
	
	private String sendDate;
	
	private int hour;
	
	private int minute;
	
	private Long timestamp;
	
	public String getNotificationType() {
		return notificationType;
	}
	
	public void setNotificationType(String notificationType) {
		this.notificationType = notificationType;
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getDetails() {
		return details;
	}
	
	public void setDetails(String details) {
		this.details = details;
	}
	
	public String getSendDate() {
		return sendDate;
	}
	
	public void setSendDate(String sendDate) {
		this.sendDate = sendDate;
	}
	
	public int getHour() {
		return hour;
	}
	
	public void setHour(int hour) {
		this.hour = hour;
	}
	
	public int getMinute() {
		return minute;
	}
	
	public void setMinute(int minute) {
		this.minute = minute;
	}
	
	public Long getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
	
}
