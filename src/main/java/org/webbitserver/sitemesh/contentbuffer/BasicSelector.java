package org.webbitserver.sitemesh.contentbuffer;

import org.webbitserver.HttpRequest;

/**
 * Basic implementation of {@link Selector}. Will select OK responses that match a particular
 * MIME type, and (optionally) error pages. It will also only kick in once per request.
 *
 * <p>For more control, this can be subclassed, or replaced with a different implementation of
 * {@link Selector}.
 *
 * @author Joe Walnes
 */
public class BasicSelector implements Selector {

    private static final String ALREADY_APPLIED_KEY = BasicSelector.class.getName() + ".APPLIED_ONCE";

    private final String[] mimeTypesToBuffer;
    private final boolean includeErrorPages;

    public BasicSelector(String... mimeTypesToBuffer) {
        this(false, mimeTypesToBuffer);
    }

    public BasicSelector(boolean includeErrorPages, String... mimeTypesToBuffer) {
        this.mimeTypesToBuffer = mimeTypesToBuffer;
        this.includeErrorPages = includeErrorPages;
    }

    public boolean shouldBufferForContentType(String contentType, String mimeType, String encoding) {
        if (mimeType == null) {
            return false;
        }
        for (String mimeTypeToBuffer : mimeTypesToBuffer) {
            if (mimeTypeToBuffer.equalsIgnoreCase(mimeType)) {
                return true;
            }
        }
        return false;
    }

    public boolean shouldAbortBufferingForHttpStatusCode(int statusCode) {
        return !(statusCode == 200 || includeErrorPages && statusCode >= 400);
    }

    public boolean shouldBufferForRequest(HttpRequest request) {
        return !filterAlreadyAppliedForRequest(request);
    }

    protected boolean filterAlreadyAppliedForRequest(HttpRequest request) {
        if (Boolean.TRUE.equals(request.data(ALREADY_APPLIED_KEY))) {
            return true;
        } else {
            request.data(ALREADY_APPLIED_KEY, true);
            return false;
        }
    }

}
