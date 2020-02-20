package org.opensrp.web.listener;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.opensrp.domain.Client;
import org.opensrp.domain.Event;
import org.opensrp.repository.ClientsRepository;
import org.opensrp.repository.EventsRepository;
import org.opensrp.service.EventService;
import org.opensrp.service.formSubmission.EventsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClientEventListener {

	private static Logger logger = LoggerFactory.getLogger(EventsListener.class.toString());

	private static final ReentrantLock lock = new ReentrantLock();

	private EventsRepository allEvents;

	private ClientsRepository allClients;

	private EventService eventService;

	@Autowired
	public ClientEventListener(EventsRepository allEvents, ClientsRepository allClients, EventService eventService) {
		this.allEvents = allEvents;
		this.allClients = allClients;
		this.eventService = eventService;
	}	

	public synchronized void addServerVersion() {			
		if (!lock.tryLock()) {
			logger.warn("Not fetching events from Message Queue. It is already in progress.");
			return;
		}		
		try {
			List<Client> clients = allClients.findByEmptyServerVersion();
			logger.info("RUNNING addServerVersion clients = " + clients.size());
			long currentTimeMillis = getCurrentMilliseconds();
			while (clients != null && !clients.isEmpty()) {
				for (Client client : clients) {
					try {
						Thread.sleep(1);
						client.setServerVersion(currentTimeMillis);
						allClients.update(client);
						logger.info("Add server_version: found new client " + client.getBaseEntityId());
					} catch (InterruptedException e) {
						logger.error("", e);
					}
					currentTimeMillis += 1;
				}
				clients = allClients.findByEmptyServerVersion();
			}

			List<Event> events = allEvents.findByEmptyServerVersion();
			logger.info("RUNNING addServerVersion events = " + events.size());
			while (events != null && !events.isEmpty()) {
				for (Event event : events) {
					try {
						Thread.sleep(1);
						event = eventService.processOutOfArea(event);
						event.setServerVersion(currentTimeMillis);
						allEvents.update(event);

						logger.info("Add server_version: found new event " + event.getBaseEntityId());
					} catch (InterruptedException e) {
						logger.error("", e);
					}
					currentTimeMillis += 1;
				}

				events = allEvents.findByEmptyServerVersion();
			}
		} catch (Exception e) {
			logger.error("", e);
		}
		finally {
			lock.unlock();
		}
	}

	public long getCurrentMilliseconds() {
		return System.currentTimeMillis();
	}
}
