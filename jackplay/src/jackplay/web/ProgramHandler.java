package jackplay.web;

import com.sun.net.httpserver.HttpExchange;
import jackplay.play.InfoCenter;
import jackplay.play.ProgramManager;
import jackplay.bootstrap.Genre;


import java.util.Map;

public class ProgramHandler extends BaseHandler {
    ProgramManager pm;
    InfoCenter infoCenter;

    public ProgramHandler(ProgramManager pm, InfoCenter infoCenter) {
        this.pm = pm;
        this.infoCenter = infoCenter;
    }

    @SuppressWarnings("unchecked")
    public void process(HttpExchange http, String uri, Map<String, String> params) throws Exception {
        switch (getUriPath(uri)) {
            case "/program/addTrace":
                addTrace(http, params);
                break;
            case "/program/redefine":
                redefine(http, params);
                break;
            case "/program/undoClass":
                undoClass(http, params);
                break;
            case "/program/undoMethod":
                undoMethod(http, params);
                break;
            case "/program/currentProgram":
                CommonHandling.willReturnJson(http);
                CommonHandling.serveStringBody(http, 200, JSON.objectToJson(pm.getCurrentProgram()));
                break;
            default:
                CommonHandling.error_404(http);
        }
    }

    private void redefine(HttpExchange http, Map<String, String> params) throws Exception {
        pm.submitMethodRedefinition(params.get("longMethodName"), params.get("src"));
        CommonHandling.serveStringBody(http, 200, "Method Redefined");
    }

    private void addTrace(HttpExchange http, Map<String, String> params) throws Exception {
        pm.submitMethodTrace(params.get("methodFullName"));
        CommonHandling.serveStringBody(http, 200, "Trace Added");
    }

    private void undoMethod(HttpExchange http, Map<String, String> params) throws Exception {
        Genre g = Genre.valueOf(params.get("genre"));
        pm.removeMethodFromProgramAndReplay(g,  params.get("methodFullName"));
        CommonHandling.serveStringBody(http, 200, "Method Undone");
    }

    private void undoClass(HttpExchange http, Map<String, String> params) throws Exception {
        Genre g = Genre.valueOf(params.get("genre"));
        pm.removeClassFromProgramAndReplay(g,  params.get("classFullName"));
        CommonHandling.serveStringBody(http, 200, "class undone");
    }
}