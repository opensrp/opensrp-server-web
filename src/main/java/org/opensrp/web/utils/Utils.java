/**
 *
 */
package org.opensrp.web.utils;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.lang.Nullable;

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

	public static Date getDateFromString(@Nullable String date) {
		DateTime dateTime = getDateTimeFromString(date);
		if (dateTime != null) {
			return dateTime.toDate();
		}
		return null;
	}

	public static DateTime getDateTimeFromString(@Nullable String date) {
		if (StringUtils.isNotBlank(date)) {
			try {
				Long aLong = Long.parseLong(date);
				return new DateTime(aLong);
			}
			catch (NumberFormatException e) {
				try {
					return new DateTime(date);
				}
				catch (IllegalArgumentException illegalArgumentException) {
					return null;
				}
			}
		} else {
			return null;
		}
	}

	public static boolean checkRoleIfRoleExists(List<String> roleList, String role) {
		for (String roleName : roleList)
			if (StringUtils.containsIgnoreCase(roleName, role))
				return true;
		return false;
	}
}
