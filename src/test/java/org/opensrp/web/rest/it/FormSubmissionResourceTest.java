package org.opensrp.web.rest.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.opensrp.common.AllConstants.BaseEntity.BASE_ENTITY_ID;
import static org.opensrp.common.AllConstants.Event.EVENT_TYPE;
import static org.opensrp.common.AllConstants.Event.PROVIDER_ID;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opensrp.form.domain.FormInstance;
import org.opensrp.form.domain.FormSubmission;
import org.opensrp.form.repository.AllFormSubmissions;
import org.opensrp.web.rest.FormSubmissionResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.NestedServletException;

import com.fasterxml.jackson.databind.JsonNode;

public class FormSubmissionResourceTest extends BaseResourceTest {

	public static final String ANM_ID = "anmId";

	public static final String INTANCE_ID = "intanceId";

	public static final String FORM_NAME = "formName";

	public static final String ENTITY_ID = "eintityId";

	public static final int CLIENT_VERSION = 122;

	public static final String FORM_DATA_DEFINITION_VERSION = "formDataDefinitionVersion";

	public static final int SERVER_VERSION = 233;

	public static final String BASE_URL = "/rest/formSubmission/";

	@Autowired
	private AllFormSubmissions allFormSubmissions;

	@Autowired
	private FormSubmissionResource formSubmissionResource;

	@Before
	public void setUp() {
		allFormSubmissions.removeAll();
	}

	@After
	public void tearDown() {
		allFormSubmissions.removeAll();
	}

	@Test
	public void testRequiredField() {
		List<String> requiredProperties = formSubmissionResource.requiredProperties();
		assertTrue(requiredProperties.contains(PROVIDER_ID));
		assertTrue(requiredProperties.contains(EVENT_TYPE));
		assertTrue(requiredProperties.contains(BASE_ENTITY_ID));
	}

	@Test(expected = NestedServletException.class)
	public void shouldThrowExceptionWhileCreating() throws Exception {
		FormSubmission formSubmission = new FormSubmission(ANM_ID, INTANCE_ID, FORM_NAME, ENTITY_ID, CLIENT_VERSION,
				FORM_DATA_DEFINITION_VERSION, new FormInstance(), SERVER_VERSION);

		postCallWithJsonContent(BASE_URL, mapper.writeValueAsString(formSubmission), status().isOk());
	}

	@Test(expected = NestedServletException.class)
	public void shouldThrowExceptionWhileUpdating() throws Exception {
		FormSubmission formSubmission = new FormSubmission(ANM_ID, INTANCE_ID, FORM_NAME, ENTITY_ID, CLIENT_VERSION,
				FORM_DATA_DEFINITION_VERSION, new FormInstance(), SERVER_VERSION);
		addObjectToRepository(Collections.singletonList(formSubmission), allFormSubmissions);

		postCallWithJsonContent(BASE_URL + INTANCE_ID, mapper.writeValueAsString(formSubmission), status().isOk());
	}

	@Test(expected = NestedServletException.class)
	public void shouldThrowExceptionWhileFilter() throws Exception {
		FormSubmission formSubmission = new FormSubmission(ANM_ID, INTANCE_ID, FORM_NAME, ENTITY_ID, CLIENT_VERSION,
				FORM_DATA_DEFINITION_VERSION, new FormInstance(), SERVER_VERSION);
		addObjectToRepository(Collections.singletonList(formSubmission), allFormSubmissions);

		postCallWithJsonContent(BASE_URL + "?q=instanceId:" + INTANCE_ID, mapper.writeValueAsString(formSubmission),
				status().isOk());
	}

	@Test
	public void shouldFindByInstanceId() throws Exception {
		FormSubmission expectedFormSubmission = new FormSubmission(ANM_ID, INTANCE_ID, FORM_NAME, ENTITY_ID, CLIENT_VERSION,
				FORM_DATA_DEFINITION_VERSION, new FormInstance(), SERVER_VERSION);
		addObjectToRepository(Collections.singletonList(expectedFormSubmission), allFormSubmissions);

		JsonNode actualObj = getCallAsJsonNode(BASE_URL + INTANCE_ID, "", status().isOk());
		FormSubmission actualFormSubmission = mapper.treeToValue(actualObj, FormSubmission.class);

		assertEquals(expectedFormSubmission, actualFormSubmission);
	}

	@Test
	public void shouldSearchFormSubmission() throws Exception {
		FormSubmission expectedFormSubmission = new FormSubmission(ANM_ID, INTANCE_ID, FORM_NAME, ENTITY_ID, CLIENT_VERSION,
				FORM_DATA_DEFINITION_VERSION, new FormInstance(), SERVER_VERSION);
		addObjectToRepository(Collections.singletonList(expectedFormSubmission), allFormSubmissions);

		String searchQuery = "search?formName=" + FORM_NAME + "&version=" + (SERVER_VERSION - 1);
		JsonNode actualObj = getCallAsJsonNode(BASE_URL + searchQuery, "", status().isOk());
		FormSubmission actualFormSubmission = mapper.treeToValue(actualObj.get(0), FormSubmission.class);

		assertEquals(expectedFormSubmission, actualFormSubmission);
	}

	@Test
	public void shouldSearchFormSubmissionWithoutVersion() throws Exception {
		FormSubmission expectedFormSubmission = new FormSubmission(ANM_ID, INTANCE_ID, FORM_NAME, ENTITY_ID, CLIENT_VERSION,
				FORM_DATA_DEFINITION_VERSION, new FormInstance(), SERVER_VERSION);
		addObjectToRepository(Collections.singletonList(expectedFormSubmission), allFormSubmissions);

		String searchQuery = "search?formName=" + FORM_NAME;
		JsonNode actualObj = getCallAsJsonNode(BASE_URL + searchQuery, "", status().isOk());
		FormSubmission actualFormSubmission = mapper.treeToValue(actualObj.get(0), FormSubmission.class);

		assertEquals(expectedFormSubmission, actualFormSubmission);
	}
}
