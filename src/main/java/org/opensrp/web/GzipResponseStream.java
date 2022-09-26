package org.opensrp.web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;

/**
 * @author samuelgithengi
 */
public class GzipResponseStream extends ServletOutputStream {

	protected ByteArrayOutputStream baos = null;

	protected GZIPOutputStream gzipstream = null;

	protected boolean closed = false;

	protected HttpServletResponse response = null;

	protected ServletOutputStream output = null;

	private Logger logger = LogManager.getLogger(GzipResponseStream.class.toString());

	public GzipResponseStream(HttpServletResponse response) throws IOException {
		super();
		closed = false;
		this.response = response;
		this.output = response.getOutputStream();
		baos = new ByteArrayOutputStream();
		gzipstream = new GZIPOutputStream(baos);
	}

	public void close() throws IOException {
		if (closed) {
			throw new IOException("This output stream has already been closed");
		}
		gzipstream.finish();

		byte[] bytes = baos.toByteArray();

		response.addHeader("Content-Length", Integer.toString(bytes.length));
		response.addHeader("Content-Encoding", "gzip");
		output.write(bytes);
		output.flush();
		output.close();
		closed = true;
	}

	public void flush() throws IOException {
		if (!closed) {
			gzipstream.flush();
		}

	}

	public void write(int b) throws IOException {
		if (!closed) {
			gzipstream.write((byte) b);
		}
	}

	public void write(byte b[]) throws IOException {
		write(b, 0, b.length);
	}

	public void write(byte b[], int off, int len) throws IOException {
		if (!closed) {
			gzipstream.write(b, off, len);
		}

	}

	public boolean closed() {
		return (this.closed);
	}

	@Override
	public boolean isReady() {
		return true;
	}

	@Override
	public void setWriteListener(WriteListener writeListener) {
		try {
			writeListener.onWritePossible();
		} catch (IOException e){
			logger.error(e.getMessage(), e);
			writeListener.onError(e);
		}
	}
}
