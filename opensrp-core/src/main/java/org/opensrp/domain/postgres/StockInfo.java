package org.opensrp.domain.postgres;

public class StockInfo {
	
	private String expireyDate;
	
	private Long productId;
	
	private Long stockId;
	
	private String productName;
	
	private int quantity;
	
	private String receiveDate;
	
	private int year;
	
	private int month;
	
	private Long timestamp;
	
	public String getExpireyDate() {
		return expireyDate;
	}
	
	public void setExpireyDate(String expireyDate) {
		this.expireyDate = expireyDate;
	}
	
	public Long getProductId() {
		return productId;
	}
	
	public void setProductId(Long productId) {
		this.productId = productId;
	}
	
	public Long getStockId() {
		return stockId;
	}
	
	public void setStockId(Long stockId) {
		this.stockId = stockId;
	}
	
	public String getProductName() {
		return productName;
	}
	
	public void setProductName(String productName) {
		this.productName = productName;
	}
	
	public int getQuantity() {
		return quantity;
	}
	
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	
	public String getReceiveDate() {
		return receiveDate;
	}
	
	public void setReceiveDate(String receiveDate) {
		this.receiveDate = receiveDate;
	}
	
	public int getYear() {
		return year;
	}
	
	public void setYear(int year) {
		this.year = year;
	}
	
	public int getMonth() {
		return month;
	}
	
	public void setMonth(int month) {
		this.month = month;
	}
	
	public Long getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
	
}
