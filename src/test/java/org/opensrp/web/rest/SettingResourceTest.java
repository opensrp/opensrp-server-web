package org.opensrp.web.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.opensrp.domain.setting.SettingConfiguration;
import org.opensrp.repository.SettingRepository;
import org.opensrp.search.SettingSearchBean;
import org.opensrp.service.SettingService;
import org.opensrp.util.DateTimeTypeConverter;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.WebApplicationContext;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = { "classpath:test-webmvc-config.xml", })
public class SettingResourceTest {
	
	@Autowired
	protected WebApplicationContext webApplicationContext;
	
	private SettingService settingService;
	
	private SettingRepository settingRepository;
	
	private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
	        .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();
	
	private String settingJson = "{\n" + "    \"_id\": \"1\",\n" + "    \"_rev\": \"v1\",\n"
	        + "    \"type\": \"SettingConfiguration\",\n" + "    \"identifier\": \"site_characteristics\",\n"
	        + "    \"documentId\": \"document-id\",\n" + "    \"locationId\": \"\",\n" + "    \"providerId\": \"\",\n"
	        + "    \"teamId\": \"my-team-id\",\n" + "    \"dateCreated\": \"1970-10-04T10:17:09.993+03:00\",\n"
	        + "    \"serverVersion\": 1,\n" + "    \"settings\": [\n" + "        {\n"
	        + "            \"key\": \"site_ipv_assess\",\n"
	        + "            \"label\": \"Minimum requirements for IPV assessment\",\n" + "            \"value\": null,\n"
	        + "            \"description\": \"Are all of the following in place at your facility: \\r\\n\\ta. A protocol or standard operating procedure for Intimate Partner Violence (IPV); \\r\\n\\tb. A health worker trained on how to ask about IPV and how to provide the minimum response or beyond;\\r\\n\\tc. A private setting; \\r\\n\\td. A way to ensure confidentiality; \\r\\n\\te. Time to allow for appropriate disclosure; and\\r\\n\\tf. A system for referral in place. \"\n"
	        + "        },\n" + "        {\n" + "            \"key\": \"site_anc_hiv\",\n"
	        + "            \"label\": \"Generalized HIV epidemic\",\n" + "            \"value\": null,\n"
	        + "            \"description\": \"Is the HIV prevalence consistently > 1% in pregnant women attending antenatal clinics at your facility?\"\n"
	        + "        },\n" + "        {\n" + "            \"key\": \"site_ultrasound\",\n"
	        + "            \"label\": \"Ultrasound available\",\n" + "            \"value\": null,\n"
	        + "            \"description\": \"Is an ultrasound machine available and functional at your facility and a trained health worker available to use it?\"\n"
	        + "        },\n" + "        {\n" + "            \"key\": \"site_bp_tool\",\n"
	        + "            \"label\": \"Automated BP measurement tool\",\n" + "            \"value\": null,\n"
	        + "            \"description\": \"Does your facility use an automated blood pressure (BP) measurement tool?\"\n"
	        + "        }\n" + "    ]\n" + "}";
	
