package org.opensrp.web.config.filter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opensrp.common.AllConstants;
import org.opensrp.domain.setting.SettingConfiguration;
import org.opensrp.repository.SettingRepository;
import org.opensrp.search.SettingSearchBean;
import org.opensrp.service.SettingService;
import org.opensrp.web.config.security.filter.CrossSiteScriptingPreventionFilter;
import org.opensrp.web.config.security.filter.XssPreventionRequestWrapper;
import org.opensrp.web.rest.SettingResource;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.owasp.encoder.Encode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = { "classpath:test-webmvc-config.xml", })
public class CrossSiteScriptingPreventionTest {

	@Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;

	private SettingService settingService;

	private SettingRepository settingRepository;

	private List<SettingConfiguration> listSettingConfigurations;

	@InjectMocks
	private CrossSiteScriptingPreventionFilter crossSiteScriptingPreventionFilter;

	private String BASE_URL = "/rest/settings/";

	@Before
	public void setUp() throws Exception {
		settingService = Mockito.spy(new SettingService());
		settingRepository = Mockito.mock(SettingRepository.class);
		settingService.setSettingRepository(settingRepository);
		SettingResource settingResource = context.getBean(SettingResource.class);
		settingResource.setSettingService(settingService);

		listSettingConfigurations = new ArrayList<>();

		SettingConfiguration settingConfiguration = new SettingConfiguration();
		settingConfiguration.setIdentifier("site_characteristics");
		settingConfiguration.setTeamId("my-team-id");
		listSettingConfigurations.add(settingConfiguration);

		MockitoAnnotations.initMocks(this);
		mockMvc = MockMvcBuilders.webAppContextSetup(context).
				addFilter(new CrossSiteScriptingPreventionFilter(), "/*").build();
	}

	@Test
	public void testGetParameter() {
		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest(HttpMethod.GET.toString(),
				"/actions?anmIdentifier=123&timeStamp=123");
		mockHttpServletRequest.setParameter("anmIdentifier", "<script>hi</script>");
		String queryParam = "<script>hi</script>";
		queryParam = Encode.forUriComponent(queryParam);
		XssPreventionRequestWrapper xssPreventionRequestWrapper = new XssPreventionRequestWrapper(mockHttpServletRequest);
		String sanitizedParameterFromFilter = xssPreventionRequestWrapper.getParameter("anmIdentifier");
		assertEquals(queryParam, sanitizedParameterFromFilter);
	}

	@Test
	public void shouldApiCallGoToFilterFirst() throws Exception {
		SettingSearchBean settingQueryBean = new SettingSearchBean();
		settingQueryBean.setTeamId("my-team-id");

		List<SettingConfiguration> settingConfig = new ArrayList<>();

		SettingConfiguration config = new SettingConfiguration();
		settingConfig.add(config);
		settingQueryBean.setServerVersion(0L);
		Mockito.when(settingService.findSettings(settingQueryBean)).thenReturn(settingConfig);

		MvcResult result = mockMvc.perform(get(BASE_URL + "/sync").param(AllConstants.BaseEntity.SERVER_VERSIOIN, "0")
				.param(AllConstants.Event.TEAM_ID, "<script>xyz</script>").param(AllConstants.Event.PROVIDER_ID, "demo"))
				.andExpect(status().isOk()).andReturn();

		Mockito.verify(settingService, Mockito.times(1)).findSettings(settingQueryBean);
		assertEquals(new ArrayList<>().toString(), result.getResponse().getContentAsString());

	}

}
