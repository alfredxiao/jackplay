package myapp.greeter;

import myapp.Demo;

public class RudeGreeter implements Greeter {

    public String greet(String name) {
        Demo.sleepSmallRandom();

        int mood = Demo.nextInt(0, 20);
        if (mood < 2) {
            return "Why are you here? " + name;
        } else if (mood < 4) {
            return "What the hell are you doing? " + name;
        } else if (mood < 6) {
            return "Piss off";
        } else if (mood < 9) {
            return "I am feeling violent";
        } else if (mood < 12) {
            return "I need more drugs, and Panadol.";
        } else if (mood < 14) {
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

    final static String[] COMMENTS = new String[]
        {"Can you tell me what the hell is cloud computing!",
         "I don't wanna go to school, I just wanna break the rule.",
         "What you said was a bunch of lie",
         "Life is simple, but we insist on making it complicated.",
         "Life is too short to be following the rules",
         "I work all night, I work all day, to pay the bills I have to pay. Ain't it sad?",
         "Catch this exception or perish.",
         "Rules and models destroy genius and art.",
         "This should never happen - ok, time to find a new job",
         "Dennis Ritchie invents a powerful gun that shoots both forward and backward simultaneously. Not satisfied with the number of deaths and permanent maimings from that invention he invents C and Unix.",
         "Memory management is so important that the designers of C and C++ believe that it can only be taken care of by the programmers, whereas Lisp and Java designers believe that memory management is so important that it should never be handled by the programmers.",
         "Linux Administrator: date ; unzip ; strip ; touch ; grep ; finger ; mount ; fsck ; more ; yes ; umount ; sleep; ",
         "Stop thinking and get certified today. - From a Scrum training ad",
         "When SOAP was created, we really thought it was simple?",
         "Haskell gets some resistance due to the complexity of using monads to control side effects. Wadler tries to appease critics by explaining that 'a monad is a monoid in the category of endofunctors, what's the problem?",
         "I was told to follow Agile practices, but I was also told that Agile is not a process. So what is it?",
         "When you think there is a problem, you can act like an ostrich that sticks its head in the sand and pretend there is no problem",
         "Fake it till you make it",
         "Buddhism teaches that it’s the nature of things to come together and fall apart, and that struggling against this impermanent nature of things is what causes us pain and suffering."
        };

    public static String getRandomCommentsOnLife() {
        return COMMENTS[Demo.nextInt(0, COMMENTS.length)];
    }

}
