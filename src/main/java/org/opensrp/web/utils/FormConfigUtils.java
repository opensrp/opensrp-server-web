package org.opensrp.web.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.json.JSONObject;

public class FormConfigUtils {

	public static final String FORMS_VERSION = "forms_version";

	public static final String JSON = "json";

	public static String getNewVersion(String initialVersion) {
		String formVersion;
		DefaultArtifactVersion defaultArtifactVersion = new DefaultArtifactVersion(initialVersion);
		if (defaultArtifactVersion.getIncrementalVersion() < 1000) {
			int newVersion = defaultArtifactVersion.getIncrementalVersion() + 1;
			formVersion =
					defaultArtifactVersion.getMajorVersion() + "." + defaultArtifactVersion.getMinorVersion() +
							"." + newVersion;
		} else if (defaultArtifactVersion.getMinorVersion() < 1000) {
			int newVersion = defaultArtifactVersion.getMinorVersion() + 1;
			formVersion = defaultArtifactVersion.getMajorVersion() + "." + newVersion + ".0";
		} else {
			int newVersion = defaultArtifactVersion.getMajorVersion() + 1;
			formVersion =
					newVersion + ".0" + ".0";
		}
		return formVersion;
	}

	public static String getFormsVersion(String jsonString) {
		String formVersion = "";
		if (StringUtils.isNotBlank(jsonString)) {
			JSONObject parentObject = new JSONObject(jsonString);
			JSONObject jsonObject = (parentObject.has(JSON)) ?
					new JSONObject(Utils.getStringFromJSON(parentObject, "json")) :
					parentObject;
			if (jsonObject.has(FORMS_VERSION)) {
				formVersion = Utils.getStringFromJSON(jsonObject, FORMS_VERSION);
			}
		}
		return formVersion;
	}
}
