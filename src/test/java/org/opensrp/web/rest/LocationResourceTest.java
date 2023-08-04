package org.opensrp.web.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.AssertionErrors.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.opensrp.api.util.LocationTree;
import org.opensrp.common.AllConstants.BaseEntity;
import org.opensrp.connector.dhis2.location.DHIS2ImportLocationsStatusService;
import org.opensrp.connector.dhis2.location.DHIS2ImportOrganizationUnits;
import org.opensrp.connector.dhis2.location.DHIS2LocationsImportSummary;
import org.opensrp.connector.dhis2.location.DHISImportLocationsJobStatus;
import org.opensrp.domain.LocationDetail;
import org.opensrp.domain.StructureDetails;
import org.opensrp.search.LocationSearchBean;
import org.opensrp.service.PhysicalLocationService;
import org.opensrp.service.PlanService;
import org.opensrp.web.GlobalExceptionHandler;
import org.opensrp.web.bean.Identifier;
import org.opensrp.web.bean.LocationSearchcBean;
import org.opensrp.web.config.security.filter.CrossSiteScriptingPreventionFilter;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.smartregister.domain.Geometry;
import org.smartregister.domain.Jurisdiction;
import org.smartregister.domain.PhysicalLocation;
import org.smartregister.domain.PlanDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = { "classpath:test-webmvc-config.xml", })
public class LocationResourceTest {

	@Autowired
	protected WebApplicationContext webApplicationContext;

	@Rule
	public MockitoRule rule = MockitoJUnit.rule();

	@Captor
	ArgumentCaptor<List<PhysicalLocation>> locationsArgumentCaptor;

	@Captor
	private ArgumentCaptor<PhysicalLocation> argumentCaptor;

	@Captor
	private ArgumentCaptor<Map<String, String>> mapCaptor;

	@Captor
	private ArgumentCaptor<Boolean> booleanCaptor;

	@Captor
	private ArgumentCaptor<String> stringCaptor;

	@Captor
	private ArgumentCaptor<Integer> integerCaptor;
	
	@Captor
    private ArgumentCaptor<Set<String>> stringSetCaptor;

	@InjectMocks
	private LocationResource locationResource;

	private MockMvc mockMvc;
	
	@Mock
	private PhysicalLocationService locationService;
	
	@Mock
	private DHIS2ImportLocationsStatusService dhis2ImportLocationsStatusService;
	
	@Mock
	private DHIS2ImportOrganizationUnits dhis2ImportOrganizationUnits;
	
	@Mock
	private PlanService planService;

	protected ObjectMapper mapper = new ObjectMapper();
	private String MESSAGE = "The server encountered an error processing the request.";

	private String BASE_URL = "/rest/location/";
	private boolean DEFAULT_RETURN_BOOLEAN = true;

	public static String structureJson = "{\"type\":\"Feature\",\"id\":\"90397\",\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[32.5978597,-14.1699446],[32.5978956,-14.1699609],[32.5978794,-14.1699947],[32.5978434,-14.1699784],[32.5978597,-14.1699446]]]},\"properties\":{\"uid\":\"41587456-b7c8-4c4e-b433-23a786f742fc\",\"code\":\"21384443\",\"type\":\"Residential Structure\",\"status\":\"Active\",\"parentId\":\"3734\",\"geographicLevel\":5,\"effectiveStartDate\":\"2017-01-10T0000\",\"version\":0}}";

