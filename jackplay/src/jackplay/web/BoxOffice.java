package jackplay.web;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import jackplay.Options;
import jackplay.play.InfoCenter;
import jackplay.play.ProgramManager;

// singleton
public class BoxOffice extends Thread {
    Options options;
    ProgramManager pm;
    InfoCenter infoCenter;
    Map<String, HttpHandler> contextMap;

    public void wireUp(Options options, ProgramManager pm, InfoCenter infoCenter) {
        this.options = options;
        this.pm = pm;
        this.infoCenter = infoCenter;

        super.setDaemon(true);
        this.initContextMap();
    }

    private void initContextMap() {
        this.contextMap = new HashMap<>();
        this.contextMap.put("/", new RootHandler());
        this.contextMap.put("/program", new ProgramHandler(this.pm, this.infoCenter));
        this.contextMap.put("/info", new InfoHandler(infoCenter));
    }

    public void run() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(options.port()), 0);
            for (String contextPath : contextMap.keySet()) {
                HttpContext context = server.createContext(contextPath, contextMap.get(contextPath));
                context.getFilters().add(new ParameterFilter());
            }
            server.start();
        } catch(IOException ioe) {
            ioe.printStackTrace(System.err);
        }
    }
}
