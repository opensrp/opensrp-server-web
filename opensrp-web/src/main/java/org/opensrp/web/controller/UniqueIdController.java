package org.opensrp.web.controller;

import static org.opensrp.web.rest.RestUtils.getStringFilter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.opensrp.api.domain.User;
import org.opensrp.common.util.HttpResponse;
import org.opensrp.common.util.HttpUtil;
import org.opensrp.common.util.TurnOffCertificateValidation;
import org.opensrp.connector.openmrs.service.OpenmrsUserService;
import org.opensrp.service.EventService;
import org.opensrp.service.OpenmrsIDService;
import org.opensrp.web.utils.PdfUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.ibm.icu.text.SimpleDateFormat;

@Controller
@RequestMapping("/uniqueids")
public class UniqueIdController {
	
	private static Logger logger = LoggerFactory.getLogger(UniqueIdController.class.toString());
	
	@Autowired
	private EventService eventService;
	
	@Value("#{opensrp['qrcodes.directory.name']}")
	private String qrCodesDir;
	
	@Value("#{opensrp['opensrp.web.url']}")
	private String opensrpWebUurl;
	
	@Value("#{opensrp['opensrp.web.username']}")
	private String opensrpWebUsername;
	
	@Value("#{opensrp['opensrp.web.password']}")
	private String opensrpWebPassword;
	
	@Autowired
	OpenmrsIDService openmrsIdService;
	
	@Autowired
	OpenmrsUserService openmrsUserService;
	
	@Autowired
	private UserController userController;
	
	/**
	 * Download extra ids from openmrs if less than the specified batch size, convert the ids to qr
	 * and print to a pdf
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws JSONException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/print")
	@ResponseBody
	public ResponseEntity<String> thisMonthDataSendTODHIS2(HttpServletRequest request, HttpServletResponse response)
	    throws JSONException {
		
		String message = "";
		User user = null;
		try {
			Integer numberToGenerate = Integer.valueOf(getStringFilter("batchSize", request));
			
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String currentPrincipalName = authentication.getName();
			user = openmrsUserService.getUser(currentPrincipalName);
			if (!checkRoleIfRoleExitst(user.getRoles(), "opensrp-generate-qr-code")) {
				return new ResponseEntity<>("Sorry, insufficient privileges to generate ID QR codes", HttpStatus.OK);
			}
			
			openmrsIdService.downloadAndSaveIds(numberToGenerate, currentPrincipalName);
			List<String> idsToPrint = openmrsIdService.getNotUsedIdsAsString(numberToGenerate);
			SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy-HHmmss");
			
			String fileName = "QRCodes_".concat(df.format(new Date())).concat("_").concat(currentPrincipalName)
			        .concat("_" + numberToGenerate + ".pdf");
			ByteArrayOutputStream byteArrayOutputStream = PdfUtil.generatePdf(idsToPrint, 140, 140, 1, 5);
			if (byteArrayOutputStream.size() > 0) {
				//mark ids as used
				FileOutputStream fileOutputStream = new FileOutputStream(qrCodesDir + File.separator + fileName);
				fileOutputStream.write(byteArrayOutputStream.toByteArray());
				fileOutputStream.close();
				openmrsIdService.markIdsAsUsed(idsToPrint);
				
				response.setHeader("Expires", "0");
				response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
				response.setHeader("Pragma", "public");
				response.setContentType("application/pdf");
				response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
				
				OutputStream os = response.getOutputStream();
				byteArrayOutputStream.writeTo(os);
				os.flush();
				os.close();
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
	 * @return json array object with ids
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
	
	@RequestMapping(value = "/get", method = RequestMethod.GET)
	@ResponseBody
	protected ResponseEntity<String> get(HttpServletRequest request) throws JSONException, KeyManagementException,
	    NoSuchAlgorithmException, KeyStoreException {
		
		String numberToGenerate = getStringFilter("numberToGenerate", request);
		String source = getStringFilter("source", request);
		Map<String, Object> map = new HashMap<>();
		
		map.put(
		    "identifiers",
		    openmrsIdService.getOpenMRSIdentifiers(source, numberToGenerate, SecurityContextHolder.getContext()
		            .getAuthentication().getName(), userController.getAuthenticationAdvisor(request).getCredentials()
		            .toString()));
		
		return new ResponseEntity<>(new Gson().toJson(map), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/get/health-id", method = RequestMethod.GET)
	@ResponseBody
	protected ResponseEntity<String> getHealthId(HttpServletRequest request) throws JSONException, KeyManagementException,
	    NoSuchAlgorithmException, KeyStoreException {		
		/*System.err.println("getHealthId:"+eventService.getHealthId());*/
		HttpResponse op = HttpUtil.get(opensrpWebUurl + "/rest/api/v1/health-id/reserved", "", opensrpWebUsername,
		    opensrpWebPassword);
		JSONObject jsonObj = new JSONObject(op.body());
		logger.info("health id fetched :" + jsonObj.length());
		return new ResponseEntity<>(jsonObj.toString(), HttpStatus.OK);
	}
	
}
