package jackplay.play;


import javassist.CtClass;

public interface Performer {
    CtClass play(CtClass aClass) throws Exception;
}
