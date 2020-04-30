package org.opensrp.web.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultimediaUtil {
	private static final Pattern special = Pattern.compile("[:\"\\?|<>*/\r\t\f\n\0]");

	public static Boolean hasSpecialCharacters(String fileName) {
		Matcher hasSpecialCharacters = special.matcher(fileName);
		return hasSpecialCharacters.find();
	}

}
