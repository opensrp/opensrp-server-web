package org.opensrp.web.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opensrp.domain.Manifest;
import org.opensrp.service.ManifestService;
import org.springframework.test.web.server.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ManifestResourceTest extends BaseResourceTest<Manifest> {

    private ManifestService manifestService;

    private final static String BASE_URL = "/rest/manifest";
    private ArgumentCaptor<Manifest> argumentCaptor = ArgumentCaptor.forClass(Manifest.class);

    private final static String manifestJson = "{\"identifier\":\"mani1234\",\"json\":\"{}\",\"appVersion\":\"123456\",\"appId\":\"1234567frde\"}";
    private final static String manifestJsonOnlyValue = "{\"json\": \"{\\\"forms_version\\\":\\\"1.0.2\\\",\\\"identifiers\\\":[\\\"add_structure.json\\\"]}\"}";
    private final static String existingManifestJson = "{\n"
            + "    \"json\": \"{\\\"forms_version\\\":\\\"1.0.2\\\",\\\"identifiers\\\":[\\\"add_structure.json\\\"]}\",\n"
            + "    \"appId\": \"org.smartregister.anc\",\n"
            + "    \"createdAt\": \"2020-06-12T14:24:32.871+03:00\",\n"
            + "    \"updatedAt\": \"2020-06-12T14:24:32.871+03:00\",\n"
            + "    \"appVersion\": \"3.4.2\",\n"
            + "    \"identifier\": \"1.0.1\"\n"
            + "}";

    @Before
    public void setUp() {
        manifestService = mock(ManifestService.class);
        ManifestResource manifestResource = webApplicationContext.getBean(ManifestResource.class);
        manifestResource.setManifestService(manifestService);
        manifestResource.setObjectMapper(mapper);
    }

    private static Manifest initTestManifest() {
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

    private static Manifest initTestManifest2() {
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

    private static Manifest initTestManifest3() {
        Manifest manifest = new Manifest();
        String identifier = "1.0.2";
        String appVersion = "3.4.2";
        String appId = "org.smartregister.anc";

        manifest.setAppId(appId);
        manifest.setAppVersion(appVersion);
        manifest.setIdentifier(identifier);
        manifest.setJson(existingManifestJson);

        return manifest;
    }

    @Test
    public void testAllGetManifest() throws Exception {
        List<Manifest> expectedManifests = new ArrayList<>();

        Manifest expectedManifest = initTestManifest();
        expectedManifests.add(expectedManifest);

        Manifest expectedManifest1 = initTestManifest2();
        expectedManifests.add(expectedManifest1);

        doReturn(expectedManifests).when(manifestService).getAllManifest();

        String actualManifestsString = getResponseAsString(BASE_URL, null, MockMvcResultMatchers.status().isOk());
        List<Manifest> actualManifests = mapper.readValue(actualManifestsString, new TypeReference<List<Manifest>>() {});

        assertListsAreSameIgnoringOrder(actualManifests, expectedManifests);
    }

    @Test
    public void testGetManifestByUniqueIdShouldReturnCorrectManifests() throws Exception {
        List<Manifest> expectedManifests = new ArrayList<>();

        Manifest expectedManifest = initTestManifest();
        expectedManifests.add(expectedManifest);

        List<String> expectedIdManifests = new ArrayList<>();
        expectedIdManifests.add(expectedManifest.getIdentifier());

        doReturn(expectedManifest).when(manifestService).getManifest(anyString());

        String actualManifestsString = getResponseAsString(BASE_URL + "/mani1234", null,
                MockMvcResultMatchers.status().isOk());
        Manifest actualManifest = mapper.readValue(actualManifestsString, new TypeReference<Manifest>() {});

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
        doReturn(new Manifest()).when(manifestService).addManifest(any());
        Manifest expectedManifest = initTestManifest();

        postRequestWithJsonContent(BASE_URL, manifestJson, MockMvcResultMatchers.status().isCreated());

        verify(manifestService).addManifest(argumentCaptor.capture());
        assertEquals(argumentCaptor.getValue().getIdentifier(), expectedManifest.getIdentifier());

    }

    @Test
    public void testCreateNewManifestWithJsonOnly() throws Exception {
        Manifest existingManifest = new Manifest();

        existingManifest.setAppId("org.smartregister.anc");
        existingManifest.setAppVersion("3.4.2");
        existingManifest.setJson(existingManifestJson);
        List manifestList = new ArrayList<Manifest>();
        manifestList.add(existingManifest);

        doReturn(manifestList).when(manifestService).getAllManifest(1);
        doReturn(new Manifest()).when(manifestService).addManifest(any());

        Manifest expectedManifest = initTestManifest3();

        postRequestWithJsonParam(BASE_URL, manifestJsonOnlyValue, MockMvcResultMatchers.status().isCreated());

        verify(manifestService).addManifest(argumentCaptor.capture());
        assertEquals(argumentCaptor.getValue().getIdentifier(), expectedManifest.getIdentifier());
    }

    @Test
    public void testUpdateManifestResource() throws Exception {
        Manifest expectedManifest = initTestManifest();

        String manifestJson = mapper.writeValueAsString(expectedManifest);
        putRequestWithJsonContent(BASE_URL, manifestJson, MockMvcResultMatchers.status().isCreated());

        verify(manifestService).updateManifest(argumentCaptor.capture());
        assertEquals(argumentCaptor.getValue().getIdentifier(), expectedManifest.getIdentifier());
    }

    @Test
    public void testBatchSaveShouldBaseSaveManifests() throws Exception {
        ArgumentCaptor<List<Manifest>> manifestListArgumentCaptor = ArgumentCaptor.forClass(List.class);
        doReturn(new HashSet<String>()).when(manifestService).saveManifests(any());

        ArrayList<Manifest> manifests = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Manifest manifest = new Manifest();
            manifest.setAppId("org.smartregister.giz");
            manifest.setAppVersion("0.0.1");
            manifest.setIdentifier("opd/registration.json");
            manifest.setJson("{}");

            manifests.add(manifest);
        }

        String manifestsJson = mapper.writeValueAsString(manifests);

        postRequestWithJsonContentAndReturnString(BASE_URL + "/add", manifestsJson, MockMvcResultMatchers.status().isCreated());

        verify(manifestService).saveManifests(manifestListArgumentCaptor.capture());
        List<Manifest> capturedManifests = manifestListArgumentCaptor.getValue();
        assertEquals(10, capturedManifests.size());

        for (Manifest manifest: capturedManifests) {
            assertEquals("org.smartregister.giz", manifest.getAppId());
            assertEquals("0.0.1", manifest.getAppVersion());
            assertEquals("opd/registration.json", manifest.getIdentifier());
            assertEquals("{}", manifest.getJson());
        }
    }

    @Test
    public void testDeleteShouldReturnAccepted() throws Exception {
        doNothing().when(manifestService).deleteManifest(argumentCaptor.capture());
        deleteRequestWithJsonContent(BASE_URL, "{\"identifier\":\"opd/registration.json\",\"json\":\"{}\",\"appId\":\"org.smartregister.giz\",\"appVersion\":\"0.0.1\"}"
                , MockMvcResultMatchers.status().isAccepted());

        verify(manifestService).deleteManifest(argumentCaptor.capture());

        Manifest manifest = argumentCaptor.getValue();
        assertEquals("0.0.1", manifest.getAppVersion());
        assertEquals("org.smartregister.giz", manifest.getAppId());
    }

    @Test
    public void testGetManifestByAppId() throws Exception {
        Manifest manifest = new Manifest();
        manifest.setAppId("org.smartregister.giz");
        manifest.setAppVersion("0.0.1");
        manifest.setIdentifier("opd/registration.json");
        manifest.setJson("{}");

        doReturn(manifest).when(manifestService).getManifestByAppId(eq("org.smartregister.giz"));

        String responseString = getResponseAsString(BASE_URL + "/appId/org.smartregister.giz",  null, MockMvcResultMatchers.status().isOk());

        verify(manifestService).getManifestByAppId(eq("org.smartregister.giz"));
        Manifest returned = mapper.readValue(responseString, Manifest.class);

        assertEquals("0.0.1", returned.getAppVersion());
        assertEquals("org.smartregister.giz", returned.getAppId());

    }

    @Test
    public void testGetManifestByAppIdAndAppVersion() throws Exception {
        Manifest manifest = new Manifest();
        String appId = "org.smartregister.giz";
        manifest.setAppId(appId);
        String appVersion = "0.0.1";
        manifest.setAppVersion(appVersion);
        manifest.setIdentifier("opd/registration.json");
        manifest.setJson("{}");

        List<Manifest> manifestList = new ArrayList<>();
        manifestList.add(manifest);

        doReturn(manifestList).when(manifestService).getManifestsByAppId(eq(appId));

        String responseString = getResponseAsString(BASE_URL + "/search",  String.format("app_id=%s&app_version=%s", appId, appVersion)
                , MockMvcResultMatchers.status().isOk());

        verify(manifestService).getManifestsByAppId(eq(appId));
        Manifest returned = mapper.readValue(responseString, Manifest.class);

        assertEquals(appVersion, returned.getAppVersion());
        assertEquals(appId, returned.getAppId());

    }

    @Test
    public void testGetManifestByAppIdAndAppVersionShouldReturnNotFound() throws Exception {
        String appId = "org.smartregister.giz";
        String appVersion = "0.0.1";

        getResponseAsString(BASE_URL + "/search",  String.format("app_id=%s&app_version=%s", appId, appVersion)
                , MockMvcResultMatchers.status().isNotFound());

        verify(manifestService).getManifestsByAppId(eq(appId));

    }

    @Test
    public void testGetManifestByAppIdAndAppVersionShouldReturnLowerVersionManifestWhenStrictDefaultIsFalseAndLowerVersionManifestIsAvailable() throws Exception {
        List<Manifest> manifestList = new ArrayList<>();
        String appId = "org.smartregister.giz";
        String requestedAppVersion = "0.0.11";

        for (int i = 0; i < 10; i++) {
            Manifest manifest = new Manifest();
            manifest.setAppId(appId);
            String appVersion = "0.0." + (i + 1);
            manifest.setAppVersion(appVersion);
            manifest.setIdentifier("opd/registration.json");
            manifest.setJson("{}");

            manifestList.add(manifest);
        }

        doReturn(manifestList).when(manifestService).getManifestsByAppId(eq(appId));

        String responseString = getResponseAsString(BASE_URL + "/search",  String.format("app_id=%s&app_version=%s", appId, requestedAppVersion)
                , MockMvcResultMatchers.status().isOk());

        verify(manifestService).getManifestsByAppId(eq(appId));
        Manifest returned = mapper.readValue(responseString, Manifest.class);

        assertEquals("0.0.10", returned.getAppVersion());
        assertEquals(appId, returned.getAppId());
    }

    @Test
    public void testGetManifestByAppIdAndAppVersionShouldReturn404WhenStrictIsTrue() throws Exception {
        List<Manifest> manifestList = new ArrayList<>();
        String appId = "org.smartregister.giz";
        String requestedAppVersion = "0.0.11";

        for (int i = 0; i < 10; i++) {
            Manifest manifest = new Manifest();
            manifest.setAppId(appId);
            String appVersion = "0.0." + (i + 1);
            manifest.setAppVersion(appVersion);
            manifest.setIdentifier("opd/registration.json");
            manifest.setJson("{}");

            manifestList.add(manifest);
        }

        doReturn(manifestList).when(manifestService).getManifestsByAppId(eq(appId));

        getResponseAsString(BASE_URL + "/search",  String.format("app_id=%s&app_version=%s&strict=true", appId, requestedAppVersion)
                , MockMvcResultMatchers.status().isNotFound());

        verify(manifestService).getManifest(eq(appId), eq(requestedAppVersion));
    }


    @Test
    public void testGetManifestByAppIdAndAppVersionShouldReturn404WhenStrictDefaultIsFalseAndLowerVersionManifestIsNotFound() throws Exception {
        List<Manifest> manifestList = new ArrayList<>();
        String appId = "org.smartregister.giz";
        String requestedAppVersion = "0.0.10";

        for (int i = 0; i < 10; i++) {
            Manifest manifest = new Manifest();
            manifest.setAppId(appId);
            String appVersion = "0.0." + (i + 11);
            manifest.setAppVersion(appVersion);
            manifest.setIdentifier("opd/registration.json");
            manifest.setJson("{}");

            manifestList.add(manifest);
        }

        doReturn(manifestList).when(manifestService).getManifestsByAppId(eq(appId));

        getResponseAsString(BASE_URL + "/search",  String.format("app_id=%s&app_version=%s", appId, requestedAppVersion)
                , MockMvcResultMatchers.status().isNotFound());

        verify(manifestService).getManifestsByAppId(eq(appId));
    }
}
