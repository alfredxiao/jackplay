package jackplay.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import jackplay.Logger;
import jackplay.play.InformationCenter;

import java.io.IOException;
import java.lang.instrument.Instrumentation;

public class ProgramHandler implements HttpHandler {
    Instrumentation inst;

    public ProgramHandler(Instrumentation inst) {
        this.inst = inst;
    }

    @Override
    public void handle(HttpExchange http) throws IOException {
        try {
            http.getResponseHeaders().add("Content-Type", "application/json");
            CommonHandling.serveStringBody(http, 200, InformationCenter.programAsJson(inst));
        } catch (Exception e) {
            Logger.error(e);
            CommonHandling.serveStringBody(http, 500, e.getMessage());
        }
    }
}