package org.webbitserver.sitemesh;

import org.sitemesh.DecoratorSelector;
import org.sitemesh.content.Content;
import org.sitemesh.content.ContentProcessor;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import org.webbitserver.sitemesh.contentbuffer.ContentBufferingHandler;
import org.webbitserver.sitemesh.contentbuffer.Selector;

import java.io.IOException;
import java.nio.CharBuffer;

public class SiteMeshHandler extends ContentBufferingHandler {

    private final ContentProcessor contentProcessor;
    private final DecoratorSelector<WebbitSiteMeshContext> decoratorSelector;

    public SiteMeshHandler(Selector selector,
                           ContentProcessor contentProcessor,
                           DecoratorSelector<WebbitSiteMeshContext> decoratorSelector) {
        super(selector);
        this.contentProcessor = contentProcessor;
        this.decoratorSelector = decoratorSelector;
    }

    protected void postProcessBuffer(HttpRequest httpRequest, HttpResponse httpResponse, CharBuffer buffer)
            throws IOException {

        if (buffer == null) {

        }
        WebbitSiteMeshContext context = createContext(httpRequest, contentProcessor);

        // Parse page contents
        Content content = contentProcessor.build(buffer, context);

        // Apply decorators
        if (content != null) {
            for (String decoratorPath : decoratorSelector.selectDecoratorPaths(content, context)) {
                // TODO: Make this bit non-blocking
                content = context.decorate(decoratorPath, content);
            }
        }

        System.out.println("content = " + content);

        if (content != null) {
            // Write decorated response
            httpResponse.content(content.getData().getValue());
        } else {
            // Write original content
            httpResponse.content(buffer.toString());
        }
    }

    protected WebbitSiteMeshContext createContext(HttpRequest request, ContentProcessor contentProcessor) {
        return new WebbitSiteMeshContext(request, contentProcessor);
    }
}
