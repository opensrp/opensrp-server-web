package org.opensrp.web.controller;

import static org.opensrp.web.rest.RestUtils.getStringFilter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.opensrp.api.domain.User;
import org.opensrp.connector.openmrs.service.OpenmrsUserService;
import org.opensrp.service.OpenmrsIDService;
import org.opensrp.web.utils.PdfUtil;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.gson.Gson;

@Controller
@RequestMapping("/uniqueids")
public class UniqueIdController {

	private static Logger logger = LogManager.getLogger(UniqueIdController.class.toString());

	@Value("#{opensrp['qrcodes.directory.name']}")
	private String qrCodesDir;
	
	@Value("#{opensrp['openmrs.username']}")
	protected String OPENMRS_USER;
	
	@Value("#{opensrp['openmrs.password']}")
	protected String OPENMRS_PWD;

	@Autowired
	private OpenmrsIDService openmrsIdService;
	
	@Autowired
	private OpenmrsUserService openmrsUserService;

	/**
	 * Download extra ids from openmrs if less than the specified batch size, convert the ids to qr
	 * and print to a pdf
	 *
	 * @param request
	 * @param response
	 * @return
	 * @throws JSONException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/print", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> thisMonthDataSendTODHIS2(HttpServletRequest request, HttpServletResponse response){

		String message;
		User user;
		Integer numberToGenerate = Integer.valueOf(getStringFilter("batchSize", request));
		List<String> idsToPrint = openmrsIdService.getNotUsedIdsAsString(numberToGenerate);
		SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy-HHmmss");
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String currentPrincipalName = authentication.getName();
		String fileName = "QRCodes_".concat(df.format(new Date())).concat("_").concat(currentPrincipalName)
				.concat("_" + numberToGenerate + ".pdf");
		response.setHeader("Expires", "0");
		response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
		response.setHeader("Pragma", "public");
		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

		try (ByteArrayOutputStream byteArrayOutputStream = PdfUtil.generatePdf(idsToPrint, 140, 140, 1, 5);
		     FileOutputStream fileOutputStream = new FileOutputStream(qrCodesDir + File.separator + fileName);
		     OutputStream os = response.getOutputStream();) {
			user = openmrsUserService.getUser(currentPrincipalName);
			if (!checkRoleIfRoleExitst(user.getRoles(), "opensrp-generate-qr-code")) {
				return new ResponseEntity<>("Sorry, insufficient privileges to generate ID QR codes", HttpStatus.OK);
			}

			openmrsIdService.downloadAndSaveIds(numberToGenerate, currentPrincipalName);
			if (byteArrayOutputStream.size() > 0) {
				//mark ids as used
				fileOutputStream.write(byteArrayOutputStream.toByteArray());
				openmrsIdService.markIdsAsUsed(idsToPrint);
				byteArrayOutputStream.writeTo(os);
			}
			message = "Successfully generated the ID QR codes";

		}
		catch (Exception e) {
			logger.error("", e);
			message = "Sorry, an error occured when generating the qr code pdf";
		}

		return new ResponseEntity<>(new Gson().toJson("" + message), HttpStatus.OK);
	}

	boolean checkRoleIfRoleExitst(List<String> roleList, String role) {
		for (String roleName : roleList)
			if (StringUtils.containsIgnoreCase(roleName, role))
				return true;
		return false;
	}
	/**
	 * Fetch unique Ids from OMRS
	 *
	 * @return  json array object with ids
	 */

	@RequestMapping(value = "/get", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	protected ResponseEntity<String> get(HttpServletRequest request) throws JSONException {

		String numberToGenerate = getStringFilter("numberToGenerate", request);
		String source = getStringFilter("source", request);
		Map<String, Object> map = new HashMap<>();

		map.put("identifiers", openmrsIdService.getOpenMRSIdentifiers(source, numberToGenerate));

		return new ResponseEntity<>(new Gson().toJson(map), HttpStatus.OK);
	}

}
