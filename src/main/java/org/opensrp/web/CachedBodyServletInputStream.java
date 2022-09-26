package org.opensrp.web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CachedBodyServletInputStream extends ServletInputStream {

    private InputStream cachedBodyInputSteam;
    boolean isFinished = false;
    private Logger logger = LogManager.getLogger(CachedBodyServletInputStream.class.toString());
    public CachedBodyServletInputStream(byte[] cachedBody) {
        this.cachedBodyInputSteam = new ByteArrayInputStream(cachedBody);
    }

    @Override
    public boolean isFinished() {
        return isFinished;
    }

    @Override
    /*
     * We've already copied InputStream in a byte array. Return true to indicate that it's always available
     */
    public boolean isReady() {
        return true;
    }

    @Override
    public void setReadListener(ReadListener readListener) {
        try {
            readListener.onDataAvailable();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            readListener.onError(e);
        }
    }

    @Override
    public int read() throws IOException {
        int data = cachedBodyInputSteam.read();
        if(data == -1)
            isFinished = true;
        return data;
    }
}
