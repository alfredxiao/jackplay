package jackplay;

import jackplay.web.WebServer;

import java.lang.instrument.*;

class DaemonJack extends Thread {

    JackOptions options;
    Instrumentation inst;
    Player player;

    public DaemonJack(JackOptions options, Instrumentation inst, Player player) {
        this.options = options;
        this.inst = inst;
        this.player = player;
        this.setDaemon(true);
    }

    public void run() {
        try {
            WebServer web = new WebServer(options, inst, player);
            web.start();
        } catch (Exception e) {
            System.err.println("error:" + e.getMessage());
        }
    }
}
