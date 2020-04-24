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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

public class MultimediaControllerTest {

	@InjectMocks
	private MultimediaController multimediaController;

	@Mock
	private MultimediaService multimediaService;

	private MockMvc mockMvc;

	private final String allowedMimeTypes = "application/octet-stream,image/jpeg,image/gif,image/png";

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

}
