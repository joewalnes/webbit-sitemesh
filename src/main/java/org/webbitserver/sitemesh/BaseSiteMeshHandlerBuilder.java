package org.webbitserver.sitemesh;

import org.sitemesh.DecoratorSelector;
import org.sitemesh.builder.BaseSiteMeshBuilder;
import org.sitemesh.config.PathMapper;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.handler.StringHttpHandler;
import org.webbitserver.sitemesh.contentbuffer.BasicSelector;
import org.webbitserver.sitemesh.contentbuffer.Selector;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Functionality for building a {@link SiteMeshHandler}.
 * Inherits common functionality from {@link org.sitemesh.builder.BaseSiteMeshBuilder}.
 *
 * <p>Clients should use the concrete {@link SiteMeshHandlerBuilder} implementation.</p>
 *
 * @see BaseSiteMeshBuilder
 * @see SiteMeshHandler
 *
 * @param <BUILDER> The type to return from the builder methods. Subclasses
 *                  should type this as their own class type.
 *
 * @author Joe Walnes
 */
public abstract class BaseSiteMeshHandlerBuilder<BUILDER extends BaseSiteMeshBuilder>
        extends BaseSiteMeshBuilder<BUILDER, WebbitSiteMeshContext, HttpHandler> {

    private Collection<String> mimeTypes;

    private PathMapper<HttpHandler[]> decorators = new PathMapper<HttpHandler[]>();
    private PathMapper<Boolean> excludesMapper = new PathMapper<Boolean>();
    private Selector customSelector;

    /**
     * Create the SiteMesh Handler.
     */
    @Override
    public abstract HttpHandler create();

    /**
     * See {@link BaseSiteMeshHandlerBuilder#setupDefaults()}.
     * In addition to the parent setup, this also calls {@link #setMimeTypes(String[])} with
     * <code>{"text/html"}</code>.
     */
    @Override
    protected void setupDefaults() {
        super.setupDefaults();
        setMimeTypes("text/html");
    }

    // --------------------------------------------------------------
    // Selector setup.

    /**
     * Add a path to be excluded by SiteMesh.
     */
    public BUILDER addExcludedPath(String exclude) {
        excludesMapper.put(exclude, true);
        return self();
    }

    // --------------------------------------------------------------
    // Selector setup.

    /**
     * Set MIME types that the Filter should intercept. The default
     * is <code>{"text/html"}</code>.
     *
     * <p>Note: The MIME types are ignored if {@link #setCustomSelector(Selector)} is called.</p>
     */
    public BUILDER setMimeTypes(String... mimeTypes) {
        this.mimeTypes = Arrays.asList(mimeTypes);
        return self();
    }

    /**
     * Set MIME types that the Filter should intercept. The default
     * is <code>{"text/html"}</code>.
     *
     * <p>Note: The MIME types are ignored if {@link #setCustomSelector(Selector)} is called.</p>
     */
    public BUILDER setMimeTypes(List<String> mimeTypes) {
        this.mimeTypes = mimeTypes;
        return self();
    }

    /**
     * Set a custom {@link Selector}.
     *
     * <p>Note: If this is called, it will override any MIME types
     * passed to {@link #setMimeTypes(String[])} as these are specific
     * to the default Selector.</p>
     */
    public BUILDER setCustomSelector(Selector selector) {
        this.customSelector = selector;
        return self();
    }

    /**
     * Get configured {@link Selector}.
     */
    public Selector getSelector() {
        if (customSelector != null) {
            return customSelector;
        } else {
            String[] mimeTypesArray = mimeTypes.toArray(new String[mimeTypes.size()]);
            return new BasicSelector(mimeTypesArray) {
                @Override
                public boolean shouldBufferForRequest(HttpRequest request) {
                    String requestPath = WebbitSiteMeshContext.getRequestPath(request);
                    return super.shouldBufferForRequest(request)
                            && excludesMapper.get(requestPath) == null;
                }
            };
        }
    }

    @Override
    public BUILDER addDecoratorPaths(String contentPath, String... decoratorPaths) {
        throw new UnsupportedOperationException("This cannot be used in Webbit. Use decorate() instead.");
    }

    @Override
    public BUILDER addDecoratorPaths(String contentPath, List<String> decoratorPaths) {
        throw new UnsupportedOperationException("This cannot be used in Webbit. Use decorate() instead.");
    }

    @Override
    public BUILDER addDecoratorPath(String contentPath, String decoratorPath) {
        throw new UnsupportedOperationException("This cannot be used in Webbit. Use decorate() instead.");
    }

    @Override
    public BUILDER setCustomDecoratorSelector(DecoratorSelector<WebbitSiteMeshContext> webbitSiteMeshContextDecoratorSelector) {
        throw new UnsupportedOperationException("This cannot be used in Webbit. Use decorate() instead.");
    }

    @Override
    public DecoratorSelector<WebbitSiteMeshContext> getDecoratorSelector() {
        throw new UnsupportedOperationException("This cannot be used in Webbit. Use decorate() instead.");
    }

    public BUILDER decorate(String path, HttpHandler... decorators) {
        this.decorators.put(path, decorators);
        return self();
    }

    public PathMapper<HttpHandler[]> getDecorators() {
        return decorators;
    }

}
