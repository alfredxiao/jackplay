package jackplay.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import jackplay.Logger;
import jackplay.play.ProgramManager;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.Map;

public class RedefineMethodHandler implements HttpHandler {
    Instrumentation inst;
    ProgramManager pm;

    public RedefineMethodHandler(Instrumentation inst, ProgramManager pm) {
        this.inst = inst;
        this.pm = pm;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handle(HttpExchange http) throws IOException {
        Map<String, Object> params = (Map<String, Object>) http.getAttribute("parameters");
        String longMethodName = (String) params.get("longMethodName");
        String newSource = (String) params.get("newSource");

        try {
            pm.addPlayAsRedefinition(longMethodName, newSource);
            CommonHandling.serveStringBody(http, 200, "OK");
        } catch (Exception e) {
            Logger.error(e);
            CommonHandling.serveStringBody(http, 500, e.getMessage());
        }
    }
}