package jackplay.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import jackplay.JackLogger;
import jackplay.play.Composer;
import jackplay.play.PlayGround;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.Map;

public class RedefineMethodHandler implements HttpHandler {
    Instrumentation inst;
    Composer composer;

    public RedefineMethodHandler(Instrumentation inst, Composer composer) {
        this.inst = inst;
        this.composer = composer;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handle(HttpExchange t) throws IOException {
        Map<String, Object> params = (Map<String, Object>) t.getAttribute("parameters");
        String playGroundParam = (String) params.get("playGround");
        String newSource = (String) params.get("newSource");
        JackLogger.debug("playGround:" + playGroundParam);
        JackLogger.debug("newSource:" + newSource);

        try {
            PlayGround playGround = new PlayGround(playGroundParam);
            composer.redefineMethod(playGround, newSource);
            CommonHandling.serveStringBody(t, 200, "OK");
        } catch (Exception e) {
            JackLogger.error(e);
            CommonHandling.serveStringBody(t, 500, e.getMessage());
        }
    }
}