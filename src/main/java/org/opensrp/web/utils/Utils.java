/**
 * 
 */
package org.opensrp.web.utils;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Samuel Githengi created on 06/12/20
 */
public class Utils {
	
	public static String getStringFromJSON(JSONObject jsonObject, String key) {
		Object value = jsonObject.get(key);
		if (value instanceof JSONArray) {
			return jsonObject.getJSONArray(key).toString();
		} else {
			return jsonObject.getString(key);
		}
	}
}
