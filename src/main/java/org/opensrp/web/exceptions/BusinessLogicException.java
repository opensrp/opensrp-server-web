package org.opensrp.web.exceptions;

public class BusinessLogicException extends Exception {

	public BusinessLogicException(String error) {
		this.businessLogicError = error;
	}

	private String businessLogicError;

	public String getBusinessLogicError() {
		return businessLogicError;
	}

	public void setBusinessLogicError(String businessLogicError) {
		this.businessLogicError = businessLogicError;
	}
}
