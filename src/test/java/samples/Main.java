package samples;

import org.webbitserver.handler.StringHttpHandler;
import org.webbitserver.sitemesh.SiteMeshHandlerBuilder;

import java.io.IOException;

import static org.webbitserver.WebServers.createWebServer;

public class Main {

    public static void main(String[] args) throws IOException {
        System.out.println("Listening on: " +
                createWebServer(1234)
                        .add(new SiteMeshHandlerBuilder()
                                .addDecoratorPath("/*", "/decorator")
                                .create())
                        .add("/hello", new StringHttpHandler("text/html",
                                "<html><body><h1>Hello</h1></body></html>"))
                        .add("/decorator", new StringHttpHandler("text/html",
                                "<html><style>body { background-color: #ffff00 }</style>" +
                                        "<body><sitemesh:write property='body'/></body></html>"))
                        .start()
                        .getUri());
    }
}
