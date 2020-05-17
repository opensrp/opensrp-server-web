package org.opensrp.web.rest.v2;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import org.apache.http.util.TextUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opensrp.common.AllConstants.BaseEntity;
import org.opensrp.domain.setting.Setting;
import org.opensrp.domain.setting.SettingConfiguration;
import org.opensrp.repository.postgres.handler.SettingTypeHandler;
import org.opensrp.search.SettingSearchBean;
import org.opensrp.service.SettingService;
import org.opensrp.util.DateTimeTypeConverter;
import org.opensrp.web.rest.RestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import static java.text.MessageFormat.format;
import static org.opensrp.common.AllConstants.Event.LOCATION_ID;
import static org.opensrp.common.AllConstants.Event.PROVIDER_ID;
import static org.opensrp.common.AllConstants.Event.TEAM;
import static org.opensrp.common.AllConstants.Event.TEAM_ID;
import static org.opensrp.web.rest.RestUtils.getStringFilter;

;

@Controller
@RequestMapping (value = "/rest/v2/settings")
public class SettingResource {
	
	private static final Logger logger = LoggerFactory.getLogger(SettingResource.class.toString());
	private final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
			.registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();
	private SettingService settingService;
	
	@Autowired
	public void setSettingService(SettingService settingService) {
		this.settingService = settingService;
	}
	
	
	@RequestMapping (value = "/{identifier}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<String> getByUniqueId(@PathVariable ("identifier") String identifier) {
		SettingSearchBean settingQueryBean = new SettingSearchBean();
		settingQueryBean.setIdentifier(identifier);
		List<SettingConfiguration> settingConfigurations = settingService.findSettings(settingQueryBean);
		
		return new ResponseEntity<>(gson.toJson(extractSettings(settingConfigurations)), RestUtils.getJSONUTF8Headers(),
				HttpStatus.OK);
	}
	
	
	/**
	 * Fetch v2 compatible setting ordered by serverVersion ascending
	 *
	 * @return A list of settings
	 */
	@RequestMapping (value = "/", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<String> getAll(HttpServletRequest request) {
		try {
			JSONObject response = new JSONObject();
			String serverVersion = getStringFilter(BaseEntity.SERVER_VERSIOIN, request);
			String providerId = getStringFilter(PROVIDER_ID, request);
			String locationId = getStringFilter(LOCATION_ID, request);
			String team = getStringFilter(TEAM, request);
			String teamId = getStringFilter(TEAM_ID, request);
			
			if ((TextUtils.isBlank(team) && TextUtils.isBlank(providerId) && TextUtils.isBlank(locationId)
					&& TextUtils.isBlank(teamId) && TextUtils.isBlank(teamId)) || TextUtils.isBlank(serverVersion)) {
				return new ResponseEntity<>(response.toString(), RestUtils.getJSONUTF8Headers(), HttpStatus.BAD_REQUEST);
			}
			
			Long lastSyncedServerVersion = Long.parseLong(serverVersion) + 1;
			
			SettingSearchBean settingQueryBean = new SettingSearchBean();
			settingQueryBean.setTeam(team);
			settingQueryBean.setTeamId(teamId);
			settingQueryBean.setProviderId(providerId);
			settingQueryBean.setLocationId(locationId);
			settingQueryBean.setServerVersion(lastSyncedServerVersion);
			
			List<SettingConfiguration> settingConfigurations = settingService.findSettings(settingQueryBean);
			List<Setting> settingList = extractSettings(settingConfigurations);
			SettingTypeHandler settingTypeHandler = new SettingTypeHandler();
			String settingsArrayString = settingTypeHandler.mapper.writeValueAsString(settingList);
			
			
			return new ResponseEntity<>(new JSONArray(settingsArrayString).toString(), RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
			
		} catch (Exception e) {
			logger.error(format("Fetching settings failed with the following error {0}.- ", e));
			return new ResponseEntity<>(RestUtils.getJSONUTF8Headers(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	
	private List<Setting> extractSettings(List<SettingConfiguration> settingConfigurations) {
		List<Setting> settingList = new ArrayList<>();
		if (settingConfigurations != null && settingConfigurations.size() > 0) {
			for (SettingConfiguration settingConfiguration : settingConfigurations) {
				settingList.addAll(settingConfiguration.getSettings());
			}
		}
		
		return settingList;
	}
	
	@RequestMapping(method = {RequestMethod.POST,RequestMethod.PUT}, consumes = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.TEXT_PLAIN_VALUE })
	public ResponseEntity<HttpStatus> createOrUpdate(@RequestBody String entity) {
		try {
			Setting setting = gson.fromJson(entity, Setting.class);
			settingService.saveSetting(convertSettingToSettingConfiguration(setting));
			return new ResponseEntity<>(HttpStatus.CREATED);
		}
		catch (JsonSyntaxException e) {
			logger.error("The request doesnt contain a valid settings representation" + entity);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		
	}
	
	private String convertSettingToSettingConfiguration(Setting setting) {
		SettingConfiguration settingConfiguration = new SettingConfiguration();
		List<Setting> settingList = new ArrayList<>();
		settingList.add(setting);
		settingConfiguration.setSettings(settingList);
		settingConfiguration.setIdentifier(setting.getIdentifier());
		settingConfiguration.setType(setting.getType());
		
		return  settingConfiguration.toString();
	}
	
	
	/*@RequestMapping(method = RequestMethod.GET, value = "/sync")
	public @ResponseBody ResponseEntity<String> findSettingsByVersion(HttpServletRequest request) {
		JSONObject response = new JSONObject();
		
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-Type", "application/json; charset=utf-8");
		try {
			
			String serverVersion = getStringFilter(BaseEntity.SERVER_VERSIOIN, request);
			String providerId = getStringFilter(PROVIDER_ID, request);
			String locationId = getStringFilter(LOCATION_ID, request);
			String team = getStringFilter(TEAM, request);
			String teamId = getStringFilter(TEAM_ID, request);
			
			if ((TextUtils.isBlank(team) && TextUtils.isBlank(providerId) && TextUtils.isBlank(locationId)
			        && TextUtils.isBlank(teamId) && TextUtils.isBlank(teamId)) || TextUtils.isBlank(serverVersion)) {
				return new ResponseEntity<>(response.toString(), responseHeaders, HttpStatus.BAD_REQUEST);
			}
			
			Long lastSyncedServerVersion = serverVersion != null ? Long.valueOf(serverVersion) + 1 : 0;
			
			SettingSearchBean settingQueryBean = new SettingSearchBean();
			settingQueryBean.setTeam(team);
			settingQueryBean.setTeamId(teamId);
			settingQueryBean.setProviderId(providerId);
			settingQueryBean.setLocationId(locationId);
			settingQueryBean.setServerVersion(lastSyncedServerVersion);
			
			List<SettingConfiguration> SettingConfigurations = settingService.findSettings(settingQueryBean);

			SettingTypeHandler settingTypeHandler = new SettingTypeHandler();
			String settingsArrayString = settingTypeHandler.mapper.writeValueAsString(SettingConfigurations);

			return new ResponseEntity<>(new JSONArray(settingsArrayString).toString(), responseHeaders,
					HttpStatus.OK);
			
		}
		catch (Exception e) {
			logger.error(format("Sync data processing failed with exception {0}.- ", e));
			return new ResponseEntity<>(responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}*/
	
	/*@RequestMapping (headers = {"Accept=application/json"}, method = POST, value = "/sync")
	public ResponseEntity<String> saveSetting(@RequestBody String data) {
		JSONObject response = new JSONObject();
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-Type", "application/json; charset=utf-8");
		
		JSONObject syncData = new JSONObject(data);
		
		if (!syncData.has("settingConfigurations")) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		} else {
			
			JSONArray clientSettings = syncData.getJSONArray(AllConstants.Event.SETTING_CONFIGURATIONS);
			JSONArray dbSettingsArray = new JSONArray();
			
			for (int i = 0; i < clientSettings.length(); i++) {
				
				dbSettingsArray.put(settingService.saveSetting(clientSettings.getString(i)));
				
			}
			
			response.put("validated_records", dbSettingsArray);
			
		}
		
		return new ResponseEntity<>(response.toString(), responseHeaders, HttpStatus.OK);
	}*/
}
