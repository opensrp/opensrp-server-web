package org.opensrp.web.rest.it;

import org.opensrp.domain.ErrorTrace;
import org.opensrp.repository.ClientsRepository;
import org.opensrp.repository.ErrorTraceRepository;
import org.opensrp.scheduler.Action;
import org.opensrp.scheduler.Alert;
import org.opensrp.scheduler.repository.ActionsRepository;
import org.opensrp.scheduler.repository.AlertsRepository;
import org.smartregister.domain.Client;

import java.util.List;

public final class ResourceTestUtility {

    private ResourceTestUtility() {
    }

    public static void createClients(List<Client> allClient, ClientsRepository allClients) {
        for (Client client : allClient) {
            allClients.add(client);
        }
    }

    public static void createActions(List<Action> actions, ActionsRepository allActions) {
        for (Action action : actions) {
            allActions.add(action);
        }
    }

    public static void createAlerts(List<Alert> alerts, AlertsRepository allAlerts) {
        for (Alert alert : alerts) {
            allAlerts.add(alert);
        }
    }

    public static void createErrorTraces(List<ErrorTrace> errorTraces, ErrorTraceRepository allErrorTrace) {
        for (ErrorTrace errorTrace : errorTraces) {
            allErrorTrace.add(errorTrace);
        }
    }

}
