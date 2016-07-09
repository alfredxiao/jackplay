package jackplay.web;

import jackplay.play.Composer;
import jackplay.JackLogger;

import jackplay.play.PlayGround;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.Map;

public class LogMethodHandler implements HttpHandler {
    Instrumentation inst;
    Composer composer;

    public LogMethodHandler(Instrumentation inst, Composer composer) {
        this.inst = inst;
        this.composer = composer;
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        Map<String, String> params = WebUtils.parseParams(t.getRequestURI());
        String playGroundParam = params.get("playGround");

        JackLogger.debug("playGround:" + playGroundParam);

        try {
            PlayGround playGround = new PlayGround(playGroundParam);
            composer.logMethod(playGround);
            CommonHandling.serveStringBody(t, 200, "OK");
        } catch (Exception e) {
            JackLogger.error(e);
            CommonHandling.serveStringBody(t, 500, e.getMessage());
        }
    }
}