Webbit SiteMesh
===============

Together at last. The only 2 Java web-frameworks you really need.

*SiteMesh* [www.sitemesh.org](http://www.sitemesh.org/) is a web-page 
composition mechanism, allowing for clear separation of HTML based 
content (documents, forms, etc) from presentation (layout, common
branding, navigation, etc).

*Webbit* [www.webbitserver.org](http://www.webbitserver.org/) is a simple
and scalable non-blocking event based web-server.

*Webbit-Sitemesh* allows SiteMesh to be used in a Webbit server.

Quick Start
-----------

### Step 1: Create some Content

First let's create a simple web-server serving static content using
Webbit.

    import static org.webbitserver.WebServers.createWebServer;
    import org.webbitserver.handler.StaticFileHandler;

    public class Sample {

      public static void main(String[] args) throws Exception {
        createWebServer(1234)
           .add(new StaticFileHandler("/path/to/html"))
           .start();
      }

    }

And some content `index.html`:

    <html>
      <head>
        <title>My page</title>
      </head>
      <body>
        <h1>Hello World!</h1>
        <ul>
          <li>Blah</li>
          <li>Blah</li>
          <li>Blah</li>
        </ul>
      </body>
    </html>

Run the server and visit: [http://localhost:1234](http://localhost:1234/).
You'll see the static page served up as expected.

Try creating some more pages. If you're familiar with Webbit, try
creating some custom HttpHandlers to serve up dynamic content. Go nuts.
I won't judge you.

### Step 2: Create a Decorator

We now want to create a common look and feel for our pages. In SiteMesh
terms, we call this a *decorator*. As page requests come in, SiteMesh
will allow the *content* to be served, but intercepts the HTML on the
way back from the server and _merges_ it with the decorator.

A decorator is an HTML page that acts as a template for common layout to
be applied across the site.

Let's create `decorators/simple.html`:

    <!DOCTYPE html>
    <html>
      <head>
        <title><sitemesh:write property="title"/></title>
        <sitemesh:write property="head"/>
        <style>
          body     { background-color: black; color: white; font-family: sans-serif; }
          a        { color: yellow; }
          .content { border: 2px solid #333; margin: 100px 0 30px 0; padding: 10px; }
          header   { position: absolute; top: 0; left: 0; right: 0; height: 100px;
          footer   { position: absolute; bottom: 0; left: 0; right: 0; height: 30px;
                     text-align: center; background-color: #333; }
        </style>
      </head>
      <body>
        <header>
          <h1>My common header</h1>
          <h2><sitemesh:write property="title"/></h2>
        </header>
        <div id="content">
          <sitemesh:write property="body/>
        </div>
        <footer>My common footer</footer>
      </body>
    </html>

This page contains the layout. It should contain your common page
branding, navigation, header, footer, CSS, layout etc. The only thing it
misses is the actual content - this is inserted dynamically using the
`<sitemesh:write property="..."/>` tags.

The `<sitemesh:write property="..."/>` tag extracts values from the
original requested page. Typically there are 3 properties you care
about:

* `title`: The contents of the original `<title>...</title>` element.
* `head`: The contents of the original `<head>...</head>` element
  (excluding the `<title>` element). This may contain page specific CSS,
  JavaScript etc.
* `body`: The contents of the original `<body>...</body>` element.

There are other properties available and you can extend SiteMesh to add
your own, but the majority of SiteMesh apps only use these 3, so we
won't cover them here.

### Step 3: Wire up your Decorator to your Content

Now we tell Webbit to apply the decorator to certain requests for
content:

    import static org.webbitserver.WebServers.createWebServer;
    import org.webbitserver.handler.StaticFileHandler;
    import org.webbitserver.sitemesh.SiteMeshHandlerBuilder;

    public class Sample {

      public static void main(String[] args) throws Exception {
        createWebServer(1234)
           .add(new SiteMeshHandlerBuilder()
                    .setDefaultHandler(new StaticFileHandler(
                        "/path/to/html/decorators"))
                    .addDecoratorPath("/*", "decorator.html")
                    .create())
           .add(new StaticFileHandler("/path/to/html"))
           .start();
      }

    }

What's going on here?

*    `SiteMeshHandlerBuilder` is a class that can simplify the configuration
    of SiteMesh for common uses.
*    `addDecoratorPath(String pattern, String decorator)` is used to specify that
    all requests to `/*` (i.e. everything) will have the decorator
    `decorator.html` applied. 
*   `setDefaultHandler(HttpHandler)` is used to specify the Webbit
    HttpHandler to serve the decorator. In this case we just use static
    files from the `decorators` directory, but these could also be
    dynamically generated.

Restart the server and return to: [http://localhost:1234](http://localhost:1234/).

Woah! It's magic. The content and the decorator have merged. Viewing the
source would confirm this:

    <!DOCTYPE html>
    <html>
      <head>
        <title>My page</title>
        <style>
          body     { background-color: black; color: white; font-family: sans-serif; }
          a        { color: yellow; }
          .content { border: 2px solid #333; margin: 100px 0 30px 0; padding: 10px; }
          header   { position: absolute; top: 0; left: 0; right: 0; height: 100px;
          footer   { position: absolute; bottom: 0; left: 0; right: 0; height: 30px;
                     text-align: center; background-color: #333; }
        </style>
      </head>
      <body>
        <header>
          <h1>My common header</h1>
          <h2>My page</h2>
        </header>
        <div id="content">
          <h1>Hello World!</h1>
          <ul>
            <li>Blah</li>
            <li>Blah</li>
            <li>Blah</li>
          </ul>
        </div>
        <footer>My common footer</footer>
      </body>
    </html>

That's the basics of SiteMesh.

