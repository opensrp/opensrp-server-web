package org.opensrp.web.exceptions;

public class UploadValidationException extends IllegalArgumentException {

    public UploadValidationException(String error) {
        super(error);
    }
}
