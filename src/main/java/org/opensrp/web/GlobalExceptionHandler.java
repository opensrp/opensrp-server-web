package org.opensrp.web;

import static org.opensrp.web.Constants.DEFAULT_EXCEPTION_HANDLER_MESSAGE;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensrp.web.dto.ResponseDto;
import org.opensrp.web.exceptions.MissingTeamAssignmentException;
import org.opensrp.web.exceptions.UploadValidationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.net.ConnectException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LogManager.getLogger(GlobalExceptionHandler.class.toString());

    @ResponseBody
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDto<?> exceptionHandler(HttpMessageNotReadableException exception) {
        logger.error("HttpMessageNotReadableException occurred : ", exception);
        return buildErrorResponseForBadRequest(HttpStatus.BAD_REQUEST, "");
    }

    @ResponseBody
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDto<?> exceptionHandler(MissingServletRequestParameterException exception) {
        logger.error("MissingServletRequestParameterException occurred : ", exception);
        return buildErrorResponseForBadRequest(HttpStatus.BAD_REQUEST, "");
    }

    @ResponseBody
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDto<?> exceptionHandler(BindException exception) {
        logger.error("BindException occurred : ", exception);
        return buildErrorResponseForBadRequest(HttpStatus.BAD_REQUEST, "");
    }

    @ResponseBody
    @ExceptionHandler(value = {IllegalArgumentException.class, IllegalStateException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDto<?> illegalExceptionsHandler(RuntimeException exception) {
        logger.error("IllegalArgumentException occurred : ", exception);
        return buildErrorResponseForBadRequest(HttpStatus.BAD_REQUEST, "");
    }


    @ResponseBody
    @ExceptionHandler(MissingTeamAssignmentException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseDto<?> exceptionHandler(MissingTeamAssignmentException exception) {
        logger.error("MissingTeamAssignmentException occurred : ", exception);
        return buildErrorResponseForBadRequest(HttpStatus.FORBIDDEN, exception.getMessage());
    }


    @ResponseBody
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ResponseDto<?> exceptionHandler(HttpRequestMethodNotSupportedException exception) {
        logger.error("Method not allowed occurred : ", exception);
        return buildErrorResponse(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ResponseBody
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDto<?> exceptionHandler(RuntimeException exception) {
        logger.error("Runtime Exception occurred : ", exception);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ResponseBody
    @ExceptionHandler(UploadValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDto<?> exceptionHandler(UploadValidationException exception) {
        logger.error("UploadValidationException occurred : ", exception);
        return buildErrorResponseForBadRequest(HttpStatus.BAD_REQUEST, exception.getMessage());
    }


    @ResponseBody
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDto<?> exceptionHandler(DataIntegrityViolationException exception) {
        logger.error("DataIntegrityViolationException:  occurred : ", exception);
        return buildErrorResponseForBadRequest(HttpStatus.BAD_REQUEST, " DataIntegrityViolationException");
    }

    @ResponseBody
    @ExceptionHandler(ConnectException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDto<?> exceptionHandler(ConnectException exception) {
        logger.error("Connnection Exception occurred : ", exception);
        ResponseDto<?> dto = buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR);
        dto.setMessage("Connnection Exception: Connection refused");
        return dto;
    }


    @ResponseBody
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseDto<?> exceptionHandler(AccessDeniedException exception) {
        logger.warn("Access denied : ", exception.getMessage());
        ResponseDto<?> dto = buildErrorResponse(HttpStatus.FORBIDDEN);
        dto.setMessage("Access is denied. You do not have enough permissions for the resource.");
        return dto;
    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDto<?> exceptionHandler(Exception exception) {
        logger.error("Exception occurred : ", exception);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public ResponseDto<Object> buildErrorResponse(HttpStatus status) {
        ResponseDto<Object> dto = new ResponseDto<>().makeFailureResponse(status);
        dto.setData(null);
        dto.setMessage(DEFAULT_EXCEPTION_HANDLER_MESSAGE);
        return dto;
    }

    public ResponseDto<Object> buildErrorResponseForBadRequest(HttpStatus status, String message) {
        ResponseDto<Object> dto = new ResponseDto<>().makeFailureResponse(status);
        dto.setData(null);
        dto.setMessage(message);
        return dto;
    }
}
