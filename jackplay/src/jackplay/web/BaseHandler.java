package jackplay.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import jackplay.JackplayLogger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseHandler implements HttpHandler {
    @Override
    final public void handle(HttpExchange http) throws IOException {
        try {
            String uri = http.getRequestURI().toString();
            Map<String, String> params = extractParams(http);
            this.process(http, uri, params);
        } catch (Throwable t) {
            JackplayLogger.error("baseHandler", t);
            CommonHandling.serveBody(http, 500,
                                           (t.getMessage() == null ? t.getClass().getName() : t.getMessage()));
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> extractParams(HttpExchange http) {
        Map<String, Object> paramObjects = (Map<String, Object>) http.getAttribute("parameters");
        Map<String, String> params = new HashMap<>();

        for (String paramName : paramObjects.keySet()) {
            params.put(paramName, (String) paramObjects.get(paramName));
        }

        return params;
    }

    abstract void process(HttpExchange http, String uri, Map<String, String> params) throws Exception;

    public String getUriPath(String uri) {
        int qmark = uri.indexOf("?");
        return (qmark > 0) ? uri.substring(0, qmark) : uri;
    }
}
