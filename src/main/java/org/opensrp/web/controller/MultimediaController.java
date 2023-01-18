package org.opensrp.web.controller;

import com.google.gson.Gson;
import org.apache.http.util.TextUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensrp.domain.Multimedia;
import org.opensrp.dto.form.MultimediaDTO;
import org.opensrp.service.MultimediaService;
import org.opensrp.web.security.DrishtiAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipOutputStream;

import static org.opensrp.web.rest.RestUtils.zipFiles;
import static org.opensrp.web.utils.MultimediaUtil.hasSpecialCharacters;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping("/multimedia")
public class MultimediaController {

	private static Logger logger = LogManager.getLogger(MultimediaController.class.toString());

	@Value("#{opensrp['multimedia.directory.name']}")
	private String multiMediaDir;

	@Value("#{opensrp['multimedia.allowed.file.types'] ?: 'application/octet-stream,image/jpeg,image/gif,image/png'}")
	private String allowedMimeTypes;

	@Autowired
	@Qualifier("drishtiAuthenticationProvider")
	private DrishtiAuthenticationProvider provider;

	private MultimediaService multimediaService;

	private final String MULTI_VERSION = "multi_version";

	public static final String FILE_NAME_ERROR_MESSAGE = "Sorry. File name should not contain any special character";
	public static final String ENTITY_ID_ERROR_MESSAGE = "Sorry. Entity ID should not contain any special character";

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
	public void downloadFileWithAuth(HttpServletResponse response, @PathVariable("fileName") String fileName, HttpServletRequest request) {

		try {
			if (hasSpecialCharacters(fileName)) {
				specialCharactersError(response, FILE_NAME_ERROR_MESSAGE);
				return;
			}

			File file = multimediaService.retrieveFile(multiMediaDir + File.separator + "images" + File.separator + fileName.trim());
			if (file != null) {
				if (fileName.endsWith("mp4")) {
					file = new File(multiMediaDir + File.separator + "videos" + File.separator + fileName.trim());
				}
				downloadFile(file, response);
			} else {
				writeFileNotFound(response);
			}
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	/**
	 * Downloads a file from the server given the client id. A search is made to the
	 * multimedia repo to see if any file exists mapped to the user whereby the filepath is recorded
	 *
	 * @param response
	 * @param baseEntityId
	 * @param userName
	 * @param password
	 * @throws Exception
	 */
	@RequestMapping(value = "/profileimage/{baseEntityId}", method = RequestMethod.GET)
	public void downloadFileByClientId(HttpServletResponse response, @PathVariable("baseEntityId") String baseEntityId,
			@RequestHeader(value = "username") String userName,
			@RequestHeader(value = "password") String password, HttpServletRequest request) {

		try {
			if (hasSpecialCharacters(baseEntityId)) {
				specialCharactersError(response, ENTITY_ID_ERROR_MESSAGE);
				return;
			}
			downloadFileWithAuth(baseEntityId, userName, password, request, response);
		} catch (Exception e) {
			logger.error("Exception occurred in downloading file by client ID ", e);
		}
	}

	/**
	 * Downloads all media files belonging to the specified {@param entityId} if {@param fileCategory} is multi_version
	 * If multi_version {@param fileCategory} is not specified, a single media file (belonging to {@param entityId})
	 * is downloaded from the default multimedia directory
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
	public void downloadFiles(HttpServletResponse response,
			HttpServletRequest request,
			@PathVariable("entity-id") String entityId,
			@RequestParam(value = "content-type", required = false) String contentType,
			@RequestParam(value = "file-category", required = false) String fileCategory,
			@RequestHeader(value = "username") String userName,
			@RequestHeader(value = "password") String password) {


		if (!authenticate(userName, password, request).isAuthenticated()) { return; }

		if (!TextUtils.isBlank(fileCategory) && MULTI_VERSION.equals(fileCategory)) {
			List<Multimedia> multimediaFiles = multimediaService
					.getMultimediaFiles(entityId.trim(), contentType.trim(), fileCategory.trim());
			response.setContentType("image/jpeg/zip");
			response.setHeader("Content-Disposition", "attachment; filename=images.zip");
			try {
				ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(response.getOutputStream()));
				zipFiles(zipOutputStream, multimediaFiles, multimediaService.getFileManager());
				zipOutputStream.close();
			} catch (IOException e) {
				logger.error("", e);
			}
		} else {
			// default to single profile image retrieval logic
			downloadFileWithAuth(entityId, userName, password, request, response);
		}
	}

	@RequestMapping(headers = { "Accept=multipart/form-data" }, method = POST, value = "/upload")
	public ResponseEntity<String> uploadFiles(@RequestParam("anm-id") String providerId,
			@RequestParam("entity-id") String entityId,
			@RequestParam("file-category") String fileCategory,
			@RequestParam("file") MultipartFile file) {

		if(hasSpecialCharacters(file.getOriginalFilename())) {
			logger.error(FILE_NAME_ERROR_MESSAGE);
			return new ResponseEntity<String>(FILE_NAME_ERROR_MESSAGE, HttpStatus.BAD_REQUEST);
		}

		if (hasSpecialCharacters(entityId)) {
			logger.error("Could not save multimedia file. Entity Id should not contain any special character!");
			return new ResponseEntity<String>("Entity Id should not contain any special character!", HttpStatus.BAD_REQUEST);
		}

		String mimeType = file.getContentType();
		if (!allowedMimeTypes.contains(mimeType)) {
			logger.error("Could not save multimedia file. MIME type is not allowed!");
			return new ResponseEntity<String>("MIME Type is not allowed", HttpStatus.BAD_REQUEST);
		}

		MultimediaDTO multimediaDTO = new MultimediaDTO(entityId.trim(), providerId.trim(), file.getContentType().trim(), null, fileCategory.trim());
		multimediaDTO.withOriginalFileName(file.getOriginalFilename()).withDateUploaded(new Date());
		String status = null;
		try {
			logger.info("Saving multimedia file...");
			status = multimediaService.saveFile(multimediaDTO, file.getBytes(), file.getOriginalFilename());
		} catch (IOException e) {
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
	private void downloadFileWithAuth(String baseEntityId, String userName, String password, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			if (!authenticate(userName, password, request).isAuthenticated()) { return; }
			MultimediaDTO multimediaDTO = new MultimediaDTO(baseEntityId, "", "image/jpeg", null, "");
			File file = multimediaService.retrieveFile(multimediaService.getFileManager().getMultimediaFilePath(multimediaDTO, baseEntityId));
			if (file != null) {
				downloadFile(file, response);
			} else {
				writeFileNotFound(response);
			}
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	private Authentication authenticate(String userName, String password, HttpServletRequest request) {
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userName, password);
		WebAuthenticationDetails details = new WebAuthenticationDetailsSource().buildDetails(request);
		auth.setDetails(details);
		return provider.authenticate(auth);
	}

	/**
	 * Retrieves file and writes content to response (downloads file)
	 *
	 * @param file
	 * @param response
	 * @throws Exception
	 */
	private void downloadFile(File file, HttpServletResponse response) throws Exception {

		if(hasSpecialCharacters(file.getName())) {
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
	 *
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
}
