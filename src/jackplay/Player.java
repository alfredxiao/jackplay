package jackplay;

import java.lang.instrument.*;

public class Player {
    public static void play(Instrumentation inst, String className, String[] methodNames) throws Exception {
        System.out.println("Snowden start working...");
        inst.addTransformer(new PlayBook(className, methodNames), true);
        Class[] classes = inst.getAllLoadedClasses();
        for (Class c : classes) {
            if (c.getName().equals(className) && inst.isModifiableClass(c)) {
                System.out.println("change the world");
                inst.retransformClasses(c);
                break;
            }
        }
    }
}
