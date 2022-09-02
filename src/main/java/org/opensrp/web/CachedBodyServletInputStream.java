package org.opensrp.web;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CachedBodyServletInputStream extends ServletInputStream {

    private InputStream cachedBodyInputSteam;
    boolean isFinished = false;

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

    }

    @Override
    public int read() throws IOException {
        int data = cachedBodyInputSteam.read();
        if(data == -1)
            isFinished = true;
        return data;
    }
}
