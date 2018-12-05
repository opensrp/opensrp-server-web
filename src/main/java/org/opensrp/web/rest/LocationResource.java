package org.opensrp.web.rest;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.opensrp.common.AllConstants.BaseEntity;
import org.opensrp.domain.PhysicalLocation;
import org.opensrp.service.PhysicalLocationService;
import org.opensrp.util.TaskDateTimeTypeConverter;
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
import org.springframework.web.bind.annotation.RequestParam;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

@Controller
@RequestMapping(value = "/rest/location")
public class LocationResource {

	private static Logger logger = LoggerFactory.getLogger(LocationResource.class.toString());

	public static Gson gson = new GsonBuilder().registerTypeAdapter(DateTime.class, new TaskDateTimeTypeConverter())
			.create();

	public static final String IS_JURISDICTION = "is_jurisdiction";

	public static final String PARENT_ID = "parent_id";

	private static final String FALSE = "false";

	private PhysicalLocationService locationService;

	public static final String LOCATION_NAMES = "location_names";


	@Autowired
	public void setLocationService(PhysicalLocationService locationService) {
		this.locationService = locationService;
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> getByUniqueId(@PathVariable("id") String id,
			@RequestParam(value = IS_JURISDICTION, defaultValue = FALSE, required = false) boolean isJurisdiction) {
		try {
			return new ResponseEntity<>(
					gson.toJson(isJurisdiction ? locationService.getLocation(id) : locationService.getStructure(id)),
					HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/sync", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> getLocations(@RequestParam(BaseEntity.SERVER_VERSIOIN) String serverVersion,
			@RequestParam(value = IS_JURISDICTION, defaultValue = FALSE, required = false) boolean isJurisdiction,
			@RequestParam(value = LOCATION_NAMES, required = false) String locationNames,
			@RequestParam(value = PARENT_ID, required = false) String parentId) {
		long currentServerVersion = 0;
		try {
			currentServerVersion = Long.parseLong(serverVersion);
		} catch (NumberFormatException e) {
			logger.error("server version not a number");
		}

		try {
			if (isJurisdiction) {
				if(StringUtils.isBlank(locationNames)) {
					return new ResponseEntity<>(gson.toJson(locationService.findLocationsByServerVersion(currentServerVersion)), HttpStatus.OK);
				}
				return new ResponseEntity<>(gson.toJson(locationService.findLocationsByNames(locationNames)), HttpStatus.OK);

			} else {
				if (StringUtils.isBlank(parentId)) {
					return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
				}
				return new ResponseEntity<>(
						gson.toJson(
								locationService.findStructuresByParentAndServerVersion(parentId, currentServerVersion)),
						HttpStatus.OK);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.TEXT_PLAIN_VALUE })
	public ResponseEntity<HttpStatus> create(@RequestBody String entity,
			@RequestParam(value = IS_JURISDICTION, defaultValue = FALSE, required = false) boolean isJurisdiction) {
		try {
			PhysicalLocation location = gson.fromJson(entity, PhysicalLocation.class);
			location.setJurisdiction(isJurisdiction);
			locationService.add(location);
			return new ResponseEntity<>(HttpStatus.CREATED);
		} catch (JsonSyntaxException e) {
			logger.error("The request doesnt contain a valid location representation" + entity);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			logger.error(e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.TEXT_PLAIN_VALUE })
	public ResponseEntity<HttpStatus> update(@RequestBody String entity,
			@RequestParam(value = IS_JURISDICTION, defaultValue = FALSE, required = false) boolean isJurisdiction) {
		try {
			PhysicalLocation location = gson.fromJson(entity, PhysicalLocation.class);
			location.setJurisdiction(isJurisdiction);
			locationService.update(location);
			return new ResponseEntity<>(HttpStatus.CREATED);
		} catch (JsonSyntaxException e) {
			logger.error("The request doesnt contain a valid location representation" + entity);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			logger.error(e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/add", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.TEXT_PLAIN_VALUE })
	public ResponseEntity<String> saveBatch(@RequestBody String entity,
			@RequestParam(value = IS_JURISDICTION, defaultValue = FALSE, required = false) boolean isJurisdiction) {
		try {
			Type listType = new TypeToken<List<PhysicalLocation>>() {
			}.getType();
			List<PhysicalLocation> locations = gson.fromJson(entity, listType);
			Set<String> locationWithErrors = locationService.saveLocations(locations, isJurisdiction);
			if (locationWithErrors.isEmpty())
				return new ResponseEntity<>("All Locations  processed", HttpStatus.CREATED);
			else
				return new ResponseEntity<>("Locations with Ids not processed: " + String.join(",", locationWithErrors),
						HttpStatus.CREATED);

		} catch (JsonSyntaxException e) {
			logger.error("The request doesnt contain a valid location representation" + entity);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			logger.error(e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
