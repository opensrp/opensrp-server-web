package org.opensrp.web.rest.v2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import org.apache.http.util.TextUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opensrp.common.AllConstants;
import org.opensrp.common.AllConstants.BaseEntity;
import org.opensrp.domain.setting.Setting;
import org.opensrp.domain.setting.SettingConfiguration;
import org.opensrp.repository.postgres.handler.BaseTypeHandler;
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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping (value = "/rest/v2/settings")
public class SettingResource {
	
	private static final Logger logger = LoggerFactory.getLogger(SettingResource.class.toString());
	public static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
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
	public ResponseEntity<String> getAllSettings(HttpServletRequest request) throws JsonProcessingException {
		try {
			JSONObject response = new JSONObject();
			String serverVersion = RestUtils.getStringFilter(BaseEntity.SERVER_VERSIOIN, request);
			String providerId = RestUtils.getStringFilter(AllConstants.Event.PROVIDER_ID, request);
			String locationId = RestUtils.getStringFilter(AllConstants.Event.LOCATION_ID, request);
			String team = RestUtils.getStringFilter(AllConstants.Event.TEAM, request);
			String teamId = RestUtils.getStringFilter(AllConstants.Event.TEAM_ID, request);
			
			if ((TextUtils.isBlank(team) && TextUtils.isBlank(providerId) && TextUtils.isBlank(locationId)
					&& TextUtils.isBlank(teamId) && TextUtils.isBlank(team)) || TextUtils.isBlank(serverVersion)) {
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
			logger.error(MessageFormat.format("Fetching settings failed with the following error {0}.- ", e));
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
	
	@RequestMapping (method = {RequestMethod.POST, RequestMethod.PUT}, consumes = {MediaType.APPLICATION_JSON_VALUE,
			MediaType.TEXT_PLAIN_VALUE})
	public ResponseEntity<String> createOrUpdate(@RequestBody String entity) {
		try {
			Setting setting = gson.fromJson(entity, Setting.class);
			String identifier = settingService.saveSetting(convertSettingToSettingConfiguration(setting));
			return new ResponseEntity<>(identifier, RestUtils.getJSONUTF8Headers(), HttpStatus.CREATED);
		} catch (JsonSyntaxException e) {
			logger.error("The request doesnt contain a valid settings representation" + entity);
			return new ResponseEntity<>("", RestUtils.getJSONUTF8Headers(),HttpStatus.BAD_REQUEST);
		}
		
	}
	
	private String convertSettingToSettingConfiguration(Setting setting) {
		SettingConfiguration settingConfiguration = new SettingConfiguration();
		List<Setting> settingList = new ArrayList<>();
		settingList.add(setting);
		settingConfiguration.setSettings(settingList);
		settingConfiguration.setIdentifier(setting.getIdentifier());
		settingConfiguration.setType(setting.getType());
		
		return settingConfiguration.toString();
	}
}
