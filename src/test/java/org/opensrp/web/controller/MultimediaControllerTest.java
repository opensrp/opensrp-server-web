package org.opensrp.web.controller;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opensrp.dto.form.MultimediaDTO;
import org.opensrp.service.MultimediaService;
import org.opensrp.web.security.DrishtiAuthenticationProvider;
import org.powermock.reflect.Whitebox;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;


public class MultimediaControllerTest {

	@InjectMocks
	private MultimediaController multimediaController;

	@Mock
	private MultimediaService multimediaService;

	private MockMvc mockMvc;

	private final String allowedMimeTypes = "application/octet-stream,image/jpeg,image/gif,image/png";
	private final String FILE_NAME_ERROR = "File Name with special characters is not allowed!";
	private final String ENTITY_ID_ERROR = "Entity Id should not contain any special character!";

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(multimediaController).build();
		ReflectionTestUtils.setField(multimediaController, "allowedMimeTypes", allowedMimeTypes);
	}

	@Test
	public void testUploadShouldUploadFileWithCorrectName() throws Exception {
		MultipartFile multipartFile = Mockito.mock(MultipartFile.class);
		Mockito.doReturn("originalName").when(multipartFile).getOriginalFilename();
		Mockito.doReturn("image/jpeg").when(multipartFile).getContentType();
		Mockito.doReturn(new byte[10]).when(multipartFile).getBytes();

		multimediaController.uploadFiles("providerID", "entity-id", "file-category", multipartFile);

		ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);

		// verify call
		Mockito.verify(multimediaService)
				.saveFile(Mockito.any(MultimediaDTO.class), Mockito.any(byte[].class), stringArgumentCaptor.capture());

		// verify call arguments
		Assert.assertEquals(stringArgumentCaptor.getValue(), "originalName");
	}

	@Test
	public void testDownloadWithAuth() {
		MultimediaController controller = Mockito.spy(new MultimediaController());

		MultimediaService multimediaService = Mockito.mock(MultimediaService.class);
		DrishtiAuthenticationProvider provider = Mockito.mock(DrishtiAuthenticationProvider.class);
		HttpServletResponse httpServletResponse = Mockito.mock(HttpServletResponse.class);
		HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
		Whitebox.setInternalState(controller, "multimediaService", multimediaService);
		Whitebox.setInternalState(controller, "provider", provider);
		Mockito.doReturn(getMockedAuthentication()).when(provider).authenticate(any(Authentication.class));

		controller.downloadFileWithAuth(httpServletResponse, "fileName", "testUser", "password", httpServletRequest);

		// verify call to the service
		Mockito.verify(multimediaService).retrieveFile(anyString());
	}

	@Test
	public void testUploadShouldReturnBadRequestWithInvalidEntityId() throws Exception {
		MultipartFile multipartFile = Mockito.mock(MultipartFile.class);
		Mockito.doReturn("originalName").when(multipartFile).getOriginalFilename();
		Mockito.doReturn("image/jpeg").when(multipartFile).getContentType();
		Mockito.doReturn(new byte[10]).when(multipartFile).getBytes();

		ResponseEntity<String> response = multimediaController.uploadFiles("providerID", "entity-id"+"\r", "file-category", multipartFile);
		assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
		assertEquals(response.getBody(),ENTITY_ID_ERROR);
	}

	@Test
	public void testUploadShouldReturnBadRequestWithSpecialCharacterFileName() throws Exception {
		MultipartFile multipartFile = Mockito.mock(MultipartFile.class);
		Mockito.doReturn("originalName" + "\t").when(multipartFile).getOriginalFilename();
		Mockito.doReturn("image/jpeg").when(multipartFile).getContentType();
		Mockito.doReturn(new byte[10]).when(multipartFile).getBytes();

		ResponseEntity<String> response = multimediaController.uploadFiles("providerID", "entity-id"+"\r", "file-category", multipartFile);
		assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
		assertEquals(response.getBody(),FILE_NAME_ERROR);
	}


	private Authentication getMockedAuthentication() {
		Authentication authentication = new Authentication() {

			@Override
			public Collection<? extends GrantedAuthority> getAuthorities() {
				return null;
			}

			@Override
			public Object getCredentials() {
				return "";
			}

			@Override
			public Object getDetails() {
				return null;
			}

			@Override
			public Object getPrincipal() {
				return "Test User";
			}

			@Override
			public boolean isAuthenticated() {
				return true;
			}

			@Override
			public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

			}

			@Override
			public String getName() {
				return "admin";
			}
		};

		return authentication;
	}


}
