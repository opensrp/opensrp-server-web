package org.opensrp.web.uniqueid;

import org.opensrp.domain.IdentifierSource;
import org.opensrp.service.IdentifierSourceService;
import org.opensrp.service.UniqueIdentifierService;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class UniqueIdentifierProvider implements UniqueIDProvider {

	private final UniqueIdentifierService uniqueIdentifierService;

	private final IdentifierSourceService identifierSourceService;

	private final Queue<String> availableIDs = new LinkedList<>();

	private String source;

	public UniqueIdentifierProvider(
			UniqueIdentifierService uniqueIdentifierService,
			IdentifierSourceService identifierSourceService,
			String source,
			int expectedIDs) {
		this(uniqueIdentifierService, identifierSourceService, source);
		if (expectedIDs > 0)
			fetchNewIDs(expectedIDs);
	}

	public UniqueIdentifierProvider(
			UniqueIdentifierService uniqueIdentifierService,
			IdentifierSourceService identifierSourceService,
			String source
	) {
		this.uniqueIdentifierService = uniqueIdentifierService;
		this.identifierSourceService = identifierSourceService;
		this.source = source;
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
		IdentifierSource identifierSource = identifierSourceService.findByIdentifier(source);
		List<String> openMRSIDs = this.uniqueIdentifierService.generateIdentifiers(identifierSource, size, "uploadService");
		availableIDs.addAll(openMRSIDs);
	}
}
