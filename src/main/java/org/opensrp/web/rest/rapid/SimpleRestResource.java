package org.opensrp.web.rest.rapid;

import org.opensrp.web.rest.RestUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.List;

public abstract class SimpleRestResource<T> {
    @RequestMapping(method = RequestMethod.POST, consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    private T createNew(@RequestBody T entity) {
        RestUtils.verifyRequiredProperties(requiredProperties(), entity);
        return create(entity);
    }

    @RequestMapping(value = "/{uniqueId}", method = RequestMethod.POST, consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    private T updateExisting(@PathVariable("uniqueId") String uniqueId, @RequestBody T entity) {
//TODO		RestUtils.verifyUpdatableProperties(requiredProperties(), entity);
        RestUtils.verifyRequiredProperties(requiredProperties(), entity);
        return update(entity);//TODO
    }

    @RequestMapping(value = "/{uniqueId}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    private T getById(@PathVariable("uniqueId") String uniqueId) {
        return getByUniqueId(uniqueId);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/search", produces = {MediaType.APPLICATION_JSON_VALUE})
    private List<T> searchBy(HttpServletRequest request) throws ParseException {
        return search(request);
    }

    public abstract List<T> search(HttpServletRequest request) throws ParseException;

    public abstract T getByUniqueId(String uniqueId);

    public abstract List<String> requiredProperties();

    public abstract T create(T entity);

    public abstract T update(T entity);

}