	private String settingJsonUpdate = "{\n" + "    \"_id\": \"1\",\n" + "    \"_rev\": \"v1\",\n"
	        + "    \"type\": \"SettingConfiguration\",\n" + "    \"identifier\": \"site_characteristics\",\n"
	        + "    \"documentId\": \"document-id\",\n" + "    \"id\": \"document-id\",\n" + "    \"locationId\": \"\",\n"
	        + "    \"providerId\": \"\",\n" + "    \"teamId\": \"my-team-id\",\n"
	        + "    \"dateCreated\": \"1970-10-04T10:17:09.993+03:00\",\n" + "    \"serverVersion\": 1,\n"
	        + "    \"settings\": [\n" + "        {\n" + "            \"key\": \"site_ipv_assess\",\n"
	        + "            \"label\": \"Minimum requirements for IPV assessment\",\n" + "            \"value\": null,\n"
	        + "            \"description\": \"Are all of the following in place at your facility: \\r\\n\\ta. A protocol or standard operating procedure for Intimate Partner Violence (IPV); \\r\\n\\tb. A health worker trained on how to ask about IPV and how to provide the minimum response or beyond;\\r\\n\\tc. A private setting; \\r\\n\\td. A way to ensure confidentiality; \\r\\n\\te. Time to allow for appropriate disclosure; and\\r\\n\\tf. A system for referral in place. \"\n"
	        + "        },\n" + "        {\n" + "            \"key\": \"site_anc_hiv\",\n"
	        + "            \"label\": \"Generalized HIV epidemic\",\n" + "            \"value\": null,\n"
	        + "            \"description\": \"Is the HIV prevalence consistently > 1% in pregnant women attending antenatal clinics at your facility?\"\n"
	        + "        },\n" + "        {\n" + "            \"key\": \"site_ultrasound\",\n"
	        + "            \"label\": \"Ultrasound available\",\n" + "            \"value\": null,\n"
	        + "            \"description\": \"Is an ultrasound machine available and functional at your facility and a trained health worker available to use it?\"\n"
	        + "        },\n" + "        {\n" + "            \"key\": \"site_bp_tool\",\n"
	        + "            \"label\": \"Automated BP measurement tool\",\n" + "            \"value\": null,\n"
	        + "            \"description\": \"Does your facility use an automated blood pressure (BP) measurement tool?\"\n"
	        + "        }\n" + "    ]\n" + "}";
	
	private List<SettingConfiguration> listSettingConfigurations;
	
	private ArgumentCaptor<SettingConfiguration> settingConfigurationArgumentCaptor = ArgumentCaptor
	        .forClass(SettingConfiguration.class);
	
	@Before
	public void setUp() {
		settingService = Mockito.spy(new SettingService());
		settingRepository = Mockito.mock(SettingRepository.class);
		settingService.setSettingRepository(settingRepository);
		SettingResource settingResource = webApplicationContext.getBean(SettingResource.class);
		settingResource.setSettingService(settingService);
		
		listSettingConfigurations = new ArrayList<>();
		
		SettingConfiguration settingConfiguration = new SettingConfiguration();
		settingConfiguration.setIdentifier("site_characteristics");
		settingConfiguration.setTeamId("my-team-id");
		listSettingConfigurations.add(settingConfiguration);
	}
	
	@Test
	public void testFindSettingsByVersionAndTeamId() throws Exception {
		SettingSearchBean sQB = new SettingSearchBean();
		sQB.setTeamId("my-team-id");
		sQB.setTeam(null);
		sQB.setLocationId(null);
		sQB.setProviderId(null);
		sQB.setServerVersion(1000L);
		
		settingService.findSettings(sQB);
		verify(settingRepository, times(1)).findSettings(sQB);
		verifyNoMoreInteractions(settingRepository);
		
	}
	
	@Test
	public void testSaveSetting() throws Exception {
		settingService.saveSetting(settingJson);
		
		verify(settingRepository, times(1)).add(settingConfigurationArgumentCaptor.capture());
		verifyNoMoreInteractions(settingRepository);
	}
	
	@Test
	public void testUpdateSetting() throws Exception {
		settingService.saveSetting(settingJsonUpdate);
		
		verify(settingRepository, times(1)).update(settingConfigurationArgumentCaptor.capture());
		verifyNoMoreInteractions(settingRepository);
	}
	
	@Test
	public void testAddServerVersion() throws Exception {
		
		settingService.addServerVersion();
		verify(settingRepository, times(1)).findByEmptyServerVersion();
		verifyNoMoreInteractions(settingRepository);
	}
	
	@Test
	public void testValidValue() throws Exception {
		SettingConfiguration settingConfiguration = getSettingConfigurationObject();
		assertNotNull(settingConfiguration);
		assertEquals("site_characteristics", settingConfiguration.getIdentifier());
		assertEquals("my-team-id", settingConfiguration.getTeamId());
	}
	
	private SettingConfiguration getSettingConfigurationObject() {
		return gson.fromJson(settingJson, new TypeToken<SettingConfiguration>() {}.getType());
	}
}
