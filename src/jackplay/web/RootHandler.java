package jackplay.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import jackplay.JackLogger;
import jackplay.play.Composer;

import java.io.IOException;
import java.lang.instrument.Instrumentation;

public class RootHandler implements HttpHandler {
    private final Instrumentation inst;
    private final Composer composer;

    public RootHandler(Instrumentation inst, Composer composer) {
        this.inst = inst;
        this.composer = composer;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String uri = exchange.getRequestURI().toString();
        if (uri.isEmpty() || "/".equals(uri)) uri = "/index.html";
        JackLogger.debug("URI:" + uri);

        if (uri.startsWith("/logMethod")) {
            new LogMethodHandler(inst, composer).handle(exchange);
        } else if (uri.startsWith("/logHistory")) {
            new LogHistoryHandler().handle(exchange);
        } else if (uri.startsWith("/clearLogHistory")) {
            new ClearLogHistoryHandler().handle(exchange);
        } else {
            if ("get".equalsIgnoreCase(exchange.getRequestMethod())) {
                CommonHandling.serveStaticResource(exchange, 200, uri);
            } else {
                CommonHandling.error_404(exchange);
            }
        }
    }
}