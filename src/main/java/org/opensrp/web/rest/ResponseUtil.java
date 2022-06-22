package org.opensrp.web.rest;

import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

public class ResponseUtil {

    public static <T> List<T> prepareDataResponse(List<T> list, String[] fieldsToIgnore) throws InstantiationException, IllegalAccessException {
        List<T> list2 = new ArrayList<T>();
        for (T object : list) {
            @SuppressWarnings("unchecked")
            T targeto = (T) object.getClass().newInstance();
            BeanUtils.copyProperties(object, targeto, fieldsToIgnore);
            list2.add(targeto);
        }

        return list2;
    }

    @SuppressWarnings("unchecked")
    public static <T> T prepareDataResponse(T object, String[] fieldsToIgnore) throws InstantiationException, IllegalAccessException {
        T targeto = (T) object.getClass().newInstance();
        BeanUtils.copyProperties(object, targeto, fieldsToIgnore);

        return targeto;
    }
}
