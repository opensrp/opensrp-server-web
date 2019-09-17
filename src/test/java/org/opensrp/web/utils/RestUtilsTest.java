package org.opensrp.web.utils;

import org.junit.Test;
import org.opensrp.common.AllConstants.Client;
import org.opensrp.domain.Multimedia;
import org.opensrp.web.rest.RestUtils;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class RestUtilsTest {
	@Test
	public void test() throws ParseException {
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.addParameter(Client.BIRTH_DATE, "2015-02-01:2016-03-01");
		
		System.out.println(RestUtils.getDateRangeFilter(Client.BIRTH_DATE, req));
	}

	@Test
	public void testZipFilesShouldZipFiles() throws IOException {
		final String ROOT_DIR = Paths.get("").toAbsolutePath().toString() + "/src/test/java/org/opensrp/web/utils/";

		List<Multimedia> multimediaFiles = new ArrayList<>();
		Multimedia multimedia = new Multimedia();
		multimedia.setFilePath(ROOT_DIR + "test_file_1");
		multimediaFiles.add(multimedia);

		multimedia = new Multimedia();
		multimedia.setFilePath(ROOT_DIR + "test_file_2");
		multimediaFiles.add(multimedia);

		multimedia = new Multimedia();
		multimedia.setFilePath(ROOT_DIR + "test_file_3");
		multimediaFiles.add(multimedia);

		ZipOutputStream zipOutputStream = mock(ZipOutputStream.class);
		RestUtils.zipFiles(zipOutputStream, multimediaFiles);
		verify(zipOutputStream, atLeastOnce()).putNextEntry(any(ZipEntry.class));
		verify(zipOutputStream, atLeastOnce()).write(any(byte.class));
		verify(zipOutputStream, atLeastOnce()).closeEntry();
	}
}
