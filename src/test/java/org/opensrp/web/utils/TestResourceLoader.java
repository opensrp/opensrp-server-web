package org.opensrp.web.utils;

import org.apache.commons.lang3.StringUtils;
import org.opensrp.connector.openmrs.service.EncounterService;
import org.opensrp.connector.openmrs.service.PatientService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class TestResourceLoader {

    protected String openmrsOpenmrsUrl;

    protected String openmrsUsername;

    protected String openmrsPassword;

    protected String formDirPath;

    protected String formToDownload;

    protected boolean pushToOpenmrsForTest;

    protected PatientService patientService;

    protected EncounterService encounterService;

    public TestResourceLoader() throws IOException {
        Resource resource = new ClassPathResource("opensrp.properties");
        Properties props = PropertiesLoaderUtils.loadProperties(resource);
        openmrsOpenmrsUrl = props.getProperty("openmrs.url");
        openmrsUsername = props.getProperty("openmrs.username");
        openmrsPassword = props.getProperty("openmrs.password");
        formDirPath = props.getProperty("form.directory.name", "/form");
        formToDownload = props.getProperty("form.download.files", "form.xml, model.xml, form_definition.json").replace(" ", "");
        String rc = props.getProperty("openmrs.test.make-rest-call");
        pushToOpenmrsForTest = StringUtils.isBlank(rc) ? false : Boolean.parseBoolean(rc);

        this.patientService = new PatientService(openmrsOpenmrsUrl, openmrsUsername, openmrsPassword);
        this.encounterService = new EncounterService(openmrsOpenmrsUrl, openmrsUsername, openmrsPassword);
        this.encounterService.setPatientService(patientService);
    }

    public byte[] getFormDirectoryAsZip(String directoryName) throws IOException {
        ResourceLoader loader = new DefaultResourceLoader();
        return zipFiles(new File(loader.getResource(formDirPath).getURI().getPath() + "/" + directoryName));
    }

    private byte[] zipFiles(File directory) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        byte bytes[] = new byte[2048];
        String[] fl = directory.list();
        for (String fileName : fl) {
            if (formToDownload.matches("(.+,)?" + fileName + "(,.+)?$")) {

                FileInputStream fis = new FileInputStream(directory.getPath() + "/" + fileName);
                BufferedInputStream bis = new BufferedInputStream(fis);

                zos.putNextEntry(new ZipEntry(fileName));

                int bytesRead;
                while ((bytesRead = bis.read(bytes)) != -1) {
                    zos.write(bytes, 0, bytesRead);
                }
                zos.closeEntry();
                bis.close();
                fis.close();
            }
        }

        zos.flush();
        baos.flush();
        zos.close();
        baos.close();

        return baos.toByteArray();
    }
}
