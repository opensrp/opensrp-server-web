package org.opensrp.web;

import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.ReadListener;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CachedBodyServletInputStreamTest {

    @Test
    public void testIsFinishedShouldReturnTrueWhenReadIsDone() throws IOException {
        String content = "This is a test request";
        CachedBodyServletInputStream cachedBodyServletInputStream  = new CachedBodyServletInputStream(content.getBytes(StandardCharsets.UTF_8));
        Assert.assertFalse(cachedBodyServletInputStream.isFinished);
        int data;
        while(( data = cachedBodyServletInputStream.read() )!= -1){
            // do nothing
        }

        Assert.assertTrue(cachedBodyServletInputStream.isFinished);

    }

    @Test
    public void testIsReadyShouldAlwaysReturnTrue(){
        String content = "test";
        CachedBodyServletInputStream cachedBodyServletInputStream = new CachedBodyServletInputStream(content.getBytes(StandardCharsets.UTF_8));
        Assert.assertTrue(cachedBodyServletInputStream.isReady());
    }

    @SneakyThrows
    @Test
    public void testSetReadListenerShouldCallListenerOnDataAvailable(){
        String content = "test";
        CachedBodyServletInputStream cachedBodyServletInputStream = new CachedBodyServletInputStream(content.getBytes(StandardCharsets.UTF_8));
        ReadListener listener  = Mockito.mock(ReadListener.class);
        cachedBodyServletInputStream.setReadListener(listener);
        Mockito.verify(listener).onDataAvailable();
    }

    @SneakyThrows
    @Test
    public void testSetReadListenerShouldCallListenerOnErrorWhenIOExceptionIsThrown(){
        String content = "test";
        CachedBodyServletInputStream cachedBodyServletInputStream = new CachedBodyServletInputStream(content.getBytes(StandardCharsets.UTF_8));
        ReadListener listener  = Mockito.mock(ReadListener.class);
        Mockito.doThrow(new IOException()).when(listener).onDataAvailable();
        cachedBodyServletInputStream.setReadListener(listener);
        Mockito.verify(listener).onError(Mockito.any());
    }

}