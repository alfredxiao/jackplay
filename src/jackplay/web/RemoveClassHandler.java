package jackplay.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import jackplay.Logger;
import jackplay.play.ProgramManager;
import jackplay.play.domain.Genre;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.Map;

public class RemoveClassHandler implements HttpHandler {
    Instrumentation inst;
    ProgramManager pm;

    public RemoveClassHandler(Instrumentation inst, ProgramManager pm) {
        this.inst = inst;
        this.pm = pm;
    }

    @Override
    public void handle(HttpExchange http) throws IOException {
        Map<String, String> params = WebUtils.parseParams(http.getRequestURI());
        String className = params.get("classFullName");
        String genre = params.get("genre");

        try {
            Genre g = Genre.valueOf(genre);
            pm.removeProgrammedClass(g, className);
            CommonHandling.serveStringBody(http, 200, "OK");
        } catch (Exception e) {
            Logger.error(e);
            CommonHandling.serveStringBody(http, 500, e.getMessage());
        }
    }
}