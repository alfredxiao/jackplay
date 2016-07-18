package jackplay.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import jackplay.Logger;
import jackplay.play.PlayKeeper;

import java.io.IOException;

public class ClearLogHistoryHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange http) throws IOException {
        try {
            PlayKeeper.clearLogHistory();
            CommonHandling.serveStringBody(http, 200, "OK");
        } catch (Exception e) {
            Logger.error(e);
            CommonHandling.serveStringBody(http, 500, e.getMessage());
        }
    }
}