package jackplay.web;

import java.lang.instrument.Instrumentation;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;
import jackplay.JackOptions;

public class WebServer {
  JackOptions options;
  Instrumentation inst;

  public WebServer(JackOptions options, Instrumentation inst) {
      this.options = options;
      this.inst = inst;
  }

  public void start() throws Exception {
    HttpServer server = HttpServer.create(new InetSocketAddress(options.port()), 0);
    server.createContext("/jackplay", new RootHandler(inst));
    server.start();
  }
}
