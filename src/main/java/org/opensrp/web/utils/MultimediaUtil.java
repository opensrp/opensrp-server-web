package org.opensrp.web.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultimediaUtil {

	public static Boolean hasSpecialCharacters(String fileName) {
		Pattern special = Pattern.compile("[:\"\\\\?|/'<>*/\r\t\f\n\0]");
		Matcher hasSpecialCharacters = special.matcher(fileName);
		return hasSpecialCharacters.find();
	}

}
