package org.opensrp.web.utils;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.smartregister.domain.Address;
import org.smartregister.domain.Client;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaskingUtilsTest {

    @Test
    public void testMaskDateFromStringWithValidDate() {

        MaskingUtils util = new MaskingUtils();
        String param = "2019-12-15";
        String result = util.maskDate(param);

        Assert.assertEquals("2019-01-01", result);

    }

    @Test
    public void testMaskDateFromStringWithInvalidDate() {

        MaskingUtils util = new MaskingUtils();
        String param = "2011kdjf";
        String result = util.maskDate(param);

        Assert.assertEquals("", result);

    }

    @Test
    public void testMaskDateFromStringWithEmptyParam() {

        MaskingUtils util = new MaskingUtils();
        String param = "";
        String result = util.maskDate(param);

        Assert.assertEquals("", result);

    }

    public void testMaskDateFromDateObjectWithValidDate() {

        MaskingUtils util = new MaskingUtils();

        LocalDate localDate = LocalDate.of(2015, 3, 16);

        Date result = util.maskDate(java.sql.Date.valueOf(localDate));

        Calendar cal = Calendar.getInstance();
        cal.setTime(result);

        Assert.assertEquals(2015, cal.get(Calendar.YEAR));
        Assert.assertEquals(0, cal.get(Calendar.MONTH));
        Assert.assertEquals(1, cal.get(Calendar.DATE));

    }

    @Test
    public void testMaskDateFromDateObjectWithInvalidDate() {

        MaskingUtils util = new MaskingUtils();

        Date date = null;

        Date result = util.maskDate(date);

        Assert.assertNull(result);

    }

    @Test
    public void testMaskStringForValidValue() {

        //Valid
        MaskingUtils util = new MaskingUtils();
        String param = "Jonathan Archer";
        String result = util.maskString(param);

        Assert.assertEquals("xxxxxxxxxx", result);

    }

    @Test
    public void testMaskStringForEmptyParam() {

        //Valid
        MaskingUtils util = new MaskingUtils();
        String param = "";
        String result = util.maskString(param);

        Assert.assertEquals("xxxxxxxxxx", result);

    }

    @Test
    public void testMaskStringForNullParam() {

        //Valid
        MaskingUtils util = new MaskingUtils();
        String param = null;
        String result = util.maskString(param);

        Assert.assertEquals("xxxxxxxxxx", result);

    }

    @Test
    public void testMaskDateFromJodaLocalDateObjectWithValidDate() {

        MaskingUtils util = new MaskingUtils();

        org.joda.time.LocalDate localDate = org.joda.time.LocalDate.parse("1998-10-07");

        org.joda.time.LocalDate result = util.maskDate(localDate);

        Assert.assertEquals(1998, result.getYear());
        Assert.assertEquals(1, result.getMonthOfYear());
        Assert.assertEquals(1, result.getDayOfMonth());

    }

    @Test
    public void testMaskDateFromJavaLocalDateObjectWithValidDate() {

        MaskingUtils util = new MaskingUtils();

        LocalDate localDate = LocalDate.of(2020, 2, 12);

        LocalDate result = util.maskDate(localDate);

        Assert.assertEquals(2020, result.getYear());
        Assert.assertEquals(1, result.getMonthValue());
        Assert.assertEquals(1, result.getDayOfMonth());

    }

    @Test
    public void testMaskDateFromDateTimeObjectWithValidDate() {

        MaskingUtils util = new MaskingUtils();

        DateTime dateTime = new DateTime(2010, 4, 2, 0, 0, 0, 0);

        DateTime result = util.maskDate(dateTime);

        Calendar cal = Calendar.getInstance();
        cal.setTime(result.toDate());

        Assert.assertEquals(2010, cal.get(Calendar.YEAR));
        Assert.assertEquals(0, cal.get(Calendar.MONTH));
        Assert.assertEquals(1, cal.get(Calendar.DATE));

    }

    @Test
    public void testProcessDataMasking() {

        MaskingUtils maskUtil = Mockito.mock(MaskingUtils.class, Mockito.CALLS_REAL_METHODS);

        List<Client> clientList = new ArrayList<>();
        Client client = Mockito.mock(Client.class);
        clientList.add(client);

        Mockito.doNothing().when(maskUtil).processDataMaskingForClient(client);

        maskUtil.processDataMasking(clientList);

        Mockito.verify(maskUtil, Mockito.times(1)).processDataMaskingForClient(client);
    }

    @Test
    public void testProcessDataMaskingForClient() {

        Client client = new Client("some-base-entity-id");
        client.withBirthdate(new DateTime(2015, 4, 3, 0, 0, 0, 0), false);
        client.withFirstName("Terrence").withMiddleName("Lamar").withLastName("Kimani");

        Address address = new Address();
        address.setAddressType("usual_residence");

        Map<String, String> addressFields = new HashMap<>();
        addressFields.put("address1", "pii-location-id-1");
        addressFields.put("address2", "pii-location-id-2");
        addressFields.put("address3", "pii-location-id-3");
        address.setAddressFields(addressFields);
        client.withAddress(address);

        Map<String, String> idFields = new HashMap<>();
        idFields.put("ZEIR_ID", "1002034L");
        idFields.put("M_ZEIR_ID", "10202923K");
        idFields.put("F_ZEIR_ID", "30490034M");
        idFields.put("OPENSRP_ID", "4950034G");
        client.withIdentifiers(idFields);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("Child_Register_Card_Number", "2382323/47743/82824");
        attributes.put("Second_Guardian_Phone_Number", "0723457890");
        client.withAttributes(attributes);

        MaskingUtils util = new MaskingUtils();

        util.processDataMaskingForClient(client);

        //Demographics

        Assert.assertEquals("xxxxxxxxxx", client.getFirstName());
        Assert.assertEquals("xxxxxxxxxx", client.getMiddleName());
        Assert.assertEquals("xxxxxxxxxx", client.getLastName());

        //DOB
        Calendar cal = Calendar.getInstance();
        cal.setTime(client.getBirthdate().toDate());

        Assert.assertEquals(2015, cal.get(Calendar.YEAR));
        Assert.assertEquals(0, cal.get(Calendar.MONTH));
        Assert.assertEquals(1, cal.get(Calendar.DATE));

        //Addresses
        List<Address> addresses = client.getAddresses();
        Assert.assertNotNull(addresses);
        Assert.assertEquals(1, addresses.size());

        Map<String, String> fields = addresses.get(0).getAddressFields();
        Assert.assertEquals(3, fields.size());
        Assert.assertEquals("xxxxxxxxxx", fields.get("address1"));
        Assert.assertEquals("xxxxxxxxxx", fields.get("address2"));
        Assert.assertEquals("xxxxxxxxxx", fields.get("address3"));

        //Identifiers
        Map<String, String> identifiers = client.getIdentifiers();
        Assert.assertNotNull(identifiers);
        Assert.assertEquals(4, identifiers.size());

        Assert.assertEquals("xxxxxxxxxx", identifiers.get("ZEIR_ID"));
        Assert.assertEquals("xxxxxxxxxx", identifiers.get("M_ZEIR_ID"));
        Assert.assertEquals("xxxxxxxxxx", identifiers.get("F_ZEIR_ID"));
        Assert.assertEquals("xxxxxxxxxx", identifiers.get("OPENSRP_ID"));
        Assert.assertEquals("xxxxxxxxxx", client.getBaseEntityId());

        //Attributes
        Map<String, Object> attributes_ = client.getAttributes();
        Assert.assertNotNull(attributes_);
        Assert.assertEquals(2, attributes_.size());

        Assert.assertEquals("xxxxxxxxxx", attributes_.get("Child_Register_Card_Number"));
        Assert.assertEquals("xxxxxxxxxx", attributes_.get("Second_Guardian_Phone_Number"));

    }

}
