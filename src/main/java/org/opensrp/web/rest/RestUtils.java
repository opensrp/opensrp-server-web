package org.opensrp.web.rest;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.opensrp.api.domain.User;
import org.opensrp.domain.Multimedia;
import org.opensrp.service.multimedia.MultimediaFileManager;
import org.opensrp.service.multimedia.S3MultimediaFileManager;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;

public class RestUtils {
	public static final String DATE_FORMAT = "dd-MM-yyyy";
	public static final SimpleDateFormat SDF = new SimpleDateFormat("dd-MM-yyyy");
	public static final String DATETIME_FORMAT = "dd-MM-yyyy HH:mm";
	public static final SimpleDateFormat SDTF = new SimpleDateFormat("dd-MM-yyyy HH:mm");

	private static final Logger logger = LogManager.getLogger(RestUtils.class.toString());


	public static String getStringFilter(String filter, HttpServletRequest req)
	{
	  return StringUtils.isBlank(req.getParameter(filter)) ? null : req.getParameter(filter);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Enum getEnumFilter(String filter, Class cls, HttpServletRequest req)
	{
	  String filterVal = getStringFilter(filter, req);
	  if (filterVal != null) {
	    return Enum.valueOf(cls, filterVal);
	  }
	  return null;
	}
	
	public static Integer getIntegerFilter(String filter, HttpServletRequest req)
	{
	  String strval = getStringFilter(filter, req);
	  return strval == null ? null : Integer.parseInt(strval);
	}
	
	public static Float getFloatFilter(String filter, HttpServletRequest req)
	{
	  String strval = getStringFilter(filter, req);
	  return strval == null ? null : Float.parseFloat(strval);
	}
	
	public static DateTime getDateFilter(String filter, HttpServletRequest req) throws ParseException
	{
	  String strval = getStringFilter(filter, req);
	  return strval == null ? null : new DateTime(strval);
	}
	
	public static DateTime[] getDateRangeFilter(String filter, HttpServletRequest req) throws ParseException
	{
	  String strval = getStringFilter(filter, req);
	  if(strval == null){
		  return null;
	  }
	  if (!strval.contains(":")) {
			return new DateTime[] { new DateTime(strval), new DateTime(strval) };
	  }
	  DateTime d1 = new DateTime(strval.substring(0, strval.indexOf(":")));
	  DateTime d2 = new DateTime(strval.substring(strval.indexOf(":")+1));
	  return new DateTime[]{d1,d2};
	}

	public static boolean getBooleanFilter(String filter, HttpServletRequest req) {
		String stringFilter = getStringFilter(filter, req);
		return Boolean.parseBoolean(stringFilter);
	}

	public static void main(String[] args) {
		System.out.println(new DateTime("​1458932400000"));
	}
	
	public static synchronized String setDateFilter(Date date) throws ParseException
	{
	  return date == null ? null : SDF.format(date);
	}
	
	public static <T> void verifyRequiredProperties(List<String> properties, T entity) {
		if(properties != null)
		for (String p : properties) {
			Field[] aaa = entity.getClass().getDeclaredFields();
			for (Field field : aaa) {
				if(field.getName().equals(p)){
					field.setAccessible(true);
					try {
						if(field.get(entity) == null || field.get(entity).toString().trim().equalsIgnoreCase("")){
							throw new RuntimeException("A required field "+p+" was found empty");
						}
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
						throw new RuntimeException("A required field "+p+" was not found in resource class");
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public static HttpHeaders getJSONUTF8Headers() {
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-Type", "application/json; charset=utf-8");
		return responseHeaders;
	}

    /**
     * Zips multimedia files and writes content to {@param zipOutputStream}
     *
     * @param zipOutputStream
     * @param multimediaFiles
     * @throws IOException
     */
	public static void zipFiles(ZipOutputStream zipOutputStream, List<Multimedia> multimediaFiles, MultimediaFileManager fileManager) throws IOException {
		for (Multimedia multiMedia : multimediaFiles) {
			FileInputStream inputStream;
			File file = fileManager.retrieveFile(multiMedia.getFilePath());
			if (file != null) {
				logger.info("Adding " + file.getName());
				zipOutputStream.putNextEntry(new ZipEntry(file.getName()));
				try {
					inputStream = new FileInputStream(file);
				} catch (FileNotFoundException e) {
					logger.warn("Could not find file " + file.getAbsolutePath());
					continue;
				}

				// Write the contents of the file
				BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
				int data;
				while ((data = bufferedInputStream.read()) != -1) {
					zipOutputStream.write(data);
				}
				bufferedInputStream.close();
				zipOutputStream.closeEntry();
				logger.info("Done downloading file " + file.getName());

				// clean up temp files (may want to cache in future)
				if (fileManager instanceof S3MultimediaFileManager) {
					file.delete();
				}
			}
		}
	}
	
	public static User currentUser(Authentication authentication) {
		if (authentication != null && authentication.getPrincipal() instanceof KeycloakPrincipal) {
			@SuppressWarnings("unchecked")
			KeycloakPrincipal<KeycloakSecurityContext> kp = (KeycloakPrincipal<KeycloakSecurityContext>) authentication
			        .getPrincipal();
			AccessToken token = kp.getKeycloakSecurityContext().getToken();
			User user = new User(authentication.getName());
			user.setPreferredName(token.getName());
			user.setUsername(token.getPreferredUsername());
			List<String> authorities = authentication.getAuthorities().stream().map(e -> e.getAuthority())
			        .collect(Collectors.toList());
			user.setAttributes(token.getOtherClaims());
			user.setRoles(authorities);
			user.setPermissions(authorities);
			return user;
		}
		return null;
	}

	public static void writeToZipFile(String fileName, ZipOutputStream zipStream, String filePath) throws IOException {
		File aFile;
		FileInputStream fis = null;
		ZipEntry zipEntry;
		String tempDirectory = System.getProperty("java.io.tmpdir");
		try{
			if(StringUtils.isNotBlank(fileName)) {
				aFile = new File(StringUtils.isNotBlank(filePath) ? filePath : fileName);
				fis = new FileInputStream(aFile);
				zipEntry = new ZipEntry(StringUtils.isNotBlank(filePath) ? filePath.replace(tempDirectory, "") : fileName);
				logger.info("Writing file : '" + fileName + "' to zip file");
			}
			else {
				fis = new FileInputStream(filePath);
				zipEntry = new ZipEntry(filePath);
				logger.info("Writing file : '" + filePath + "' to zip file");
			}
			zipStream.putNextEntry(zipEntry);
			byte[] bytes = new byte[1024];
			int length;
			while ((length = fis.read(bytes)) >= 0) {
				zipStream.write(bytes, 0, length);
			}

			zipStream.closeEntry();
		}
		catch (IOException e) {
			logger.error("IO Exception occurred: " + e.getMessage());
		}
		finally {
			if (fis != null) {
				fis.close();
			}
		}
	}

}
