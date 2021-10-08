package org.opensrp.web.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opensrp.domain.Template;
import org.opensrp.service.TemplateService;
import org.springframework.test.web.server.result.MockMvcResultMatchers;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TemplateResourceTest extends BaseResourceTest<Template> {

	private TemplateService templateService;

	private final static String BASE_URL = "/rest/templates";

	@Before
	public void setUp() {
		templateService = mock(TemplateService.class);
		TemplateResource templateResource = webApplicationContext.getBean(TemplateResource.class);
		templateResource.setTemplateService(templateService);
		templateResource.setObjectMapper(mapper);
	}

	private Template template123() {
		Template template = new Template();
		template.setTemplate("{\"id\":\"123\"}");
		template.setTemplateId(123);
		template.setType("plan");
		return template;
	}

	private Template template256() {
		Template template = new Template();
		template.setTemplate("{\"id\":\"256\"}");
		template.setTemplateId(256);
		template.setType("plan");
		return template;
	}

	@Override
	protected void assertListsAreSameIgnoringOrder(List<Template> expectedList, List<Template> actualList) {
		assertNotNull(expectedList);
		assertNotNull(actualList);
		assertEquals(expectedList.size(), actualList.size());
		Set<Integer> expectedIds = new HashSet<>();
		for (Template template : expectedList) {
			expectedIds.add(template.getTemplateId());
		}

		for (Template template : actualList) {
			assertTrue(expectedIds.contains(template.getTemplateId()));
		}
	}

	@Test
	public void testGetAll() throws Exception {
		List<Template> templates = Arrays.asList(template123(), template256());
		when(templateService.getAll()).thenReturn(templates);
		String responseData = getResponseAsString(BASE_URL, null,  MockMvcResultMatchers.status().isOk());
		List<Template> fetchedTemplates = mapper.readValue(responseData, new TypeReference<List<Template>>() {});
		assertListsAreSameIgnoringOrder(templates, fetchedTemplates);
	}

	@Test
	public void testGet() throws Exception {
		Template template = template256();
		when(templateService.getTemplateByTemplateId(anyInt())).thenReturn(template);
		String responseString = getResponseAsString(String.format("%s/%d", BASE_URL, template.getTemplateId()), null, MockMvcResultMatchers.status().isOk());
		Template template1 = mapper.readValue(responseString, new TypeReference<Template>() {});
		assertEquals(template.getTemplateId(), template1.getTemplateId());
	}

	@Test
	public void testPost() throws Exception {
		Template template = template256();
		template.setVersion(24);
		when(templateService.getAll(anyInt())).thenReturn(Collections.singletonList(template));

		String templateJson = mapper.writeValueAsString(template);
		postRequestWithJsonContent(BASE_URL, templateJson, MockMvcResultMatchers.status().isCreated());

		ArgumentCaptor<Template> savedTemplateCaptor = ArgumentCaptor.forClass(Template.class);
		verify(templateService).addOrUpdateTemplate(savedTemplateCaptor.capture());
		assertEquals(template.getTemplateId(), savedTemplateCaptor.getValue().getTemplateId());
		assertEquals(template.getVersion() + 1, savedTemplateCaptor.getValue().getVersion());
	}

	@Test
	public void testPut() throws Exception {
		Template template = template123();
		template.setVersion(30);
		when(templateService.getAll(anyInt())).thenReturn(Collections.singletonList(template));

		String templateJson = mapper.writeValueAsString(template);
		putRequestWithJsonContent(BASE_URL, templateJson, MockMvcResultMatchers.status().isCreated());

		ArgumentCaptor<Template> savedTemplateCaptor = ArgumentCaptor.forClass(Template.class);
		verify(templateService).addOrUpdateTemplate(savedTemplateCaptor.capture());
		assertEquals(template.getTemplateId(), savedTemplateCaptor.getValue().getTemplateId());
		assertEquals(template.getVersion() + 1, savedTemplateCaptor.getValue().getVersion());
	}
}
