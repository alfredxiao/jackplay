package jackplay.web;

import com.sun.net.httpserver.HttpExchange;
import jackplay.play.InfoCenter;
import jackplay.play.TraceKeeper;

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
                CommonHandling.serveStringBody(http, 200, JSON.objectToJson(infoCenter.getTraceLogs()));
                break;
            case "/info/loadedMethods":
                http.getResponseHeaders().add("Content-Type", "application/json");
                CommonHandling.serveStringBody(http, 200, JSON.objectToJson(infoCenter.getLoadedMethods()));
                break;
            case "/info/clearTraceLogs":
                TraceKeeper.clearLogHistory();
                CommonHandling.serveStringBody(http, 200, "OK");
                break;
            default:
                CommonHandling.error_404(http);
        }
    }
}