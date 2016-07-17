package jackplay.web;

import jackplay.Logger;

import jackplay.play.ProgramManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.Map;

public class LogMethodHandler implements HttpHandler {
    Instrumentation inst;
    ProgramManager pm;

    public LogMethodHandler(Instrumentation inst, ProgramManager pm) {
        this.inst = inst;
        this.pm = pm;
    }

    @Override
    public void handle(HttpExchange http) throws IOException {
        Map<String, String> params = WebUtils.parseParams(http.getRequestURI());
        String longMethodName = params.get("longMethodName");

        try {
            pm.addPlayAsTracing(longMethodName);
            CommonHandling.serveStringBody(http, 200, "OK");
        } catch (Exception e) {
            Logger.error(e);
            CommonHandling.serveStringBody(http, 500, e.getMessage());
        }
    }
}