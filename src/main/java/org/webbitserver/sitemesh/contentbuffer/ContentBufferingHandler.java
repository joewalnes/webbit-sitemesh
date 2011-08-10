package org.webbitserver.sitemesh.contentbuffer;

import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

import java.io.IOException;
import java.nio.CharBuffer;

public abstract class ContentBufferingHandler implements HttpHandler {

    protected abstract void postProcessBuffer(HttpRequest httpRequest, HttpResponse httpResponse, HttpControl httpControl, CharBuffer buffer)
            throws IOException;

    private final Selector selector;

    public ContentBufferingHandler(Selector selector) {
        this.selector = selector;
    }

    public Selector selector() {
        return selector;
    }

    public void handleHttpRequest(final HttpRequest httpRequest, final HttpResponse httpResponse, final HttpControl httpControl) throws Exception {

        if (!selector.shouldBufferForRequest(httpRequest)) {
            // Fast bail out, for obviously non-SiteMeshable requests
            httpControl.nextHandler();
            return;
        }

        httpControl.nextHandler(httpRequest, new BufferedResponse(selector(), httpResponse) {
            @Override
            public void postProcess(CharBuffer buffer) {
                try {
                    postProcessBuffer(httpRequest, httpResponse, httpControl, buffer);
                } catch (IOException e) {
                    httpResponse.error(e);
                }
            }
        });
    }

}

