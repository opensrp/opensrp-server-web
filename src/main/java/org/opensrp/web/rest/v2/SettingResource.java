package org.opensrp.web.rest.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.opensrp.api.domain.Location;
import org.opensrp.api.util.LocationTree;
import org.opensrp.api.util.TreeNode;
import org.opensrp.common.AllConstants;
import org.opensrp.common.AllConstants.BaseEntity;
import org.opensrp.domain.setting.Setting;
import org.opensrp.domain.setting.SettingConfiguration;
import org.opensrp.search.SettingSearchBean;
import org.opensrp.service.PhysicalLocationService;
import org.opensrp.service.SettingService;
import org.opensrp.web.Constants;
import org.opensrp.web.rest.RestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartregister.utils.DateTimeTypeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

@Controller("settingResourceV2")
@RequestMapping(value = Constants.RestEndpointUrls.SETTINGS_V2_URL)
public class SettingResource {

	private static final Logger logger = LoggerFactory.getLogger(SettingResource.class.toString());

	public static final String SETTING_IDENTIFIER = "identifier";

	public static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
			.registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();

	protected ObjectMapper objectMapper;

	private SettingService settingService;

	private PhysicalLocationService physicalLocationService;

	@Autowired
	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Autowired
	public void setSettingService(SettingService settingService, PhysicalLocationService openmrsLocationService) {
		this.settingService = settingService;
		this.physicalLocationService = openmrsLocationService;
	}

	/**
	 * Gets settings by the unique is
	 *
	 * @param identifier {@link String} - settings identifier
	 * @return setting {@link Setting} - the settings object
	 */
	@GetMapping(value = "/{id}", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> getByUniqueId(@PathVariable(Constants.RestPartVariables.ID) String identifier) {
		SettingSearchBean settingQueryBean = new SettingSearchBean();
		settingQueryBean.setId(identifier);
		List<SettingConfiguration> settingConfigurations = settingService.findSettings(settingQueryBean, null);

		return new ResponseEntity<>(gson.toJson(extractSettings(settingConfigurations)), RestUtils.getJSONUTF8Headers(),
				HttpStatus.OK);
	}

	private Map<String, TreeNode<String, Location>> getChildParentLocationTree(String locationId) {
		LocationTree locationTree = physicalLocationService.buildLocationHierachyFromLocation(locationId, false);
		Map<String, TreeNode<String, Location>> treeNodeHashMap = new HashMap<>();
		if (locationTree != null) {
			treeNodeHashMap = locationTree.getLocationsHierarchy();
		}

		return treeNodeHashMap;
	}

	/**
	 * Fetch v2 compatible setting ordered by serverVersion ascending
	 *
	 * @return A list of settings
	 */
	@GetMapping(produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> getAllSettings(HttpServletRequest request) {
		String serverVersion = RestUtils.getStringFilter(BaseEntity.SERVER_VERSIOIN, request);
		String providerId = RestUtils.getStringFilter(AllConstants.Event.PROVIDER_ID, request);
		String locationId = RestUtils.getStringFilter(AllConstants.Event.LOCATION_ID, request);
		String team = RestUtils.getStringFilter(AllConstants.Event.TEAM, request);
		String teamId = RestUtils.getStringFilter(AllConstants.Event.TEAM_ID, request);
		String identifier = RestUtils.getStringFilter(SETTING_IDENTIFIER, request);
		boolean resolveSettings = RestUtils.getBooleanFilter(AllConstants.Event.RESOLVE_SETTINGS, request);
		Map<String, TreeNode<String, Location>> treeNodeHashMap = null;

		if (StringUtils.isBlank(team) && StringUtils.isBlank(providerId) && StringUtils.isBlank(locationId)
				&& StringUtils.isBlank(teamId) && StringUtils.isBlank(team) && StringUtils.isBlank(serverVersion)) {
			return new ResponseEntity<>("All parameters cannot be null for this endpoint",
					RestUtils.getJSONUTF8Headers(), HttpStatus.BAD_REQUEST);
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
		if (StringUtils.isNotBlank(identifier)) {
			settingQueryBean.setIdentifier(identifier);
		}
		if (StringUtils.isNotBlank(locationId)) {
			settingQueryBean.setResolveSettings(resolveSettings);
			treeNodeHashMap = getChildParentLocationTree(locationId);
		}

		List<SettingConfiguration> settingConfigurations = settingService.findSettings(settingQueryBean,
				treeNodeHashMap);
		List<Setting> settingList = extractSettings(settingConfigurations);

		return new ResponseEntity<>(gson.toJson(settingList), RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
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
	 * Creates a setting
	 *
	 * @param entity {@link Setting} - the settings object
	 * @return
	 */
	@PostMapping(consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE })
	public ResponseEntity<String> create(@RequestBody String entity) {
		return performCreateOrUpdate(entity, 0L);
	}

	/**
	 * Update a setting
	 *
	 * @param entity {@link Setting} - the settings object
	 * @param id     {@link Long} - the settings-metadata id
	 * @return
	 */
	@PutMapping(value = "/{id}", consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE })
	public ResponseEntity<String> update(@RequestBody String entity,
			@PathVariable(Constants.RestPartVariables.ID) Long id) {
		return performCreateOrUpdate(entity, id);
	}

	private ResponseEntity<String> performCreateOrUpdate(String entity, Long id) {
		try {
			Setting setting = objectMapper.readValue(entity, Setting.class);
			if (id > 0) {
				setting.setSettingMetadataId(String.valueOf(id));
			}
			setting.setV1Settings(false); //used to differentiate the payload from the two endpoints
			settingService.addOrUpdateSettings(setting);
			return new ResponseEntity<>("Settings created or updated successfully", RestUtils.getJSONUTF8Headers(),
					HttpStatus.CREATED);
		}
		catch (IOException e) {
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
	@DeleteMapping(value = "/{id}", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> delete(@PathVariable(Constants.RestPartVariables.ID) Long id) {
		if (id == null) {
			return new ResponseEntity<>("Settings id is required", RestUtils.getJSONUTF8Headers(),
					HttpStatus.BAD_REQUEST);
		} else {
			settingService.deleteSetting(id);
			return new ResponseEntity<>("Settings deleted successfully", RestUtils.getJSONUTF8Headers(),
					HttpStatus.NO_CONTENT);
		}
	}
}