	public String parentJson = "{\"type\":\"Feature\",\"id\":\"3734\",\"geometry\":{\"type\":\"MultiPolygon\",\"coordinates\":[[[[32.59989007736522,-14.167432040756012],[32.599899215376524,-14.167521429770147],[32.59990104639621,-14.167611244163538],[32.599895558733124,-14.16770091736339],[32.59989008366051,-14.167739053114758],[32.59993602462787,-14.16775581827636],[32.600019423257834,-14.167793873088726],[32.60009945932262,-14.167838272618155],[32.60017562650404,-14.167888735376776],[32.60024744456398,-14.16794494480331],[32.60031445844555,-14.168006542967532],[32.60037624546743,-14.168073141362354],[32.60043241352514,-14.168144319105009],[32.6004826082858,-14.168219626534494],[32.60052651318807,-14.168298587010156],[32.60056384854283,-14.168380700508862],[32.60059438052627,-14.168465449920518],[32.600617914884936,-14.168552297450503],[32.60062693688366,-14.16860096246438],[32.600629671722004,-14.168608553641775],[32.600653206080665,-14.16869540117176],[32.60066959442628,-14.168783799132996],[32.600678733336906,-14.168873188147074],[32.60068056435659,-14.168963002540465],[32.600675075794186,-14.169052674840998],[32.60066230362247,-14.169141638475764],[32.60064232698187,-14.169229331368513],[32.60061527357607,-14.169315197738115],[32.60058131337695,-14.169398695293522],[32.6005406613225,-14.169479297031899],[32.60049357461884,-14.169556492138383],[32.600440351840795,-14.169629793180375],[32.60038132753629,-14.169698736107762],[32.60031687672256,-14.169762885648595],[32.60024740499381,-14.169821836208712],[32.60017335391722,-14.169875214569458],[32.600095189341566,-14.169922684384344],[32.600073628095515,-14.169933562583822],[32.60007671007219,-14.169963706060116],[32.600078541091875,-14.170053520453562],[32.60007305252941,-14.170143193653415],[32.600060280357695,-14.170232157288183],[32.600040303717094,-14.170319849281555],[32.60001324941197,-14.170405715651212],[32.599979289212854,-14.170489214105885],[32.59993897080693,-14.170569153943234],[32.599943946755786,-14.170617831547645],[32.599945777775474,-14.170707646840356],[32.59994028921301,-14.170797319140943],[32.59992751704135,-14.170886282775712],[32.59990754040069,-14.170973974769083],[32.59988048609562,-14.171059841138685],[32.5998465258965,-14.171143339593414],[32.59980587384205,-14.17122394043247],[32.59975878713834,-14.171301135538954],[32.59970556346104,-14.171374436580948],[32.59964653915648,-14.171443379508332],[32.59958208744342,-14.171507529049165],[32.59951261571467,-14.171566479609282],[32.59943856373877,-14.171619858869349],[32.59936039916312,-14.171667327784915],[32.599278615715605,-14.17170858778104],[32.59919373140565,-14.17174337715511],[32.599106283128485,-14.171771477371749],[32.59901682216866,-14.171792708566613],[32.59892591599828,-14.171806937640042],[32.5988341383848,-14.171814074659776],[32.59874206939105,-14.171814074659776],[32.598650291777574,-14.171806937640042],[32.59855938470787,-14.171792708566613],[32.59846992464736,-14.171771477371749],[32.59838247547094,-14.17174337715511],[32.5983273353383,-14.17172077809141],[32.59831050992216,-14.17173505572822],[32.59823645704688,-14.17178843408891],[32.59815829247128,-14.171835903903796],[32.59807650992303,-14.171877163899921],[32.597991625613076,-14.17191195327399],[32.59790417643665,-14.17194005259131],[32.597814715476765,-14.171961283786173],[32.59772380930639,-14.171975513758923],[32.59763203169297,-14.171982650778657],[32.59753996269916,-14.171982650778657],[32.59744818508574,-14.171975513758923],[32.597357278016034,-14.171961283786173],[32.59726781795547,-14.17194005259131],[32.59718036877905,-14.17191195327399],[32.597125046084045,-14.171889279566528],[32.59711651511515,-14.171899244054828],[32.59705206340203,-14.171963393595718],[32.59698259167334,-14.172022344155835],[32.59690853879806,-14.172075722516524],[32.59683037422246,-14.172123192331412],[32.596748590774894,-14.172164452327593],[32.596669124880236,-14.172197021275451],[32.59666535402294,-14.172199310949395],[32.596583571474696,-14.172240570945519],[32.59649868716474,-14.172275360319588],[32.59641123798832,-14.172303459636908],[32.596321777028436,-14.172324691731092],[32.59623087085805,-14.172338920804519],[32.59613909234531,-14.172346057824257],[32.5960470233515,-14.172346057824257],[32.595955245738025,-14.172338920804519],[32.59586433866832,-14.172324691731092],[32.595803283694636,-14.172310200954938],[32.59573533092083,-14.172367862786585],[32.59566127804561,-14.172421241147333],[32.59558311346996,-14.172468710062901],[32.59551500691208,-14.172503069560946],[32.59545514174147,-14.172546221730727],[32.59537697626649,-14.172593691545558],[32.59529519371824,-14.172634951541738],[32.59521030850897,-14.17266974091575],[32.59512285933255,-14.17269784023307],[32.59503339927198,-14.172719072327254],[32.59494249220228,-14.172733301400683],[32.59485071368954,-14.172740438420476],[32.59475864469573,-14.172740438420476],[32.59466686618299,-14.172733301400683],[32.594575960012605,-14.172719072327254],[32.59448649905272,-14.17269784023307],[32.59445585105675,-14.172687992656677],[32.59442677507565,-14.172690253552332],[32.59433470518257,-14.172690253552332],[32.5942429275691,-14.17268311653254],[32.59415202049939,-14.172668887459167],[32.594062559539566,-14.172647656264303],[32.59397511036309,-14.172619556946927],[32.593890226053134,-14.172584766673594],[32.59380844260562,-14.17254350667747],[32.593730278029966,-14.172496037761903],[32.593656225154746,-14.172442658501836],[32.593586753425996,-14.17238370794172],[32.593567544806376,-14.172364589254355],[32.59351203505241,-14.172317486362884],[32.593447582440035,-14.172253336821994],[32.59338855813547,-14.17218439389461],[32.593335334458175,-14.172111092852617],[32.59328824685514,-14.172033897746132],[32.59324759480069,-14.171953296907077],[32.593213634601625,-14.171869799351725],[32.5931865802965,-14.171783932082747],[32.5931666036559,-14.171696240089375],[32.59315383148419,-14.171607276454608],[32.59314834292172,-14.171517604154076],[32.5931501739414,-14.171427788861308],[32.59315931285203,-14.17133840074655],[32.59317570119771,-14.17125000278537],[32.59319923555631,-14.171163155255385],[32.59322976753981,-14.17107840584373],[32.593267104693155,-14.170996292344967],[32.59331100869616,-14.170917331869305],[32.59336120435614,-14.170842024439821],[32.59341737331317,-14.170770846697167],[32.59347916123437,-14.170704248302341],[32.59354617601525,-14.17064265013812],[32.593617994075196,-14.170586440711643],[32.593694163055204,-14.170535977952964],[32.59377420001937,-14.170491578423535],[32.593857599548635,-14.170453523611172],[32.59394383374098,-14.170422054534129],[32.594032357607254,-14.170397369942634],[32.594122611769194,-14.170379625419342],[32.594214026056534,-14.170368934278827],[32.5943060213059,-14.170365362171708],[32.59439801655525,-14.170368934278827],[32.594402085987554,-14.170369410020214],[32.59439156302028,-14.170336010998085],[32.594371587279,-14.170248319004656],[32.594358814207965,-14.17015935536989],[32.59435618369099,-14.170116383963887],[32.594347703084054,-14.170089466355762],[32.59432772734277,-14.170001773463014],[32.594314954271795,-14.169912809828245],[32.59430946660865,-14.169823137527715],[32.59431129672901,-14.169733323134324],[32.594320435639645,-14.169643934120245],[32.594336823985316,-14.16955553615901],[32.59436035834392,-14.169468688629022],[32.59439089032736,-14.169383940116688],[32.59442822658144,-14.16930182571866],[32.59447213148371,-14.169222865243],[32.59452232624443,-14.169147557813517],[32.59457849520146,-14.169076380070862],[32.59464028132396,-14.169009781676039],[32.5947072961049,-14.168948183511816],[32.59477911416485,-14.168891974984604],[32.59485528224553,-14.168841511326661],[32.59493531831032,-14.168797111797232],[32.595018716940274,-14.168759056984868],[32.59510495113261,-14.168727587907824],[32.595193474099574,-14.168702903316273],[32.5952837282615,-14.168685158792982],[32.59537514075021,-14.168674467652522],[32.59545842696491,-14.168671234589736],[32.5954623282239,-14.168656837343121],[32.5954928602074,-14.168572088830786],[32.595530195562105,-14.168489974432703],[32.59557410046443,-14.168411013957098],[32.595624295225086,-14.168335707426932],[32.595680463282804,-14.168264529684278],[32.59574225030468,-14.168197930390136],[32.595809264186244,-14.168136332225913],[32.59588108224619,-14.1680801236987],[32.59595725032687,-14.168029660040759],[32.596033266422175,-14.16798749083],[32.59606430112666,-14.167931676205853],[32.59611449588732,-14.16785636877637],[32.59617066394503,-14.167785191033715],[32.59623245006759,-14.167718592638892],[32.59629946394915,-14.167656993575347],[32.596371282009095,-14.167600785048137],[32.596447450089784,-14.167550322289514],[32.59652748525531,-14.167505922760085],[32.59661088388526,-14.167467867947721],[32.59669711717828,-14.167436398870677],[32.59678564014524,-14.167411714279126],[32.59687589340779,-14.167393969755835],[32.5969673058965,-14.167383278615375],[32.59705930024654,-14.1673797065082],[32.597151294596635,-14.167383278615375],[32.59724270708534,-14.167393969755835],[32.5973329603479,-14.167411714279126],[32.59742148331486,-14.167436398870677],[32.59746305357709,-14.16745156953425],[32.59747073828402,-14.167423210312847],[32.59750126936814,-14.167338461800512],[32.597508451353974,-14.167322665208815],[32.59752348801862,-14.167280928571927],[32.59752577679319,-14.16727248213931],[32.59755630787737,-14.167187732727655],[32.59759364413145,-14.16710561832957],[32.5976375481344,-14.16702665875323],[32.59768774289506,-14.166951351323744],[32.59774391095277,-14.16688017358109],[32.59780569707533,-14.166813574287005],[32.59787271005757,-14.166751976122782],[32.59794452811752,-14.16669576759557],[32.598020695298885,-14.166645303937571],[32.59810073136373,-14.166600904408142],[32.59818412909431,-14.166562850495096],[32.598270361488055,-14.166531381418054],[32.598358884455024,-14.166506696826557],[32.59844913681826,-14.166488952303268],[32.59852580672065,-14.166479984263844],[32.598559981857704,-14.166473265428806],[32.598651394346405,-14.166462574288346],[32.598743387797185,-14.166459002181169],[32.59883538214722,-14.166462574288346],[32.59892679373661,-14.166473265428806],[32.599017046999165,-14.166491009952097],[32.59910556996613,-14.166515694543648],[32.599191802359826,-14.166547163620635],[32.59927520009046,-14.166585218433056],[32.599355236155304,-14.166629617962426],[32.599431403336666,-14.166680081620425],[32.5995032204973,-14.16673629014764],[32.59957023437886,-14.166797888311862],[32.59963202050142,-14.16686448670663],[32.59968818855913,-14.166935664449284],[32.59973838331979,-14.167010971878824],[32.59978228732274,-14.167089932354429],[32.5998196226775,-14.167172046752512],[32.59985015466094,-14.167256795264848],[32.5998736890196,-14.167343642794833],[32.59989007736522,-14.167432040756012]]]]},\"properties\":{\"uid\":\"41587456-b7c8-4c4e-b433-23a786f742fc\",\"code\":\"3734\",\"type\":\"Intervention Unit\",\"status\":\"Active\",\"parentId\":\"21\",\"name\":\"01_5\",\"geographicLevel\":4,\"effectiveStartDate\":\"2015-01-01T0000\",\"version\":0}}";
	
	public String searchResponseJson = "{\"locations\":[{\"type\":\"Feature\",\"id\":\"7\",\"properties\":{\"uid\":\"694e0f30-d9ac-4576-b50a-38a3ef13ebcr\",\"type\":\"Residential Structure\",\"status\":\"PENDING_REVIEW\",\"parentId\":\"22\",\"name\":\"Faridpur\",\"geographicLevel\":0,\"effectiveStartDate\":\"Jan 1, 2015, 12:00:00 AM\",\"version\":0,\"customProperties\":{}},\"serverVersion\":1586160969806,\"locationTags\":[{\"id\":3,\"active\":true,\"name\":\"District\",\"description\":\"District\"}],\"customProperties\":{\"parent\":\"Dhaka\",\"tag_name\":\"District\",\"name\":\"Faridpur\"},\"jurisdiction\":false}],\"total\":0}";

	public String locationTree = "{\"locationsHierarchy\":{\"map\":{\"1\":{\"id\":\"1\",\"label\":\"Kenya\",\"node\":{\"locationId\":\"1\",\"name\":\"Kenya\",\"tags\":[\"Country\"],\"voided\":false},\"children\":{\"2\":{\"id\":\"2\",\"label\":\"Coast\",\"node\":{\"locationId\":\"2\",\"name\":\"Coast\",\"parentLocation\":{\"locationId\":\"1\",\"voided\":false},\"tags\":[\"Province\"],\"voided\":false},\"parent\":\"1\"}}}},\"parentChildren\":{\"1\":[\"2\"]}}}";

