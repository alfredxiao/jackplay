package jackplay.web;

import jackplay.Logger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import jackplay.play.PlayLogger;

import java.io.IOException;

public class LogHistoryHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange http) throws IOException {
        try {
            http.getResponseHeaders().add("Content-Type", "application/json");
            CommonHandling.serveStringBody(http, 200, PlayLogger.getLogHistoryAsJson());
        } catch (Exception e) {
            Logger.error(e);
            CommonHandling.serveStringBody(http, 500, e.getMessage());
        }
    }
}