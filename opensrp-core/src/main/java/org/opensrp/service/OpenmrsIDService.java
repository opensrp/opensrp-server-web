package org.opensrp.service;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opensrp.domain.Client;
import org.opensrp.domain.UniqueId;
import org.opensrp.repository.UniqueIdRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OpenmrsIDService {
	
	@Value("#{opensrp['openmrs.url']}")
	private String openmrsUrl;
	
	@Value("#{opensrp['openmrs.username']}")
	private String openmrsUserName;
	
	@Value("#{opensrp['openmrs.password']}")
	private String openmrsPassword;
	
	@Value("#{opensrp['openmrs.idgen.idsource']}")
	private int openmrsSourceId;
	
	// Client identifiers constant
	public static final String ZEIR_IDENTIFIER = "ZEIR_ID";
	
	public static final String CHILD_REGISTER_CARD_NUMBER = "Child_Register_Card_Number";
	
	public static final String OPENMRS_IDGEN_URL = "module/idgen/exportIdentifiers.form";
	
	private static Logger logger = LoggerFactory.getLogger(OpenmrsIDService.class.toString());
	
	private HttpClient client;
	
	@Autowired
	private UniqueIdRepository uniqueIdRepository;
	
	public static OpenmrsIDService createInstanceWithOpenMrsUrl(String openmrsUrl) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		OpenmrsIDService openmrsIDService = new OpenmrsIDService();
		openmrsIDService.openmrsUrl = openmrsUrl;
		return openmrsIDService;
	}
	
	public OpenmrsIDService() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		SSLContext sslContext = SSLContextBuilder
                .create()
                .loadTrustMaterial(new TrustSelfSignedStrategy())
                .build();

        // we can optionally disable hostname verification. 
        // if you don't want to further weaken the security, you don't have to include this.
        HostnameVerifier allowAllHosts = new NoopHostnameVerifier();
        
        // create an SSL Socket Factory to use the SSLContext with the trust self signed certificate strategy
        // and allow all hosts verifier.
        SSLConnectionSocketFactory connectionFactory = new SSLConnectionSocketFactory(sslContext, allowAllHosts);
        
		this.client = HttpClientBuilder.create().setSSLSocketFactory(connectionFactory).build();
	}
	
	public List<String> downloadOpenmrsIds(int size) {
		List<String> ids = new ArrayList<String>();
		String openmrsQueryUrl = this.openmrsUrl + OPENMRS_IDGEN_URL;
		// Add query parameters
		openmrsQueryUrl += "?source=" + this.openmrsSourceId + "&numberToGenerate=" + size;
		openmrsQueryUrl += "&username=" + this.openmrsUserName + "&password=" + this.openmrsPassword;
		
		HttpGet get = new HttpGet(openmrsQueryUrl);
		try {
			HttpResponse response = client.execute(get);
			String jsonResponse = EntityUtils.toString(response.getEntity());
			
			JSONObject responseJson = new JSONObject(jsonResponse);
			JSONArray jsonArray = responseJson.getJSONArray("identifiers");
			
			if (jsonArray != null && jsonArray.length() > 0) {
				for (int i = 0; i < jsonArray.length(); i++) {
					ids.add(jsonArray.getString(i));
				}
			}
		}
		catch (IOException | JSONException e) {
			logger.error("", e);
			return null;
		}
		// import IDs and client data to database together with assignments 
		return ids;
	}
	
	/**
	 * download ids only if the total unused is less than the size specified
	 * 
	 * @param size
	 */
	public void downloadAndSaveIds(int size, String userName) {
		try {
			Integer totalUnUsed = uniqueIdRepository.totalUnUsedIds();
			if (totalUnUsed < size) {
				int numberToGenerate = size - totalUnUsed;
				List<String> ids = downloadOpenmrsIds(numberToGenerate);
				for (String id : ids) {
					UniqueId uniqueId = new UniqueId();
					uniqueId.setCreatedAt(new Date());
					uniqueId.setOpenmrsId(id);
					uniqueId.setUsedBy(userName);
					uniqueId.setStatus(UniqueId.STATUS_NOT_USED);
					uniqueIdRepository.save(uniqueId);
				}
			}
		}
		catch (Exception e) {
			logger.error("", e);
		}
		
	}
	
	public void clearRecords() {
		try {
			uniqueIdRepository.clearTable();
		}
		catch (Exception e) {
			logger.error("", e);
		}
	}
	
	public Boolean checkIfClientExists(Client client) throws SQLException {
		try {
			String location = client.getAddress("usual_residence").getAddressField("address2");
			String checkIfExistQuery = "SELECT count(*) from " + UniqueId.tbName + " WHERE " + UniqueId.COL_USEDBY
			        + " = ? AND " + UniqueId.COL_LOCATION + " = ?";
			String[] args = new String[2];
			args[0] = (String) client.getAttribute(CHILD_REGISTER_CARD_NUMBER);
			args[1] = location;
			
			int rowCount = uniqueIdRepository.checkIfExists(checkIfExistQuery, args);
			
			logger.info("[checkIfClientExists] - Card Number:" + args[0] + " - [Exists] "
			        + (rowCount == 0 ? "false" : "true"));
			
			return rowCount >= 1 ? true : false;
		}
		catch (Exception e) {
			logger.error("", e);
			return null;
		}
	}
	
	public void assignOpenmrsIdToClient(String zeirID, Client client) throws SQLException {
		// create jdbc template to persist the ids
		try {
			String location = client.getAddress("usual_residence").getAddressField("address2");
			
			if (!this.checkIfClientExists(client)) {
				String childRegisterCardNumber = (String) client.getAttribute(CHILD_REGISTER_CARD_NUMBER);
				client.addIdentifier(ZEIR_IDENTIFIER, zeirID);
				UniqueId uniqueId = new UniqueId();
				uniqueId.setOpenmrsId(zeirID);
				uniqueId.setStatus(UniqueId.STATUS_USED);
				uniqueId.setUsedBy(childRegisterCardNumber);
				uniqueId.setLocation(location);
				uniqueId.setCreatedAt(new Date());
				uniqueIdRepository.save(uniqueId);
				logger.info("Assigned " + ZEIR_IDENTIFIER + " to " + client.fullName());
			}
		}
		catch (Exception e) {
			logger.error("", e);
		}
	}
	
	public List<UniqueId> getNotUsedIds(int limit) {
		return uniqueIdRepository.getNotUsedIds(limit);
	}
	
	public List<String> getNotUsedIdsAsString(int limit) {
		return uniqueIdRepository.getNotUsedIdsAsString(limit);
	}
	
	public int[] markIdsAsUsed(List<String> ids) {
		return uniqueIdRepository.markAsUsed(ids);
	}
	
	public List<String> getOpenMRSIdentifiers(String source, String numberToGenerate, String userName, String password)
	    throws JSONException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		 
		List<String> ids = new ArrayList<>();
		String openMRSUrl = this.openmrsUrl + OPENMRS_IDGEN_URL;
		openMRSUrl += "?source=" + this.openmrsSourceId + "&numberToGenerate=" + numberToGenerate;
		openMRSUrl += "&username=" + userName + "&password=" + password;		
		try (CloseableHttpClient httpclient = createAcceptSelfSignedCertificateClient()) {
			HttpGet get = new HttpGet(openMRSUrl);
			HttpResponse response = httpclient.execute(get);
			String jsonResponse = EntityUtils.toString(response.getEntity());
			
			JSONObject responseJson = new JSONObject(jsonResponse);
			JSONArray jsonArray = responseJson.getJSONArray("identifiers");
			
			if (jsonArray != null && jsonArray.length() > 0) {
				for (int i = 0; i < jsonArray.length(); i++) {
					ids.add(jsonArray.getString(i));
				}
			}
			
			return ids;
			
		}
		catch (IOException | JSONException e) {
			logger.error("", e);
			return null;
		}
		
	}
	
	
	private static CloseableHttpClient createAcceptSelfSignedCertificateClient()
            throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
        
        SSLContext sslContext = SSLContextBuilder
                .create()
                .loadTrustMaterial(new TrustSelfSignedStrategy())
                .build();
        
        HostnameVerifier allowAllHosts = new NoopHostnameVerifier();
        SSLConnectionSocketFactory connectionFactory = new SSLConnectionSocketFactory(sslContext, allowAllHosts);
        
        
        return HttpClients
                .custom()
                .setSSLSocketFactory(connectionFactory)
                .build();
    }
}
