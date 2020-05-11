package org.opensrp.web.config.security.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class CrossSiteScriptingPreventionFilter implements Filter {

	private static Logger logger = LoggerFactory.getLogger(CrossSiteScriptingPreventionFilter.class);
	private static ObjectMapper mapper = new ObjectMapper();

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
     // do nothing
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		logger.debug("Inside CrossSiteScriptingPreventionFilter  ...............");
		XssPreventionResponseWrapper xssPreventionResponseWrapper = new XssPreventionResponseWrapper(
				(HttpServletResponse) response);
		chain.doFilter(request, xssPreventionResponseWrapper);
		String responseString = xssPreventionResponseWrapper.getCaptureAsString();
		try {
			if (response.getContentType() != null) {
				if (response.getContentType().contains(MediaType.APPLICATION_JSON_VALUE)) {
					JsonNode jsonNode = mapper.readTree(responseString);
					JsonNode updatedJsonNode = encode(jsonNode);
					String encodedJsonString = updatedJsonNode.toString();
					response.getOutputStream().write(encodedJsonString.getBytes());
				} else {
					response.getOutputStream().write(responseString.getBytes());
				}
			} 
			else {
				response.getOutputStream().write(responseString.getBytes());
			}
		}
		catch (Exception e) {
			logger.error("Exception occurred while sanitizing JSON response");
		}
	}

	@Override
	public void destroy() {
		// do nothing
	}

	private static JsonNode encode(JsonNode node) {
		if (node.isValueNode()) {
			if (JsonNodeType.STRING == node.getNodeType() && !isEscapedString(node)) {
				return JsonNodeFactory.instance.textNode(Encode.forJava(node.asText()));
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
			for (Iterator<Map.Entry<String, JsonNode>> it = node.fields(); it.hasNext(); ) {
				Map.Entry<String, JsonNode> entry = it.next();
				encodedObjectNode.set(Encode.forJava(entry.getKey()), encode(entry.getValue()));
			}
			return encodedObjectNode;
		}
	}

	public static boolean isEscapedString(JsonNode jsonNode) {
		return jsonNode.toString().contains("\\") ? true : false;
	}
}


