package org.opensrp.repository.lucene.it;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.opensrp.util.SampleFullDomainObject.BASE_ENTITY_ID;
import static org.opensrp.util.SampleFullDomainObject.DIFFERENT_BASE_ENTITY_ID;
import static org.opensrp.util.SampleFullDomainObject.ENTITY_TYPE;
import static org.opensrp.util.SampleFullDomainObject.EPOCH_DATE_TIME;
import static org.opensrp.util.SampleFullDomainObject.EVENT_TYPE;
import static org.opensrp.util.SampleFullDomainObject.LOCATION_ID;
import static org.opensrp.util.SampleFullDomainObject.PROVIDER_ID;
import static org.utils.AssertionUtil.assertTwoListAreSameIgnoringOrder;
import static org.utils.CouchDbAccessUtils.addObjectToRepository;

import java.util.List;

import org.ektorp.DbAccessException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opensrp.BaseIntegrationTest;
import org.opensrp.domain.Event;
import org.opensrp.repository.couch.AllEvents;
import org.opensrp.repository.lucene.LuceneEventRepository;
import org.opensrp.search.EventSearchBean;
import org.springframework.beans.factory.annotation.Autowired;

public class LuceneEventRepositoryTest extends BaseIntegrationTest {
	
	@Autowired
	private AllEvents allEvents;
	
	@Autowired
	private LuceneEventRepository luceneEventRepository;
	
	@Before
	public void setUp() {
		allEvents.removeAll();
	}
	
	@After
	public void cleanUp() {
		//allEvents.removeAll();
	}
	
	@Test
	public void shouldFindByBaseEntityId() {
		Event expectedEvent = new Event();
		expectedEvent.setBaseEntityId(BASE_ENTITY_ID);
		expectedEvent.setDateCreated(EPOCH_DATE_TIME);
		
		addObjectToRepository(asList(expectedEvent), allEvents);
		
		EventSearchBean eventSearchBean = new EventSearchBean();
		eventSearchBean.setBaseEntityId(BASE_ENTITY_ID);
		List<Event> actualEvents = luceneEventRepository.getByCriteria(eventSearchBean);
		
		assertEquals(1, actualEvents.size());
		assertEquals(expectedEvent, actualEvents.get(0));
	}
	
	@Test(expected = DbAccessException.class)
	public void shouldThrowExceptionIfEventDateCreatedFieldFound() {
		Event expectedEvent = new Event();
		expectedEvent.setBaseEntityId(BASE_ENTITY_ID);
		
		addObjectToRepository(asList(expectedEvent), allEvents);
		
		EventSearchBean eventSearchBean = new EventSearchBean();
		eventSearchBean.setBaseEntityId(BASE_ENTITY_ID);
		luceneEventRepository.getByCriteria(eventSearchBean);
		
	}
	
	@Test
	public void shouldFindByAllCriteria() {
		addRandomInvalidEvents();
		Event expectedEvent = new Event();
		expectedEvent.setBaseEntityId(BASE_ENTITY_ID);
		expectedEvent.setDateCreated(EPOCH_DATE_TIME);
		expectedEvent.setEventDate(new DateTime(DateTimeZone.UTC));
		expectedEvent.setEventType(EVENT_TYPE);
		expectedEvent.setEntityType(ENTITY_TYPE);
		expectedEvent.setProviderId(PROVIDER_ID);
		expectedEvent.setLocationId(LOCATION_ID);
		expectedEvent.setDateEdited(new DateTime(DateTimeZone.UTC));
		
		addObjectToRepository(asList(expectedEvent), allEvents);
		
		EventSearchBean eventSearchBean = new EventSearchBean();
		eventSearchBean.setBaseEntityId(BASE_ENTITY_ID);
		eventSearchBean.setEventDateFrom(EPOCH_DATE_TIME);
		eventSearchBean.setEventDateTo(new DateTime(DateTimeZone.UTC));
		eventSearchBean.setEntityType(expectedEvent.getEventType());
		eventSearchBean.setEntityType(expectedEvent.getEntityType());
		eventSearchBean.setProviderId(expectedEvent.getProviderId());
		eventSearchBean.setLocationId(expectedEvent.getLocationId());
		eventSearchBean.setLastEditFrom(EPOCH_DATE_TIME);
		eventSearchBean.setLastEditTo(new DateTime(DateTimeZone.UTC));
		
		List<Event> actualEvents = luceneEventRepository.getByCriteria(eventSearchBean);
		
		assertEquals(1, actualEvents.size());
		assertEquals(expectedEvent, actualEvents.get(0));
	}
	
