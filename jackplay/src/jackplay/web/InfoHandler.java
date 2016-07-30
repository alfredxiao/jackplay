package jackplay.web;

import com.sun.net.httpserver.HttpExchange;
import jackplay.play.InfoCenter;
import jackplay.play.PlayKeeper;

import java.util.Map;

public class InfoHandler extends BaseHandler {
    InfoCenter infoCenter;

    public InfoHandler(InfoCenter infoCenter) {
        this.infoCenter = infoCenter;
    }

    @Override
    public void process(HttpExchange http, String uri, Map<String, String> params) throws Exception {
        switch (getUriPath(uri)) {
            case "/info/traceLogs":
                http.getResponseHeaders().add("Content-Type", "application/json");
                CommonHandling.serveStringBody(http, 200, PlayKeeper.getLogHistoryAsJson());
                break;
            case "/info/loadedMethods":
                http.getResponseHeaders().add("Content-Type", "application/json");
                CommonHandling.serveStringBody(http, 200, infoCenter.loadedMethodsAsJson());
                break;
            case "/info/clearTraceLogs":
                PlayKeeper.clearLogHistory();
                CommonHandling.serveStringBody(http, 200, "OK");
                break;
            default:
                CommonHandling.error_404(http);
        }
    }
}