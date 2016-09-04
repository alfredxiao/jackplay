package jackplay.web;

import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.util.List;
import java.util.zip.GZIPOutputStream;

class CommonHandling {

    static boolean clientSupportsGzip(HttpExchange http) {
        List<String> accepted = http.getRequestHeaders().get("Accept-Encoding");
        return accepted != null &&
                accepted.size() >= 0 &&
                accepted.get(0).contains("gzip");
    }

    static void serveBody(HttpExchange exchange, int status, String body) throws IOException {
        serveBody(exchange, status, new ByteArrayInputStream(body.getBytes()), clientSupportsGzip(exchange));
    }

    static void serveBody(HttpExchange exchange, int status, InputStream body) throws IOException {
        serveBody(exchange, status, body, clientSupportsGzip(exchange));
    }

    private static void serveBody(HttpExchange exchange, int status, InputStream body, boolean gzip) throws IOException {
        if (gzip) {
            serveGZippedStringBody(exchange, status, body);
        } else {
            exchange.sendResponseHeaders(status, body.available());
            OutputStream os = exchange.getResponseBody();
            copy(body, os);
            os.close();
        }
    }

    private static void serveGZippedStringBody(HttpExchange exchange, int status, InputStream body) throws IOException {
        exchange.getResponseHeaders().add("Content-Encoding", "gzip");
        OutputStream os = exchange.getResponseBody();
        try {
            byte[] compressedData;
            try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream(body.available())) {
                try (GZIPOutputStream zipStream = new GZIPOutputStream(byteStream)) {
                    copy(body, zipStream);
                }
                compressedData = byteStream.toByteArray();
            }

            exchange.sendResponseHeaders(status, compressedData.length);
            os.write(compressedData);
            os.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    static void serveStaticResource(HttpExchange exchange, int status, String resourcePath) throws IOException {
        InputStream resource = loadResource(resourcePath);
        if (resourcePath.endsWith("css")) exchange.getResponseHeaders().add("Content-Type", "text/css");
        if (resourcePath.endsWith("js")) exchange.getResponseHeaders().add("Content-Type", "application/javascript");
        if (resourcePath.endsWith("html")) exchange.getResponseHeaders().add("Content-Type", "text/html");

        serveBody(exchange, status, resource);
    }

    private static void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[128];
        while (inputStream.available() > 0) {
            int read = inputStream.read(buffer);
            outputStream.write(buffer, 0, read);
        }
    }

    private static InputStream loadResource(String resourcePath) throws IOException {
        InputStream resourceStream = CommonHandling.class.getResourceAsStream("/web" + resourcePath);

        if (null == resourceStream) {
            resourceStream = CommonHandling.class.getResourceAsStream("/web/404.html");
        }

        return resourceStream;
    }

    static void error_404(HttpExchange exchange) throws IOException {
        serveStaticResource(exchange, 404, "/web/404.html");
    }

    static void willReturnJson(HttpExchange http) {
        http.getResponseHeaders().add("Content-Type", "application/json");
        http.getResponseHeaders().add("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
        http.getResponseHeaders().add("Pragma", "no-cache"); // HTTP 1.0.
        http.getResponseHeaders().add("Expires", "0"); // Proxies.
    }
}