	@Test(expected = RuntimeException.class)
	public void shouldThrowExceptionWithNoCriteriaWithTeamIds() {
		addRandomInvalidEvents();
		Event expectedEvent = new Event();
		expectedEvent.setBaseEntityId(BASE_ENTITY_ID);
		expectedEvent.setDateCreated(EPOCH_DATE_TIME);
		expectedEvent.setEventDate(new DateTime(DateTimeZone.UTC));
		expectedEvent.setEventType(EVENT_TYPE);
		expectedEvent.setEntityType(ENTITY_TYPE);
		expectedEvent.setProviderId(PROVIDER_ID);
		expectedEvent.setLocationId(LOCATION_ID);
		expectedEvent.setDateEdited(new DateTime(DateTimeZone.UTC));
		
		addObjectToRepository(asList(expectedEvent), allEvents);
		
		luceneEventRepository.getByCriteria(new EventSearchBean());
	}
	
	@Test
	public void shouldFindByAllCriteriaWithTeamId() {
		addRandomInvalidEvents();
		Event expectedEvent = new Event();
		expectedEvent.setBaseEntityId(BASE_ENTITY_ID);
		expectedEvent.setDateCreated(EPOCH_DATE_TIME);
		expectedEvent.setEventDate(new DateTime(DateTimeZone.UTC));
		expectedEvent.setEventType(EVENT_TYPE);
		expectedEvent.setEntityType(ENTITY_TYPE);
		expectedEvent.setProviderId(PROVIDER_ID);
		expectedEvent.setLocationId(LOCATION_ID);
		expectedEvent.setDateEdited(new DateTime(DateTimeZone.UTC));
		
		Event expectedEvent2 = new Event();
		expectedEvent2.setBaseEntityId(DIFFERENT_BASE_ENTITY_ID);
		expectedEvent2.setDateCreated(EPOCH_DATE_TIME);
		expectedEvent2.setEventDate(new DateTime(DateTimeZone.UTC));
		expectedEvent2.setEventType(EVENT_TYPE);
		expectedEvent2.setEntityType(ENTITY_TYPE);
		expectedEvent2.setProviderId(DIFFERENT_BASE_ENTITY_ID);
		expectedEvent2.setLocationId(LOCATION_ID);
		expectedEvent2.setDateEdited(new DateTime(DateTimeZone.UTC));
		
		addObjectToRepository(asList(expectedEvent, expectedEvent2), allEvents);
		
		String teamIds = PROVIDER_ID + "," + DIFFERENT_BASE_ENTITY_ID;
		String baseEntityIds = BASE_ENTITY_ID + "," + DIFFERENT_BASE_ENTITY_ID;
		
		EventSearchBean eventSearchBean = new EventSearchBean();
		eventSearchBean.setBaseEntityId(baseEntityIds);
		eventSearchBean.setProviderId(teamIds);
		eventSearchBean.setLocationId(LOCATION_ID);
		eventSearchBean.setServerVersion(EPOCH_DATE_TIME.getMillis());
		List<Event> actualEvents = luceneEventRepository.getByCriteria(eventSearchBean, null, "desc", 100);
		
		assertTwoListAreSameIgnoringOrder(asList(expectedEvent, expectedEvent2), actualEvents);
	}
	
