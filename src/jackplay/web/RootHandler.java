package jackplay.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import jackplay.JackLogger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.instrument.Instrumentation;

public class RootHandler implements HttpHandler {
    private final Instrumentation inst;

    public RootHandler(Instrumentation inst) {
        this.inst = inst;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String uri = exchange.getRequestURI().toString().substring("/jackplay".length());
        if (uri.isEmpty()) uri = "/index.html";
        JackLogger.debug("URI:" + uri);

        if (uri.startsWith("/play")) {
            new PlayHandler(inst).handle(exchange);
        } else {
            InputStream resource = loadResource(uri);
            exchange.sendResponseHeaders(200, resource.available());
            OutputStream os = exchange.getResponseBody();
            copy(resource, os);
            os.close();
        }
    }

    private static void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[128];
        while (inputStream.available() > 0) {
            int read = inputStream.read(buffer);
            outputStream.write(buffer, 0, read);
        }
    }

    private InputStream loadResource(String uri) throws IOException {
        //InputStream resourceStream = this.getClass().getResourceAsStream("/jackplay/web/resources" + uri);
        InputStream resourceStream = new FileInputStream("/home/alfred/development/jackplay/src/jackplay/web/resources" + uri);
        if (null == resourceStream) {
            resourceStream = this.getClass().getResourceAsStream("/jackplay/web/resources/404.html");
        }

        return resourceStream;
    }
}