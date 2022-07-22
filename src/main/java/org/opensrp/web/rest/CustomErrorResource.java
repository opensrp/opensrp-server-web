package org.opensrp.web.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.opensrp.web.dto.ResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
public class CustomErrorResource {

    public static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HHmm").create();

    @RequestMapping(value = "error", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseDto<?> customError(HttpServletResponse response) {
        int status = response.getStatus();

        HttpStatus httpStatus = HttpStatus.resolve(status);
        String message = httpStatus.name();

        ResponseDto<Object> dto = new ResponseDto<>().makeFailureResponse(httpStatus);
        dto.setData(null);
        dto.setMessage(message);

        if (status == 200) {
            dto.setSuccess(true);
        }

        return dto;
    }
}