	//TODO: fix source
	@Test(expected = NullPointerException.class)
	public void shouldThrowExceptionFindByCriteriaWithTeamIdWithOutSortOrder() {
		addRandomInvalidEvents();
		Event expectedEvent = new Event();
		expectedEvent.setBaseEntityId(BASE_ENTITY_ID);
		expectedEvent.setDateCreated(EPOCH_DATE_TIME);
		expectedEvent.setEventDate(new DateTime(DateTimeZone.UTC));
		expectedEvent.setEventType(EVENT_TYPE);
		expectedEvent.setEntityType(ENTITY_TYPE);
		expectedEvent.setProviderId(PROVIDER_ID);
		expectedEvent.setLocationId(LOCATION_ID);
		expectedEvent.setDateEdited(new DateTime(DateTimeZone.UTC));
		
		Event expectedEvent2 = new Event();
		expectedEvent2.setBaseEntityId(DIFFERENT_BASE_ENTITY_ID);
		expectedEvent2.setDateCreated(EPOCH_DATE_TIME);
		expectedEvent2.setEventDate(new DateTime(DateTimeZone.UTC));
		expectedEvent2.setEventType(EVENT_TYPE);
		expectedEvent2.setEntityType(ENTITY_TYPE);
		expectedEvent2.setProviderId(DIFFERENT_BASE_ENTITY_ID);
		expectedEvent2.setLocationId(LOCATION_ID);
		expectedEvent2.setDateEdited(new DateTime(DateTimeZone.UTC));
		
		addObjectToRepository(asList(expectedEvent, expectedEvent2), allEvents);
		
		String teamIds = PROVIDER_ID + "," + DIFFERENT_BASE_ENTITY_ID;
		String baseEntityIds = BASE_ENTITY_ID + "," + DIFFERENT_BASE_ENTITY_ID;
		
		EventSearchBean eventSearchBean = new EventSearchBean();
		eventSearchBean.setBaseEntityId(baseEntityIds);
		eventSearchBean.setProviderId(teamIds);
		eventSearchBean.setLocationId(LOCATION_ID);
		eventSearchBean.setServerVersion(EPOCH_DATE_TIME.getMillis());
		
		List<Event> actualEvents = luceneEventRepository.getByCriteria(eventSearchBean, null, null, 100);
		
		assertTwoListAreSameIgnoringOrder(asList(expectedEvent, expectedEvent2), actualEvents);
	}
	
	@Test(expected = RuntimeException.class)
	public void shouldThrowExceptionWithNoCriteria() {
		addRandomInvalidEvents();
		Event expectedEvent = new Event();
		expectedEvent.setBaseEntityId(BASE_ENTITY_ID);
		expectedEvent.setDateCreated(EPOCH_DATE_TIME);
		expectedEvent.setEventDate(new DateTime(DateTimeZone.UTC));
		expectedEvent.setEventType(EVENT_TYPE);
		expectedEvent.setEntityType(ENTITY_TYPE);
		expectedEvent.setProviderId(PROVIDER_ID);
		expectedEvent.setLocationId(LOCATION_ID);
		expectedEvent.setDateEdited(new DateTime(DateTimeZone.UTC));
		
		addObjectToRepository(asList(expectedEvent), allEvents);
		
		luceneEventRepository.getByCriteria(new EventSearchBean(), null, null, 0);
	}
	
	@Test
	public void shouldFindByStringQuery() {
		String query = "eventDate<date>:[1970-01-01T00:00:00 TO 3017-08-30T08:12:38] AND lastEdited<date>:[1970-01-01T00:00:00 TO 3017-08-30T08:12:38] AND baseEntityId:baseEntityId AND eventType:eventType AND entityType:entityType AND providerId:providerId AND locationId:locationId";
		addRandomInvalidEvents();
		Event expectedEvent = new Event();
		expectedEvent.setBaseEntityId(BASE_ENTITY_ID);
		expectedEvent.setDateCreated(EPOCH_DATE_TIME);
		expectedEvent.setEventDate(EPOCH_DATE_TIME);
		expectedEvent.setEventType(EVENT_TYPE);
		expectedEvent.setEntityType(ENTITY_TYPE);
		expectedEvent.setProviderId(PROVIDER_ID);
		expectedEvent.setLocationId(LOCATION_ID);
		expectedEvent.setDateEdited(EPOCH_DATE_TIME);
		
		addObjectToRepository(asList(expectedEvent), allEvents);
		
		List<Event> actualEvents = luceneEventRepository.getByCriteria(query);
		assertEquals(1, actualEvents.size());
		assertEquals(expectedEvent, actualEvents.get(0));
		
	}
	
	private void addRandomInvalidEvents() {
		for (int i = 0; i < 100; i++) {
			Event event = new Event();
			event.setBaseEntityId(BASE_ENTITY_ID + i);
			event.setDateCreated(EPOCH_DATE_TIME);
			event.setEventDate(new DateTime(DateTimeZone.UTC));
			event.setEventType(EVENT_TYPE + i);
			event.setEntityType(ENTITY_TYPE + i);
			event.setProviderId(PROVIDER_ID + i);
			event.setLocationId(LOCATION_ID + i);
			event.setDateEdited(new DateTime(DateTimeZone.UTC));
			allEvents.add(event);
		}
	}
	
}
