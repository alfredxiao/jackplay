package jackplay.web;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import jackplay.Options;
import jackplay.play.Composer;
import jackplay.play.Opera;
import jackplay.play.ProgramManager;

// singleton
public class BoxOffice extends Thread {
    Options options;
    Instrumentation inst;
    Composer composer;
    ProgramManager pm;

    public void init(Opera opera) {
        this.options = opera.getOptions();
        this.inst = opera.getInstrumentation();
        this.composer = opera.getComposer();
        this.pm = opera.getProgramManager();
        super.setDaemon(true);
    }

    public void run() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(options.port()), 0);
            HttpContext context = server.createContext("/", new RootHandler(inst, pm));
            context.getFilters().add(new ParameterFilter());
            server.start();
        } catch(IOException ioe) {
            ioe.printStackTrace(System.err);
        }
    }
}
