package myapp;

import myapp.greeter.Greeter;
import myapp.greeter.NiceGreeter;
import myapp.greeter.RudeGreeter;
import myapp.greeter.QAGreeter;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Demo implements Runnable {
  static Greeter[] greeters = new Greeter[3];
  static {
    greeters[0] = new NiceGreeter();
    greeters[1] = new RudeGreeter();
    greeters[2] = new QAGreeter();
  }

  public static void main(String[] args) throws Exception {
    int threadCount = (args.length > 0) ? Integer.parseInt(args[0]) : 3;

    for (int i=0; i<threadCount; i++) {
      Thread t = new Thread(new Demo());
      t.start();
    }

    Class clz = Test.class;
  }

  static void visitGreeters() {
    for (Greeter g : greeters) {
      try {
        g.greet("Alfred-" + new Random().nextInt(200));
      } catch (Exception e) {
      }
    }
  }

  public void run() {
    while (true) {
      try {
        Thread.sleep(getRandomSleep());
      } catch(Exception e) {}
      visitGreeters();
    }
  }

  public static long getRandomSleep() {
    return ThreadLocalRandom.current().nextLong(3000, 12000);
  }

  public static void sleepSmallRandom() {
    long r = ThreadLocalRandom.current().nextLong(200, 500);
    try {
      Thread.sleep(r);
    } catch(Exception e) {};
  }
}
