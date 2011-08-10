package org.webbitserver.sitemesh.contentbuffer;

import org.sitemesh.webapp.contentfilter.io.HttpContentType;
import org.webbitserver.HttpResponse;
import org.webbitserver.wrapper.HttpResponseWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

public abstract class BufferedResponse extends HttpResponseWrapper {

    protected abstract void postProcess(CharBuffer buffer);

    private static final Charset UTF8 = Charset.forName("UTF8");

    private final Selector selector;

    private ByteArrayOutputStream buffer = null;
    private boolean bufferingWasDisabled = false;

    public BufferedResponse(Selector selector, HttpResponse response) {
        super(response);
        this.selector = selector;
    }

    /**
     * Enable buffering for this request. Subsequent content will be written to the buffer
     * instead of the original response.
     */
    protected void enableBuffering() {
        if (buffer != null) {
            return; // Already buffering.
        }
        buffer = new ByteArrayOutputStream(1024);
    }

    /**
     * Disable buffering for this request. Subsequent content will be written to the original
     * response.
     */
    protected void disableBuffering() {
        buffer = null;
        bufferingWasDisabled = true;
    }

    @Override
    public HttpResponseWrapper header(String name, String value) {
        String lower = name.toLowerCase();
        if (lower.equals("content-type")) {
            setContentType(value);
        } else if (buffer == null || !lower.equals("content-length")) {
            super.header(name, value);
        }
        return this;
    }

    private void setContentType(String type) {
        HttpContentType httpContentType = new HttpContentType(type);
        if (selector.shouldBufferForContentType(type, httpContentType.getType(), httpContentType.getEncoding())) {
            enableBuffering();
        } else {
            disableBuffering();
        }
    }

    @Override
    public HttpResponseWrapper header(String name, long value) {
        String lower = name.toLowerCase();
        if (buffer == null || !lower.equals("content-length")) {
            return super.header(name, value);
        }
        return this;
    }

    @Override
    public HttpResponseWrapper status(int status) {
        abortBufferingIfBadStatusCode(status);
        return super.status(status);
    }

    @Override
    public HttpResponseWrapper error(Throwable error) {
        abortBufferingIfBadStatusCode(500);
        return super.error(error);
    }

    @Override
    public HttpResponseWrapper content(String content) {
        try {
            if (buffer == null) {
                return super.content(content);
            } else {
                buffer.write(content.getBytes(charset()));
                return this;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public HttpResponseWrapper write(String content) {
        try {
            if (buffer == null) {
                return super.write(content);
            } else {
                buffer.write(content.getBytes(UTF8));
                return this;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public HttpResponseWrapper content(byte[] content) {
        try {
            if (buffer == null) {
                return super.content(content);
            } else {
                buffer.write(content);
                return this;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public HttpResponseWrapper end() {
        postProcess(buffer == null ?
                null :
                CharBuffer.wrap(new String(buffer.toByteArray(), charset())));
        return super.end();
    }

    protected void abortBufferingIfBadStatusCode(int statusCode) {
        if (selector.shouldAbortBufferingForHttpStatusCode(statusCode)) {
            disableBuffering();
        }
    }

}