	private final String DHIS_IMPORT_JOB_STATUS_END_POINT = "/rest/location/dhis2/status";

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(locationResource)
				.setControllerAdvice(new GlobalExceptionHandler()).
						addFilter(new CrossSiteScriptingPreventionFilter(), "/*").
						build();
	}

	@Test
	public void testGetLocationByUniqueId() throws Exception {
		when(locationService.getLocation("3734", DEFAULT_RETURN_BOOLEAN, false)).thenReturn(createLocation());

		MvcResult result = mockMvc
				.perform(get(BASE_URL + "/{id}", "3734").param(LocationResource.IS_JURISDICTION, "true"))
				.andExpect(status().isOk()).andReturn();
		verify(locationService).getLocation("3734", DEFAULT_RETURN_BOOLEAN, false);
		verifyNoMoreInteractions(locationService);
		assertEquals(parentJson, result.getResponse().getContentAsString());

	}

	@Test
	public void testGetLocationByUniqueIdShouldReturnServerError() throws Exception {
		when(locationService.getLocation("3734", DEFAULT_RETURN_BOOLEAN,false)).thenThrow(new RuntimeException());
		mockMvc.perform(get(BASE_URL + "/{id}", "3734").param(LocationResource.IS_JURISDICTION, "true"))
				.andExpect(status().isInternalServerError());
		verify(locationService).getLocation("3734", DEFAULT_RETURN_BOOLEAN, false);
		verifyNoMoreInteractions(locationService);

	}

	@Test
	public void testGetStructureByUniqueId() throws Exception {
		when(locationService.getStructure("90397", DEFAULT_RETURN_BOOLEAN)).thenReturn(createStructure());

		MvcResult result = mockMvc
				.perform(get(BASE_URL + "/{id}", "90397").param(LocationResource.IS_JURISDICTION, "false"))
				.andExpect(status().isOk()).andReturn();
		verify(locationService).getStructure("90397", DEFAULT_RETURN_BOOLEAN);
		verifyNoMoreInteractions(locationService);
		assertEquals(structureJson, result.getResponse().getContentAsString());

	}

	@Test
	public void testGetStructureByUniqueIdShouldReturnServerError() throws Exception {
		when(locationService.getStructure("90397", DEFAULT_RETURN_BOOLEAN)).thenThrow(new RuntimeException());
		mockMvc.perform(get(BASE_URL + "/{id}", "90397").param(LocationResource.IS_JURISDICTION, "false"))
				.andExpect(status().isInternalServerError()).andReturn();
		verify(locationService).getStructure("90397", DEFAULT_RETURN_BOOLEAN);
		verifyNoMoreInteractions(locationService);

	}

	@Test
	public void testSyncLocationsByServerVersions() throws Exception {
		List<PhysicalLocation> expected = new ArrayList<>();
		expected.add(createLocation());
		when(locationService.findLocationsByServerVersion(1542640316113l)).thenReturn(expected);

		MvcResult result = mockMvc.perform(post(BASE_URL + "/sync").contentType(MediaType.APPLICATION_JSON)
				.content("{\"serverVersion\":\"1542640316113\", \"is_jurisdiction\":\"true\"}".getBytes()))
				.andExpect(status().isOk()).andReturn();
		verify(locationService).findLocationsByServerVersion(1542640316113l);
		verifyNoMoreInteractions(locationService);

		JSONArray jsonreponse = new JSONArray(result.getResponse().getContentAsString());
		assertEquals(1, jsonreponse.length());
		JSONAssert.assertEquals(parentJson, jsonreponse.get(0).toString(), JSONCompareMode.STRICT_ORDER);
	}

	@Test
	public void testGetSyncLocationsByServerVersions() throws Exception {
		List<PhysicalLocation> expected = new ArrayList<>();
		expected.add(createLocation());
		when(locationService.findLocationsByServerVersion(1542640316113l)).thenReturn(expected);

		MvcResult result = mockMvc.perform(get(BASE_URL + "/sync").param(BaseEntity.SERVER_VERSIOIN, "1542640316113")
				.param(LocationResource.IS_JURISDICTION, "true")).andExpect(status().isOk()).andReturn();
		verify(locationService).findLocationsByServerVersion(1542640316113l);
		verifyNoMoreInteractions(locationService);

		JSONArray jsonreponse = new JSONArray(result.getResponse().getContentAsString());
		assertEquals(1, jsonreponse.length());
		JSONAssert.assertEquals(parentJson, jsonreponse.get(0).toString(), JSONCompareMode.STRICT_ORDER);
	}

	@Test
	public void testsSyncLocationsByNames() throws Exception {

		String locationNames = "01_5";
		List<PhysicalLocation> expected = new ArrayList<>();
		expected.add(createLocation());
		when(locationService.findLocationsByNames(locationNames, 0l)).thenReturn(expected);
		MvcResult result = mockMvc.perform(post(BASE_URL + "/sync").contentType(MediaType.APPLICATION_JSON)
				.content(("{\"serverVersion\":0,\"is_jurisdiction\":\"true\", \"location_names\":[\"" + locationNames +"\"]}").getBytes()))
				.andExpect(status().isOk()).andReturn();
		verify(locationService).findLocationsByNames(locationNames, 0l);
		verifyNoMoreInteractions(locationService);

		JSONArray jsonResponse = new JSONArray(result.getResponse().getContentAsString());
		assertEquals(1, jsonResponse.length());
		PhysicalLocation location = LocationResource.gson.fromJson(jsonResponse.get(0).toString(),
				PhysicalLocation.class);

		assertEquals("01_5", location.getProperties().getName());
		assertEquals("Feature", location.getType());
		assertEquals("3734", location.getId());
		assertEquals(Geometry.GeometryType.MULTI_POLYGON, location.getGeometry().getType());

//		search with more than one name
		locationNames = "01_5,other_location_name";
		expected = new ArrayList<>();
		expected.add(createLocation());
		when(locationService.findLocationsByNames(locationNames, 0l)).thenReturn(expected);
		result = mockMvc.perform(post(BASE_URL + "/sync").contentType(MediaType.APPLICATION_JSON)
				.content(("{\"serverVersion\":\"0\",\"is_jurisdiction\":\"true\", \"location_names\":[\"" + locationNames + "\"]}").getBytes()))
				.andExpect(status().isOk()).andReturn();
		verify(locationService).findLocationsByNames(locationNames, 0l);
		verifyNoMoreInteractions(locationService);

		jsonResponse = new JSONArray(result.getResponse().getContentAsString());
		assertEquals(1, jsonResponse.length());
		location = LocationResource.gson.fromJson(jsonResponse.get(0).toString(), PhysicalLocation.class);

		assertEquals("01_5", location.getProperties().getName());
		assertEquals("Feature", location.getType());
		assertEquals("3734", location.getId());
		assertEquals(Geometry.GeometryType.MULTI_POLYGON, location.getGeometry().getType());

	}

	@Test
	public void testsGetSyncLocationsByNames() throws Exception {

		String locationNames = "01_5";
		List<PhysicalLocation> expected = new ArrayList<>();
		expected.add(createLocation());
		when(locationService.findLocationsByNames(locationNames, 0l)).thenReturn(expected);
		MvcResult result = mockMvc.perform(get(BASE_URL + "/sync").param(BaseEntity.SERVER_VERSIOIN, "0")
				.param(LocationResource.IS_JURISDICTION, "true").param(LocationResource.LOCATION_NAMES, locationNames))
				.andExpect(status().isOk()).andReturn();
		verify(locationService).findLocationsByNames(locationNames, 0l);
		verifyNoMoreInteractions(locationService);

		JSONArray jsonResponse = new JSONArray(result.getResponse().getContentAsString());
		assertEquals(1, jsonResponse.length());
		PhysicalLocation location = LocationResource.gson.fromJson(jsonResponse.get(0).toString(),
				PhysicalLocation.class);

		assertEquals("01_5", location.getProperties().getName());
		assertEquals("Feature", location.getType());
		assertEquals("3734", location.getId());
		assertEquals(Geometry.GeometryType.MULTI_POLYGON, location.getGeometry().getType());

//		search with more than one name
		locationNames = "01_5,other_location_name";
		expected = new ArrayList<>();
		expected.add(createLocation());
		when(locationService.findLocationsByNames(locationNames, 0l)).thenReturn(expected);
		result = mockMvc.perform(get(BASE_URL + "/sync").param(BaseEntity.SERVER_VERSIOIN, "0")
				.param(LocationResource.IS_JURISDICTION, "true").param(LocationResource.LOCATION_NAMES, locationNames))
				.andExpect(status().isOk()).andReturn();
		verify(locationService).findLocationsByNames(locationNames, 0l);
		verifyNoMoreInteractions(locationService);

		jsonResponse = new JSONArray(result.getResponse().getContentAsString());
		assertEquals(1, jsonResponse.length());
		location = LocationResource.gson.fromJson(jsonResponse.get(0).toString(), PhysicalLocation.class);

		assertEquals("01_5", location.getProperties().getName());
		assertEquals("Feature", location.getType());
		assertEquals("3734", location.getId());
		assertEquals(Geometry.GeometryType.MULTI_POLYGON, location.getGeometry().getType());
	}

	@Test
	public void testSyncLocationsByInvalidServerVersionsShouldReturnAllServerVersions() throws Exception {
		List<PhysicalLocation> expected = new ArrayList<>();
		expected.add(createLocation());
		when(locationService.findLocationsByServerVersion(0l)).thenReturn(expected);

		MvcResult result = mockMvc.perform(post(BASE_URL + "/sync").contentType(MediaType.APPLICATION_JSON)
				.content("{\"serverVersion\": 0, \"is_jurisdiction\":\"true\"}".getBytes())).andExpect(status().isOk()).andReturn();
		verify(locationService).findLocationsByServerVersion(0l);
		verifyNoMoreInteractions(locationService);

		JSONArray jsonreponse = new JSONArray(result.getResponse().getContentAsString());
		assertEquals(1, jsonreponse.length());
		JSONAssert.assertEquals(parentJson, jsonreponse.get(0).toString(), JSONCompareMode.STRICT_ORDER);
	}

	@Test
	public void testGetSyncLocationsByInvalidServerVersionsShouldReturnAllServerVersions() throws Exception {
		List<PhysicalLocation> expected = new ArrayList<>();
		expected.add(createLocation());
		when(locationService.findLocationsByServerVersion(0l)).thenReturn(expected);

		MvcResult result = mockMvc.perform(get(BASE_URL + "/sync").param(BaseEntity.SERVER_VERSIOIN, "dfgdf")
				.param(LocationResource.IS_JURISDICTION, "true")).andExpect(status().isOk()).andReturn();
		verify(locationService).findLocationsByServerVersion(0l);
		verifyNoMoreInteractions(locationService);

		JSONArray jsonreponse = new JSONArray(result.getResponse().getContentAsString());
		assertEquals(1, jsonreponse.length());
		JSONAssert.assertEquals(parentJson, jsonreponse.get(0).toString(), JSONCompareMode.STRICT_ORDER);
	}

	@Test
	public void testSyncLocationsShouldReturmServerError() throws Exception {

		when(locationService.findLocationsByServerVersion(0l)).thenThrow(new RuntimeException());

		mockMvc.perform(post(BASE_URL + "/sync").contentType(MediaType.APPLICATION_JSON).
				content("{\"serverVersion\":\"0\", \"is_jurisdiction\": \"true\"}".getBytes())).andExpect(status().isInternalServerError());
		verify(locationService).findLocationsByServerVersion(0l);
		verifyNoMoreInteractions(locationService);

	}

	@Test
	public void testGetSyncLocationsShouldReturmServerError() throws Exception {

		when(locationService.findLocationsByServerVersion(0l)).thenThrow(new RuntimeException());

		mockMvc.perform(get(BASE_URL + "/sync").param(BaseEntity.SERVER_VERSIOIN, "0")
				.param(LocationResource.IS_JURISDICTION, "true")).andExpect(status().isInternalServerError());
		verify(locationService).findLocationsByServerVersion(0l);
		verifyNoMoreInteractions(locationService);

	}

	@Test
	public void testSyncStructuresByParentIdAndServerVersion() throws Exception {
		List<PhysicalLocation> expected = new ArrayList<>();
		expected.add(createLocation());
		when(locationService.findStructuresByParentAndServerVersion("3734", 1542640316l)).thenReturn(expected);

		MvcResult result = mockMvc.perform(post(BASE_URL + "/sync").contentType(MediaType.APPLICATION_JSON)
				.content("{\"serverVersion\":1542640316,\"parent_id\":[\"3734\"], \"is_jurisdiction\":\"false\"}".getBytes())).andExpect(status().isOk()).andReturn();
		verify(locationService).findStructuresByParentAndServerVersion("3734", 1542640316l);
		verifyNoMoreInteractions(locationService);

		JSONArray jsonreponse = new JSONArray(result.getResponse().getContentAsString());
		assertEquals(1, jsonreponse.length());
		JSONAssert.assertEquals(parentJson, jsonreponse.get(0).toString(), JSONCompareMode.STRICT_ORDER);

		when(locationService.findStructuresByParentAndServerVersion("3734,001", 1542640316l)).thenReturn(expected);
		result = mockMvc.perform(post(BASE_URL + "/sync").contentType(MediaType.APPLICATION_JSON)
				.content("{\"serverVersion\":1542640316,\"parent_id\":[\"3734,001\"], \"is_jurisdiction\":\"false\"}".getBytes())).andExpect(status().isOk()).andReturn();
		verify(locationService).findStructuresByParentAndServerVersion("3734,001", 1542640316l);
		verifyNoMoreInteractions(locationService);

		jsonreponse = new JSONArray(result.getResponse().getContentAsString());
		assertEquals(1, jsonreponse.length());
		JSONAssert.assertEquals(parentJson, jsonreponse.get(0).toString(), JSONCompareMode.STRICT_ORDER);

	}

	@Test
	public void testGetSyncStructuresByParentIdAndServerVersion() throws Exception {
		List<PhysicalLocation> expected = new ArrayList<>();
		expected.add(createLocation());
		when(locationService.findStructuresByParentAndServerVersion("3734", 1542640316l)).thenReturn(expected);

		MvcResult result = mockMvc.perform(get(BASE_URL + "/sync").param(BaseEntity.SERVER_VERSIOIN, "1542640316")
				.param(LocationResource.PARENT_ID, "3734")).andExpect(status().isOk()).andReturn();
		verify(locationService).findStructuresByParentAndServerVersion("3734", 1542640316l);
		verifyNoMoreInteractions(locationService);

		JSONArray jsonreponse = new JSONArray(result.getResponse().getContentAsString());
		assertEquals(1, jsonreponse.length());
		JSONAssert.assertEquals(parentJson, jsonreponse.get(0).toString(), JSONCompareMode.STRICT_ORDER);

		when(locationService.findStructuresByParentAndServerVersion("3734,001", 1542640316l)).thenReturn(expected);
		result = mockMvc.perform(get(BASE_URL + "/sync").param(BaseEntity.SERVER_VERSIOIN, "1542640316")
				.param(LocationResource.PARENT_ID, "3734,001")).andExpect(status().isOk()).andReturn();
		verify(locationService).findStructuresByParentAndServerVersion("3734,001", 1542640316l);
		verifyNoMoreInteractions(locationService);

		jsonreponse = new JSONArray(result.getResponse().getContentAsString());
		assertEquals(1, jsonreponse.length());
		JSONAssert.assertEquals(parentJson, jsonreponse.get(0).toString(), JSONCompareMode.STRICT_ORDER);

	}

	@Test
	public void testSyncStructuresByServerVersionsWithoutServerVersion() throws Exception {

		when(locationService.findStructuresByParentAndServerVersion(null, 1542640316l))
				.thenThrow(new IllegalArgumentException());
		MvcResult result = mockMvc.perform(post(BASE_URL + "/sync").contentType(MediaType.APPLICATION_JSON).
				content("{\"serverVersion\", \"1542640316\"}".getBytes()))
				.andExpect(status().isBadRequest()).andReturn();
		verify(locationService, never()).findStructuresByParentAndServerVersion(anyString(), anyLong());
		verifyNoMoreInteractions(locationService);

		String responseString = result.getResponse().getContentAsString();
		if (responseString.isEmpty()) {
			fail("Test case failed");
		}
		JsonNode actualObj = mapper.readTree(responseString);
		assertTrue(actualObj.get("message").asText().isEmpty());

	}

	@Test
	public void testGetSyncStructuresByServerVersionsWithoutServerVersion() throws Exception {

		when(locationService.findStructuresByParentAndServerVersion(null, 1542640316l))
				.thenThrow(new IllegalArgumentException());
		MvcResult result = mockMvc.perform(get(BASE_URL + "/sync").param(BaseEntity.SERVER_VERSIOIN, "1542640316"))
				.andExpect(status().isBadRequest()).andReturn();
		verify(locationService, never()).findStructuresByParentAndServerVersion(anyString(), anyLong());
		verifyNoMoreInteractions(locationService);

		assertTrue(result.getResponse().getContentAsString().isEmpty());

	}

	@Test
	public void testCreateLocation() throws Exception {
		mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(parentJson.getBytes()))
				.andExpect(status().isCreated());
		verify(locationService).add(argumentCaptor.capture());
		verifyNoMoreInteractions(locationService);
		assertEquals(parentJson, LocationResource.gson.toJson(argumentCaptor.getValue()));
	}

	@Test
	public void testCreateLocationShouldReturnServerError() throws Exception {
		doThrow(new RuntimeException()).when(locationService).add(any(PhysicalLocation.class));
		mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(parentJson.getBytes()))
				.andExpect(status().isInternalServerError());
		verify(locationService).add(argumentCaptor.capture());
		verifyNoMoreInteractions(locationService);
	}

	@Test
	public void testCreateLocationWithInvalidJson() throws Exception {
		mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
				.param(LocationResource.IS_JURISDICTION, "true").content(parentJson.substring(4).getBytes()))
				.andExpect(status().isBadRequest());
		verify(locationService, never()).add(argumentCaptor.capture());
		verifyNoMoreInteractions(locationService);
	}

	@Test
	public void testCreateStructure() throws Exception {
		mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
				.param(LocationResource.IS_JURISDICTION, "false").content(structureJson.getBytes()))
				.andExpect(status().isCreated());
		verify(locationService).add(argumentCaptor.capture());
		verifyNoMoreInteractions(locationService);
		assertFalse(argumentCaptor.getValue().isJurisdiction());
		assertEquals(structureJson, LocationResource.gson.toJson(argumentCaptor.getValue()));
	}

	@Test
	public void testCreateStructureWithInvalidJson() throws Exception {
		mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(parentJson.substring(8).getBytes()))
				.andExpect(status().isBadRequest());
		verify(locationService, never()).add(argumentCaptor.capture());
		verifyNoMoreInteractions(locationService);
	}

	@Test
	public void testUpdateLocation() throws Exception {
		mockMvc.perform(put(BASE_URL).contentType(MediaType.APPLICATION_JSON)
				.param(LocationResource.IS_JURISDICTION, "true").content(parentJson.getBytes()))
				.andExpect(status().isCreated());
		verify(locationService).update(argumentCaptor.capture());
		verifyNoMoreInteractions(locationService);
		assertTrue(argumentCaptor.getValue().isJurisdiction());
		assertEquals(parentJson, LocationResource.gson.toJson(argumentCaptor.getValue()));
	}

	@Test
	public void testUpdateLocationWithInvalidJson() throws Exception {
		mockMvc.perform(put(BASE_URL).contentType(MediaType.APPLICATION_JSON)
				.param(LocationResource.IS_JURISDICTION, "true").content(parentJson.substring(6).getBytes()))
				.andExpect(status().isBadRequest());
		verify(locationService, never()).update(argumentCaptor.capture());
		verifyNoMoreInteractions(locationService);
	}

	@Test
	public void testUpdateLocationShouldReturnServerError() throws Exception {
		doThrow(new RuntimeException()).when(locationService).update(any(PhysicalLocation.class));
		mockMvc.perform(put(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(parentJson.getBytes()))
				.andExpect(status().isInternalServerError());
		verify(locationService).update(argumentCaptor.capture());
		verifyNoMoreInteractions(locationService);
	}

	@Test
	public void testUpdateStructure() throws Exception {
		mockMvc.perform(put(BASE_URL).contentType(MediaType.APPLICATION_JSON)
				.param(LocationResource.IS_JURISDICTION, "false").content(structureJson.getBytes()))
				.andExpect(status().isCreated());
		verify(locationService).update(argumentCaptor.capture());
		verifyNoMoreInteractions(locationService);
		assertFalse(argumentCaptor.getValue().isJurisdiction());
		assertEquals(structureJson, LocationResource.gson.toJson(argumentCaptor.getValue()));
	}

	@Test
	public void testUpdateStructureWithInvalidJson() throws Exception {
		mockMvc.perform(put(BASE_URL).contentType(MediaType.APPLICATION_JSON)
				.param(LocationResource.IS_JURISDICTION, "false").content(parentJson.substring(8).getBytes()))
				.andExpect(status().isBadRequest());
		verify(locationService, never()).update(argumentCaptor.capture());
		verifyNoMoreInteractions(locationService);
	}

	@Test
	public void saveBatch() throws Exception {
		List<PhysicalLocation> locations = new ArrayList<>();
		locations.add(createLocation());
		locations.add(createStructure());
		String request = LocationResource.gson.toJson(locations);
		mockMvc.perform(post(BASE_URL + "add").contentType(MediaType.APPLICATION_JSON)
				.param(LocationResource.IS_JURISDICTION, "true").content(request.getBytes()))
				.andExpect(status().isCreated());

		ArgumentCaptor<Boolean> isParentArgumentCaptor = ArgumentCaptor.forClass(Boolean.class);
		verify(locationService).saveLocations(locationsArgumentCaptor.capture(), isParentArgumentCaptor.capture());
		verifyNoMoreInteractions(locationService);

		assertTrue(isParentArgumentCaptor.getValue());
		assertEquals(2, locationsArgumentCaptor.getValue().size());
		assertEquals(parentJson, LocationResource.gson.toJson(locationsArgumentCaptor.getValue().get(0)));
		assertEquals(structureJson, LocationResource.gson.toJson(locationsArgumentCaptor.getValue().get(1)));

	}

	@Test
	public void saveBatchWithInvalidJson() throws Exception {
		mockMvc.perform(post(BASE_URL + "add").contentType(MediaType.APPLICATION_JSON)
				.param(LocationResource.IS_JURISDICTION, "true").content(structureJson.getBytes()))
				.andExpect(status().isBadRequest());

		ArgumentCaptor<Boolean> isParentArgumentCaptor = ArgumentCaptor.forClass(Boolean.class);
		verify(locationService, never()).saveLocations(locationsArgumentCaptor.capture(),
				isParentArgumentCaptor.capture());
		verifyNoMoreInteractions(locationService);

	}

	@Test
	public void saveBatchShouldReturnServerError() throws Exception {

		List<PhysicalLocation> locations = new ArrayList<>();
		locations.add(createLocation());
		locations.add(createStructure());
		String request = LocationResource.gson.toJson(locations);

		doThrow(new RuntimeException()).when(locationService).saveLocations(anyList(), anyBoolean());
		mockMvc.perform(post(BASE_URL + "add").contentType(MediaType.APPLICATION_JSON)
				.param(LocationResource.IS_JURISDICTION, "true").content(request.getBytes()))
				.andExpect(status().isInternalServerError());

		verify(locationService).saveLocations(anyList(), anyBoolean());
		verifyNoMoreInteractions(locationService);

	}

	@Test
	public void testGetStructuresWithinCordinatesWithoutAllParamsReturns400() throws Exception {
		Collection<StructureDetails> expectedDetails = new ArrayList<>();
		StructureDetails structure = new StructureDetails(UUID.randomUUID().toString(), "3221", "Mosquito Point");
		expectedDetails.add(structure);
		double latitude = -14.1619809;
		double longitude = 32.5978597;
		when(locationService.findStructuresWithinRadius(latitude, longitude, 100)).thenReturn(expectedDetails);

		MvcResult result = mockMvc
				.perform(get(BASE_URL + "findWithCordinates").param(LocationResource.LATITUDE, latitude + "")
						.param(LocationResource.LONGITUDE, longitude + "").content(structureJson.getBytes()))
				.andExpect(status().isBadRequest()).andReturn();

		verify(locationService, never()).findStructuresWithinRadius(latitude, longitude, 100);
		assertNotNull(result);

		String responseString = result.getResponse().getContentAsString();
		if (responseString.isEmpty()) {
			fail("Test case failed");
		}
		JsonNode actualObj = mapper.readTree(responseString);
		assertTrue(actualObj.get("message").asText().isEmpty());

	}

	@Test
	public void testGetStructuresWithinCordinates() throws Exception {
		Collection<StructureDetails> expectedDetails = new ArrayList<>();
		StructureDetails structure = new StructureDetails(UUID.randomUUID().toString(), "3221", "Mosquito Point");
		expectedDetails.add(structure);
		double latitude = -14.1619809;
		double longitude = 32.5978597;
		double radius = 100;
		when(locationService.findStructuresWithinRadius(latitude, longitude, radius)).thenReturn(expectedDetails);

		MvcResult result = mockMvc
				.perform(get(BASE_URL + "/findWithCordinates").param(LocationResource.LATITUDE, latitude + "")
						.param(LocationResource.LONGITUDE, longitude + "").param(LocationResource.RADIUS, radius + ""))
				.andExpect(status().isOk()).andReturn();
		assertEquals(LocationResource.gson.toJson(expectedDetails), result.getResponse().getContentAsString());
		verify(locationService).findStructuresWithinRadius(latitude, longitude, radius);

	}

	@Test
	public void testFindByLocationPropertiesWithoutParamsQueriesStructures() throws Exception {
		List<PhysicalLocation> locations = Collections.singletonList(createStructure());
		locations.get(0).setGeometry(null);
		when(locationService.findStructuresByProperties(false, null, null)).thenReturn(locations);
		MvcResult result = mockMvc.perform(get(BASE_URL + "/findByProperties")).andExpect(status().isOk()).andReturn();
		verify(locationService).findStructuresByProperties(false, null, null);
		assertEquals(LocationResource.gson.toJson(locations), result.getResponse().getContentAsString());

	}

	@Test
	public void testFindByLocationPropertiesWithIsJuridictionsRetunsLocations() throws Exception {
		List<PhysicalLocation> locations = Collections.singletonList(createLocation());
		locations.get(0).setGeometry(null);
		when(locationService.findLocationsByProperties(false, null, null)).thenReturn(locations);
		MvcResult result = mockMvc
				.perform(get(BASE_URL + "/findByProperties").param(LocationResource.IS_JURISDICTION, "true"))
				.andExpect(status().isOk()).andReturn();
		verify(locationService).findLocationsByProperties(false, null, null);
		assertEquals(LocationResource.gson.toJson(locations), result.getResponse().getContentAsString());

	}

	@Test
	public void testFindByLocationPropertiesReturnsGeometry() throws Exception {
		List<PhysicalLocation> locations = Collections.singletonList(createLocation());
		when(locationService.findLocationsByProperties(true, null, null)).thenReturn(locations);
		MvcResult result = mockMvc.perform(get(BASE_URL + "/findByProperties")
				.param(LocationResource.IS_JURISDICTION, "true").param(LocationResource.RETURN_GEOMETRY, "true"))
				.andExpect(status().isOk()).andReturn();
		verify(locationService).findLocationsByProperties(true, null, null);
		assertEquals(LocationResource.gson.toJson(locations), result.getResponse().getContentAsString());

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFindByLocationPropertiesParamsPassedCorrectly() throws Exception {
		List<PhysicalLocation> locations = Collections.singletonList(createLocation());
		when(locationService.findLocationsByProperties(anyBoolean(), anyString(), any(Map.class)))
				.thenReturn(locations);
		MvcResult result = mockMvc
				.perform(get(BASE_URL + "/findByProperties").param(LocationResource.IS_JURISDICTION, "true")
						.param(LocationResource.RETURN_GEOMETRY, "true")
						.param(LocationResource.PROPERTIES_FILTER, "name:H123,type:Residential,parentId:1234"))
				.andExpect(status().isOk()).andReturn();
		verify(locationService).findLocationsByProperties(booleanCaptor.capture(), stringCaptor.capture(),
				mapCaptor.capture());
		assertEquals(LocationResource.gson.toJson(locations), result.getResponse().getContentAsString());
		assertTrue(booleanCaptor.getValue());
		assertEquals("1234", stringCaptor.getValue());
		Map<String, String> properties = mapCaptor.getValue();
		assertEquals(2, properties.size());
		assertEquals("H123", properties.get("name"));
		assertEquals("Residential", properties.get("type"));
		assertNull(properties.get("parentId"));

	}

	@Test
	public void testFindByLocationPropertiesByNullParentIdString() throws Exception {
		List<PhysicalLocation> locations = Collections.singletonList(createLocation());
		when(locationService.findLocationsByProperties(anyBoolean(), anyString(), any(Map.class)))
				.thenReturn(locations);
		MvcResult result = mockMvc
				.perform(get(BASE_URL + "/findByProperties").param(LocationResource.IS_JURISDICTION, "true")
						.param(LocationResource.RETURN_GEOMETRY, "true")
						.param(LocationResource.PROPERTIES_FILTER, "parentId:null"))
				.andExpect(status().isOk()).andReturn();
		verify(locationService).findLocationsByProperties(booleanCaptor.capture(), stringCaptor.capture(), mapCaptor.capture());
		assertEquals(LocationResource.gson.toJson(locations), result.getResponse().getContentAsString());
		assertTrue(booleanCaptor.getValue());
		assertEquals("", stringCaptor.getValue());
	}
	@Test
	public void testFindByLocationPropertiesByDoubleQuotesParentIdString() throws Exception {
		List<PhysicalLocation> locations = Collections.singletonList(createLocation());
		when(locationService.findLocationsByProperties(anyBoolean(), anyString(), any(Map.class)))
				.thenReturn(locations);
		MvcResult result = mockMvc
				.perform(get(BASE_URL + "/findByProperties").param(LocationResource.IS_JURISDICTION, "true")
						.param(LocationResource.RETURN_GEOMETRY, "false")
						.param(LocationResource.PROPERTIES_FILTER, "status:Active,parentId:\"\""))
				.andExpect(status().isOk()).andReturn();
		verify(locationService).findLocationsByProperties(booleanCaptor.capture(), stringCaptor.capture(), mapCaptor.capture());
		assertEquals(LocationResource.gson.toJson(locations), result.getResponse().getContentAsString());
		assertFalse(booleanCaptor.getValue());
		assertEquals("", stringCaptor.getValue());
	}
	@Test
	public void testFindByLocationPropertiesByEmptyParentIdString() throws Exception {
		List<PhysicalLocation> locations = Collections.singletonList(createLocation());
		when(locationService.findLocationsByProperties(anyBoolean(), anyString(), any(Map.class)))
				.thenReturn(locations);
		MvcResult result = mockMvc
				.perform(get(BASE_URL + "/findByProperties").param(LocationResource.IS_JURISDICTION, "true")
						.param(LocationResource.RETURN_GEOMETRY, "false")
						.param(LocationResource.PROPERTIES_FILTER, "status:Active,parentId:"))
				.andExpect(status().isOk()).andReturn();
		verify(locationService).findLocationsByProperties(booleanCaptor.capture(), stringCaptor.capture(), mapCaptor.capture());
		assertEquals(LocationResource.gson.toJson(locations), result.getResponse().getContentAsString());
		assertFalse(booleanCaptor.getValue());
		assertEquals("", stringCaptor.getValue());
	}
	@Test
	public void testFindByLocationPropertiesWithError() throws Exception {
		when(locationService.findStructuresByProperties(false, null, null)).thenThrow(new RuntimeException());
		MvcResult result = mockMvc.perform(get(BASE_URL + "/findByProperties"))
				.andExpect(status().isInternalServerError()).andReturn();
		verify(locationService).findStructuresByProperties(false, null, null);

		String responseString = result.getResponse().getContentAsString();
		if (responseString.isEmpty()) {
			fail("Test case failed");
		}
		JsonNode actualObj = mapper.readTree(responseString);
		assertEquals(actualObj.get("message").asText(), MESSAGE);
	}

	@Test
	public void testFindByIdWithChildren() throws Exception {
		List<PhysicalLocation> locations = Collections.singletonList(createLocation());
		when(locationService.findLocationByIdWithChildren(anyBoolean(), anyString(), anyInt()))
				.thenReturn(locations);
		MvcResult result = mockMvc
				.perform(get(BASE_URL + "/findByIdWithChildren")
						.param(LocationResource.JURISDICTION_ID, "j_id")
						.param(LocationResource.RETURN_GEOMETRY, "true")
						.param(LocationResource.PAGE_SIZE, LocationResource.DEFAULT_PAGE_SIZE))
				.andExpect(status().isOk()).andReturn();
		verify(locationService).findLocationByIdWithChildren(booleanCaptor.capture(), stringCaptor.capture(),
				integerCaptor.capture());
		assertEquals(LocationResource.gson.toJson(locations), result.getResponse().getContentAsString());
		assertTrue(booleanCaptor.getValue());
		assertEquals("j_id", stringCaptor.getValue());
		assertEquals(Integer.parseInt(LocationResource.DEFAULT_PAGE_SIZE), integerCaptor.getValue().intValue());

	}

	@Test
	public void testFindStructureIds() throws Exception {
		Pair<List<String>, Long> idsModel = Pair.of(Collections.singletonList("structure-id-1"), 12345l);
		when(locationService.findAllStructureIds(anyLong(), anyInt(), isNull(), isNull())).thenReturn(idsModel);
		MvcResult result = mockMvc.perform(get(BASE_URL + "/findStructureIds?serverVersion=0", "")).andExpect(status().isOk())
				.andReturn();

		String actualStructureIdString = result.getResponse().getContentAsString();
		Identifier actualIdModels = new Gson().fromJson(actualStructureIdString, new TypeToken<Identifier>(){}.getType());
		List<String> actualStructureIdList = actualIdModels.getIdentifiers();

		verify(locationService).findAllStructureIds(anyLong(), anyInt(), isNull(), isNull());
		verifyNoMoreInteractions(locationService);
		assertEquals("{\"identifiers\":[\"structure-id-1\"],\"lastServerVersion\":12345}", result.getResponse().getContentAsString());
		assertEquals((idsModel.getLeft()).get(0), actualStructureIdList.get(0));
		assertEquals(idsModel.getRight(), actualIdModels.getLastServerVersion());
	}

	@Test
	public void testGetAllLocations() throws Exception {
		List<PhysicalLocation> locations = Collections.singletonList(createLocation());
		when(locationService.findAllLocations(anyBoolean(), anyLong(), anyInt(), anyBoolean()))
				.thenReturn(locations);
		MvcResult result = mockMvc
				.perform(get(BASE_URL + "/getAll")
						.param(LocationResource.IS_JURISDICTION, "true")
						.param(LocationResource.RETURN_GEOMETRY, "true")
						.param(BaseEntity.SERVER_VERSIOIN, "0"))
				.andExpect(status().isOk()).andReturn();
		verify(locationService).findAllLocations(anyBoolean(), anyLong(), anyInt(), anyBoolean());
		assertEquals(LocationResource.gson.toJson(locations), result.getResponse().getContentAsString());

	}

	@Test
	public void testGetAllStructures() throws Exception {
		List<PhysicalLocation> locations = Collections.singletonList(createStructure());
		when(locationService.findAllStructures(anyBoolean(), anyLong(), anyInt(), nullable(Integer.class), nullable(String.class), nullable(String.class)))
				.thenReturn(locations);
		MvcResult result = mockMvc
				.perform(get(BASE_URL + "/getAll")
						.param(LocationResource.IS_JURISDICTION, "false")
						.param(LocationResource.RETURN_GEOMETRY, "true")
						.param(BaseEntity.SERVER_VERSIOIN, "0"))
				.andExpect(status().isOk()).andReturn();
		verify(locationService).findAllStructures(anyBoolean(), anyLong(),
				anyInt(), nullable(Integer.class), nullable(String.class), nullable(String.class));
		assertEquals(LocationResource.gson.toJson(locations), result.getResponse().getContentAsString());

	}

	@Test
	public void testCountAllLocations() throws Exception {
		when(locationService.countAllLocations(anyLong()))
				.thenReturn(1L);
		MvcResult result = mockMvc
				.perform(get(BASE_URL + "/countAll")
						.param(LocationResource.IS_JURISDICTION, "true")
						.param(BaseEntity.SERVER_VERSIOIN, "0"))
				.andExpect(status().isOk()).andReturn();
		verify(locationService).countAllLocations(anyLong());
		assertEquals(1, new JSONObject(result.getResponse().getContentAsString()).optInt("count"));
	}

	@Test
	public void testCountAllStructures() throws Exception {
		when(locationService.countAllStructures(anyLong()))
				.thenReturn(1L);
		MvcResult result = mockMvc
				.perform(get(BASE_URL + "/countAll")
						.param(LocationResource.IS_JURISDICTION, "false")
						.param(BaseEntity.SERVER_VERSIOIN, "0"))
				.andExpect(status().isOk()).andReturn();
		verify(locationService).countAllStructures(anyLong());
		assertEquals(1, new JSONObject(result.getResponse().getContentAsString()).optInt("count"));
	}

	@Test
	public void testFindLocationIds() throws Exception {
		Pair<List<String>, Long> idsModel = Pair.of(Collections.singletonList("location-id-1"), 12345l);
		when(locationService.findAllLocationIds(anyLong(), anyInt(), isNull(), isNull())).thenReturn(idsModel);
		MvcResult result = mockMvc.perform(get(BASE_URL + "/findLocationIds?serverVersion=0", "")).andExpect(status().isOk())
				.andReturn();

		String actualLocationIdString = result.getResponse().getContentAsString();
		Identifier actualIdModels = new Gson().fromJson(actualLocationIdString, new TypeToken<Identifier>(){}.getType());
		List<String> actualLocationIdList = actualIdModels.getIdentifiers();

		verify(locationService).findAllLocationIds(anyLong(), anyInt(), isNull(), isNull());
		verifyNoMoreInteractions(locationService);
		assertEquals("{\"identifiers\":[\"location-id-1\"],\"lastServerVersion\":12345}", result.getResponse().getContentAsString());
		assertEquals((idsModel.getLeft()).get(0), actualLocationIdList.get(0));
		assertEquals(idsModel.getRight(), actualIdModels.getLastServerVersion());
	}

	private PhysicalLocation createLocation() {
		PhysicalLocation parentLocation = LocationResource.gson.fromJson(parentJson, PhysicalLocation.class);
		parentLocation.setJurisdiction(true);
		return parentLocation;
	}

	public static PhysicalLocation createStructure() {
		return LocationResource.gson.fromJson(structureJson, PhysicalLocation.class);
	}
	
	@Test
	public void testGetSearchLocationsWithParams() throws Exception {
		List<PhysicalLocation> expected = new ArrayList<>();
		expected.add(createSearchLocation());
		when(locationService.searchLocations((LocationSearchBean) any())).thenReturn(expected);
		MvcResult result = mockMvc
		        .perform(
		            get(BASE_URL + "search-by-tag/").param("locationTagId", "2").param("name", "a")
		                    .param("orderByFieldName", "id")
		                    .param("orderByType", "ASC").param("parentId", "1").param("status", "PENDING_REVIEW"))
		        .andExpect(status().isOk()).andReturn();
		LocationSearchcBean expectedLocations = new LocationSearchcBean();
		expectedLocations.setLocations(expected);
		expectedLocations.setTotal(0);
		verify(locationService).searchLocations((LocationSearchBean) any());
		verifyNoMoreInteractions(locationService);
		assertEquals(LocationResource.gson.toJson(expectedLocations), result.getResponse().getContentAsString());
	}
	
	private PhysicalLocation createSearchLocation() {
		PhysicalLocation searchLocation = LocationResource.gson.fromJson(searchResponseJson, PhysicalLocation.class);
		
		return searchLocation;
	}
	
	@Test
	public void testSyncLocationsByServerVersionsWithReturnCount() throws Exception {
		List<PhysicalLocation> expected = new ArrayList<>();
		expected.add(createLocation());
		when(locationService.findLocationsByServerVersion(1542640316113l)).thenReturn(expected);
		long totalRecords = 5l;
		when(locationService.countLocationsByServerVersion(1542640316113l)).thenReturn(totalRecords);

		MvcResult result = mockMvc.perform(post(BASE_URL + "/sync").contentType(MediaType.APPLICATION_JSON)
				.content("{\"serverVersion\":\"1542640316113\", \"is_jurisdiction\":\"true\", \"return_count\":true}".getBytes()))
				.andExpect(status().isOk()).andReturn();
		verify(locationService).findLocationsByServerVersion(1542640316113l);
		verify(locationService).countLocationsByServerVersion(1542640316113l);
		verifyNoMoreInteractions(locationService);

		JSONArray jsonreponse = new JSONArray(result.getResponse().getContentAsString());
		assertEquals(1, jsonreponse.length());
		JSONAssert.assertEquals(parentJson, jsonreponse.get(0).toString(), JSONCompareMode.STRICT_ORDER);

		Long actualTotalRecords = Long.parseLong(result.getResponse().getHeader("total_records"));
		assertEquals(totalRecords, actualTotalRecords.longValue());
	}

	@Test
	public void testsSyncLocationsByNamesWithreturnCount() throws Exception {

		String locationNames = "01_5";
		List<PhysicalLocation> expected = new ArrayList<>();
		expected.add(createLocation());
		when(locationService.findLocationsByNames(locationNames, 0l)).thenReturn(expected);
		long totalRecords = 5l;
		when(locationService.countLocationsByNames(locationNames, 0l)).thenReturn(totalRecords);
		MvcResult result = mockMvc.perform(post(BASE_URL + "/sync").contentType(MediaType.APPLICATION_JSON)
				.content(("{\"serverVersion\":0,\"is_jurisdiction\":\"true\", \"location_names\":[\"" + locationNames +"\"],\"return_count\":true}").getBytes()))
				.andExpect(status().isOk()).andReturn();
		verify(locationService).findLocationsByNames(locationNames, 0l);
		verify(locationService).countLocationsByNames(locationNames, 0l);
		verifyNoMoreInteractions(locationService);

		JSONArray jsonResponse = new JSONArray(result.getResponse().getContentAsString());
		assertEquals(1, jsonResponse.length());
		PhysicalLocation location = LocationResource.gson.fromJson(jsonResponse.get(0).toString(),
				PhysicalLocation.class);

		assertEquals("01_5", location.getProperties().getName());
		assertEquals("Feature", location.getType());
		assertEquals("3734", location.getId());
		assertEquals(Geometry.GeometryType.MULTI_POLYGON, location.getGeometry().getType());

	}

	@Test
	public void testSyncStructuresByParentIdAndServerVersionWithReturnCount() throws Exception {
		List<PhysicalLocation> expected = new ArrayList<>();
		expected.add(createLocation());
		when(locationService.findStructuresByParentAndServerVersion("3734", 1542640316l)).thenReturn(expected);
		long totalRecords = 3l;
		when(locationService.countStructuresByParentAndServerVersion("3734", 1542640316l)).thenReturn(totalRecords);

		MvcResult result = mockMvc.perform(post(BASE_URL + "/sync").contentType(MediaType.APPLICATION_JSON)
				.content("{\"serverVersion\":1542640316,\"parent_id\":[\"3734\"], \"is_jurisdiction\":\"false\",\"return_count\":true}".getBytes())).andExpect(status().isOk()).andReturn();
		verify(locationService).findStructuresByParentAndServerVersion("3734", 1542640316l);
		verify(locationService).countStructuresByParentAndServerVersion("3734", 1542640316l);
		verifyNoMoreInteractions(locationService);

		Long actualTotalRecords = Long.parseLong(result.getResponse().getHeader("total_records"));
		assertEquals(totalRecords, actualTotalRecords.longValue());

	}

	@Test
	public void testGenerateLocationTree() throws Exception {
		LocationTree tree = LocationResource.gson.fromJson(locationTree, LocationTree.class);

		when(locationService.buildLocationHierachyFromLocation(anyString(), anyBoolean(), anyBoolean())).thenReturn(tree);

		MvcResult result = mockMvc
				.perform(get(BASE_URL + "/hierarchy/" + 1)
						.param(LocationResource.RETURN_TAGS, "false"))
				.andExpect(status().isOk()).andReturn();

		verify(locationService).buildLocationHierachyFromLocation(stringCaptor.capture(), booleanCaptor.capture(), booleanCaptor.capture());

		String actualTreeString = result.getResponse().getContentAsString();
		assertEquals(LocationResource.gson.toJson(tree), actualTreeString);
		assertFalse(booleanCaptor.getAllValues().get(0));
		assertFalse(booleanCaptor.getAllValues().get(1));
		assertEquals("1", stringCaptor.getValue());
	}
	
	@Test
	public void testGetHierarchyForPlan() throws Exception {
		LocationTree tree = LocationResource.gson.fromJson(locationTree, LocationTree.class);
		String planId = "80deb645-f42e-50f0-b5cc-84a4dcfb2db4";
		String locationId = "3921";

		Set<String> locationIdentifiers = new HashSet<String>();
		locationIdentifiers.add(locationId);

		PlanDefinition planDefinition = new PlanDefinition();
		planDefinition.setIdentifier(planId);
		Jurisdiction jurisdiction = new Jurisdiction();
		jurisdiction.setCode(locationId);
		planDefinition.setJurisdiction(Collections.singletonList(jurisdiction));

		when(planService.getPlan(planId))
				.thenReturn(planDefinition);
		when(locationService.buildLocationHierachy(locationIdentifiers, false, false)).thenReturn(tree);
		when(locationService.buildLocationHierachy(locationIdentifiers, false, false)).thenReturn(tree);

		MvcResult result = mockMvc
				.perform(get(BASE_URL + "/hierarchy/plan/" + planId)
						.param(LocationResource.RETURN_STRUCTURE_COUNT, "false"))
				.andExpect(status().isOk()).andReturn();

		verify(locationService).buildLocationHierachy(stringSetCaptor.capture(), booleanCaptor.capture(), booleanCaptor.capture());

		String actualTreeString = result.getResponse().getContentAsString();

		assertEquals(LocationResource.gson.toJson(tree), actualTreeString);
		assertFalse(booleanCaptor.getAllValues().get(0));
		assertEquals(1, stringSetCaptor.getValue().size());
		assertTrue(stringSetCaptor.getValue().contains(locationId));
	}

	@Test
	public void testImportLocationsWithBeginning() throws Exception {
		MvcResult result = mockMvc
				.perform(post(BASE_URL + "/dhis2/import?beginning=true").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		verify(dhis2ImportOrganizationUnits).importOrganizationUnits("1");
		String actualString = result.getResponse().getContentAsString();
		assertTrue(actualString.contains(DHIS_IMPORT_JOB_STATUS_END_POINT));
	}

	@Test
	public void testImportLocationsWithStartPageParam() throws Exception {
		MvcResult result = mockMvc.perform(
				post(BASE_URL + "/dhis2/import?startPage=2&beginning=false").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		verify(dhis2ImportOrganizationUnits).importOrganizationUnits("2");
		String actualString = result.getResponse().getContentAsString();
		assertTrue(actualString.contains(DHIS_IMPORT_JOB_STATUS_END_POINT));
	}

	@Test
	public void testImportLocationsWithInvalidParams() throws Exception {
		MvcResult result = mockMvc
				.perform(post(BASE_URL + "/dhis2/import?startPage=2&beginning=true").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		String actualString = result.getResponse().getContentAsString();
		assertEquals(
				"Both the parameters are conflicting. Please make sure you want to start from beginning or from a particular page number",
				actualString);
	}

	@Test
	public void testImportLocationsWithMissingParam() throws Exception {
		MvcResult result = mockMvc
				.perform(post(BASE_URL + "/dhis2/import?beginning=false").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();

		String actualString = result.getResponse().getContentAsString();
		assertEquals("Start page must be specified", actualString);
	}

	@Test
	public void testGetStatusOfJob() throws Exception {
		DHIS2LocationsImportSummary dhis2LocationsImportSummary = new DHIS2LocationsImportSummary();
		dhis2LocationsImportSummary.setDhisImportLocationsJobStatus(DHISImportLocationsJobStatus.RUNNING);
		dhis2LocationsImportSummary.setDhisLocationsCount(1000);
		dhis2LocationsImportSummary.setDhisPageCount(10);
		dhis2LocationsImportSummary.setLastPageSynced(2);
		dhis2LocationsImportSummary.setNumberOfRowsProcessed(200);
		when(dhis2ImportLocationsStatusService.getSummaryOfDHISImportsFromAppStateTokens())
				.thenReturn(dhis2LocationsImportSummary);
		MvcResult result = mockMvc.perform(get(BASE_URL + "/dhis2/status")).andExpect(status().isOk()).andReturn();
		String actualString = result.getResponse().getContentAsString();
		DHIS2LocationsImportSummary summary = LocationResource.gson.fromJson(actualString,
				DHIS2LocationsImportSummary.class);
		assertEquals(DHISImportLocationsJobStatus.RUNNING, summary.getDhisImportLocationsJobStatus());
		assertEquals(new Integer(1000), summary.getDhisLocationsCount());
		assertEquals(new Integer(10), summary.getDhisPageCount());
		assertEquals(new Integer(2), summary.getLastPageSynced());
		assertEquals(new Integer(200), summary.getNumberOfRowsProcessed());
	}

	@Test
	public void testGenerateLocationTreeWithAncestors() throws Exception {
		Set<LocationDetail> locationDetails = new HashSet<>();

		LocationDetail country = LocationDetail.builder().name("Country 1").id(2l).identifier("1").tags("Country").build();
		LocationDetail province1 = LocationDetail.builder().name("Province 1").id(3l).identifier("11").parentId("1")
				.tags("Province").build();
		LocationDetail province2 = LocationDetail.builder().name("Province 2").id(4l).identifier("12").parentId("1")
				.tags("Province").build();
		LocationDetail district1 = LocationDetail.builder().name("District 1").id(5l).identifier("111").parentId("11")
				.tags("District").build();
		LocationDetail district2 = LocationDetail.builder().name("District 2").id(6l).identifier("121").parentId("12")
				.tags("District").build();
		LocationDetail district3 = LocationDetail.builder().name("District 3").id(7l).identifier("122").parentId("12")
				.tags("District").build();

		locationDetails.add(country);
		locationDetails.add(province1);
		locationDetails.add(province2);
		locationDetails.add(district1);
		locationDetails.add(district2);
		locationDetails.add(district3);

		when(locationService.buildLocationHeirarchyWithAncestors(anyString())).thenReturn(locationDetails);

		MvcResult result = mockMvc
				.perform(get(BASE_URL + "/heirarchy/ancestors/" + 1))
				.andExpect(status().isOk()).andReturn();

		verify(locationService).buildLocationHeirarchyWithAncestors(stringCaptor.capture());

		Set<LocationDetail> locationDetailsSet = (Set<LocationDetail>) result.getModelAndView().getModel().get("locationDetailList");
		assertEquals(6, locationDetailsSet.size());
		LocationDetail actuallocationDetail = locationDetailsSet.iterator().next();
		assertEquals("Province 1", actuallocationDetail.getName());
		assertEquals("Province", actuallocationDetail.getTags());
	}

	@Test
	public void testFindStructuresByAncestorGetsStructures() throws Exception {
		final String ancestorId = "04d663ee-f445-4e45-b982-9b6886822de1";
		PhysicalLocation physicalLocation = spy(PhysicalLocation.class);
		physicalLocation.setId(ancestorId);
		List<PhysicalLocation> location = Collections.singletonList(physicalLocation);
		when(locationService.findLocationByIdWithChildren(eq(false), eq(ancestorId), anyInt()))
				.thenReturn(location);

		mockMvc.perform(get(BASE_URL + "/findStructuresByAncestor/?id=" + ancestorId))
				.andExpect(status().isOk());
		verify(locationService).findStructuresByParentAndServerVersion(eq(physicalLocation.getId()), eq(0L));

	}

}
