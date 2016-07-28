package jackplay.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import jackplay.Logger;
import jackplay.play.ProgramManager;
import jackplay.play.domain.Genre;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.Map;

public class RemoveMethodHandler implements HttpHandler {
    Instrumentation inst;
    ProgramManager pm;

    public RemoveMethodHandler(Instrumentation inst, ProgramManager pm) {
        this.inst = inst;
        this.pm = pm;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handle(HttpExchange http) throws IOException {
        Map<String, Object> params = (Map<String, Object>) http.getAttribute("parameters");
        String methodFullName = (String) params.get("methodFullName");
        String genre = (String) params.get("genre");

        try {
            Genre g = Genre.valueOf(genre);
            pm.removeProgrammedMethod(g, methodFullName);
            CommonHandling.serveStringBody(http, 200, "OK");
        } catch (Exception e) {
            Logger.error(e);
            CommonHandling.serveStringBody(http, 500, e.getMessage());
        }
    }
}