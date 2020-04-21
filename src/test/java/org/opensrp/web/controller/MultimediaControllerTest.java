package org.opensrp.web.controller;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.opensrp.dto.form.MultimediaDTO;
import org.opensrp.service.MultimediaService;
import org.opensrp.web.security.DrishtiAuthenticationProvider;
import org.powermock.reflect.Whitebox;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;


public class MultimediaControllerTest {

	@Test
	public void testUploadShouldUploadFileWithCorrectName() throws Exception {
		MultimediaController controller = Mockito.spy(new MultimediaController());

		MultimediaService multimediaService = Mockito.mock(MultimediaService.class);
		Whitebox.setInternalState(controller, "multimediaService", multimediaService);

		MultipartFile multipartFile = Mockito.mock(MultipartFile.class);
		Mockito.doReturn("originalName").when(multipartFile).getOriginalFilename();
		Mockito.doReturn("image/jpeg").when(multipartFile).getContentType();
		Mockito.doReturn(new byte[10]).when(multipartFile).getBytes();

		controller.uploadFiles("providerID", "entity-id", "file-category", multipartFile);

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
