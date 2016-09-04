package jackplay.web;

import com.sun.net.httpserver.HttpExchange;
import jackplay.bootstrap.PlayGround;
import jackplay.play.Jack;
import jackplay.bootstrap.Genre;


import java.util.Map;

public class ProgramHandler extends BaseHandler {
    Jack jack;

    public ProgramHandler(Jack jack) {
        this.jack = jack;
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
            default:
                CommonHandling.error_404(http);
        }
    }

    private void redefine(HttpExchange http, Map<String, String> params) throws Exception {
        jack.redefine(new PlayGround(params.get("longMethodName")), params.get("src"));
        CommonHandling.serveBody(http, 200, "Method is redefined");
    }

    private void addTrace(HttpExchange http, Map<String, String> params) throws Exception {
        jack.trace(new PlayGround(params.get("methodFullName")));
        CommonHandling.serveBody(http, 200, "Method trace is added");
    }

    private void undoMethod(HttpExchange http, Map<String, String> params) throws Exception {
        Genre g = Genre.valueOf(params.get("genre"));
        String methodFullName = params.get("methodFullName");
        jack.undoPlay(g, new PlayGround(methodFullName));
        CommonHandling.serveBody(http, 200, getGenreDescriptor(g) + " method is now undone - " + methodFullName);
    }

    private void undoClass(HttpExchange http, Map<String, String> params) throws Exception {
        Genre g = Genre.valueOf(params.get("genre"));
        String className = params.get("classFullName");
        jack.undoClass(g, className);
        CommonHandling.serveBody(http, 200, getGenreDescriptor(g) + " class is now undone - " + className);
    }

    private static String getGenreDescriptor(Genre genre) {
        if (genre == Genre.TRACE) {
            return "Traced";
        } else {
            return "Redefined";
        }
    }
}