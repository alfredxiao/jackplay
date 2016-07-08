package jackplay.web;

import jackplay.JackLogger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import jackplay.play.PlayLogger;

import java.io.IOException;

public class LogHistoryHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange t) throws IOException {
        try {
            CommonHandling.serveStringBody(t, 200, PlayLogger.getLogHistoryAsJson());
        } catch (Exception e) {
            JackLogger.error(e);
            CommonHandling.serveStringBody(t, 500, e.getMessage());
        }
    }
}