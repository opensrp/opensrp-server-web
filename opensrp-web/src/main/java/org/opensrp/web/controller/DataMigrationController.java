/*package org.opensrp.web.controller;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpStatus;
import org.opensrp.common.util.HttpResponse;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opensrp.common.util.TurnOffCertificateValidation;
import org.opensrp.connector.openmrs.service.OpenmrsLocationService;
import org.opensrp.domain.Address;
import org.opensrp.domain.Client;
import org.opensrp.domain.Event;
import org.opensrp.domain.Obs;
import org.opensrp.repository.ClientsRepository;
import org.opensrp.service.ClientService;
import org.opensrp.service.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.mysql.jdbc.StringUtils;

@Controller
@RequestMapping("/data/")
public class DataMigrationController {
	
	@Value("#{opensrp['qrcodes.directory.name']}")
	private String qrCodesDir;
	
	@Value("#{opensrp['opensrp.web.url']}")
	private String opensrpWebUurl;
	
	@Value("#{opensrp['opensrp.web.username']}")
	private String opensrpWebUsername;
	
	@Value("#{opensrp['opensrp.web.password']}")
	private String opensrpWebPassword;
	
	@Value("#{opensrp['openmrs.url']}")
	private String openMRSURL;
	
	@Autowired
	@Qualifier("clientsRepositoryPostgres")
	private ClientsRepository clientsRepository;
	
	private static Logger logger = LoggerFactory.getLogger(DataMigrationController.class.toString());
	
	private static final String GEN_ID_URL = "ws/rest/v1/idgen";
	
	@Autowired
	private EventService eventService;
	
	@Autowired
	private ClientService clientService;
	
	@Autowired
	private OpenmrsLocationService openmrsLocationService;
	
	@RequestMapping(value = "migration.html", method = RequestMethod.GET)
	public String csvUpload(ModelMap model, HttpSession session) throws JSONException {
		model.addAttribute("location", new Location());
		return "/upload_csv";
	}
	
	@RequestMapping(value = "/event.html", method = RequestMethod.GET)
	public String eventUpdate(ModelMap model, HttpSession session) throws JSONException {
		List<Event> events = eventService.getAll();
		for (Event event : events) {
			event.setProviderId("");
			eventService.updateEvent(event);
		}
		model.addAttribute("location", new Location());
		return "/upload_csv";
		
	}

	public String updateAddressByClientUnion() {
		List<String> addresses = new ArrayList<>();
		return "";
	}
	
	@RequestMapping(value = "/migration.html", method = RequestMethod.POST)
	public ModelAndView csvUpload(@RequestParam MultipartFile file, HttpServletRequest request, ModelMap model)
	    throws Exception {
		String msg = "";
		if (file.isEmpty()) {
			model.put("msg", "failed to upload file because its empty");
			model.addAttribute("msg", "failed to upload file because its empty");
			return new ModelAndView("/location/upload_csv");
		} else if (!"text/csv".equalsIgnoreCase(file.getContentType())) {
			model.addAttribute("msg", "file type should be '.csv'");
			return new ModelAndView("/upload_csv");
		}
		
		String rootPath = request.getSession().getServletContext().getRealPath("/");
		File dir = new File(rootPath + File.separator + "uploadedfile");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		
		File csvFile = new File(dir.getAbsolutePath() + File.separator + file.getOriginalFilename());
		
		try {
			try (InputStream is = file.getInputStream();
			        BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(csvFile))) {
				int i;
				
				while ((i = is.read()) != -1) {
					stream.write(i);
				}
				stream.flush();
			}
		}
		catch (IOException e) {
			model.put("msg", "failed to process file because : " + e.getMessage());
			return new ModelAndView("/upload_csv");
		}
		logger.info("CSV FIle:" + csvFile.getName());
		String fileName = csvFile.getName();
		if (fileName.equalsIgnoreCase("hh.csv")) {
			msg = addHousehold(csvFile);
		} else if (fileName.equalsIgnoreCase("member.csv")) {
			msg = addMember(csvFile);
		} else {
			logger.info("Please give coorect file");
		}
		if (!msg.isEmpty()) {
			model.put("msg", msg);
			
		} else {
			model.put("msg", "successfully uploaded  " + fileName);
		}
		return new ModelAndView("/upload_csv");
		
	}
	
	public class Location {
		
	}
	
	@SuppressWarnings("resource")
	public String addMember(File csvFile) throws Exception {
		String msg = "";
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		SimpleDateFormat getYYYYMMDDTHHMMSSFormat = new SimpleDateFormat("mm/dd/yyyy");
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sss");
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:sss");
		SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd");
		
		try {
			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {
				
				HttpResponse op1 = HttpUtil.get(opensrpWebUurl + "/rest/api/v1/health-id/reserved/single/migration", "",
				    opensrpWebUsername, opensrpWebPassword);
				JSONObject healthObj = new JSONObject(op1.body());
				String healthId = "";
				
				if (healthObj.has("identifiers")) {
					healthId = healthObj.getString("identifiers");
				} else {
					logger.info("No health id found...");
				}
				String[] member = line.split(cvsSplitBy);
				
				Client client = new Client(null);
				//client.addIdentifier("Patient_Identifier", healthId);
				String baseEntityId = UUID.randomUUID().toString().trim();
				client.setBaseEntityId(baseEntityId);
				String gender = member[9];
				String firstName = member[23];
				if (!firstName.isEmpty()) {
					firstName = firstName.trim();
				}
				if (!gender.isEmpty()) {
					gender = gender.trim();
				}
				DateTime dt = null;
				Date defaultDate = new Date();
				String dd = "";
				String reg = "";
				//format datetime in db. make shortDate in csv then upload. 
				String dob = member[8];
				if (!dob.isEmpty()) {
					dob = dob.trim();
					java.util.Date date = getYYYYMMDDTHHMMSSFormat.parse(dob);
					dd = format.format(date);
					reg = format2.format(date);
					dt = formatter.parseDateTime(dd);
				} else {
					dd = format.format(defaultDate);
					reg = format2.format(dd);
					dt = formatter.parseDateTime(dd);
				}
				client.withFirstName(firstName).withLastName("").withGender(gender).withBirthdate(dt, false)
				        .withDeathdate(null, false);
				client.setServerVersion(System.currentTimeMillis());
				/// attribute
				
				String MaritalStatus = member[10];
				if (!MaritalStatus.isEmpty()) {
					MaritalStatus = MaritalStatus.trim();
				}
				String education = member[20];
				if (!education.isEmpty()) {
					education = education.trim();
				}
				String occupation = member[21];
				if (!occupation.isEmpty()) {
					occupation = occupation.trim();
				}
				String Religion = member[11];
				if (!Religion.isEmpty()) {
					Religion = Religion.trim();
				}
				String nationalId = member[12];
				if (!nationalId.isEmpty()) {
					nationalId = nationalId.trim();
				}
				String RelationshipWithHH = member[25];
				client.addAttribute("MaritalStatus", MaritalStatus);
				client.addAttribute("education", education);
				client.addAttribute("occupation", occupation);
				client.addAttribute("Religion", Religion);
				client.addAttribute("Realtion_With_Household_Head", RelationshipWithHH);
				client.addAttribute("nationalId", nationalId);
				client.addAttribute("idtype", "NID");
				//client.withIsSendToOpenMRS("yes");
				client.withIsSendToOpenMRS("no");
				String FamilyDiseaseHistory = "";
				String diabetes = member[13];
				if (!diabetes.isEmpty()) {
					diabetes = diabetes.trim();
				}
				String hypertension = member[14];
				if (!hypertension.isEmpty()) {
					hypertension = hypertension.trim();
				}
				String cancer = member[15];
				if (!cancer.isEmpty()) {
					cancer = cancer.trim();
				}
				String respiratoryDisease = member[16];
				if (!respiratoryDisease.isEmpty()) {
					respiratoryDisease = respiratoryDisease.trim();
				}
				String phycologicalDisease = member[17];
				if (!phycologicalDisease.isEmpty()) {
					phycologicalDisease = phycologicalDisease.trim();
				}
				String obesity = member[18];
				if (!obesity.isEmpty()) {
					obesity = obesity.trim();
				}
				if (!diabetes.equalsIgnoreCase("NULL")) {
					FamilyDiseaseHistory = diabetes;
				} else if (!hypertension.equalsIgnoreCase("NULL")) {
					FamilyDiseaseHistory = hypertension;
				} else if (!cancer.equalsIgnoreCase("NULL")) {
					FamilyDiseaseHistory = cancer;
				} else if (!respiratoryDisease.equalsIgnoreCase("NULL")) {
					FamilyDiseaseHistory = respiratoryDisease;
				} else if (!phycologicalDisease.equalsIgnoreCase("NULL")) {
					FamilyDiseaseHistory = phycologicalDisease;
				} else if (!obesity.equalsIgnoreCase("NULL")) {
					FamilyDiseaseHistory = obesity;
				} else {
					
				}
				if (!FamilyDiseaseHistory.isEmpty()) {
					client.addAttribute("family_diseases_details", FamilyDiseaseHistory);
				}
				String householdCode = member[26];
				if (!householdCode.isEmpty()) {
					householdCode = householdCode.trim();
				}
				//System.err.println("householdCode:::::::::" + householdCode);
				List<Client> clients = clientService.findAllByAttribute("householdCode", householdCode);
				//System.err.println("Size::::::::::::::::" + clients.size());
				if (clients.size() != 0) {
					client.addRelationship("household", clients.get(0).getBaseEntityId());
				}
				
				String address1 = member[4];
				if (!address1.isEmpty()) {
					address1 = address1.trim();
				}
				String address2 = member[5];
				if (!address2.isEmpty()) {
					address2 = address2.trim();
				}
				String stateProvince = member[1];
				if (!stateProvince.isEmpty()) {
					stateProvince = stateProvince.trim();
				}
				String countyDistrict = member[2];
				if (!countyDistrict.isEmpty()) {
					countyDistrict = countyDistrict.trim();
				}
				String cityVillage = member[3];
				if (!cityVillage.isEmpty()) {
					cityVillage = cityVillage.trim();
				}
				
				// address put
				Map<String, String> addressFields = new HashMap<String, String>();
				addressFields.put("cityVillage", cityVillage);// upazilla
				addressFields.put("country", "BANGLADESH"); // country
				addressFields.put("address1", address1); // union
				addressFields.put("address2", address2); // ward
				addressFields.put("address3", null);
				addressFields.put("address4", null);
				addressFields.put("address5", null);
				addressFields.put("address6", null);
				addressFields.put("stateProvince", stateProvince); // division
				addressFields.put("countyDistrict", countyDistrict); // district
				addressFields.put("gps", null + " " + null);
				Address address = new Address();
				address.setAddressFields(addressFields);
				address.setAddressType("usual_residence");
				client.addAddress(address);
				
				//System.err.println("Client:" + client.toString());
				
				String locationId = "";
				String teamId = "";
				String team = "";
				org.opensrp.api.domain.Location location = null;
				try {
					location = openmrsLocationService.getLocation(address2);
					locationId = location.getLocationId();
					HttpResponse op = HttpUtil.get(opensrpWebUurl + "/rest/api/v1/team/team-by-location" + "/?name="
					        + address2, "", opensrpWebUsername, opensrpWebPassword);
					JSONObject jsonObj = new JSONObject(op.body());
					JSONObject map = jsonObj.getJSONObject("map");
					locationId = (String) map.get("locationUuid");
					//teamId = (String) map.get("teamUuid");
					//team = (String) map.get("team");
					
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				
				String eventType = "Woman Member Registration";
				String entityType = "ec_woman";
				if (RelationshipWithHH.equalsIgnoreCase("Household_Head") && gender.equalsIgnoreCase("M")) {
					eventType = "Member Registration";
					entityType = "ec_member";
				}
				Event event = new Event();
				event.setServerVersion(System.currentTimeMillis());
				event.setTeam(team);
				event.setTeamId(teamId);
				event.setBaseEntityId(baseEntityId);
				event.setDateCreated(new DateTime());
				event.setEventDate(new DateTime());
				event.withProviderId("");
				event.setVersion(System.currentTimeMillis());
				event.setLocationId(locationId);
				event.setFormSubmissionId(UUID.randomUUID().toString().trim());
				event.withIsSendToOpenMRS("no").withEventType(eventType).withEntityType(entityType);
				
				List<String> eventAddress = new ArrayList<String>();
				eventAddress.add("BANGLADESH");
				eventAddress.add(stateProvince);
				eventAddress.add(countyDistrict);
				eventAddress.add(cityVillage);
				eventAddress.add(address1);
				eventAddress.add(address2);
				JSONArray addressFieldValue = new JSONArray(eventAddress);
				
				event.addObs(new Obs("formsubmissionField", "text", "HIE_FACILITIES", "" //TODO handle parent,
				        addressFieldValue.toString(), ""comments, "HIE_FACILITIES"formSubmissionField));
				
				List<Object> values = new ArrayList<Object>();
				values.add(reg);
				String fieldDataType = "text";
				event.addObs(new Obs("formsubmissionField", fieldDataType, "Date_Of_Reg", "" //TODO handle parent,
				        values, ""comments, "Date_Of_Reg"formSubmissionField));
				clientService.addOrUpdate(client);
				eventService.addorUpdateEvent(event);
				clientService.addOrUpdate(client);
				//System.err.println("Event:::::::::::::::" + event.toString());
				
			}
			
		}
		catch (Exception e) {
			logger.info("Some problem occured, please contact with admin..");
			msg = "failed to process file because : " + e.fillInStackTrace();
			e.printStackTrace();
		}
		return msg;
	}
	
	@SuppressWarnings("resource")
	public String addHousehold(File csvFile) throws Exception {
		List<String> ids = new ArrayList<String>();
		ids.add("a59876eb-691c-4840-b9c7-88cb59c4c8e5");
		ids.add("d43bb068-ee72-4dee-bde5-0e684ab87f19");
		String field = "baseEntityId";
		eventService.findByFieldValue(field, ids, 2549279507797l);
		ClientSearchBean searchBean = new ClientSearchBean();
		searchBean.setServerVersion(1551783879608l);
		AddressSearchBean addressSearchBean = new AddressSearchBean();
		addressSearchBean.setStateProvince("DHAKA");
		System.err.println("Size:" + clientService.findByCriteria(searchBean, addressSearchBean));
		String msg = "";
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		
		try {
			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {
				
				String[] member = line.split(cvsSplitBy);
				
				Client client = new Client(null);
				//for health id
				HttpResponse op1 = HttpUtil.get(opensrpWebUurl + "/rest/api/v1/health-id/reserved/single/migration", "",
				    opensrpWebUsername, opensrpWebPassword);
				JSONObject healthObj = new JSONObject(op1.body());
				
				String healthId = "";
				if (healthObj.has("identifiers")) {
					healthId = healthObj.getString("identifiers");
				} else {
					logger.info("No health id found...");
				}
				client.addIdentifier("Patient_Identifier", healthId);
				//end: for health id
				String baseEntityId = UUID.randomUUID().toString().trim();
				client.setBaseEntityId(baseEntityId);
				String firstName = member[55];
				String phoneNumber = member[29];
				if (!firstName.isEmpty()) {
					firstName = firstName.trim();
				}
				if (!phoneNumber.isEmpty()) {
					phoneNumber = phoneNumber.trim();
				}
				client.withFirstName(firstName).withLastName("").withGender("H").withBirthdate(new DateTime(), false)
				        .withDeathdate(null, false);
				client.setServerVersion(System.currentTimeMillis());
				
				String householdCode = member[0];
				if (!householdCode.isEmpty()) {
					householdCode = householdCode.trim();
				}
				client.withIsSendToOpenMRS("no");
				client.addAttribute("householdCode", householdCode);
				client.addAttribute("phoneNumber", phoneNumber);
				String address1 = member[4];
				if (!address1.isEmpty()) {
					address1 = address1.trim();
				}
				String address2 = member[5];
				if (!address2.isEmpty()) {
					address2 = address2.trim();
				}
				String stateProvince = member[52];
				if (!stateProvince.isEmpty()) {
					stateProvince = stateProvince.trim();
				}
				String countyDistrict = member[53];
				if (!countyDistrict.isEmpty()) {
					countyDistrict = countyDistrict.trim();
				}
				String cityVillage = member[54];
				if (!cityVillage.isEmpty()) {
					cityVillage = cityVillage.trim();
				}
				
				Map<String, String> addressFields = new HashMap<String, String>();
				addressFields.put("cityVillage", cityVillage);// upazilla
				addressFields.put("country", "BANGLADESH"); // country
				addressFields.put("address1", address1); // union
				addressFields.put("address2", address2); // ward
				addressFields.put("address3", null);
				addressFields.put("address4", null);
				addressFields.put("address5", null);
				addressFields.put("address6", null);
				addressFields.put("stateProvince", stateProvince); // division
				addressFields.put("countyDistrict", countyDistrict); // district
				addressFields.put("gps", null + " " + null);
				Address address = new Address();
				address.setAddressFields(addressFields);
				address.setAddressType("usual_residence");
				client.addAddress(address);
				
				String locationId = "";
				String teamId = "";
				String team = "";
				org.opensrp.api.domain.Location location = null;
				try {
					//location = openmrsLocationService.getLocation(address2);
					//locationId = location.getLocationId();
					HttpResponse op = HttpUtil.get(opensrpWebUurl + "/rest/api/v1/team/team-by-location" + "/?name="
					        + address2, "", opensrpWebUsername, opensrpWebPassword);
					JSONObject jsonObj = new JSONObject(op.body());
					JSONObject map = jsonObj.getJSONObject("map");
					locationId = (String) map.get("locationUuid");
					//teamId = (String) map.get("teamUuid");
					//team = (String) map.get("team");
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				
				Event event = new Event();
				event.setServerVersion(System.currentTimeMillis());
				event.setTeam(team);
				event.setTeamId(teamId);
				event.setBaseEntityId(baseEntityId);
				event.setDateCreated(new DateTime());
				event.setEventDate(new DateTime());
				event.withProviderId("");
				event.setVersion(System.currentTimeMillis());
				event.setLocationId(locationId);
				event.setFormSubmissionId(UUID.randomUUID().toString().trim());
				event.withIsSendToOpenMRS("no").withEventType("Household Registration").withEntityType("ec_household");
				// drinking water source 
				
				String TubewellRed = member[8];
				if (!TubewellRed.isEmpty()) {
					TubewellRed = TubewellRed.trim();
				}
				String TubewellGreen = member[9];
				if (!TubewellGreen.isEmpty()) {
					TubewellGreen = TubewellGreen.trim();
					
				}
				String TubewellNotTested = member[10];
				
				if (!TubewellNotTested.isEmpty()) {
					TubewellNotTested = TubewellNotTested.trim();
				}
				String RainWater = member[11];
				if (!RainWater.isEmpty()) {
					RainWater = RainWater.trim();
				}
				
				String RiverCanal = member[12];
				if (!RiverCanal.isEmpty()) {
					RiverCanal = RiverCanal.trim();
				}
				String Tap = member[13];
				if (!Tap.isEmpty()) {
					Tap = Tap.trim();
				}
				String Pond = member[15];
				if (!Pond.isEmpty()) {
					Pond = Pond.trim();
				}
				String WaterOthers = member[16];
				if (!WaterOthers.isEmpty()) {
					WaterOthers = WaterOthers.trim();
				}
				
				String dws = "";
				String dwsConceptId = "";
				if (!TubewellRed.equalsIgnoreCase("NULL")) {
					dws = TubewellRed;
					dwsConceptId = "";
				} else if (!TubewellGreen.equalsIgnoreCase("NULL")) {
					dws = TubewellGreen;
					dwsConceptId = "";
				} else if (!TubewellNotTested.equalsIgnoreCase("NULL")) {
					dws = TubewellNotTested;
					dwsConceptId = "";
				} else if (!RainWater.equalsIgnoreCase("NULL")) {
					dws = RainWater;
					dwsConceptId = "";
				} else if (!RiverCanal.equalsIgnoreCase("NULL")) {
					dws = RiverCanal;
					dwsConceptId = "";
				} else if (!Tap.equalsIgnoreCase("NULL")) {
					dws = Tap;
					dwsConceptId = "";
				} else if (!Pond.equalsIgnoreCase("NULL")) {
					dws = Pond;
					dwsConceptId = "";
				} else if (!WaterOthers.equalsIgnoreCase("NULL")) {
					dws = WaterOthers;
					dwsConceptId = "";
				}
				//System.err.println("dws:::::" + dws + ",dwsConceptId::::::" + dwsConceptId);
				List<Object> values = new ArrayList<Object>();
				values.add(dws);
				List<Object> humanReadableValues = new ArrayList<Object>();
				humanReadableValues.add(dws);
				String fieldDataType = "text";
				event.addObs(new Obs("concept", fieldDataType, "3a46b207-dc8b-4e5b-8b1f-162fca3905ca", "", values, "",
				        "water_source"));
				
				String Sanitary = member[17];
				if (!Sanitary.isEmpty()) {
					Sanitary = Sanitary.trim();
				}
				String Construction = member[18];
				if (!Construction.isEmpty()) {
					Construction = Construction.trim();
				}
				String UnderConstruction = member[19];
				if (!UnderConstruction.isEmpty()) {
					UnderConstruction = UnderConstruction.trim();
				}
				String OpenArea = member[20];
				if (!OpenArea.isEmpty()) {
					OpenArea = OpenArea.trim();
				}
				String Bush = member[21];
				if (!Bush.isEmpty()) {
					Bush = Bush.trim();
				}
				String LatrineOthers = member[22];
				if (!LatrineOthers.isEmpty()) {
					LatrineOthers = LatrineOthers.trim();
				}
				String latrine_value = "";
				String latrine_valueConceptId = "";
				if (!Sanitary.equalsIgnoreCase("NULL")) {
					latrine_value = Sanitary;
					latrine_valueConceptId = "";
				} else if (!Construction.equalsIgnoreCase("NULL")) {
					latrine_value = Construction;
					latrine_valueConceptId = "";
				} else if (!UnderConstruction.equalsIgnoreCase("NULL")) {
					latrine_value = UnderConstruction;
					latrine_valueConceptId = "";
				} else if (!OpenArea.equalsIgnoreCase("NULL")) {
					latrine_value = OpenArea;
					latrine_valueConceptId = "";
				} else if (!Bush.equalsIgnoreCase("NULL")) {
					latrine_value = Bush;
					latrine_valueConceptId = "";
				} else if (!LatrineOthers.equalsIgnoreCase("NULL")) {
					latrine_value = LatrineOthers;
					latrine_valueConceptId = "";
				}
				
				List<Object> latrine_values = new ArrayList<Object>();
				latrine_values.add(latrine_value);
				List<Object> latrine_humanReadableValues = new ArrayList<Object>();
				latrine_humanReadableValues.add(latrine_value);
				String latrine_fieldDataType = "text";
				event.addObs(new Obs("concept", latrine_fieldDataType, "bd437fcc-f42f-40a6-8baf-b3d3af725ad4", "",
				        latrine_values, "", "latrine_structure"));
				
				String LowerMiddleClass = member[23];
				if (!LowerMiddleClass.isEmpty()) {
					LowerMiddleClass = LowerMiddleClass.trim();
				}
				String UpperMiddleClass = member[24];
				if (!UpperMiddleClass.isEmpty()) {
					UpperMiddleClass = UpperMiddleClass.trim();
				}
				String MiddleClass = member[25];
				if (!MiddleClass.isEmpty()) {
					MiddleClass = MiddleClass.trim();
				}
				String Solvent = member[26];
				if (!Solvent.isEmpty()) {
					Solvent = Solvent.trim();
				}
				String Rich = member[27];
				if (!Rich.isEmpty()) {
					Rich = Rich.trim();
				}
				String financial_value = "";
				String financial_valueConceptId = "";
				
				if (!LowerMiddleClass.equalsIgnoreCase("NULL")) {
					financial_value = LowerMiddleClass;
					financial_valueConceptId = "";
				} else if (!UpperMiddleClass.equalsIgnoreCase("NULL")) {
					financial_value = UpperMiddleClass;
					financial_valueConceptId = "";
				} else if (!MiddleClass.equalsIgnoreCase("NULL")) {
					financial_value = MiddleClass;
					financial_valueConceptId = "";
				} else if (!Solvent.equalsIgnoreCase("NULL")) {
					financial_value = Solvent;
					financial_valueConceptId = "";
				} else if (!Rich.equalsIgnoreCase("NULL")) {
					financial_value = Rich;
					financial_valueConceptId = "";
				}
				
				List<Object> financial_values = new ArrayList<Object>();
				financial_values.add(financial_value);
				List<Object> financial_humanReadableValues = new ArrayList<Object>();
				financial_humanReadableValues.add(financial_value);
				String financial_fieldDataType = "text";
				List<String> eventAddress = new ArrayList<String>();
				eventAddress.add("BANGLADESH");
				eventAddress.add(stateProvince);
				eventAddress.add(countyDistrict);
				eventAddress.add(cityVillage);
				eventAddress.add(address1);
				eventAddress.add(address2);
				JSONArray addressFieldValue = new JSONArray(eventAddress);
				
				event.addObs(new Obs("formsubmissionField", "text", "HIE_FACILITIES", "", addressFieldValue.toString(), "",
				        "HIE_FACILITIES"));
				
				event.addObs(new Obs("concept", financial_fieldDataType, "95066bce-55eb-405e-9664-9be70e5c17b2", "",
				        financial_values, "", "financial_status"));
				
				//System.err.println("Event:::::::::::::::" + event.toString());				
				eventService.addorUpdateEvent(event);
				logger.info("\n\n\n Client : "+ client.toString()+" \n\n\n");
				clientService.addOrUpdate(client);
				
			}
			
		}
		catch (Exception e) {
			logger.info("Some problem occured, please contact with admin..");
			msg = "failed to process file because : " + e.getMessage();
			e.printStackTrace();
		}
		return msg;
	}
	
	Event addObervation(String fieldType, String fieldDataType, String fieldCode, List<Object> values,
	                    String formSubmissionField, Event event) {
		
		return event.addObs(new Obs(fieldType, fieldDataType, fieldCode, "" //TODO handle parent, values,
		        ""comments, formSubmissionFieldformSubmissionField));
	}
	
	public String generateID() throws JSONException {
		String prefix = "ZEIR_ID Generator";
		JSONObject data = new JSONObject();
		data.put("identifierSourceName", prefix);
		
		return post(openMRSURL + GEN_ID_URL, "", data.toString(), "sohel", "Sohel@123").body();
	}
	
	public static HttpResponse post(String url, String payload, String data, String username, String password) {
		new TurnOffCertificateValidation().ForHTTPSConnections();
		String output = null;
		if (url.endsWith("/")) {
			url = url.substring(0, url.lastIndexOf("/"));
		}
		url = (url + (StringUtils.isEmptyOrWhitespaceOnly(payload) ? "" : ("?" + payload))).replaceAll(" ", "%20");
		try {
			URL urlo = new URL(url);
			HttpURLConnection con = (HttpURLConnection) urlo.openConnection();
			con.setRequestProperty("Content-Type", "application/json");
			String charset = "UTF-8";
			con.setRequestProperty("Accept-Charset", charset);
			String encoded = new String(Base64.encodeBase64((username + ":" + password).getBytes()));
			con.setRequestProperty("Authorization", "Basic " + encoded);
			con.setRequestMethod(HttpMethod.POST.name());
			con.setFixedLengthStreamingMode(data.toString().getBytes().length);
			con.setDoOutput(true);
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(con.getOutputStream(), charset), true); // true = autoFlush, important!
			writer.print(data.toString());
			if (writer != null)
				writer.close();
			int statusCode = con.getResponseCode();
			if (statusCode != HttpURLConnection.HTTP_OK) {
				// throw some exception
			}
			BufferedReader br = new BufferedReader(new InputStreamReader((con.getInputStream())));
			StringBuilder sb = new StringBuilder();
			
			while ((output = br.readLine()) != null) {
				sb.append(output);
			}
			//System.out.println(sb.toString());	
			return new HttpResponse(con.getResponseCode() == HttpStatus.SC_OK, sb.toString());
		}
		catch (FileNotFoundException e) {
			return new HttpResponse(true, "");
		}
		catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(e);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
*/
