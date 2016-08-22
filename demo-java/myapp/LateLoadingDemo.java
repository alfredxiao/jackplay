package myapp;

public class LateLoadingDemo implements Runnable {
    public String echo(String arg) {
        return arg;
    }

    public void run() {
        while (true) {
            Demo.sleepSmallRandom();
            echo("how are you doing, " + Demo.getRandomName() + " ?");
        }
    }
}
