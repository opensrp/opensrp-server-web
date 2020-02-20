package org.opensrp.web.custom.service;

import java.util.List;

public interface UniqueIdentifierService {
	List<String> generateIdentifiers(String usedBy, int numberOfIdsToGenerate);
	String updateStatus(String usedBy, List<String> ids);
}
