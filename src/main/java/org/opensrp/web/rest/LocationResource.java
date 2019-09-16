package org.opensrp.web.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonProperty;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.opensrp.web.config.SwaggerDocStringHelper.GET_LOCATION_TREE_BY_ID_ENDPOINT;
import static org.opensrp.web.config.SwaggerDocStringHelper.GET_LOCATION_TREE_BY_ID_ENDPOINT_NOTES;
import static org.opensrp.web.config.SwaggerDocStringHelper.LOCATION_RESOURCE;


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

	private static final String TRUE = "true";

	public static final String LOCATION_NAMES = "location_names";

	public static final String LATITUDE = "latitude";

	public static final String LONGITUDE = "longitude";

	public static final String RADIUS = "radius";

	public static final String RETURN_GEOMETRY = "return_geometry";

	public static final String PROPERTIES_FILTER = "properties_filter";

    public static final String JURISDICTION_IDS = "jurisdiction_ids";

	public static final String JURISDICTION_ID = "jurisdiction_id";

	public static final String PAGE_SIZE = "page_size";

	public static final String DEFAULT_PAGE_SIZE = "1000";

	private PhysicalLocationService locationService;

	@Autowired
	public void setLocationService(PhysicalLocationService locationService) {
		this.locationService = locationService;
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ApiOperation(value = GET_LOCATION_TREE_BY_ID_ENDPOINT, notes = GET_LOCATION_TREE_BY_ID_ENDPOINT_NOTES)
	public ResponseEntity<String> getByUniqueId(@PathVariable("id") String id,
			@RequestParam(value = IS_JURISDICTION, defaultValue = FALSE, required = false) boolean isJurisdiction,
			@RequestParam(value = RETURN_GEOMETRY, defaultValue = TRUE, required = false) boolean returnGeometry) {
		try {
			return new ResponseEntity<>(
					gson.toJson(isJurisdiction ? locationService.getLocation(id, returnGeometry) : locationService.getStructure(id, returnGeometry)),
					RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/sync", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> getLocations(@RequestBody LocationSyncRequestWrapper locationSyncRequestWrapper) {
		long currentServerVersion = 0;
		try {
			currentServerVersion = Long.parseLong(locationSyncRequestWrapper.getServerVersion());
		} catch (NumberFormatException e) {
			logger.error("server version not a number");
		}

		Boolean isJurisdiction = locationSyncRequestWrapper.getIsJurisdiction();
		String locationNames = locationSyncRequestWrapper.getLocationNames();
		String parentIds = locationSyncRequestWrapper.getParentId();

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

    /**
     * This methods provides an API endpoint that searches for jurisdictions using a list of provided jurisdiction ids.
     * It returns the Geometry optionally if @param returnGeometry is set to true.
     * @param returnGeometry boolean which controls if geometry is returned
     * @param jurisdictionIds list of jurisdiction ids
     * @return jurisdictions whose ids match the provided params
     */
	@RequestMapping(value = "/findByJurisdictionIds", method = RequestMethod.GET, produces = {
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> findByJurisdictionIds(
			@RequestParam(value = RETURN_GEOMETRY, defaultValue = FALSE, required = false) boolean returnGeometry,
			@RequestParam(value = JURISDICTION_IDS, required = false) List<String> jurisdictionIds) {

        try {
            return new ResponseEntity<>(
                    gson.toJson(locationService.findLocationsByIds(returnGeometry, jurisdictionIds)), HttpStatus.OK);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

	/**
	 * This methods provides an API endpoint that searches for a location and it's children using the provided location id
	 * It returns the Geometry optionally if @param returnGeometry is set to true.
	 * @param returnGeometry boolean which controls if geometry is returned
	 * @param jurisdictionId location id
	 * @param pageSize number of records to be returned
	 * @return location together with it's children whose id matches the provided param
	 */
	@RequestMapping(value = "/findByIdWithChildren", method = RequestMethod.GET, produces = {
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> findByIdWithChildren(
			@RequestParam(value = RETURN_GEOMETRY, defaultValue = FALSE, required = false) boolean returnGeometry,
			@RequestParam(value = PAGE_SIZE, defaultValue = DEFAULT_PAGE_SIZE, required = false) int pageSize,
			@RequestParam(value = JURISDICTION_ID, required = false) String jurisdictionId) {

		try {
			return new ResponseEntity<>(
					gson.toJson(locationService.findLocationByIdWithChildren(returnGeometry, jurisdictionId, pageSize)), HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	static class LocationSyncRequestWrapper {
		@JsonProperty
		private Boolean is_jurisdiction;

		@JsonProperty
		private String location_names;

		@JsonProperty
		private String parent_id;

		@JsonProperty
		private String serverVersion;

		public Boolean getIsJurisdiction() {
			return is_jurisdiction;
		}

		public String getLocationNames() {
			return location_names;
		}

		public String getParentId() {
			return parent_id;
		}

		public String getServerVersion() {
			return serverVersion;
		}
	}

}
