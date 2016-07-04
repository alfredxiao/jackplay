package jackplay;

import jackplay.web.WebServer;

import java.lang.instrument.*;

class DaemonJack extends Thread {

  JackOptions options;
  Instrumentation inst;

  public DaemonJack(JackOptions options, Instrumentation inst) {
    this.options = options;
    this.inst = inst;
    this.setDaemon(true);
  }

  public void run() {
    try {
      WebServer web = new WebServer(options, inst);
      web.start();
    } catch(Exception e) {
      System.err.println("error:" + e.getMessage());
    }
  }
}
