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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.opensrp.domain.Multimedia;
import org.opensrp.service.multimedia.MultimediaFileManager;
import org.opensrp.service.multimedia.S3MultimediaFileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;

public class RestUtils {
	public static final String DATE_FORMAT = "dd-MM-yyyy";
	public static final SimpleDateFormat SDF = new SimpleDateFormat("dd-MM-yyyy");
	public static final String DATETIME_FORMAT = "dd-MM-yyyy HH:mm";
	public static final SimpleDateFormat SDTF = new SimpleDateFormat("dd-MM-yyyy HH:mm");

	private static Logger logger = LoggerFactory.getLogger(RestUtils.class.toString());


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
	
	public static Long getLongFilter(String filter, HttpServletRequest req) {
		String strval = getStringFilter(filter, req);
		return strval == null ? null : Long.parseLong(strval);
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
	  DateTime d1 = new DateTime(strval.substring(0, strval.indexOf(":")));
	  DateTime d2 = new DateTime(strval.substring(strval.indexOf(":")+1));
	  return new DateTime[]{d1,d2};
	}
	
	
	public static void main(String[] args) {
		System.out.println(new DateTime("​1458932400000"));
	}
	
	public static String setDateFilter(Date date) throws ParseException
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
}
