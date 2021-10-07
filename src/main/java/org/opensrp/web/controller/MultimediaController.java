package org.opensrp.web.controller;

import static org.opensrp.web.rest.RestUtils.zipFiles;
import static org.opensrp.web.utils.MultimediaUtil.hasSpecialCharacters;
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

import javax.servlet.http.HttpServletResponse;

import org.apache.http.util.TextUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensrp.domain.Multimedia;
import org.opensrp.dto.form.MultimediaDTO;
import org.opensrp.service.MultimediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;
import java.util.Date;

@Controller
@RequestMapping("/multimedia")
public class MultimediaController {
	
	private static Logger logger = LogManager.getLogger(MultimediaController.class.toString());
	
	@Value("#{opensrp['multimedia.directory.name']}")
	private String multiMediaDir;
	
	@Value("#{opensrp['multimedia.allowed.file.types']}")
	private String allowedMimeTypes;

	@Value("#{opensrp['multimedia.file.manager']}")
	private String fileManager;
	
	private MultimediaService multimediaService;
	
	public final static String FILE_NAME_ERROR_MESSAGE = "Sorry. File Name should not contain any special character";
	
	private final static String ENTITY_ID_ERROR_MESSAGE = "Sorry. Entity Id should not contain any special character";

	private final static String FILE_SYSTEM_MULTIMEDIA_MANAGER = "FileSystemMultimediaFileManager";
	
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
	 * @throws IOException
	 */
	@RequestMapping(value = "/download/{fileName:.+}", method = RequestMethod.GET)
	public void downloadFileWithAuth(HttpServletResponse response, @PathVariable("fileName") String fileName)
	        throws IOException {
		
		if (hasSpecialCharacters(fileName)) {
			specialCharactersError(response, FILE_NAME_ERROR_MESSAGE);
			return;
		}
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
		downloadFile(file, response);
	}
	
	/**
	 * Downloads a file from the server given the client id. A search is made to the multimedia repo
	 * to see if any file exists mapped to the user whereby the filepath is recorded
	 *
	 * @param response
	 * @param baseEntityId
	 * @throws IOException
	 * @throws Exception
	 */
	@RequestMapping(value = "/profileimage/{baseEntityId}", method = RequestMethod.GET)
	public void downloadFileByClientId(HttpServletResponse response, @PathVariable("baseEntityId") String baseEntityId)
	        throws IOException {
		
		if (hasSpecialCharacters(baseEntityId)) {
			specialCharactersError(response, ENTITY_ID_ERROR_MESSAGE);
			return;
		}
		downloadFileWithAuth(baseEntityId, response, false);
		
	}
	
