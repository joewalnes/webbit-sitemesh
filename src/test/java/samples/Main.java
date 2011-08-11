package samples;

import org.webbitserver.handler.StaticFileHandler;
import org.webbitserver.handler.StringHttpHandler;
import org.webbitserver.sitemesh.SiteMeshHandlerBuilder;

import java.io.File;
import java.io.IOException;

import static org.webbitserver.WebServers.createWebServer;

public class Main {

    public static void main(String[] args) throws IOException {
        File contentDir = new File(args.length == 0 ? "src/test/java/samples/content" : args[0]);

        System.out.println("Listening on: " +
                createWebServer(1234)
                        .add(new SiteMeshHandlerBuilder()
                                .setDefaultHandler(new StaticFileHandler(contentDir))
                                .addDecoratorPath("/*", "decorators/decorator.html")
                                .create())
                        .add("/hello", new StringHttpHandler("text/html",
                                "<html><body><h1>Hello</h1></body></html>"))
                        .add(new StaticFileHandler(contentDir))
                        .start()
                        .getUri());
    }
}
