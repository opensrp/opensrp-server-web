package org.opensrp.web.rest.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opensrp.domain.User;
import org.opensrp.repository.couch.AllUsers;
import org.springframework.beans.factory.annotation.Autowired;

@Ignore("Excluding couchdb specific test cases. These should pick repo from db that is being used")
public class ProviderResourceTest extends BaseResourceTest {

	private final static String BASE_URL = "/rest/provider/";

	private final static String AUTHENTICATE_URL = "authenticate/";

	public static final String SALT = "salt";

	public static final String PASSWORD = "password";

	public static final String USER_NAME = "userName";

	public static final String BASE_ENTITY_ID = "1";

	@Autowired
	AllUsers allUsers;

	@Before
	public void setUp() {
		User user = new User(BASE_ENTITY_ID, USER_NAME, PASSWORD, SALT);
		allUsers.add(user);
	}

	@After
	public void cleanUp() {
		allUsers.removeAll();
	}

	@Test
	public void shouldReturnErrorMessageWithOutTeamIdAndUserName() throws Exception {
		String query = "?p=" + PASSWORD;
		String urlWithQuery = BASE_URL + AUTHENTICATE_URL + query;

		JsonNode actualObj = getCallAsJsonNode(urlWithQuery, "", status().isOk());
		Map<String, String> response = mapper.treeToValue(actualObj, Map.class);

		assertTrue(response.containsKey("ERROR"));
		assertEquals("Username and Password not provided.", response.get("ERROR"));
	}

	@Test
	public void shouldReturnErrorMessageWithOutTeamIdAndPassword() throws Exception {
		String query = "?u=" + PASSWORD;
		String urlWithQuery = BASE_URL + AUTHENTICATE_URL + query;

		JsonNode actualObj = getCallAsJsonNode(urlWithQuery, "", status().isOk());
		Map<String, String> response = mapper.treeToValue(actualObj, Map.class);

		assertTrue(response.containsKey("ERROR"));
		assertEquals("Username and Password not provided.", response.get("ERROR"));
	}

	@Test
	public void shouldReturnEmptyMessageWithOutOpenmrsAuthentication() throws Exception {
		String query = "?u=" + USER_NAME + "&p=" + PASSWORD;
		String urlWithQuery = BASE_URL + AUTHENTICATE_URL + query;

		JsonNode actualObj = getCallAsJsonNode(urlWithQuery, "", status().isOk());
		Map<String, String> response = mapper.treeToValue(actualObj, Map.class);

		assertTrue(response.containsKey("ERROR"));
		assertEquals("Authentication failed with given credentials", response.get("ERROR"));
	}

	//TODO: Write rest of the tests  after openmrs intigration.
}
