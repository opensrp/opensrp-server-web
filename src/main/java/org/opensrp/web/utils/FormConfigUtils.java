package org.opensrp.web.utils;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

public class FormConfigUtils {

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
			formVersion =
					defaultArtifactVersion.getMajorVersion() + "." + newVersion + "." + defaultArtifactVersion
							.getIncrementalVersion();
		} else {
			int newVersion = defaultArtifactVersion.getMajorVersion() + 1;
			formVersion =
					newVersion + "." + defaultArtifactVersion.getMinorVersion() + "." + defaultArtifactVersion
							.getIncrementalVersion();
		}
		return formVersion;
	}
}
