package jackplay.web;

import java.lang.instrument.Instrumentation;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;
import jackplay.JackOptions;
import jackplay.Player;

public class WebServer {
    JackOptions options;
    Instrumentation inst;
    Player player;

    public WebServer(JackOptions options, Instrumentation inst, Player player) {
        this.options = options;
        this.inst = inst;
        this.player = player;
    }

    public void start() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(options.port()), 0);
        server.createContext("/jackplay", new RootHandler(inst, player));
        server.start();
    }
}
