package org.opensrp.web.controller;

import static org.mockito.ArgumentMatchers.anyString;

import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opensrp.dto.form.MultimediaDTO;
import org.opensrp.service.MultimediaService;
import org.powermock.reflect.Whitebox;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;


public class MultimediaControllerTest {

	@InjectMocks
	private MultimediaController multimediaController;

	@Mock
	private MultimediaService multimediaService;

	private final String allowedMimeTypes = "application/octet-stream,image/jpeg,image/gif,image/png";

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
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
		HttpServletResponse httpServletResponse = Mockito.mock(HttpServletResponse.class);
		Whitebox.setInternalState(controller, "multimediaService", multimediaService);

		controller.downloadFileWithAuth(httpServletResponse, "fileName");

		// verify call to the service
		Mockito.verify(multimediaService).retrieveFile(anyString());
	}

}
