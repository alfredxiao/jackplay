package jackplay.web;

import com.sun.net.httpserver.HttpExchange;
import jackplay.play.InfoCenter;
import jackplay.model.TraceKeeper;

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
                CommonHandling.willReturnJson(http);
                CommonHandling.serveBody(http, 200, JSON.objectToJson(TraceKeeper.getTraces()));
                break;
            case "/info/loadedMethods":
                CommonHandling.willReturnJson(http);
                CommonHandling.serveBody(http, 200, JSON.objectToJson(infoCenter.allModifiableMethods()));
                break;
            case "/info/clearTraceLogs":
                TraceKeeper.clearLogHistory();
                CommonHandling.serveBody(http, 200, "OK");
                break;
            case "/info/settings":
                CommonHandling.willReturnJson(http);
                CommonHandling.serveBody(http, 200, JSON.objectToJson(infoCenter.getConfigurableOptions()));
                break;
            case "/info/updateSettings":
                CommonHandling.willReturnJson(http);
                infoCenter.configOption("traceLogLimit", params.get("traceLogLimit"));
                infoCenter.configOption("autoSuggestLimit", params.get("autoSuggestLimit"));
                infoCenter.configOption("intervalSyncTraceLogs", params.get("intervalSyncTraceLogs"));
                infoCenter.configOption("intervalSyncModifiableMethods", params.get("intervalSyncModifiableMethods"));
                infoCenter.configOption("traceStringLength", params.get("traceStringLength"));
                infoCenter.configOption("traceArrayLength", params.get("traceArrayLength"));
                CommonHandling.serveBody(http, 200, JSON.objectToJson(infoCenter.getConfigurableOptions()));
                break;
            case "/info/currentProgram":
                CommonHandling.willReturnJson(http);
                CommonHandling.serveBody(http, 200, JSON.objectToJson(infoCenter.getCurrentProgram()));
                break;
            default:
                CommonHandling.error_404(http);
        }
    }
}