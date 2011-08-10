package org.webbitserver.sitemesh;

import org.webbitserver.HttpHandler;

/**
 * Convenient API for building the main SiteMesh {@link HttpHandler}.
 *
 * <p>This follows the API builder pattern - each method returns a reference to the original builder
 * so they can be chained together. When configured, call the {@link #create()} method which will
 * return the final immutable {@link HttpHandler}.</p>
 *
 * <h3>Examples</h3>
 *
 * <pre>
 * // Simplest example...
 * HttpHandler siteMeshHandler = new SiteMeshHandlerBuilder()
 *     .addDecoratorPath("/*", "/decorator.html")
 *     .create();
 *
 * // A few more options (shows applying multiple decorators to a single page)...
 * HttpHandler siteMeshHandler = new SiteMeshHandlerBuilder()
 *     .addDecoratorPaths("/*", "/decorators/main-layout.html", "/decorators-common-style.html")
 *     .addDecoratorPaths("/admin/*", "/decorators/admin-layout.html", "/decorators-common-style.html")
 *     .addTagRuleBundle(new MyLinkRewriterBundle())
 *     .addExcludePath("/javadoc/*")
 *     .addExcludePath("/portfolio/*")
 *     .create();
 *
 * // If you want to get a bit crazy and totally customize SiteMesh...
 * HttpHandler siteMeshHandler = new SiteMeshHandlerBuilder()
 *     .setMimeTypes("image/svg+xml")
 *     .setCustomContentProcessor(new MySvgContentProcessor())
 *     .setCustomDecoratorSelector(new MyDatabaseDrivenDecoratorSelector())
 *     .create();
 * </pre>
 *
 * <h3>Custom implementations (advanced)</h3>
 *
 * <p>This is only for advanced users who need to change the behavior of the builder...</p>
 * 
 * <p>If you ever find the need to subclass SiteMeshHandlerBuilder (e.g. to add more convenience
 * methods, to change the implementation returned, or add new functionality), it is instead recommended
 * that you extend {@link BaseSiteMeshHandlerBuilder}. This way, the generic type signature can
 * be altered.</p>
 *
 * @author Joe Walnes
 */
public class SiteMeshHandlerBuilder
        extends BaseSiteMeshHandlerBuilder<SiteMeshHandlerBuilder> {

    /**
     * Create the SiteMesh Handler.
     */
    public HttpHandler create() {
        return new SiteMeshHandler(
                getSelector(),
                getContentProcessor(),
                getDecoratorSelector());
    }

}
