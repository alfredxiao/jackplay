package myapp;

import myapp.greeter.Greeter;
import myapp.greeter.NiceGreeter;
import myapp.greeter.AnnoyingGreeter;
import myapp.greeter.QAGreeter;

import java.util.concurrent.ThreadLocalRandom;

public class Demo {
  static Greeter[] greeters = new Greeter[3];
  static {
    greeters[0] = new NiceGreeter();
    greeters[1] = new AnnoyingGreeter();
    greeters[2] = new QAGreeter();
  }

  public static void main(String[] args) throws Exception {
    visitGreeters();

    while (true) {
      Thread.sleep(getRandomSleep());
      visitGreeters();
    }
  }

  static void visitGreeters() {
    for (Greeter g : greeters) {
      try {
        g.greet("Alfred");
      } catch (Exception e) {
      }
    }
  }

  public static long getRandomSleep() {
    return ThreadLocalRandom.current().nextLong(500, 12000);
  }
}
