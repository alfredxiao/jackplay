package jackplay.web;

import jackplay.play.Composer;
import jackplay.JackLogger;

import static jackplay.play.PlayCategory.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.Map;

public class PlayHandler implements HttpHandler {
    Instrumentation inst;
    Composer composer;

    public PlayHandler(Instrumentation inst, Composer composer) {
        this.inst = inst;
        this.composer = composer;
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        Map<String, String> params = WebUtils.parseParams(t.getRequestURI());
        JackLogger.debug("params:" + params);
        JackLogger.debug("className:" + params.get("className"));
        JackLogger.debug("methodName:" + params.get("methodName"));

        try {
            //composer.play(MethodLogging, "Greeter", new String[]{"greet", "beautify"});
            composer.play(MethodLogging, params.get("className"), params.get("methodName"));
            CommonHandling.serveStringBody(t, 200, "OK");
        } catch (Exception e) {
            JackLogger.error(e);
            CommonHandling.serveStringBody(t, 500, e.getMessage());
        }
    }
}