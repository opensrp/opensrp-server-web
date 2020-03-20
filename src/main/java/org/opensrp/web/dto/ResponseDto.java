package org.opensrp.web.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.io.Serializable;

import static org.opensrp.web.Constants.FAILURE;

public class ResponseDto<T> implements Serializable {

    private static final long serialVersionUID = -3031863362202637459L;

    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseDto.class);

    private String message;
    private transient T data;
    protected boolean success;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public ResponseDto<T> makeFailureResponse(HttpStatus status, boolean rollbackTransaction) {

        setSuccess(Boolean.FALSE);
        setMessage(FAILURE);

        if (rollbackTransaction) {
            try {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            } catch (NoTransactionException e) {
                // Ignore if no transaction was present
            } catch (Exception e) {
                LOGGER.error("MunchiesErrorType while rolling back transaction", e);
            }
        }
        return this;
    }

}
