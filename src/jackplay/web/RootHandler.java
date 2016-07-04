package jackplay.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import jackplay.JackLogger;
import jackplay.Player;

import java.io.IOException;
import java.lang.instrument.Instrumentation;

public class RootHandler implements HttpHandler {
    private final Instrumentation inst;
    private final Player player;

    public RootHandler(Instrumentation inst, Player player) {
        this.inst = inst;
        this.player = player;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String uri = exchange.getRequestURI().toString().substring("/jackplay".length());
        if (uri.isEmpty()) uri = "/index.html";
        JackLogger.debug("URI:" + uri);

        if (uri.startsWith("/play")) {
            new PlayHandler(inst, player).handle(exchange);
        } else {
            if ("get".equalsIgnoreCase(exchange.getRequestMethod())) {
                CommonHandling.serveStaticResource(exchange, 200, uri);
            } else {
                CommonHandling.error_404(exchange);
            }
        }
    }
}