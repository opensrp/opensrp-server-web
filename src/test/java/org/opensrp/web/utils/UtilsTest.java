package org.opensrp.web.utils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

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
}
