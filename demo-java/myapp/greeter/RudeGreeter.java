package myapp.greeter;

import myapp.Demo;

import java.util.concurrent.ThreadLocalRandom;

public class RudeGreeter implements Greeter {

    public String greet(String name) {
        Demo.sleepSmallRandom();

        int mood = ThreadLocalRandom.current().nextInt(0, 10);
        if (mood < 2) {
            return "Who are you?";
        } else if (mood < 4) {
            return "What are you doing?" + name;
        } else if (mood < 8) {
            return "Go away, " + name;
        } else {
            throw new RuntimeException(name + ", can you tell me what the hell is cloud computing!");
        }
    }
}
