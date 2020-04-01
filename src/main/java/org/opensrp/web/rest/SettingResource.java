package org.opensrp.web.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.util.TextUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opensrp.common.AllConstants;
import org.opensrp.common.AllConstants.BaseEntity;
import org.opensrp.domain.setting.SettingConfiguration;
import org.opensrp.repository.postgres.handler.SettingTypeHandler;
import org.opensrp.search.SettingSearchBean;
import org.opensrp.service.SettingService;
import org.opensrp.util.DateTimeTypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static java.text.MessageFormat.format;
import static org.opensrp.common.AllConstants.Event.LOCATION_ID;
import static org.opensrp.common.AllConstants.Event.PROVIDER_ID;
import static org.opensrp.common.AllConstants.Event.TEAM;
import static org.opensrp.web.rest.RestUtils.getStringFilter;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping(value = "/rest/settings")
public class SettingResource {
	
	private SettingService settingService;
	
	private static Logger logger = LoggerFactory.getLogger(SettingResource.class.toString());
	
	private String TEAM_ID = "teamId";
	
	private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
	        .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();
	
	@Autowired
	public void setSettingService(SettingService settingService) {
		this.settingService = settingService;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/sync")
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
		
	}
	
	@RequestMapping(headers = { "Accept=application/json" }, method = POST, value = "/sync")
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

				dbSettingsArray.put(settingService.saveSetting(clientSettings.getString(i).toString()));

			}

			response.put("validated_records", dbSettingsArray);

		}

		return new ResponseEntity<>(response.toString(), responseHeaders, HttpStatus.OK);
	}
}
