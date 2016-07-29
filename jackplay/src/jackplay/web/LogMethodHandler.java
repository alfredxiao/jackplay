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
    @SuppressWarnings("unchecked")
    public void handle(HttpExchange http) throws IOException {
        Map<String, Object> params = (Map<String, Object>) http.getAttribute("parameters");
        String methodFullName = (String) params.get("methodFullName");

        try {
            pm.addPlayAsTracing(methodFullName);
            CommonHandling.serveStringBody(http, 200, "OK");
        } catch (Exception e) {
            Logger.error(e);
            CommonHandling.serveStringBody(http, 500, e.getMessage());
        }
    }
}