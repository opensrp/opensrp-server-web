package org.opensrp.web.config.security.filter;

import com.google.gson.*;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class CrossSiteScriptingPreventionFilter implements Filter {

	private static Logger logger = LoggerFactory.getLogger(CrossSiteScriptingPreventionFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		logger.debug("Inside CrossSiteScriptingPreventionFilter  ...............");
		XssPreventionResponseWrapper xssPreventionResponseWrapper = new XssPreventionResponseWrapper(
				(HttpServletResponse) response);
		chain.doFilter(request, xssPreventionResponseWrapper);
		try {
			String responseString = xssPreventionResponseWrapper.getCaptureAsString();
			JsonElement jsonElement = new JsonParser().parse(responseString);
			JsonElement updatedJsonElement = encode(jsonElement);
			String encodedJsonString = updatedJsonElement.toString();

			if (response.getContentType().contains(MediaType.APPLICATION_JSON_VALUE)) {
				response.getOutputStream().write(encodedJsonString.getBytes());
			} else {
				response.getOutputStream().write(responseString.getBytes());
			}
		}
		catch (Exception e) {
			logger.error("Exception occurred while sanitizing JSON response");
		}
	}

	@Override
	public void destroy() {

	}

	private static JsonElement encode(JsonElement element) {
		if (element.isJsonPrimitive()) {
			JsonPrimitive primitive = element.getAsJsonPrimitive();
			if (primitive != null && primitive.isString() && !isEscapedString(primitive)) {
				return new JsonPrimitive(Encode.forJava(primitive.getAsString()));
			} else {
				return primitive;
			}
		} else if (element.isJsonArray()) {
			JsonArray jsonArray = element.getAsJsonArray();
			JsonArray cleanedNewArray = new JsonArray();
			for (JsonElement jsonElement : jsonArray) {
				cleanedNewArray.add(encode(jsonElement));
			}
			return cleanedNewArray;
		} else if (element.isJsonNull()) {
			return element.getAsJsonNull();
		} else {
			JsonObject obj = element.getAsJsonObject();
			JsonObject encodedJsonObject = new JsonObject();
			for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
				encodedJsonObject.add(Encode.forJava(entry.getKey()), encode(entry.getValue()));
			}
			return encodedJsonObject;
		}
	}

	public static boolean isEscapedString(JsonPrimitive primitive) {
		return primitive.toString().contains("\\") ? true : false;
	}
}


