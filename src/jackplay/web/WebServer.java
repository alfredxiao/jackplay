package jackplay.web;

import java.lang.instrument.Instrumentation;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import jackplay.JackOptions;
import jackplay.play.Composer;

// singleton
public class WebServer {
    JackOptions options;
    Instrumentation inst;
    Composer composer;

    public WebServer(JackOptions options, Instrumentation inst, Composer composer) {
        this.options = options;
        this.inst = inst;
        this.composer = composer;
    }

    public void start() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(options.port()), 0);
        HttpContext context = server.createContext("/", new RootHandler(inst, composer));
        context.getFilters().add(new ParameterFilter());
        server.start();
    }
}
