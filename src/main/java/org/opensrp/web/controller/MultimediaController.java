package org.opensrp.web.controller;

import static org.opensrp.web.rest.RestUtils.zipFiles;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.List;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.util.TextUtils;
import org.opensrp.domain.Multimedia;
import org.opensrp.dto.form.MultimediaDTO;
import org.opensrp.service.MultimediaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;

@Controller
@RequestMapping("/multimedia")
public class MultimediaController {
	
	private static Logger logger = LoggerFactory.getLogger(MultimediaController.class.toString());
	
	@Value("#{opensrp['multimedia.directory.name']}")
	private String multiMediaDir;

	@Value("#{opensrp['multimedia.allowed.file.types']}")
	private String allowedMimeTypes;
	
	private MultimediaService multimediaService;
	
	@Autowired
	public void setMultimediaService(MultimediaService multimediaService) {
		this.multimediaService = multimediaService;
	}
	
	/**
	 * Download a file from the multimedia directory. The method also assumes two file types mp4 and
	 * images whereby all images are stored in the images folder and videos in mp4 in the multimedia
	 * directory This method is set to bypass spring security config but authenticate through the
	 * username/password passed at the headers
	 *
	 * @param response
	 * @param fileName
	 * @param userName
	 * @param password
	 * @throws IOException
	 */
	@RequestMapping(value = "/download/{fileName:.+}", method = RequestMethod.GET)
	public void downloadFileWithAuth(HttpServletResponse response, @PathVariable("fileName") String fileName) {
		
		try {
			File file = multimediaService
			        .retrieveFile(multiMediaDir + File.separator + "images" + File.separator + fileName.trim());
			if (file != null) {
				if (fileName.endsWith("mp4")) {
					file = new File(multiMediaDir + File.separator + "videos" + File.separator + fileName.trim());
				}
				downloadFile(file, response);
			} else {
				writeFileNotFound(response);
			}
		}
		catch (Exception e) {
			logger.error("", e);
		}
	}
	
	/**
	 * Downloads a file from the server given the client id. A search is made to the multimedia repo
	 * to see if any file exists mapped to the user whereby the filepath is recorded
	 *
	 * @param response
	 * @param baseEntityId
	 * @param userName
	 * @param password
	 * @throws Exception
	 */
	@RequestMapping(value = "/profileimage/{baseEntityId}", method = RequestMethod.GET)
	public void downloadFileByClientId(HttpServletResponse response, @PathVariable("baseEntityId") String baseEntityId,
	        @RequestHeader(value = "username") String userName, @RequestHeader(value = "password") String password,
	        HttpServletRequest request) {
		downloadFileWithAuth(baseEntityId, response);
	}
	
	/**
	 * Downloads all media files belonging to the specified {@param entityId} if
	 * {@param fileCategory} is multi_version If multi_version {@param fileCategory} is not
	 * specified, a single media file (belonging to {@param entityId}) is downloaded from the
	 * default multimedia directory
	 *
	 * @param response
	 * @param request
	 * @param entityId
	 * @param contentType
	 * @param fileCategory
	 * @param userName
	 * @param password
	 */
	@RequestMapping(value = "/media/{entity-id}", method = RequestMethod.GET)
	public void downloadFiles(HttpServletResponse response, @PathVariable("entity-id") String entityId,
	        @RequestParam(value = "content-type", required = false) String contentType,
	        @RequestParam(value = "file-category", required = false) String fileCategory) {
		
		// todo: change this to a common repo constant
		if (!TextUtils.isBlank(fileCategory) && "multi_version".equals(fileCategory)) {
			List<Multimedia> multimediaFiles = multimediaService.getMultimediaFiles(entityId.trim(), contentType.trim(),
			    fileCategory.trim());
			response.setContentType("image/jpeg/zip");
			response.setHeader("Content-Disposition", "attachment; filename=images.zip");
			try {
				ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(response.getOutputStream()));
				zipFiles(zipOutputStream, multimediaFiles, multimediaService.getFileManager());
				zipOutputStream.close();
			}
			catch (IOException e) {
				logger.error("", e);
			}
		} else {
			// default to single profile image retrieval logic
			downloadFileWithAuth(entityId, response);
		}
	}
	
	@RequestMapping(headers = { "Accept=multipart/form-data" }, method = POST, value = "/upload")
	public ResponseEntity<String> uploadFiles(@RequestParam("anm-id") String providerId,
			@RequestParam("entity-id") String entityId,
			@RequestParam("file-category") String fileCategory,
			@RequestParam("file") MultipartFile file) {

		String mimeType = file.getContentType();
		if (!allowedMimeTypes.contains(mimeType)) {
			return new ResponseEntity<String>("MIME Type is not allowed", HttpStatus.BAD_REQUEST);
		}

		MultimediaDTO multimediaDTO = new MultimediaDTO(entityId.trim(), providerId.trim(), file.getContentType().trim(), null, fileCategory.trim());
		String status = null;
		try {
			status = multimediaService.saveFile(multimediaDTO, file.getBytes(), file.getOriginalFilename());
		}
		catch (IOException e) {
			logger.error("", e);
		}
		
		return new ResponseEntity<>(new Gson().toJson(status), HttpStatus.OK);
	}
	
	/**
	 * Downloads file on successful authentication
	 *
	 * @param baseEntityId
	 * @param userName
	 * @param password
	 * @param request
	 * @param response
	 */
	private void downloadFileWithAuth(String baseEntityId, HttpServletResponse response) {
		try {
			File file = multimediaService.retrieveFile(multiMediaDir + File.separator + MultimediaService.IMAGES_DIR
			        + File.separator + baseEntityId.trim() + ".jpg");
			if (file != null) {
				downloadFile(file, response);
			} else {
				writeFileNotFound(response);
			}
		}
		catch (Exception e) {
			logger.error("", e);
		}
	}
	
	/**
	 * Retrieves file and writes content to response (downloads file)
	 *
	 * @param file
	 * @param response
	 * @throws Exception
	 */
	private void downloadFile(File file, HttpServletResponse response) throws Exception {
		
		if (!file.exists()) {
			writeFileNotFound(response);
			return;
		}
		
		String mimeType = URLConnection.guessContentTypeFromName(file.getName());
		if (mimeType == null) {
			logger.info("mimetype is not detectable, will take default");
			mimeType = "application/octet-stream";
		}
		
		logger.info("mimetype : " + mimeType);
		
		response.setContentType(mimeType);
		
		/* "Content-Disposition : inline" will show viewable types [like images/text/pdf/anything viewable by browser] right on browser 
		    while others(zip e.g) will be directly downloaded [may provide save as popup, based on your browser setting.]*/
		response.setHeader("Content-Disposition", String.format("inline; filename=\"" + file.getName() + "\""));
		
		/* "Content-Disposition : attachment" will be directly download, may provide save as popup, based on your browser setting*/
		//response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", file.getName()));
		
		response.setContentLength((int) file.length());
		
		InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
		
		//Copy bytes from source to destination(outputstream in this example), closes both streams.
		FileCopyUtils.copy(inputStream, response.getOutputStream());
	}
	
	/**
	 * Writes a file not found error response
	 *
	 * @param response
	 * @throws IOException
	 */
	private void writeFileNotFound(HttpServletResponse response) throws IOException {
		String errorMessage = "Sorry. The file you are looking for does not exist";
		logger.info(errorMessage);
		OutputStream outputStream = response.getOutputStream();
		outputStream.write(errorMessage.getBytes(Charset.forName("UTF-8")));
		outputStream.close();
	}
}
