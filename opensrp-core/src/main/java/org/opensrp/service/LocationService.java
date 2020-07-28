package org.opensrp.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opensrp.api.domain.Location;
import org.opensrp.domain.postgres.CustomQuery;
import org.opensrp.repository.couch.AllLocations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LocationService {

	private final AllLocations allLocations;

	@Autowired
	public LocationService(AllLocations allLocations) {
		this.allLocations = allLocations;
	}

	public List<Location> getAllLocations() {
		ArrayList<Location> apiLocations = new ArrayList<>();

		List<org.opensrp.domain.Location> locations = allLocations.findAllLocations();

		for (org.opensrp.domain.Location location : locations) {
			org.opensrp.domain.Location parentLocation = location.getParentLocation();

			Location apiParentLocation = new Location().withLocationId(parentLocation.getLocationId())
					.withName(parentLocation.getName()).withTags(parentLocation.getTags())
					//.withAddress(parentLocation.getAddress())
					.withAttributes(parentLocation.getAttributes()).withIdentifiers(parentLocation.getIdentifiers());

			Location apiLocation = new Location().withLocationId(location.getLocationId()).withName(location.getName())
					.withParentLocation(apiParentLocation).withTags(location.getTags())
					//  .withAddress(location.getAddress())
					.withIdentifiers(location.getIdentifiers()).withAttributes(location.getAttributes());
			
			
				/*
					      apiLocation.withCreator(location.getCreator());
					      apiLocation.withEditor(location.getEditor());
						  apiLocation.withVoider(location.getVoider());
						  apiLocation.withDateCreated(location.getDateCreated());
						  apiLocation.withDateEdited(location.getDateEdited());
						  apiLocation.withDateVoided(location.getDateVoided());
						  apiLocation.withVoided(location.getVoided());
						  apiLocation.withVoidReason(location.getRevision());*/

			apiLocations.add(apiLocation);

		}

		return apiLocations;

	}

	public void addLocation(Location location) {
		org.opensrp.domain.Location domainLocation = new org.opensrp.domain.Location()
				.withLocationId(location.getLocationId()).withName(location.getName()).withTags(location.getTags())
				//.withAddress(location.getAddress())
				.withIdentifiers(location.getIdentifiers()).withAttributes(location.getAttributes());
		allLocations.add(domainLocation);

	}

	// FIXME: 8/28/17 api.Location to domain.Location doens't set id field.
	public void updateLocation(Location location) {
		org.opensrp.domain.Location domainLocation = new org.opensrp.domain.Location()
				.withLocationId(location.getLocationId()).withName(location.getName()).withTags(location.getTags())
				//.withAddress(location.getAddress())
				.withIdentifiers(location.getIdentifiers()).withAttributes(location.getAttributes());
		allLocations.update(domainLocation);

	}

	public JSONArray convertLocationTreeToJSON(List<CustomQuery> treeDTOS, Boolean enable,String fullName) throws JSONException {
		JSONArray locationTree = new JSONArray();

		Map<String, Boolean> mp = new HashMap<>();
		JSONObject object = new JSONObject();
		JSONArray locations = new JSONArray();
		JSONObject fullLocation = new JSONObject();

		int counter = 0, limit = 0;
		String username = "";

		for (CustomQuery treeDTO: treeDTOS) {
			counter++;
			limit++;
			if (mp.get(treeDTO.getUsername()) == null || !mp.get(treeDTO.getUsername())) {
				if (counter > 1) {
					fullLocation = setEmptyValues(fullLocation);
					locations.put(fullLocation);
					object.put("username", username.trim());
					object.put("locations", locations);
					object.put("full_name", fullName);
					object.put("simprints_enable", enable);
					locationTree.put(object);
					locations = new JSONArray();
					object = new JSONObject();
					fullLocation = new JSONObject();
					counter = 1;
				}
				mp.put(treeDTO.getUsername(), true);
			}

			username = treeDTO.getFirstName();

			if (treeDTO.getLocationTagName().equalsIgnoreCase("country")) {
				if (counter > 1) {
					fullLocation = setEmptyValues(fullLocation);
					locations.put(fullLocation);
					fullLocation = new JSONObject();
				}
			}

			String[] names = treeDTO.getName().split(":");
			String locationName = names[0];

			JSONObject location = new JSONObject();
			location.put("code", treeDTO.getCode().trim());
			location.put("id", treeDTO.getId());
			location.put("name", locationName.trim());
			String name = treeDTO.getLocationTagName().toLowerCase().replaceAll(" ", "_");
			fullLocation.put(name, location);

			if (limit == treeDTOS.size()) {
				fullLocation = setEmptyValues(fullLocation);
				locations.put(fullLocation);
				object.put("username", username.trim());
				object.put("locations", locations);
				object.put("full_name", fullName);
				object.put("simprints_enable", enable);
				locationTree.put(object);
				object = new JSONObject();
				locations = new JSONArray();
			}
		}
		return locationTree;
	}

	private JSONObject getLocationProperty() throws JSONException {
		JSONObject property = new JSONObject();
		property.put("name", "");
		property.put("id", 0);
		property.put("code", "00");
		return property;
	}

	private JSONObject setEmptyValues(JSONObject fullLocation) throws JSONException {
		if (!fullLocation.has("country")) {
			fullLocation.put("country", getLocationProperty());
		}
		if (!fullLocation.has("division")) {
			fullLocation.put("division", getLocationProperty());
		}
		if (!fullLocation.has("district")) {
			fullLocation.put("district", getLocationProperty());
		}
		if (!fullLocation.has("city_corporation_upazila")) {
			fullLocation.put("city_corporation_upazila", getLocationProperty());
		}
		if (!fullLocation.has("pourasabha")) {
			fullLocation.put("pourasabha", getLocationProperty());
		}
		if (!fullLocation.has("union_ward")) {
			fullLocation.put("union_ward", getLocationProperty());
		}
		if (!fullLocation.has("village")) {
			fullLocation.put("village", getLocationProperty());
		}
		return fullLocation;
	}
}
