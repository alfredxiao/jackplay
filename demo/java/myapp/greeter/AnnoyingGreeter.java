package myapp.greeter;

import java.util.concurrent.ThreadLocalRandom;

public class AnnoyingGreeter implements Greeter {

    public String greet(String name) {
        int mood = ThreadLocalRandom.current().nextInt(0, 10);
        if (mood < 2) {
            return "Hi buddy";
        } else {
            throw new RuntimeException("What the hell is cloud computing!");
        }
    }
}
