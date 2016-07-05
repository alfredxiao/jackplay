package jackplay.web;

import com.sun.net.httpserver.HttpExchange;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CommonHandling {

    public static void serveStringBody(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes();
        exchange.sendResponseHeaders(status, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    public static void serveStaticResource(HttpExchange exchange, int status, String resourcePath) throws IOException {
        InputStream resource = loadResource(resourcePath);
        exchange.sendResponseHeaders(status, resource.available());
        OutputStream os = exchange.getResponseBody();
        copy(resource, os);
        os.close();
    }

    private static void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[128];
        while (inputStream.available() > 0) {
            int read = inputStream.read(buffer);
            outputStream.write(buffer, 0, read);
        }
    }

    private static InputStream loadResource(String resourcePath) throws IOException {
        //InputStream resourceStream = this.getClass().getResourceAsStream("/web/resources" + resourcePath);
        InputStream resourceStream = new FileInputStream("/home/alfred/development/jackplay/src/jackplay/web/resources" + resourcePath);

        if (null == resourceStream) {
            resourceStream = CommonHandling.class.getResourceAsStream("/web/resources/404.html");
        }

        return resourceStream;
    }

    static void error_404(HttpExchange exchange) throws IOException {
        serveStaticResource(exchange, 404, "/web/resources/404.html");
    }
}
