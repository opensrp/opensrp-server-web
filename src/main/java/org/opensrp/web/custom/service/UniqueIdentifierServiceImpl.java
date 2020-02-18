package org.opensrp.web.custom.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.opensrp.domain.custom.UniqueIdentifier;
import org.opensrp.domain.setting.Setting;
import org.opensrp.domain.setting.SettingConfiguration;
import org.opensrp.repository.SettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UniqueIdentifierServiceImpl implements UniqueIdentifierService {

	@Autowired
	UniqueIdentifierRepository uniqueIdentifierRepository;

	@Autowired
	SettingRepository settingRepository;

	@Transactional
	@Override
	public List<String> generateIdentifiers(String usedBy, int numberOfIdsToGenerate) {
		UniqueIdentifier uniqueIdentifier = uniqueIdentifierRepository.findUniqueIdentifierOrderByIdDesc();
		return generateIds(uniqueIdentifier, usedBy, numberOfIdsToGenerate);
	}
	
	private void saveIds(List<String> ids, String location, String status, Date updatedAt, String usedBy,
			Date createdAt) {
		for (int i = 0; i < ids.size(); i++) {
			UniqueIdentifier uniqueIdentifier = new UniqueIdentifier(ids.get(i), status, usedBy, location, createdAt,
					updatedAt);
			uniqueIdentifierRepository.save(uniqueIdentifier);
		}
	}

	@Override
	public String updateStatus(String usedBy, List<String> ids) {
		throw new NotImplementedException();
	}

	private String getFirstID() {
		List<SettingConfiguration> settingConfigurationList = settingRepository.findAllSettings();
		for (int i = 0; i < settingConfigurationList.size(); i++) {
			if (settingConfigurationList.get(i).getIdentifier().equals("system-initialuniqueidentifier")) {
				Setting setting = settingConfigurationList.get(i).getSettings().get(0);
				return setting.getValue();
			}
		}
		return "";
	}

	private List<String> generateIds(UniqueIdentifier uniqueIdentifier, String usedBy, int numberToGenerate) {
		List<String> output = new ArrayList<>();
		String id = "";
		if (uniqueIdentifier == null) {
			id = getFirstID();
		} else {
			id = uniqueIdentifier.getIdentifier();
		}

		if(id.equals("")) {
			throw new IllegalArgumentException("Settings for First ID not found");
		}
		
		String prefix = id.substring(0, 2);
		String numberPart = id.substring(3, 7);

		for (int i = 0; i < numberToGenerate; i++) {
			if ((Integer.parseInt(numberPart) + 1) <= 9999) {
				numberPart = (Integer.parseInt(numberPart) + 1) + "";
				output.add(prefix + "-" + numberPart);
			} else {
				numberPart = "1001";
				prefix = getPrefix(prefix);
				output.add(prefix + "-" + numberPart);
			}
		}

		if (output.size() == numberToGenerate) {
			saveIds(output, null, "not_used", new Date(), usedBy, new Date());
		}

		return output;
	}

	private String getPrefix(String input) {
		byte[] chars = input.getBytes(StandardCharsets.US_ASCII);

		if (chars[1] < 90) {
			chars[1] += 1;
		} else if (chars[1] == 90 && chars[0] < 90) {
			chars[0] += 1;
			chars[1] = 65;
		}
		return Character.toString((char) chars[0]) + Character.toString((char) chars[1]);		
	}
}
