package org.opensrp.web.rest;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.opensrp.service.LocationTagService;
import org.smartregister.domain.LocationTag;
import org.springframework.test.web.server.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class LocationTagResourceTest extends BaseResourceTest<LocationTag> {

	private final static String BASE_URL = "/rest/location-tag/";

	private final static String DELETE_ENDPOINT = "delete/";

	private LocationTagService locationTagService;

	private ArgumentCaptor<LocationTag> argumentCaptor = ArgumentCaptor.forClass(LocationTag.class);

	@Captor
	private ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);

	private final String locationTagJson = "{\"active\":true,\"name\":\"Country\",\"description\":\"descriptions\",\"id\":0}";

	@Before
	public void setUp() {
		locationTagService = mock(LocationTagService.class);
		LocationTagResource locationTagResource = webApplicationContext.getBean(LocationTagResource.class);
		locationTagResource.setLocationTagService(locationTagService);
		locationTagResource.setObjectMapper(mapper);
	}

	@Test
	public void testGetLocationTagById() throws Exception {
		LocationTag expectedLocationTag = initTestLocationTag1();
		doReturn(expectedLocationTag).when(locationTagService).getLocationTagById(ArgumentMatchers.anyString());

		String actualLocationTagsString = getResponseAsString(BASE_URL + "/1", null, MockMvcResultMatchers.status().isOk());
		LocationTag actualLocationTag = new Gson().fromJson(actualLocationTagsString, new TypeToken<LocationTag>() {

		}.getType());

		assertEquals("Country", actualLocationTag.getName());
	}

	@Test
	public void testShouldReturnAllLocationTags() throws Exception {
		List<LocationTag> expectedLocationTags = new ArrayList<>();

		LocationTag expectedLocationTag = initTestLocationTag1();
		expectedLocationTags.add(expectedLocationTag);

		expectedLocationTag = initTestLocationTag2();
		expectedLocationTags.add(expectedLocationTag);

		doReturn(expectedLocationTags).when(locationTagService).getAllLocationTags();

		String actualLocationTagsString = getResponseAsString(BASE_URL, null, MockMvcResultMatchers.status().isOk());
		List<LocationTag> actualLocationTags = new Gson().fromJson(actualLocationTagsString,
				new TypeToken<List<LocationTag>>() {

				}.getType());

		assertListsAreSameIgnoringOrder(actualLocationTags, expectedLocationTags);
	}

	@Test
	public void testShouldCreateNewLocationTagResource() throws Exception {
		doReturn(new LocationTag()).when(locationTagService).addOrUpdateLocationTag((LocationTag) any());

		LocationTag expectedLocationTag = initTestLocationTag1();

		postRequestWithJsonContent(BASE_URL, locationTagJson, MockMvcResultMatchers.status().isCreated());

		verify(locationTagService).addOrUpdateLocationTag(argumentCaptor.capture());
		assertEquals(argumentCaptor.getValue().getName(), expectedLocationTag.getName());

	}

	@Test
	public void testShouldUpdateExistingLocationTagResource() throws Exception {
		LocationTag expectedLocationTag = initTestLocationTag1();

		String locationTagJson = new Gson().toJson(expectedLocationTag, new TypeToken<LocationTag>() {

		}.getType());
		putRequestWithJsonContent(BASE_URL, locationTagJson, MockMvcResultMatchers.status().isCreated());

		verify(locationTagService).addOrUpdateLocationTag(argumentCaptor.capture());
		assertEquals(argumentCaptor.getValue().getName(), expectedLocationTag.getName());
	}

	@Test
	public void testShouldDeleteExistingLocationTagResource() throws Exception {

		deleteRequestWithParams(BASE_URL + DELETE_ENDPOINT + 1, null, MockMvcResultMatchers.status().isNoContent());

		verify(locationTagService).deleteLocationTag(longArgumentCaptor.capture());
		assertEquals(longArgumentCaptor.getValue().longValue(), 1);
	}

	@Override
	protected void assertListsAreSameIgnoringOrder(List<LocationTag> expectedList, List<LocationTag> actualList) {
		if (expectedList == null || actualList == null) {
			throw new AssertionError("One of the lists is null");
		}

		assertEquals(expectedList.size(), actualList.size());

		Set<String> expectedNames = new HashSet<>();
		for (LocationTag locationtag : expectedList) {
			expectedNames.add(locationtag.getName());
		}

		for (LocationTag locationTag : actualList) {
			assertTrue(expectedNames.contains(locationTag.getName()));
		}
	}

	private LocationTag initTestLocationTag1() {
		LocationTag locationTag = new LocationTag();
		locationTag.setName("Country");
		locationTag.setDescription("first label tag name");
		locationTag.setActive(true);
		return locationTag;
	}

	private LocationTag initTestLocationTag2() {
		LocationTag locationTag = new LocationTag();
		locationTag.setName("Ward");
		locationTag.setDescription("first label tag name");
		locationTag.setActive(true);
		return locationTag;
	}

}
