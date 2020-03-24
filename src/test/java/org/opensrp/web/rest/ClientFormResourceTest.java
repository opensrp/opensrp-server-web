package org.opensrp.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opensrp.domain.IdVersionTuple;
import org.opensrp.domain.postgres.ClientForm;
import org.opensrp.domain.postgres.ClientFormMetadata;
import org.opensrp.service.ClientFormService;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.server.MockMvc;
import org.springframework.test.web.server.MvcResult;
import org.springframework.test.web.server.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.server.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.server.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = {"classpath:test-webmvc-config.xml",})
public class ClientFormResourceTest {

    @Autowired
    protected WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private ClientFormService clientFormService;

    private String BASE_URL = "/rest/clientForm/";
    private String JSON_FORM_FILE = "{\"count\":\"1\",\"encounter_type\":\"AEFI\",\"entity_id\":\"\",\"metadata\":{\"start\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"start\",\"openmrs_entity_id\":\"163137AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"end\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"end\",\"openmrs_entity_id\":\"163138AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"today\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"encounter\",\"openmrs_entity_id\":\"encounter_date\"},\"deviceid\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"deviceid\",\"openmrs_entity_id\":\"163149AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"subscriberid\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"subscriberid\",\"openmrs_entity_id\":\"163150AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"simserial\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"simserial\",\"openmrs_entity_id\":\"163151AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"phonenumber\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"phonenumber\",\"openmrs_entity_id\":\"163152AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"encounter_location\":\"\"},\"step1\":{\"title\":\"Adverse Event Reporting\",\"fields\":[{\"key\":\"Reaction_Vaccine\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"6042AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_data_type\":\"select one\",\"type\":\"spinner\",\"hint\":\"Vaccine that caused the reaction\",\"values\":[\"BCG\",\"HepB\",\"OPV\",\"Penta\",\"PCV\",\"Rota\",\"Measles\",\"MR\",\"Yellow Fever\"],\"openmrs_choice_ids\":{\"BCG\":\"149310AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"HepB\":\"162269AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"OPV\":\"129578AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"Penta\":\"162265AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"PCV\":\"162266AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"Rota\":\"162272AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"Measles\":\"149286AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"MR\":\"149286AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"Yellow Fever\":\"149253AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"v_required\":{\"value\":true,\"err\":\"Please enter the vaccine that caused the reaction\"}},{\"key\":\"aefi_start_date\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"type\":\"date_picker\",\"hint\":\"Date the adverse effects began\",\"expanded\":false,\"v_required\":{\"value\":true,\"err\":\"Please enter the date the adverse effects began\"}},{\"key\":\"reaction\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"openmrs_data_type\":\"\",\"type\":\"check_box\",\"label\":\"Select the reaction\",\"hint\":\"Select the reaction\",\"label_text_style\":\"bold\",\"options\":[{\"key\":\"bacteria_abscesses\",\"text\":\"Minor AEFI Bacteria abscesses\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\"},{\"key\":\"lymphadenitis\",\"text\":\"Minor AEFI Lymphadenitis\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\"},{\"key\":\"sepsis\",\"text\":\"Minor AEFI Sepsis\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\"},{\"key\":\"local_reaction\",\"text\":\"Minor AEFI Severe local reaction\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\"},{\"key\":\"birth_defect\",\"text\":\"Serious AEFI Birth Defect\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\"},{\"key\":\"death\",\"text\":\"Serious AEFI Death\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\"},{\"key\":\"encephalopathy\",\"text\":\"Serious AEFI Encephalopathy\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\"},{\"key\":\"high_fever \",\"text\":\"Serious AEFI High fever > 38 Degrees Celcius\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\"},{\"key\":\"paralysis\",\"text\":\"Serious AEFI Paralysis\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\"},{\"key\":\"seizures\",\"text\":\"Serious AEFI Seizures\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\"},{\"key\":\"birth_defect\",\"text\":\"Serious AEFI Significant Disability\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\"},{\"key\":\"toxic_shock_syndrome\",\"text\":\"Serious AEFI Toxic shock syndrome\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\"},{\"key\":\"other\",\"text\":\"Other (specify)\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\"}],\"v_required\":{\"value\":false,\"err\":\"Please select at least one reaction\"}},{\"key\":\"other_reaction\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"openmrs_data_type\":\"text\",\"type\":\"edit_text\",\"hint\":\"Other Reaction\",\"relevance\":{\"step1:reaction\":{\"ex-checkbox\":[{\"or\":[\"other\"]}]}}},{\"key\":\"child_referred\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"163340AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_data_type\":\"select one\",\"type\":\"native_radio\",\"label\":\"Child Referred?\",\"label_text_style\":\"bold\",\"options\":[{\"key\":\"Yes\",\"text\":\"Yes\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"1267AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},{\"key\":\"No\",\"text\":\"No\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"163339AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"}]},{\"key\":\"aefi_form\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"163340AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_data_type\":\"select one\",\"type\":\"native_radio\",\"label\":\"Was the AEFI form completed?\",\"label_text_style\":\"bold\",\"options\":[{\"key\":\"Yes\",\"text\":\"Yes\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"1267AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},{\"key\":\"No\",\"text\":\"No\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"163339AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"}]}]}}";

    @Before
    public void setUp() {
        clientFormService = Mockito.mock(ClientFormService.class);
        ClientFormResource clientFormResource = webApplicationContext.getBean(ClientFormResource.class);
        clientFormResource.setClientFormService(clientFormService);

        mockMvc = MockMvcBuilders.webApplicationContextSetup(webApplicationContext).build();
    }

    @Test
    public void testSearchForFormByFormVersion() throws Exception {
        String formIdentifier = "opd/reg.json";
        String formVersion = "0.1.1";
        String currentFormVersion = "0.0.1";
        List<IdVersionTuple> idVersionTuples = new ArrayList<>();
        idVersionTuples.add(new IdVersionTuple(1, "0.0.1"));
        idVersionTuples.add(new IdVersionTuple(2, "0.0.2"));
        idVersionTuples.add(new IdVersionTuple(3, "0.0.3"));

        ClientForm clientForm = new ClientForm();
        clientForm.setJson("{}");
        clientForm.setId(3L);

        ClientFormMetadata clientFormMetadata = new ClientFormMetadata();
        clientFormMetadata.setIdentifier(formIdentifier);
        clientFormMetadata.setVersion("0.0.3");

        when(clientFormService.isClientFormExists(formIdentifier)).thenReturn(true);
        when(clientFormService.getClientFormMetadataByIdentifierAndVersion(formIdentifier, formVersion)).thenReturn(null);
        when(clientFormService.getAvailableClientFormMetadataVersionByIdentifier(formIdentifier)).thenReturn(idVersionTuples);
        when(clientFormService.getClientFormById(3L)).thenReturn(clientForm);
        when(clientFormService.getClientFormMetadataById(3L)).thenReturn(clientFormMetadata);

        MvcResult result = mockMvc.perform(get(BASE_URL)
                .param("form_identifier", formIdentifier)
                .param("form_version", formVersion)
                .param("current_form_version", currentFormVersion))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(result.getResponse().getContentAsString().length() > 1);
    }

    @Test
    public void testAddClientForm() throws Exception {
        String formIdentifier = "opd/reg.json";
        String formVersion = "0.1.1";

        MockMultipartFile file = new MockMultipartFile("form", "path/to/opd/reg.json",
                "application/json", JSON_FORM_FILE.getBytes());

        when(clientFormService.addClientForm(any(ClientForm.class), any(ClientFormMetadata.class))).thenReturn(mock(ClientFormService.CompleteClientForm.class));

        mockMvc.perform(
                fileUpload(BASE_URL)
                        .file(file)
                        .param("form_identifier", formIdentifier)
                        .param("form_version", formVersion)
                        .param("form_name", "REGISTRATION FORM"))
                .andExpect(status().isCreated())
                .andReturn();

    }

}
