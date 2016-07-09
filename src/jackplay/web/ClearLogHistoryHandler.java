package jackplay.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import jackplay.JackLogger;
import jackplay.play.PlayLogger;

import java.io.IOException;

public class ClearLogHistoryHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange t) throws IOException {
        try {
            PlayLogger.clearLogHistory();
            CommonHandling.serveStringBody(t, 200, "OK");
        } catch (Exception e) {
            JackLogger.error(e);
            CommonHandling.serveStringBody(t, 500, e.getMessage());
        }
    }
}