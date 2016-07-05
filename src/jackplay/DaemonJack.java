package jackplay;

import jackplay.play.Composer;
import jackplay.web.WebServer;

import java.lang.instrument.*;

class DaemonJack extends Thread {

    JackOptions options;
    Instrumentation inst;
    Composer composer;

    public DaemonJack(JackOptions options, Instrumentation inst, Composer composer) {
        this.options = options;
        this.inst = inst;
        this.composer = composer;
        this.setDaemon(true);
    }

    public void run() {
        try {
            WebServer web = new WebServer(options, inst, composer);
            web.start();
        } catch (Exception e) {
            System.err.println("error:" + e.getMessage());
        }
    }
}
