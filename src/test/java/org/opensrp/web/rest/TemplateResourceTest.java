package org.opensrp.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.opensrp.domain.PlanTemplate;
import org.opensrp.domain.Template;
import org.opensrp.service.TemplateService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.when;

public class TemplateResourceTest extends BaseSecureResourceTest<PlanTemplate>{

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    private final static String BASE_URL = "/rest/templates/";

    private TemplateService templateService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Captor
    private ArgumentCaptor<Template> templateArgumentCaptor = ArgumentCaptor.forClass(Template.class);

    @Before
    public void setUp() {
        TemplateResource templateResource = webApplicationContext.getBean(TemplateResource.class);
        templateService = Mockito.mock(TemplateService.class);
        templateResource.setTemplateService(templateService);
        templateResource.setObjectMapper(objectMapper);
    }

    @Test
    public void testGetAllReturnsAllTemplates() throws Exception {
        List<Template> expectedTemplates = new ArrayList<>();
        Template template = new Template();
        template.setTemplateId(1);
        template.setVersion(1);
        template.setType("Plan");
        PlanTemplate planTemplate = new PlanTemplate();
        planTemplate.setIdentifier("plan identifier");
        template.setTemplate(planTemplate);
        expectedTemplates.add(template);

        when(templateService.getAll()).thenReturn(expectedTemplates);

        String actualTemplates = getResponseAsString(BASE_URL,null, status().isOk());
        assertEquals(objectMapper.writeValueAsString(expectedTemplates),actualTemplates);
    }

    @Test
    public void testGetReturnsTemplateWithSpecifiedTemplateId() throws Exception {
        Template expectedTemplate = new Template();
        expectedTemplate.setTemplateId(1);
        expectedTemplate.setVersion(1);
        expectedTemplate.setType("Plan");
        PlanTemplate planTemplate = new PlanTemplate();
        planTemplate.setIdentifier("plan identifier");
        expectedTemplate.setTemplate(planTemplate);

        when(templateService.getTemplateByTemplateId(expectedTemplate.getTemplateId())).thenReturn(expectedTemplate);

        String actualTemplate = getResponseAsString(BASE_URL +"1", null, status().isOk());
        assertEquals(objectMapper.writeValueAsString(expectedTemplate),actualTemplate);
    }

    @Test
    public void testCreateShouldCreateNewTemplate() throws Exception {
        Template expectedTemplate = initTestTemplate();
        when(templateService.getTemplateByTemplateId(expectedTemplate.getTemplateId())).thenReturn(null);

        postRequestWithJsonContent(BASE_URL, objectMapper.writeValueAsString(expectedTemplate), status().isCreated());

        verify(templateService).getTemplateByTemplateId(expectedTemplate.getTemplateId());
        verify(templateService).addOrUpdateTemplate(templateArgumentCaptor.capture());
        Template actualTemplate = templateArgumentCaptor.getValue();
        assertEquals(expectedTemplate.getTemplateId(), actualTemplate.getTemplateId());
        assertEquals(expectedTemplate.getTemplate().getIdentifier(), actualTemplate.getTemplate().getIdentifier());
        assertEquals(expectedTemplate.getType(), actualTemplate.getType());
        assertEquals(expectedTemplate.getVersion(), actualTemplate.getVersion());
    }

    @Test
    public void testUpdateShouldUpdateExistingTemplate() throws Exception {
        Template expectedTemplate = initTestTemplate();
        when(templateService.getTemplateByTemplateId(expectedTemplate.getTemplateId())).thenReturn(null);

        putRequestWithJsonContent(BASE_URL, objectMapper.writeValueAsString(expectedTemplate), status().isCreated());

        verify(templateService).getTemplateByTemplateId(expectedTemplate.getTemplateId());
        verify(templateService).addOrUpdateTemplate(templateArgumentCaptor.capture());
        Template actualTemplate = templateArgumentCaptor.getValue();
        assertEquals(expectedTemplate.getTemplateId(), actualTemplate.getTemplateId());
        assertEquals(expectedTemplate.getTemplate().getIdentifier(), actualTemplate.getTemplate().getIdentifier());
        assertEquals(expectedTemplate.getType(), actualTemplate.getType());
        assertEquals(expectedTemplate.getVersion(), actualTemplate.getVersion());
    }

    @Override
    protected void assertListsAreSameIgnoringOrder(List<PlanTemplate> expectedList, List<PlanTemplate> actualList) {
        if (expectedList == null || actualList == null) {
            throw new AssertionError("One of the lists is null");
        }

        assertEquals(expectedList.size(), actualList.size());

        Set<String> expectedIds = new HashSet<>();
        for (PlanTemplate plan : expectedList) {
            expectedIds.add(plan.getIdentifier());
        }

        for (PlanTemplate plan : actualList) {
            assertTrue(expectedIds.contains(plan.getIdentifier()));
        }
    }

    private Template initTestTemplate() {
        Template expectedTemplate = new Template();
        expectedTemplate.setTemplateId(1);
        expectedTemplate.setVersion(0);
        expectedTemplate.setType("Plan");
        PlanTemplate planTemplate = new PlanTemplate();
        planTemplate.setIdentifier("plan identifier");
        expectedTemplate.setTemplate(planTemplate);
        return expectedTemplate;
    }
}
