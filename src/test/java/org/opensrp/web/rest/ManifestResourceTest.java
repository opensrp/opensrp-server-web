package org.opensrp.web.rest;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

import org.mockito.ArgumentCaptor;
import org.opensrp.domain.Manifest;
import org.opensrp.service.ManifestService;
import org.springframework.test.web.server.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;

public class ManifestResourceTest extends BaseResourceTest<Manifest> {

    private ManifestService manifestService;

    private final static String BASE_URL = "/rest/manifest";
    private ArgumentCaptor<Manifest> argumentCaptor = ArgumentCaptor.forClass(Manifest.class);

    private final static String manifestJson =  "{\"identifier\":\"mani1234\",\"json\":\"{}\",\"appVersion\":\"123456\",\"appId\":\"1234567frde\"}";

    @Before
    public void setUp() {
        manifestService = mock(ManifestService.class);
        ManifestResource manifestResource = webApplicationContext.getBean(ManifestResource.class);
        manifestResource.setManifestService(manifestService);
    }

    private static Manifest initTestManifest(){
        Manifest manifest = new Manifest();
        String identifier = "mani1234";
        String appVersion = "1234234";
        String json = "{\"name\":\"test\"}";
        String appId = "1234567op";

        manifest.setAppId(appId);
        manifest.setAppVersion(appVersion);
        manifest.setIdentifier(identifier);
        manifest.setJson(json);

        return manifest;
    }


    private static Manifest initTestManifest2(){
        Manifest manifest = new Manifest();
        String identifier = "mani123434";
        String appVersion = "1234234234";
        String json = "{}";
        String appId = "1234567qweop";

        manifest.setAppId(appId);
        manifest.setAppVersion(appVersion);
        manifest.setIdentifier(identifier);
        manifest.setJson(json);

        return manifest;
    }


    @Test
    public void testAllGetManifest() throws Exception {
        List<Manifest> expectedManifests =  new ArrayList<>();

        Manifest expectedManifest = initTestManifest();
        expectedManifests.add(expectedManifest);

        Manifest expectedManifest1 = initTestManifest2();
        expectedManifests.add(expectedManifest1);

        doReturn(expectedManifests).when(manifestService).getAllManifest();

        String actualManifestsString = getResponseAsString(BASE_URL, null, MockMvcResultMatchers.status().isOk());
        List<Manifest> actualManifests = new Gson().fromJson(actualManifestsString, new TypeToken<List<Manifest>>(){}.getType());

        assertListsAreSameIgnoringOrder(actualManifests, expectedManifests);
    }

    @Test
    public void testGetManifestByUniqueIdShouldReturnCorrectManifests() throws Exception {
        List<Manifest> expectedManifests =  new ArrayList<>();

        Manifest expectedManifest = initTestManifest();
        expectedManifests.add(expectedManifest);


        List<String> expectedIdManifests =  new ArrayList<>();
        expectedIdManifests.add(expectedManifest.getIdentifier());

        doReturn(expectedManifest).when(manifestService).getManifest(anyString());

        String actualManifestsString = getResponseAsString(BASE_URL + "/mani1234", null,
                MockMvcResultMatchers.status().isOk());
        Manifest actualManifest = new Gson().fromJson(actualManifestsString, new TypeToken<Manifest>(){}.getType());

        assertNotNull(actualManifest);
        assertEquals(actualManifest.getIdentifier(), expectedManifest.getIdentifier());
        assertEquals(actualManifest.getAppId(), expectedManifest.getAppId());
        assertEquals(actualManifest.getAppVersion(), expectedManifest.getAppVersion());
        assertEquals(actualManifest.getJson(), expectedManifest.getJson());
    }
    @Override
    protected void assertListsAreSameIgnoringOrder(List<Manifest> expectedList, List<Manifest> actualList) {
        if (expectedList == null || actualList == null) {
            throw new AssertionError("One of the lists is null");
        }

        assertEquals(expectedList.size(), actualList.size());

        Set<String> expectedIds = new HashSet<>();
        for (Manifest manifest : expectedList) {
            expectedIds.add(manifest.getIdentifier());
        }

        for (Manifest manifest : actualList) {
            assertTrue(expectedIds.contains(manifest.getIdentifier()));
        }
    }

    @Test
    public void testCreateNewManifestResource() throws Exception {
        doReturn(new Manifest()).when(manifestService).addManifest((Manifest) any());

        Manifest expectedManifest = initTestManifest();


        postRequestWithJsonContent(BASE_URL, manifestJson, MockMvcResultMatchers.status().isCreated());

        verify(manifestService).addManifest(argumentCaptor.capture());
        assertEquals(argumentCaptor.getValue().getIdentifier(), expectedManifest.getIdentifier());

    }

    @Test
    public void testUpdateManifestResource() throws Exception {
        Manifest expectedManifest = initTestManifest();

        String manifestJson = new Gson().toJson(expectedManifest, new TypeToken<Manifest>(){}.getType());
        putRequestWithJsonContent(BASE_URL, manifestJson, MockMvcResultMatchers.status().isCreated());

        verify(manifestService).updateManifest(argumentCaptor.capture());
        assertEquals(argumentCaptor.getValue().getIdentifier(), expectedManifest.getIdentifier());
    }
}