	/**
	 * Downloads all media files belonging to the specified {@param entityId} if
	 * {@param fileCategory} is multi_version If multi_version {@param fileCategory} is not
	 * specified, a single media file (belonging to {@param entityId}) is downloaded from the
	 * default multimedia directory
	 *
	 * @param response
	 * @param entityId
	 * @param contentType
	 * @param fileCategory
	 * @param dynamicMediaDirectory When set to true the multimedia directory value is retrieved from the fileCategory value
	 *                              else it defaults to patient_images
	 * @throws Exception
	 */
	@RequestMapping(value = "/media/{entity-id}", method = RequestMethod.GET)
	public void downloadFiles(HttpServletResponse response, @PathVariable("entity-id") String entityId,
	        @RequestParam(value = "content-type", required = false) String contentType,
	        @RequestParam(value = "file-category", required = false) String fileCategory,
			@RequestParam(value = "dynamic-media-directory", defaultValue = "false", required = false) Boolean dynamicMediaDirectory
	) throws IOException {
		
		// todo: change this to a common repo constant
		if (!TextUtils.isBlank(fileCategory) && "multi_version".equals(fileCategory)) {
			List<Multimedia> multimediaFiles = multimediaService.getMultimediaFiles(entityId.trim(), contentType.trim(),
			    fileCategory.trim());
			response.setContentType("image/jpeg/zip");
			response.setHeader("Content-Disposition", "attachment; filename=images.zip");
			ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(response.getOutputStream()));
			zipFiles(zipOutputStream, multimediaFiles, multimediaService.getFileManager());
			zipOutputStream.close();
		} else {
			// default to single profile image retrieval logic
			downloadFileWithAuth(entityId, response, dynamicMediaDirectory);
		}
	}
	
	@RequestMapping(headers = { "Accept=multipart/form-data" }, method = POST, value = "/upload")
	public ResponseEntity<String> uploadFiles(@RequestParam("anm-id") String providerId,
	        @RequestParam("entity-id") String entityId, @RequestParam("file-category") String fileCategory,
	        @RequestParam("file") MultipartFile file) throws IOException {
		
		if (hasSpecialCharacters(file.getOriginalFilename())) {
			return new ResponseEntity<String>("File Name with special characters is not allowed!", HttpStatus.BAD_REQUEST);
		}
		if (hasSpecialCharacters(entityId)) {
			return new ResponseEntity<String>("Entity Id should not contain any special character!", HttpStatus.BAD_REQUEST);
		}
		
		String mimeType = file.getContentType();
		if (!allowedMimeTypes.contains(mimeType)) {
			return new ResponseEntity<String>("MIME Type is not allowed", HttpStatus.BAD_REQUEST);
		}

		MultimediaDTO multimediaDTO = new MultimediaDTO(entityId.trim(), providerId.trim(), file.getContentType().trim(), null, fileCategory.trim());
		multimediaDTO.withOriginalFileName(file.getOriginalFilename()).withDateUploaded(new Date());

		logger.info("Saving multimedia file...");
		String status = multimediaService.saveFile(multimediaDTO, file.getBytes(), file.getOriginalFilename());

		return new ResponseEntity<>(new Gson().toJson(status), HttpStatus.OK);
	}
	
	/**
	 * Downloads file on successful authentication
	 *
	 * @param baseEntityId
	 * @param response
	 * @param dynamicMediaDirectory When set to true the multimedia directory value is retrieved from the fileCategory value
	 *                              else it defaults to patient_images
	 * @throws Exception
	 */
	private void downloadFileWithAuth(String baseEntityId, HttpServletResponse response, boolean dynamicMediaDirectory) throws IOException {
		Multimedia multimedia = multimediaService.findByCaseId(String.valueOf(baseEntityId));
		String extension = "";
		if (multimedia != null && multimedia.getContentType() != null) {
			extension = getFileExtension(multimedia);
		}
		String fileExtension = StringUtils.isEmpty(extension) ? ".jpg" : extension;
		String multimediaDirectory = dynamicMediaDirectory ? multimedia.getFileCategory() : MultimediaService.IMAGES_DIR;
		String fileLocation = !FILE_SYSTEM_MULTIMEDIA_MANAGER.equals(fileManager)?
				multimediaDirectory + File.separator + baseEntityId.trim() + fileExtension :
				multiMediaDir + File.separator + MultimediaService.IMAGES_DIR + File.separator + baseEntityId.trim() + fileExtension;

		File file = multimediaService.retrieveFile(fileLocation);
		if (file != null) {
			downloadFile(file, response);
		} else {
			writeFileNotFound(response);
		}
	}
	
	/**
	 * Retrieves file and writes content to response (downloads file)
	 *
	 * @param file
	 * @param response
	 * @throws Exception
	 */
	private void downloadFile(File file, HttpServletResponse response) throws IOException {
		
		if (hasSpecialCharacters(file.getName())) {
			specialCharactersError(response, FILE_NAME_ERROR_MESSAGE);
			return;
		}
		
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
	
	private void specialCharactersError(HttpServletResponse response, String errorMessage) throws IOException {
		logger.error(errorMessage);
		OutputStream outputStream = response.getOutputStream();
		response.setStatus(HttpStatus.BAD_REQUEST.value());
		outputStream.write(errorMessage.getBytes(Charset.forName("UTF-8")));
		outputStream.close();
	}

	private String getFileExtension(Multimedia multimedia) {
		String fileExt = "";
		switch (multimedia.getContentType()) {

			case "application/octet-stream":
				fileExt = ".mp4";
				break;
			case "image/jpeg":
				fileExt = ".jpg";
				break;
			case "image/gif":
				fileExt = ".gif";
				break;
			case "image/png":
				fileExt = ".png";
				break;
			case "text/csv":
			case "application/vnd.ms-excel":
				fileExt = ".csv";
				break;
			default:
				throw new IllegalArgumentException("Unknown content type : " + multimedia.getContentType());
		}
		return fileExt;
	}
}
