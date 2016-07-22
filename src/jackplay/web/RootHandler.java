package jackplay.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import jackplay.play.ProgramManager;

import java.io.IOException;
import java.lang.instrument.Instrumentation;

public class RootHandler implements HttpHandler {
    private final Instrumentation inst;
    private final ProgramManager pm;

    public RootHandler(Instrumentation inst, ProgramManager pm) {
        this.inst = inst;
        this.pm = pm;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String uri = exchange.getRequestURI().toString();
        if (uri.isEmpty() || "/".equals(uri)) uri = "/index.html";

        if (uri.startsWith("/logMethod")) {
            new LogMethodHandler(inst, pm).handle(exchange);
        } else if (uri.startsWith("/logHistory")) {
            new LogHistoryHandler().handle(exchange);
        } else if (uri.startsWith("/clearLogHistory")) {
            new ClearLogHistoryHandler().handle(exchange);
        } else if (uri.startsWith("/loadedTargets")) {
            new LoadedTargetsHandler(inst).handle(exchange);
        } else if (uri.startsWith("/program")) {
            new ProgramHandler(inst).handle(exchange);
        } else if (uri.startsWith("/redefineMethod")) {
            new RedefineMethodHandler(inst, pm).handle(exchange);
        } else if (uri.startsWith("/removeMethod")) {
            new RemoveMethodHandler(inst, pm).handle(exchange);
        } else if (uri.startsWith("/removeClass")) {
            new RemoveClassHandler(inst, pm).handle(exchange);
        } else {
            if ("get".equalsIgnoreCase(exchange.getRequestMethod())) {
                CommonHandling.serveStaticResource(exchange, 200, uri);
            } else {
                CommonHandling.error_404(exchange);
            }
        }
    }
}