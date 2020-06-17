package org.opensrp.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.opensrp.domain.Client;
import org.opensrp.domain.Event;
import org.opensrp.domain.Multimedia;
import org.opensrp.repository.MultimediaRepository;
import org.opensrp.search.UploadValidationBean;
import org.opensrp.service.ClientService;
import org.opensrp.service.EventService;
import org.opensrp.service.MultimediaService;
import org.opensrp.service.OpenmrsIDService;
import org.opensrp.service.UploadService;
import org.opensrp.util.DateTimeTypeConverter;
import org.opensrp.web.GlobalExceptionHandler;
import org.opensrp.web.bean.UploadBean;
import org.opensrp.web.config.security.filter.CrossSiteScriptingPreventionFilter;
import org.opensrp.web.exceptions.BusinessLogicException;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.opensrp.web.rest.UploadController.DEFAULT_RESIDENCE;
import static org.opensrp.web.rest.UploadController.FILE_CATEGORY;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = { "classpath:test-webmvc-config.xml", })
@ActiveProfiles(profiles = { "jedis", "postgres", "basic_auth" })
public class UploadControllerTest {

	@Rule
	public MockitoRule rule = MockitoJUnit.rule();

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	protected WebApplicationContext webApplicationContext;

	private MockMvc mockMvc;

	@InjectMocks
	private UploadController uploadController;

	@Mock
	private MultimediaService multimediaService;

	@Mock
	private MultimediaRepository multimediaRepository;

	@Mock
	private ClientService clientService;

	@Mock
	private UploadService uploadService;

	@Mock
	private EventService eventService;

	@Mock
	private OpenmrsIDService openmrsIDService;

	private final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
			.registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();

	private final String BASE_URL = "/rest/upload/";

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(uploadController)
				.setControllerAdvice(new GlobalExceptionHandler()).
						addFilter(new CrossSiteScriptingPreventionFilter(), "/*").build();

