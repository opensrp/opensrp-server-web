
package org.opensrp.web;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpMethod;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.io.ByteStreams;
import com.google.common.net.HttpHeaders;

import edu.umd.cs.findbugs.annotations.Nullable;

public class GzipBodyDecompressFilter implements Filter {
	
	private static Logger logger = LogManager.getLogger(GzipBodyDecompressFilter.class.toString());
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {//do nothing
	}
	
	/**
	 * Analyzes servlet request for possible gzipped body. When Content-Encoding header has "gzip"
	 * value and request method is POST we read all the gzipped stream and if it has any data unzip
	 * it. In case when gzip Content-Encoding header specified but body is not actually in gzip
	 * format we will throw ZipException.
	 *
	 * @param servletRequest servlet request
	 * @param servletResponse servlet response
	 * @param chain filter chain
	 * @throws IOException throws when fails
	 * @throws ServletException thrown when fails
	 */
	@Override
	public final void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
	        final FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		boolean isGzipped = request.getHeader(HttpHeaders.CONTENT_ENCODING) != null
		        && request.getHeader(HttpHeaders.CONTENT_ENCODING).contains("gzip");
		HttpMethod httpMethod=HttpMethod.resolve(request.getMethod());
		boolean requestTypeSupported = HttpMethod.POST.equals(httpMethod)
		        || HttpMethod.PUT.equals(httpMethod);
		if (isGzipped && !requestTypeSupported) {
			throw new IllegalStateException(request.getMethod() + " is not supports gzipped body of parameters."
			        + " Only POST requests are currently supported.");
		}
		if (isGzipped && requestTypeSupported) {
			request = new GzippedInputStreamWrapper((HttpServletRequest) servletRequest);
		}
		chain.doFilter(request, response);
		
	}
	
	/**
	 * @inheritDoc
	 */
	@Override
	public final void destroy() {//do nothing
	}
	
	/**
	 * Wrapper class that detects if the request is gzipped and ungzipps it.
	 */
	final class GzippedInputStreamWrapper extends HttpServletRequestWrapper {
		
		/**
		 * Default encoding that is used when post parameters are parsed.
		 */
		public static final String DEFAULT_ENCODING = "UTF-8";
		
		/**
		 * Serialized bytes array that is a result of unzipping gzipped body.
		 */
		private byte[] bytes;
		
		/**
		 * Constructs a request object wrapping the given request. In case if Content-Encoding
		 * contains "gzip" we wrap the input stream into byte array to original input stream has
		 * nothing in it but new wrapped input stream always returns reproducible ungzipped input
		 * stream.
		 *
		 * @param request request which input stream will be wrapped.
		 * @throws java.io.IOException when input stream retrieval failed.
		 */
		public GzippedInputStreamWrapper(final HttpServletRequest request) throws IOException {
			super(request);
			try {
				final InputStream in = new GZIPInputStream(request.getInputStream());
				bytes = ByteStreams.toByteArray(in);
			}
			catch (Exception e) {
				logger.error(e.getMessage(), e);
				bytes = new byte[0];
			}
		}
		
		/**
		 * @return reproduceable input stream that is either equal to initial servlet input
		 *         stream(if it was not zipped) or returns unzipped input stream.
		 * @throws IOException if fails.
		 */
		@Override
		public ServletInputStream getInputStream() throws IOException {
			return new CachedBodyServletInputStream(bytes);
		}
		
		/**
		 * Need to override getParametersMap because we initially read the whole input stream and
		 * servlet container won't have access to the input stream data.
		 *
		 * @return parsed parameters list. Parameters get parsed only when Content-Type
		 *         "application/x-www-form-urlencoded" is set.
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public Map getParameterMap() {
			String contentEncodingHeader = getHeader(HttpHeaders.CONTENT_TYPE);
			if (!Strings.isNullOrEmpty(contentEncodingHeader)
			        && contentEncodingHeader.contains("application/x-www-form-urlencoded")) {
				
				Map params = new HashMap(super.getParameterMap());
				try {
					params.putAll(parseParams(new String(bytes)));
				}
				catch (UnsupportedEncodingException e) {
					logger.error(e.getMessage(), e);
				}
				return params;
			} else {
				return super.getParameterMap();
			}
		}
		
		/**
		 * parses params from the byte input stream.
		 *
		 * @param body request body serialized to string.
		 * @return parsed parameters map.
		 * @throws UnsupportedEncodingException if encoding provided is not supported.
		 */
		private Map<String, String[]> parseParams(final String body) throws UnsupportedEncodingException {
			String characterEncoding = getCharacterEncoding();
			if (null == characterEncoding) {
				characterEncoding = DEFAULT_ENCODING;
			}
			final Multimap<String, String> parameters = ArrayListMultimap.create();
			for (String pair : body.split("&")) {
				if (Strings.isNullOrEmpty(pair)) {
					continue;
				}
				int idx = pair.indexOf("=");
				
				String key = null;
				if (idx > 0) {
					key = URLDecoder.decode(pair.substring(0, idx), characterEncoding);
				} else {
					key = pair;
				}
				String value = null;
				if (idx > 0 && pair.length() > idx + 1) {
					value = URLDecoder.decode(pair.substring(idx + 1), characterEncoding);
				} else {
					value = null;
				}
				parameters.put(key, value);
			}
			return Maps.transformValues(parameters.asMap(), new Function<Collection<String>, String[]>() {
				
				@Nullable
				@Override
				public String[] apply(final Collection<String> input) {
					return Iterables.toArray(input, String.class);
				}
			});
		}
	}
}
