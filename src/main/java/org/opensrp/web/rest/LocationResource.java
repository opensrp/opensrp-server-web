package org.opensrp.web.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.opensrp.common.AllConstants.BaseEntity;
import org.opensrp.domain.LocationProperty;
import org.opensrp.domain.PhysicalLocation;
import org.opensrp.domain.StructureDetails;
import org.opensrp.service.PhysicalLocationService;
import org.opensrp.util.PropertiesConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.opensrp.web.config.SwaggerDocStrings.*;

@Controller
@RequestMapping(value = "/rest/location")
@Api(value = LOCATION_RESOURCE, produces = LOCATION_RESOURCE)
public class LocationResource {

	private static Logger logger = LoggerFactory.getLogger(LocationResource.class.toString());

	public static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HHmm")
			.registerTypeAdapter(LocationProperty.class, new PropertiesConverter()).create();

	public static final String IS_JURISDICTION = "is_jurisdiction";

	public static final String PARENT_ID = "parent_id";

	private static final String FALSE = "false";

	public static final String LOCATION_NAMES = "location_names";

	public static final String LATITUDE = "latitude";

	public static final String LONGITUDE = "longitude";

	public static final String RADIUS = "radius";

	public static final String RETURN_GEOMETRY = "return_geometry";

	public static final String PROPERTIES_FILTER = "properties_filter";

	private PhysicalLocationService locationService;

	@Autowired
	public void setLocationService(PhysicalLocationService locationService) {
		this.locationService = locationService;
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ApiOperation(value = GET_LOCATION_TREE_BY_ID_ENDPOINT, notes = GET_LOCATION_TREE_BY_ID_ENDPOINT_NOTES)
	public ResponseEntity<String> getByUniqueId(@PathVariable("id") String id,
			@RequestParam(value = IS_JURISDICTION, defaultValue = FALSE, required = false) boolean isJurisdiction) {
		try {
			return new ResponseEntity<>(
					gson.toJson(isJurisdiction ? locationService.getLocation(id) : locationService.getStructure(id)),
					RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/sync", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> getLocations(@RequestParam(BaseEntity.SERVER_VERSIOIN) String serverVersion,
			@RequestParam(value = IS_JURISDICTION, defaultValue = FALSE, required = false) boolean isJurisdiction,
			@RequestParam(value = LOCATION_NAMES, required = false) String locationNames,
			@RequestParam(value = PARENT_ID, required = false) String parentIds) {
		long currentServerVersion = 0;
		try {
			currentServerVersion = Long.parseLong(serverVersion);
		} catch (NumberFormatException e) {
			logger.error("server version not a number");
		}

		try {
			if (isJurisdiction) {
				if (StringUtils.isBlank(locationNames)) {
					return new ResponseEntity<>(
							gson.toJson(locationService.findLocationsByServerVersion(currentServerVersion)),
							RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
				}
				return new ResponseEntity<>(
						gson.toJson(locationService.findLocationsByNames(locationNames, currentServerVersion)),
						RestUtils.getJSONUTF8Headers(), HttpStatus.OK);

			} else {
				if (StringUtils.isBlank(parentIds)) {
					return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
				}
				return new ResponseEntity<>(gson.toJson(
						locationService.findStructuresByParentAndServerVersion(parentIds, currentServerVersion)),
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

	@RequestMapping(value = "/findWithCordinates", method = RequestMethod.GET, produces = {
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> getStructuresWithinCordinates(@RequestParam(value = LATITUDE) double latitude,
			@RequestParam(value = LONGITUDE) double longitude, @RequestParam(value = RADIUS) double radius) {

		try {
			Collection<StructureDetails> structures = locationService.findStructuresWithinRadius(latitude, longitude,
					radius);
			return new ResponseEntity<>(gson.toJson(structures), RestUtils.getJSONUTF8Headers(), HttpStatus.OK);

		} catch (Exception e) {
			logger.error(e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	/**
	 * This methods provides an API endpoint that searches for jurisdictions and structures with the properties including parentId. 
	 * It returns the Geometry optionally if @param returnGeometry is set to true. 
	 * @param isJurisdiction boolean which when true the search is done on jurisdictions and when false search is on structures
	 * @param returnGeometry boolean which controls if geometry is returned
	 * @param propertiesFilters list of params with each param having name and value e.g name:House1
	 * @return the structures or jurisdictions matching the params 
	 */
	@RequestMapping(value = "/findByProperties", method = RequestMethod.GET, produces = {
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> findByLocationProperties(
			@RequestParam(value = IS_JURISDICTION, defaultValue = FALSE, required = false) boolean isJurisdiction,
			@RequestParam(value = RETURN_GEOMETRY, defaultValue = FALSE, required = false) boolean returnGeometry,
			@RequestParam(value = PROPERTIES_FILTER, required = false) List<String> propertiesFilters) {

		try {
			String parentId = null;
			Map<String, String> filters = null;
			if (propertiesFilters != null) {
				filters = new HashMap<>();
				for (String filter : propertiesFilters) {
					String[] filterArray = filter.split(":");
					if (filterArray.length == 2 && (PARENT_ID.equalsIgnoreCase(filterArray[0])
							|| "parentId".equalsIgnoreCase(filterArray[0]))) {
						parentId = filterArray[1];

					} else if (filterArray.length == 2) {
						filters.put(filterArray[0], filterArray[1]);
					}
				}
			}
			if (isJurisdiction) {
				return new ResponseEntity<>(
						gson.toJson(locationService.findLocationsByProperties(returnGeometry, parentId, filters)),
						HttpStatus.OK);
			} else {
				return new ResponseEntity<>(
						gson.toJson(locationService.findStructuresByProperties(returnGeometry, parentId, filters)),
						HttpStatus.OK);
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

}
