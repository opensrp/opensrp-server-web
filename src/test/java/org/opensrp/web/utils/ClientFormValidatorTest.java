package org.opensrp.web.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opensrp.TestFileContent;
import org.opensrp.domain.postgres.ClientForm;
import org.opensrp.service.ClientFormService;

import java.text.DateFormat;
import java.util.HashSet;

public class ClientFormValidatorTest {

    @Mock
    private ClientFormService clientFormService;

    private ClientFormValidator clientFormValidator;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        clientFormValidator = new ClientFormValidator(clientFormService);
    }

    @Test
    public void testCheckForMissingFormReferencesShouldReturnEmptyHashSetWhenGivenSubForm() {
        HashSet<String> missingReferences = clientFormValidator.checkForMissingFormReferences(TestFileContent.HIV_TESTS_SUB_FORM);
        Assert.assertEquals(0, missingReferences.size());
    }

    @Test
    public void testCheckForMissingFormReferencesShouldReturnEmptyHashSetWhenGivenFormWithoutSubFormReferences() {
        HashSet<String> missingReferences = clientFormValidator.checkForMissingFormReferences(TestFileContent.ANC_QUICK_CHECK_JSON_FORM_FILE);
        Assert.assertEquals(0, missingReferences.size());
    }

    @Test
    public void testCheckForMissingFormReferencesShouldReturnNonEmptyHashSetWhenGivenFormWithMissingSubFormReferences() {
        Mockito.doReturn(true).when(clientFormService).isClientFormExists("abdominal_exam_sub_form");

        HashSet<String> missingReferences = clientFormValidator.checkForMissingFormReferences(TestFileContent.PHYSICAL_EXAM_FORM_FILE);
        Mockito.verify(clientFormService, Mockito.times(13)).isClientFormExists(Mockito.anyString());
        Assert.assertEquals(6, missingReferences.size());
    }

    @Test
    public void testCheckForMissingFormReferencesShouldReturnEmptyHashSetWhenGivenFormWithAvailableSubFormReferences() {
        Mockito.doReturn(true).when(clientFormService).isClientFormExists(Mockito.anyString());

        HashSet<String> missingReferences = clientFormValidator.checkForMissingFormReferences(TestFileContent.PHYSICAL_EXAM_FORM_FILE);
        Mockito.verify(clientFormService, Mockito.times(7)).isClientFormExists(Mockito.anyString());
        Assert.assertEquals(0, missingReferences.size());
    }

    @Test
    public void testCheckForMissingRuleReferencesShouldReturnEmptyHashSetWhenGivenFormWithoutRuleFileReferences() {
        HashSet<String> missingReferences = clientFormValidator.checkForMissingRuleReferences(TestFileContent.JSON_FORM_FILE);
        Mockito.verifyNoInteractions(clientFormService);
        Assert.assertEquals(0, missingReferences.size());
    }

    @Test
    public void testCheckForMissingRuleReferencesShouldReturnNonEmptyHashSetWhenGivenFormWithMissingRuleFileReferences() {
        Mockito.doReturn(true).when(clientFormService).isClientFormExists("physical-exam-relevance-rules.yml");

        HashSet<String> missingReferences = clientFormValidator.checkForMissingRuleReferences(TestFileContent.PHYSICAL_EXAM_FORM_FILE);
        Mockito.verify(clientFormService, Mockito.times(2)).isClientFormExists(Mockito.anyString());
        Assert.assertEquals(1, missingReferences.size());
    }

    @Test
    public void testCheckForMissingRuleReferencesShouldReturnEmptyHashSetWhenGivenFormWithAvailableRuleFiles() {
        Mockito.doReturn(true).when(clientFormService).isClientFormExists("physical-exam-relevance-rules.yml");
        Mockito.doReturn(true).when(clientFormService).isClientFormExists("physical-exam-calculations-rules.yml");

        HashSet<String> missingReferences = clientFormValidator.checkForMissingRuleReferences(TestFileContent.PHYSICAL_EXAM_FORM_FILE);
        Mockito.verify(clientFormService, Mockito.times(2)).isClientFormExists(Mockito.anyString());
        Assert.assertEquals(0, missingReferences.size());
    }

    @Test
    public void testCheckForMissingPropertyFileReferencesShouldReturnEmptyHashSetWhenGivenFormWithAvailablePropertyFiles() {
        Mockito.doReturn(true).when(clientFormService).isClientFormExists("anc_register.properties");

        HashSet<String> missingReferences = clientFormValidator.checkForMissingPropertyFileReferences(TestFileContent.ANC_REGISTER_JSON_FORM_FILE);
        Mockito.verify(clientFormService, Mockito.times(2)).isClientFormExists(Mockito.anyString());
        Assert.assertEquals(0, missingReferences.size());
    }

    @Test
    public void testCheckForMissingPropertyFileReferencesShouldReturnNonEmptyHashSetWhenGivenFormWithMissingPropertyFiles() {
        HashSet<String> missingReferences = clientFormValidator.checkForMissingPropertyFileReferences(TestFileContent.ANC_REGISTER_JSON_FORM_FILE);
        Mockito.verify(clientFormService, Mockito.times(2)).isClientFormExists(Mockito.anyString());
        Assert.assertEquals(1, missingReferences.size());
        Assert.assertEquals("anc_register", missingReferences.toArray()[0]);
    }

    @Test
    public void testCheckForMissingPropertyFileReferencesShouldReturnEmptySetWhenYamlHasPropertyFiles() {
        Mockito.doReturn(true).when(clientFormService).isClientFormExists("attention_flags.properties");

        HashSet<String> missingReferences = clientFormValidator.checkForMissingYamlPropertyFileReferences(TestFileContent.ATTENTION_FLAGS_YAML_FILE);
        Mockito.verify(clientFormService, Mockito.times(2)).isClientFormExists(Mockito.anyString());
        Assert.assertEquals(0, missingReferences.size());
    }

    @Test
    public void testCheckForMissingPropertyFileReferencesShouldReturnNonEmptySetWhenYamlMissingPropertyFiles() {
        HashSet<String> missingReferences = clientFormValidator.checkForMissingYamlPropertyFileReferences(TestFileContent.ATTENTION_FLAGS_YAML_FILE);
        Mockito.verify(clientFormService, Mockito.times(2)).isClientFormExists(Mockito.anyString());
        Assert.assertEquals(1, missingReferences.size());
        Assert.assertEquals("attention_flags", missingReferences.toArray()[0]);
    }

    @Test
    public void testPerformWidgetValidationWithMissingFields() throws JsonProcessingException {
        String formIdentifier = "anc_registration.json";

        ClientForm formValidator = new ClientForm();
        formValidator.setJson("\"{\"cannot_remove\":{\"title\":\"Fields you cannot remove\",\"fields\":[\"reaction_vaccine_duration\",\"reaction_vaccine_dosage\",\"aefi_form\"]}}\"");

        Mockito.doReturn(formValidator)
                .when(clientFormService).getMostRecentFormValidator(formIdentifier);


        ObjectMapper mapper = new ObjectMapper().enableDefaultTyping();

        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setDateFormat(DateFormat.getDateTimeInstance());

        HashSet<String> missingWidgets = clientFormValidator.performWidgetValidation(mapper, formIdentifier, TestFileContent.JSON_FORM_FILE);
        Assert.assertEquals(2, missingWidgets.size());
    }

    @Test
    public void testPerformWidgetValidationWithOutMissingFields() throws JsonProcessingException {
        String formIdentifier = "anc_registration.json";

        ClientForm formValidator = new ClientForm();
        formValidator.setJson("\"{\"cannot_remove\":{\"title\":\"Fields you cannot remove\",\"fields\":[\"aefi_form\"]}}\"");

        Mockito.doReturn(formValidator)
                .when(clientFormService).getMostRecentFormValidator(formIdentifier);


        ObjectMapper mapper = new ObjectMapper().enableDefaultTyping();

        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setDateFormat(DateFormat.getDateTimeInstance());

        HashSet<String> missingWidgets = clientFormValidator.performWidgetValidation(mapper, formIdentifier, TestFileContent.JSON_FORM_FILE);
        Assert.assertEquals(0, missingWidgets.size());
    }
}
