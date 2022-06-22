package org.opensrp.web.utils;

import org.junit.Assert;
import org.junit.Test;

public class FormConfigUtilsTest {

    @Test
    public void testGetNewVersion() {
        String formVersion = FormConfigUtils.getNewVersion("1.0.2");
        Assert.assertEquals("1.0.3", formVersion);
    }

    @Test
    public void testGetNewVersionWithMaxPatchVersion() {
        String formVersion = FormConfigUtils.getNewVersion("1.0.1000");
        Assert.assertEquals("1.1.0", formVersion);
    }

    @Test
    public void testGetNewVersionWithMaxMinorVersion() {
        String formVersion = FormConfigUtils.getNewVersion("1.1000.0");
        Assert.assertEquals("1.1000.1", formVersion);
    }

    @Test
    public void testGetNewVersionWithMaxMinorAndPatchVersion() {
        String formVersion = FormConfigUtils.getNewVersion("1.1000.1000");
        Assert.assertEquals("2.0.0", formVersion);
    }

    @Test
    public void testGetFormsVersion() {
        String jsonString = "{\"forms_version\":\"0.0.1\",\"identifiers\":[\"anc_register.json\","
                + "\"registration_calculation_rules.yml\",\"anc_register.properties\"]}";
        String formVersion = FormConfigUtils.getFormsVersion(jsonString);
        Assert.assertEquals("0.0.1", formVersion);
    }

    @Test
    public void testGetFromVersionWithFullManifestPayload() {
        String manifest = "{\"identifier\":\"0.0.1\",\"appId\":\"org.smartregister.anc\",\"appVersion\":\"1.4.0\","
                + "\"json\":\"{\\\"forms_version\\\":\\\"0.1.1\\\",\\\"identifiers\\\":[\\\"anc_register.json\\\","
                + "\\\"registration_calculation_rules.yml\\\",\\\"anc_register.properties\\\"]}\"}";

        String formVersion = FormConfigUtils.getFormsVersion(manifest);
        Assert.assertEquals("0.1.1", formVersion);
    }
}