		uploadController.setObjectMapper(objectMapper);
	}

	private void mockSecurityUser() {
		User applicationUser = mock(User.class);
		Mockito.doReturn("providerID").when(applicationUser).getUsername();

		Authentication authentication = mock(Authentication.class);
		SecurityContext securityContext = mock(SecurityContext.class);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(applicationUser);
	}

	@Test
	public void testUploadCSV() throws Exception {
		mockSecurityUser();
		String path = "src/test/resources/sample/childregistration.csv";
		MockMultipartFile firstFile = new MockMultipartFile("file", "sampleFile.txt", "text/csv",
				Files.readAllBytes(Paths.get(path)));

		UploadValidationBean validationBean = new UploadValidationBean();
		validationBean.setRowsToUpdate(1);
		validationBean.setRowsToCreate(2);
		validationBean.setHeaderColumns(4);

		int low = 10000;
		int high = 100000;

		List<String> results = new ArrayList<>();
		results.add(Integer.toString(new Random().nextInt(high - low) + low));

		List<Pair<Client, Event>> clients = new ArrayList<>();
		Client client = new Client("");
		clients.add(Pair.of(client, null));
		validationBean.setAnalyzedData(clients);

		when(openmrsIDService.downloadOpenmrsIds(Mockito.anyLong())).thenReturn(results);
		when(uploadService.validateFieldValues(Mockito.any(), Mockito.anyString(), Mockito.any()))
				.thenReturn(validationBean);

		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders
						.multipart(BASE_URL + "?team_id=teamID&team_name=name&location_id=12345&event_name=ChildReg")
						.file(firstFile)
		)
				.andExpect(status().isOk())
				.andReturn();

		// verify validation
		verify(uploadService, times(1)).validateFieldValues(Mockito.any(), Mockito.anyString(), Mockito.any());

		// verify data was created and inserted
		verify(clientService, times(1)).addorUpdate(Mockito.any(Client.class));
		verify(eventService, times(1)).addorUpdateEvent(Mockito.any(Event.class));

		// verify file was saved
		verify(multimediaService, times(1)).saveFile(Mockito.any(), Mockito.any(), Mockito.any());

		assertTrue(result.getResponse().getContentAsString().contains("size"));
		assertTrue(result.getResponse().getContentAsString().contains("imported"));
		assertTrue(result.getResponse().getContentAsString().contains("updated"));
	}

	@Test(expected = BusinessLogicException.class)
	public void testUploadCSVWithErrors() throws Exception {
		mockSecurityUser();
		String path = "src/test/resources/sample/childregistration.csv";
		MockMultipartFile firstFile = new MockMultipartFile("file", "sampleFile.txt", "text/csv",
				Files.readAllBytes(Paths.get(path)));

		UploadValidationBean validationBean = new UploadValidationBean();
		validationBean.setRowsToUpdate(1);
		validationBean.setRowsToCreate(2);
		validationBean.setHeaderColumns(4);

		List<String> errors = new ArrayList<>();
		errors.add("Sample error");
		validationBean.setErrors(errors);

		when(uploadService.validateFieldValues(Mockito.any(), Mockito.anyString(), Mockito.any()))
				.thenReturn(validationBean);

		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders
						.multipart(BASE_URL + "?team_id=teamID&team_name=name&location_id=12345&event_name=ChildReg")
						.file(firstFile)
		)
				.andExpect(status().isBadRequest())
				.andReturn();
		verify(uploadService, times(1)).validateFieldValues(Mockito.any(), Mockito.anyString(), Mockito.any());
		assertTrue(result.getResponse().getContentAsString().contains("A number of errors were found during validation"));
	}

	@Test
	public void testValidateFile() throws Exception {
		String path = "src/test/resources/sample/childregistration.csv";
		MockMultipartFile firstFile = new MockMultipartFile("file", "sampleFile.txt", "text/csv",
				Files.readAllBytes(Paths.get(path)));

		UploadValidationBean validationBean = new UploadValidationBean();
		validationBean.setRowsToUpdate(1);
		validationBean.setRowsToCreate(2);
		validationBean.setHeaderColumns(4);

		when(uploadService.validateFieldValues(Mockito.any(), Mockito.anyString(), Mockito.any()))
				.thenReturn(validationBean);

		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.multipart(BASE_URL + "/validate?event_name=ChildRegistration")
						.file(firstFile)
		)
				.andExpect(status().isOk())
				.andReturn();
		verify(uploadService, times(1)).validateFieldValues(Mockito.any(), Mockito.anyString(), Mockito.any());
		assertEquals(gson.toJson(validationBean), result.getResponse().getContentAsString());
	}

	@Test
	public void testGetHistory() throws Exception {
		mockSecurityUser();

		List<Multimedia> multimediaList = new ArrayList<>();

		Multimedia multimedia = new Multimedia();
		multimedia.setDateUploaded(new Date());
		multimedia.setOriginalFileName("filename.csv");
		multimedia.setFilePath("filename.csv");
		multimedia.setContentType("text/csv");
		multimedia.setFileCategory("csv");
		multimedia.setProviderId("provider_id");
		multimedia.setCaseId("case_id");

		multimediaList.add(multimedia);

		when(multimediaRepository.getByProviderID("", FILE_CATEGORY, 0, 50)).thenReturn(multimediaList);
		MvcResult result = mockMvc.perform(get(BASE_URL + "/history")).andExpect(status().isOk())
				.andReturn();
		verify(multimediaRepository, times(1)).getByProviderID("", FILE_CATEGORY, 0, 50);
		verifyNoMoreInteractions(multimediaRepository);

		String content = result.getResponse().getContentAsString();

		Type listOfMyClassObject = new TypeToken<ArrayList<UploadBean>>() {

		}.getType();
		List<UploadBean> values = gson.fromJson(content, listOfMyClassObject);

		assertEquals(1, values.size());
		UploadBean uploadBean = values.get(0);
		assertEquals(uploadBean.getFileName(), multimedia.getOriginalFileName());
		assertEquals(uploadBean.getIdentifier(), multimedia.getCaseId());
		assertEquals(uploadBean.getProviderID(), multimedia.getProviderId());
		assertEquals(uploadBean.getUploadDate(), multimedia.getDateUploaded());
		assertEquals(uploadBean.getUrl(), (multimedia.getCaseId() + "." + FILE_CATEGORY));
	}

	@Test
	public void testGetUploadTemplateReadsClientsAndReturnsCSV() throws Exception {
		when(uploadService.getCSVConfig("ChildRegistration")).thenReturn(new ArrayList<>());

		List<Client> clients = new ArrayList<>();
		clients.add(new Client("base_entity_id"));

		when(clientService.findAllByAttribute(DEFAULT_RESIDENCE, "12345")).thenReturn(clients);
		MvcResult result = mockMvc.perform(get(BASE_URL + "/template?location_id=12345&event_name=ChildRegistration"))
				.andExpect(status().isOk())
				.andReturn();
		verify(clientService, times(1)).findAllByAttribute(DEFAULT_RESIDENCE, "12345");

		verifyNoMoreInteractions(clientService);
		assertEquals("text/csv", result.getResponse().getContentType());
	}

	@Test
	public void testDownloadFileWhenFileDoesNotExit() throws Exception {
		MvcResult result = mockMvc.perform(get(BASE_URL + "/download/{fileName:.+}", "fileName.csv"))
				.andExpect(status().isOk())
				.andReturn();
		verify(multimediaService, times(1)).retrieveFile(Mockito.anyString());

		verifyNoMoreInteractions(multimediaService);
		assertEquals("Sorry. The file you are looking for does not exist", result.getResponse().getContentAsString());
	}

	@Test
	public void testDownloadFile() throws Exception {
		String path = "src/test/resources/sample/childregistration.csv";
		File file = new File(path);

		when(multimediaService.retrieveFile(Mockito.anyString())).thenReturn(file);
		MvcResult result = mockMvc.perform(get(BASE_URL + "/download/{fileName:.+}", "fileName.csv"))
				.andExpect(status().isOk())
				.andReturn();
		verify(multimediaService, times(1)).retrieveFile(Mockito.anyString());

		verifyNoMoreInteractions(multimediaService);
		assertEquals("text/csv", result.getResponse().getContentType());
	}
}
