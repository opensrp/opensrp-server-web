package org.opensrp.web.utils;

public class MultimediaUtil {

	public static String restrictSpecialCharacters(String fileName) {
		return fileName.replaceAll("[^a-zA-Z0-9\\.-_]+", "_");
	}

}
