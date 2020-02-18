package org.opensrp.web.controller;

import static org.opensrp.web.rest.RestUtils.getStringFilter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.opensrp.api.domain.User;
import org.opensrp.web.custom.service.UniqueIdentifierService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;

@Controller
@RequestMapping("/uniqueids")
public class UniqueIdController {

	private static Logger logger = LoggerFactory.getLogger(UniqueIdController.class.toString());

	@Value("#{opensrp['qrcodes.directory.name']}")
	private String qrCodesDir;

	/*@Autowired
	private OpenmrsIDService openmrsIdService;*/

	@Autowired
	private UserController userController;

	@Autowired
	private UniqueIdentifierService uniqueIdentifierService;
	
	/*@Autowired
	private OpenmrsUserService openmrsUserService;*/

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
	public ResponseEntity<String> thisMonthDataSendTODHIS2(HttpServletRequest request, HttpServletResponse response){

		String message;
		User user;
		try {
			/*Integer numberToGenerate = Integer.valueOf(getStringFilter("batchSize", request));

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
			}*/
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

	@RequestMapping(value = "/get", method = RequestMethod.GET)
	@ResponseBody
	protected ResponseEntity<String> get(HttpServletRequest request) throws JSONException {

		int numberToGenerate = Integer.parseInt(getStringFilter("numberToGenerate", request));
		String usedBy = getStringFilter("usedBy", request);
		String source = getStringFilter("source", request);
		Map<String, Object> map = new HashMap<>();

		map.put("identifiers", uniqueIdentifierService.generateIdentifiers(usedBy, numberToGenerate));
		
		/*map.put("identifiers", null openmrsIdService.getOpenMRSIdentifiers(source, numberToGenerate,
				SecurityContextHolder.getContext().getAuthentication().getName(),
				userController.getAuthenticationAdvisor(request).getCredentials().toString()));*/

		return new ResponseEntity<>(new Gson().toJson(map), HttpStatus.OK);
	}

}
