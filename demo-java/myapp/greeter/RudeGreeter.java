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
        } else {
            throw new RuntimeException(name + ", " + getRandomCommentsOnLife());
        }
    }

    public static void main(String[] args) {
        Class c = QAGreeter.class;
        System.out.println( QAGreeter.QAProtectedInnerClass.class.getCanonicalName() );
        System.out.println( QAGreeter.QAProtectedInnerClass.class.getName() );
    }

    final static String[] COMMENTS_ON_LIFE = new String[]
            {"Can you tell me what the hell is cloud computing!",
             "I don't wanna go to school, I just wanna break the rule.",
             "Sometimes you win, sometimes you learn.",
             "Life is simple, but we insist on making it complicated.",
             "Life is too short to be following the rules",
             "Life is simple, it is just not easy",
             "Catch the exception or perish.",
             "Rules and models destroy genius and art.",
             "I believe in rules. Sure I do. If there weren't any rules, how could you break them?",
             "I cried because I had no shoes, then I met a man who had no feet.",
             "When you think there is a problem, you can act like an ostrich that sticks its head in the sand and pretend there is no problem"
            };

    public static String getRandomCommentsOnLife() {
        return COMMENTS_ON_LIFE[Demo.nextInt(0, COMMENTS_ON_LIFE.length)];
    }

}
