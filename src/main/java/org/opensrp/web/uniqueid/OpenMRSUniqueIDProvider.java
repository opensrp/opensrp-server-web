package org.opensrp.web.uniqueid;

import org.opensrp.service.OpenmrsIDService;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Deprecated
public class OpenMRSUniqueIDProvider implements UniqueIDProvider {
    private final OpenmrsIDService openmrsIDService;
    private final Queue<String> availableIDs = new LinkedList<>();

    public OpenMRSUniqueIDProvider(OpenmrsIDService openmrsIDService, int expectedIDs) {
        this(openmrsIDService);
        if (expectedIDs > 0)
            fetchNewIDs(expectedIDs);
    }

    public OpenMRSUniqueIDProvider(OpenmrsIDService openmrsIDService) {
        this.openmrsIDService = openmrsIDService;
    }

    @Override
    public String getNewUniqueID() {
        if (!verifyID())
            throw new IllegalStateException("No available IDs");
        return availableIDs.poll();
    }

    private boolean verifyID() {
        if (availableIDs.size() == 0) {
            fetchNewIDs(1);
        }

        return availableIDs.size() > 0;
    }

    private void fetchNewIDs(int size) {
        List<String> openMRSIDs = this.openmrsIDService.downloadOpenmrsIds(size);
        availableIDs.addAll(openMRSIDs);
    }
}
