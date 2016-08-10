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
    visitGreeters();

    Thread t1 = new Thread(new Demo());
    t1.start();

    Thread t2 = new Thread(new Demo());
    t2.start();

    Thread t3 = new Thread(new Demo());
    t3.start();
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
    return ThreadLocalRandom.current().nextLong(500, 12000);
  }

  public static void sleepSmallRandom() {
    long r = ThreadLocalRandom.current().nextLong(50, 500);
    try {
      Thread.sleep(r);
    } catch(Exception e) {};
  }
}
