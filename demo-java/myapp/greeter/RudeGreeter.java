package myapp.greeter;

import myapp.Demo;

import java.util.concurrent.ThreadLocalRandom;

public class RudeGreeter implements Greeter {

    public String greet(String name) {
        Demo.sleepSmallRandom();

        int mood = Demo.nextInt(0, 20);
        if (mood < 3) {
            return "Who are you?";
        } else if (mood < 6) {
            return "Where are you from? " + name;
        } else if (mood < 9) {
            return "Go away, " + name;
        } else if (mood < 13) {
            RuntimeException e1 = new RuntimeException(name + ", I am sick of this");
            Exception e2 = new Exception("Shit happens without notice given");
            e1.initCause(e2);
            throw e1;
        } else if (mood < 16) {
            throw new RuntimeException(name + ", I don't wanna go to school, I just wanna break the rule.");
        } else {
            throw new RuntimeException(name + ", can you tell me what the hell is cloud computing!");
        }
    }
}
