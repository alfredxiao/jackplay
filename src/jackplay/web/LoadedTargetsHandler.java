package jackplay.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import jackplay.JackLogger;
import jackplay.play.InformationCenter;
import jackplay.play.PlayLogger;

import java.io.IOException;
import java.lang.instrument.Instrumentation;

public class LoadedTargetsHandler implements HttpHandler {
    Instrumentation inst;

    public LoadedTargetsHandler(Instrumentation inst) {
        this.inst = inst;
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        try {
            t.getResponseHeaders().add("Content-Type", "application/json");
            CommonHandling.serveStringBody(t, 200, InformationCenter.loadedClassesAsJson(inst));
        } catch (Exception e) {
            JackLogger.error(e);
            CommonHandling.serveStringBody(t, 500, e.getMessage());
        }
    }
}