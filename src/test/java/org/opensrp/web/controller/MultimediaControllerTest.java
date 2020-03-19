package org.opensrp.web.controller;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.opensrp.dto.form.MultimediaDTO;
import org.opensrp.service.MultimediaService;
import org.powermock.reflect.Whitebox;
import org.springframework.web.multipart.MultipartFile;

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

}
