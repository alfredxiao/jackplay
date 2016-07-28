package jackplay.play.performers;


import jackplay.javassist.CtClass;

public interface Performer {
    CtClass perform(CtClass aClass) throws Exception;
}
