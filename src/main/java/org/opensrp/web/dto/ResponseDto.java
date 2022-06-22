package org.opensrp.web.dto;


import static org.opensrp.web.Constants.FAILURE;

import org.springframework.http.HttpStatus;

import java.io.Serializable;

public class ResponseDto<T> implements Serializable {

    private static final long serialVersionUID = -3031863362202637459L;
    protected boolean success;
    private String message;
    private String status;
    private transient T data;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public ResponseDto<T> makeFailureResponse(HttpStatus status) {
        setSuccess(Boolean.FALSE);
        setMessage(FAILURE);
        setStatus(status.toString());
        return this;
    }

}
