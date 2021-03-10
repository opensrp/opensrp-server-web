package org.opensrp.web.config.security.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.owasp.encoder.Encode;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;

public class XssPreventionRequestWrapper extends HttpServletRequestWrapper {
	
	private static Logger logger = LogManager.getLogger(XssPreventionRequestWrapper.class);
	
	private static ObjectMapper mapper = new ObjectMapper();
	
	private byte[] rawData;
	
	private HttpServletRequest request;
	
	private ResettableServletInputStream servletStream;
	
	public XssPreventionRequestWrapper(HttpServletRequest request) {
		super(request);
		this.request = request;
		this.servletStream = new ResettableServletInputStream();
	}
	
	@Override
	public ServletInputStream getInputStream() throws IOException {
		if (rawData == null) {
			rawData = IOUtils.toByteArray(this.request.getInputStream());
			servletStream.stream = new ByteArrayInputStream(rawData);
		}
		updateParameters();
		return servletStream;
	}
	
	@Override
	public BufferedReader getReader() throws IOException {
		if (rawData == null) {
			rawData = IOUtils.toByteArray(this.request.getReader(), StandardCharsets.UTF_8);
			servletStream.stream = new ByteArrayInputStream(rawData);
		}
		updateParameters();
		return new BufferedReader(new InputStreamReader(servletStream));
	}
	
	private void updateParameters() throws IOException {
		String requestBody = new String(rawData, StandardCharsets.UTF_8);
		if (isValidJSON(requestBody)) {
			JsonNode jsonNode = mapper.readTree(requestBody);
			JsonNode updatedJsonNode = encode(jsonNode);
			String encodedJsonString = updatedJsonNode.toString();
			rawData = encodedJsonString.getBytes();
			servletStream.stream = new ByteArrayInputStream(rawData);
		}
	}
	
	private static JsonNode encode(JsonNode node) {
		if (node.isValueNode()) {
			if (JsonNodeType.STRING == node.getNodeType()) {
				return JsonNodeFactory.instance.textNode(Encode.forHtmlContent(node.asText()));
			} else if (JsonNodeType.NULL == node.getNodeType()) {
				return null;
			} else {
				return node;
			}
		} else if (node.isNull()) {
			return null;
		} else if (node.isArray()) {
			ArrayNode arrayNode = (ArrayNode) node;
			ArrayNode cleanedNewArrayNode = mapper.createArrayNode();
			for (JsonNode jsonNode : arrayNode) {
				cleanedNewArrayNode.add(encode(jsonNode));
			}
			return cleanedNewArrayNode;
		} else {
			ObjectNode encodedObjectNode = mapper.createObjectNode();
			for (Iterator<Map.Entry<String, JsonNode>> it = node.fields(); it.hasNext();) {
				Map.Entry<String, JsonNode> entry = it.next();
				encodedObjectNode.set(Encode.forHtmlContent(entry.getKey()), encode(entry.getValue()));
			}
			return encodedObjectNode;
		}
	}
	
	private static boolean isValidJSON(final String json) throws IOException {
		boolean valid = true;
		try {
			mapper.readTree(json);
		}
		catch (JsonProcessingException e) {
			logger.error("Error while processing JSON", e);
			valid = false;
		}
		return valid;
	}
	
	private class ResettableServletInputStream extends ServletInputStream {
		
		private InputStream stream;
		
		@Override
		public int read() throws IOException {
			return stream.read();
		}
	}
	
}
