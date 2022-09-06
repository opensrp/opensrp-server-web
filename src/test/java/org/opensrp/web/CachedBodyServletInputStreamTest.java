package org.opensrp.web;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CachedBodyServletInputStreamTest extends TestCase {

    @Test
    public void testIsFinishedShouldReturnTrueWhenReadIsDone() throws IOException {
        String content = "This is a test request";
        CachedBodyServletInputStream cachedBodyServletInputStream  = new CachedBodyServletInputStream(content.getBytes(StandardCharsets.UTF_8));
        assertFalse(cachedBodyServletInputStream.isFinished);
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



}