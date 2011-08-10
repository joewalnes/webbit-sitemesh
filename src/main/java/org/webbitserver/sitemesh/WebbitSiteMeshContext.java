package org.webbitserver.sitemesh;

import org.sitemesh.BaseSiteMeshContext;
import org.sitemesh.SiteMeshContext;
import org.sitemesh.content.Content;
import org.sitemesh.content.ContentProcessor;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpRequest;

import java.io.IOException;
import java.io.Writer;

public class WebbitSiteMeshContext extends BaseSiteMeshContext {

    /**
     * Key that the {@link org.sitemesh.content.ContentProperty} is stored under in the {@link org.webbitserver.HttpRequest}
     * attribute. It is "org.sitemesh.content.Content".
     */
    public static final String CONTENT_KEY = Content.class.getName();

    /**
     * Key that the {@link WebbitSiteMeshContext} is stored under in the {@link org.webbitserver.HttpRequest}
     * attribute. It is "org.sitemesh.SiteMeshContext".
     */
    public static final String CONTEXT_KEY = SiteMeshContext.class.getName();

    private final HttpRequest request;
    private final HttpControl httpControl;
    private Content contentToMerge;

    public WebbitSiteMeshContext(HttpRequest request, HttpControl httpControl, ContentProcessor contentProcessor) {
        super(contentProcessor);
        this.request = request;
        this.httpControl = httpControl;
    }

    /**
     * Dispatches to another {@link org.webbitserver.HttpHandler} to render a decorator.
     *
     * <p>The end point can access the {@link org.sitemesh.content.ContentProperty} and {@link SiteMeshContext} by using
     * looking them up as {@link org.webbitserver.HttpRequest} attributes under the keys
     * {@link #CONTENT_KEY} and {@link #CONTEXT_KEY} respectively.</p>
     */
    @Override
    protected void decorate(String decoratorPath, Content content, Writer out) throws IOException {
        throw new UnsupportedOperationException("Not supported in Webbit");
    }

    public String getPath() {
        return getRequestPath(request);
    }

    public static String getRequestPath(HttpRequest request) {
        return request.uri();
    }

    public void setContentToMerge(Content content) {
        this.contentToMerge = content;
    }

    @Override
    public Content getContentToMerge() {
        return contentToMerge;
    }
}
