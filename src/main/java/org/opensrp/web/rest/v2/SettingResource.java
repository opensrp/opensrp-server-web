package org.opensrp.web.rest.v2;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opensrp.common.AllConstants;
import org.opensrp.common.AllConstants.BaseEntity;
import org.opensrp.domain.setting.Setting;
import org.opensrp.domain.setting.SettingConfiguration;
import org.opensrp.repository.postgres.handler.SettingTypeHandler;
import org.opensrp.search.SettingSearchBean;
import org.opensrp.service.SettingService;
import org.opensrp.util.DateTimeTypeConverter;
import org.opensrp.web.Constants;
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

@Controller ("settingResourceV2")
@RequestMapping (value = Constants.RestEndpointUrls.SETTINGS_V2_URL)
public class SettingResource {
	
	private static final Logger logger = LoggerFactory.getLogger(SettingResource.class.toString());
	public static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
			.registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();
	protected ObjectMapper objectMapper;
	private SettingService settingService;
	
	@Autowired
	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}
	
	@Autowired
	public void setSettingService(SettingService settingService) {
		this.settingService = settingService;
	}
	
	/**
	 * Gets settings by the unique is
	 *
	 * @param identifier {@link String} - settings identifier
	 * @return setting {@link Setting} - the settings object
	 */
	@RequestMapping (value = "/{identifier}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<String> getByUniqueId(@PathVariable (Constants.RestPartVariables.IDENTIFIER) String identifier) {
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
	public ResponseEntity<String> getAllSettings(HttpServletRequest request) {
		try {
			JSONObject response = new JSONObject();
			String serverVersion = RestUtils.getStringFilter(BaseEntity.SERVER_VERSIOIN, request);
			String providerId = RestUtils.getStringFilter(AllConstants.Event.PROVIDER_ID, request);
			String locationId = RestUtils.getStringFilter(AllConstants.Event.LOCATION_ID, request);
			String team = RestUtils.getStringFilter(AllConstants.Event.TEAM, request);
			String teamId = RestUtils.getStringFilter(AllConstants.Event.TEAM_ID, request);
			boolean resolveSettings = RestUtils.getBooleanFilter(AllConstants.Event.RESOLVE_SETTINGS, request);
			
			if (StringUtils.isBlank(team) && StringUtils.isBlank(providerId) && StringUtils.isBlank(locationId)
					&& StringUtils.isBlank(teamId) && StringUtils.isBlank(team) && StringUtils.isBlank(serverVersion)) {
				return new ResponseEntity<>(response.toString(), RestUtils.getJSONUTF8Headers(), HttpStatus.BAD_REQUEST);
			}
			
			long lastSyncedServerVersion = 0L;
			if (StringUtils.isNotBlank(serverVersion)) {
				lastSyncedServerVersion = Long.parseLong(serverVersion) + 1;
			}
			
			
			SettingSearchBean settingQueryBean = new SettingSearchBean();
			settingQueryBean.setTeam(team);
			settingQueryBean.setTeamId(teamId);
			settingQueryBean.setProviderId(providerId);
			settingQueryBean.setLocationId(locationId);
			settingQueryBean.setServerVersion(lastSyncedServerVersion);
			if (StringUtils.isNotBlank(locationId)) {
				settingQueryBean.setResolveSettings(resolveSettings);
			}
			
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
	
	/**
	 * Creates or updates a given settings
	 *
	 * @param entity {@link Setting} - the settings object
	 * @return
	 */
	@RequestMapping (method = {RequestMethod.POST, RequestMethod.PUT}, consumes = {MediaType.APPLICATION_JSON_VALUE,
			MediaType.TEXT_PLAIN_VALUE})
	public ResponseEntity<String> createOrUpdate(@RequestBody String entity) {
		try {
			Setting setting = objectMapper.readValue(entity, Setting.class);
			settingService.addOrUpdateSettings(setting);
			return new ResponseEntity<>(HttpStatus.CREATED);
		} catch (JsonSyntaxException e) {
			logger.error("The request doesnt contain a valid settings json" + entity);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		} catch (JsonMappingException e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	
	/**
	 * Deletes a settings by primary key
	 *
	 * @param id {@link Long}
	 * @return
	 */
	@RequestMapping (value = "/{id}", method = RequestMethod.DELETE, produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<String> delete(@PathVariable (Constants.RestPartVariables.ID) Long id) {
		try {
			if (id == null) {
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			} else {
				settingService.deleteSetting(id);
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
		} catch (IllegalArgumentException e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
		
	}
}
