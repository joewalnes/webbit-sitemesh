package org.webbitserver.sitemesh;

import org.sitemesh.config.PathMapper;
import org.sitemesh.content.Content;
import org.sitemesh.content.ContentProcessor;
import org.sitemesh.tagprocessor.util.CharSequenceList;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import org.webbitserver.sitemesh.contentbuffer.BasicSelector;
import org.webbitserver.sitemesh.contentbuffer.BufferedResponse;
import org.webbitserver.sitemesh.contentbuffer.ContentBufferingHandler;
import org.webbitserver.sitemesh.contentbuffer.Selector;
import org.webbitserver.wrapper.HttpResponseWrapper;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.Arrays;

public class SiteMeshHandler extends ContentBufferingHandler {

    private final ContentProcessor contentProcessor;
    private final PathMapper<HttpHandler[]> decorators;

    public SiteMeshHandler(Selector selector,
                           ContentProcessor contentProcessor,
                           PathMapper<HttpHandler[]> decorators) {
        super(selector);
        this.contentProcessor = contentProcessor;
        this.decorators = decorators;
    }

    protected void postProcessBuffer(HttpRequest httpRequest, HttpResponse httpResponse, HttpControl httpControl, CharBuffer buffer)
            throws IOException {

        WebbitSiteMeshContext context = createContext(httpRequest, httpControl, contentProcessor);

        applyDecorator(
                context,
                buffer,
                contentProcessor.build(buffer, context),
                decorators.get(context.getPath()),
                0,
                httpRequest,
                httpResponse,
                httpControl);

    }

    private void applyDecorator(final WebbitSiteMeshContext context, final CharBuffer original, final Content content,
                                final HttpHandler[] decoratorHandlers, final int currentDecorator,
                                final HttpRequest httpRequest, final HttpResponse httpResponse, final HttpControl httpControl) {
        if (content == null) {
            // Content could not be parsed: Write original content
            httpResponse.content(original.toString());
        } else {

            // Apply decorator...
            HttpHandler decoratorHandler = decoratorHandlers[currentDecorator];
            try {
                Selector selector = new BasicSelector() {
                    @Override
                    public boolean shouldBufferForContentType(String contentType, String mimeType, String encoding) {
                        return true; // We know we should buffer.
                    }
                };
                decoratorHandler.handleHttpRequest(httpRequest, new BufferedResponse(selector, httpResponse) {

                    {
                        enableBuffering();
                    }

                    @Override
                    public HttpResponseWrapper header(String name, String value) {
                        if (name.equalsIgnoreCase("content-type")) {
                            return this;
                        } else {
                            return super.header(name, value);
                        }
                    }

                    @Override
                    protected void postProcess(CharBuffer buffer) {

                        try {

                            context.setContentToMerge(content);
                            Content decoratedContent = contentProcessor.build(buffer, context);

                            int nextDecorator = currentDecorator + 1;
                            if (nextDecorator < decoratorHandlers.length) {
                                // There are more decorators to be applied. Recurse...
                                applyDecorator(
                                        context,
                                        original,
                                        decoratedContent,
                                        decoratorHandlers,
                                        nextDecorator,
                                        httpRequest,
                                        httpResponse,
                                        httpControl);
                            } else {
                                // No more decorators to apply: Write the decorated result
                                CharSequenceList chars = new CharSequenceList();
                                decoratedContent.getData().writeValueTo(chars);
                                httpResponse.content(chars.toString());
                            }

                        } catch (IOException e) {
                            httpResponse.error(e);
                            return;
                        }

                    }
                }, httpControl);
            } catch (Exception e) {
                httpResponse.error(new IOException("Could not process decorator", e).fillInStackTrace());
            }

        }
    }

    protected WebbitSiteMeshContext createContext(HttpRequest request, HttpControl httpControl, ContentProcessor contentProcessor) {
        return new WebbitSiteMeshContext(request, httpControl, contentProcessor);
    }
}
