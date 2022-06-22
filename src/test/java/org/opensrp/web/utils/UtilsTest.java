package org.opensrp.web.utils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.opensrp.web.config.Role;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UtilsTest {

    @Test
    public void testGetStringFromJSONForObject() {

        JSONObject obj = new JSONObject();
        obj.put("location", "Roysa");

        String result = Utils.getStringFromJSON(obj, "location");

        Assert.assertNotNull(result);
        Assert.assertEquals("Roysa", result);
    }

    @Test
    public void testGetStringFromJSONForArray() {

        JSONArray obj = new JSONArray();
        obj.put("address1");
        obj.put("address2");
        obj.put("address3");

        JSONObject parentObj = new JSONObject();
        parentObj.put("addresses", obj);

        String result = Utils.getStringFromJSON(parentObj, "addresses");

        Assert.assertNotNull(result);
        Assert.assertEquals("[\"address1\",\"address2\",\"address3\"]", result);
    }

    @Test
    public void testGetDateTimeFromStringShouldReturnDate() {
        Date date = Utils.getDateTimeFromString("1615895228000");
        Assert.assertNotNull(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        Assert.assertEquals(calendar.get(Calendar.YEAR), 2021);
        Assert.assertEquals(calendar.get(Calendar.MONTH), 2);
        Assert.assertEquals(calendar.get(Calendar.DATE), 16);
    }

    @Test
    public void testGetDateTimeFromStringShouldReturnNull() {
        Date date = Utils.getDateTimeFromString("wrongdate");
        Assert.assertNull(date);
        date = Utils.getDateTimeFromString(null);
        Assert.assertNull(date);
    }

    @Test
    public void testCheckRoleIfRoleExists() {

        List<String> roleList = new ArrayList<>();
        roleList.add(Role.OPENSRP_GENERATE_QR_CODE);
        roleList.add(Role.PII_DATA_MASK);
        roleList.add(Role.PLANS_FOR_USER);

        boolean result = Utils.checkRoleIfRoleExists(roleList, Role.PLANS_FOR_USER);
        Assert.assertTrue(result);

        result = Utils.checkRoleIfRoleExists(roleList, Role.OPENSRP_GENERATE_QR_CODE.toUpperCase(Locale.ENGLISH));
        Assert.assertTrue(result);

        result = Utils.checkRoleIfRoleExists(roleList, Role.OPENMRS);
        Assert.assertFalse(result);

    }
}
