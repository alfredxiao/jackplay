package jackplay.play.performers;


import javassist.CtClass;

public interface Performer {
    CtClass perform(CtClass aClass) throws Exception;
}
