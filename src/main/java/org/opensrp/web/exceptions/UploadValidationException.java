package org.opensrp.web.exceptions;

public class UploadValidationException extends IllegalStateException {

	public UploadValidationException(String error) {
		super(error);
	}
}
