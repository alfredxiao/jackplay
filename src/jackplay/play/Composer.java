package jackplay.play;

import jackplay.JackOptions;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.HashMap;
import java.util.Map;

public class Composer {
    JackOptions options;
    Instrumentation inst;

    Map<PlayUnit, PlayBook> playScripts;

    public Composer(JackOptions options, Instrumentation inst) {
        this.options = options;
        this.inst =  inst;
        this.playScripts = new HashMap<PlayUnit, PlayBook>();
    }

    public void play(PlayCategory category, String className, String methodName) throws Exception {
        PlayUnit unit = new PlayUnit(className, methodName);
        PlayBook book = new PlayBook(category);

        if (!playScripts.containsKey(unit)) {
            performPlay(className, unit, book);

        }
    }

    private void performPlay(String className, PlayUnit unit, PlayBook book) throws Exception {
        inst.addTransformer(book.createPlayer(unit), true);

        Class c = Class.forName(className);
        if  (inst.isModifiableClass(c)) {
            inst.retransformClasses(c);
        } else {
            throw new Exception("class not modifiable:" + className);
        }
    }

}

class PlayUnit {
    final String className;
    final String methodName;

    public PlayUnit(String className, String methodName) {
        this.className = className;
        this.methodName = methodName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlayUnit playUnit = (PlayUnit) o;

        if (!className.equals(playUnit.className)) return false;
        return methodName.equals(playUnit.methodName);

    }

    @Override
    public int hashCode() {
        int result = className.hashCode();
        result = 31 * result + methodName.hashCode();
        return result;
    }
}

class PlayBook {
    final PlayCategory category;
    final String newSource;

    public PlayBook(PlayCategory category) {
        this.category = category;
        this.newSource = null;
    }

    public PlayBook(PlayCategory category, String newSource) {
        this.category = category;
        this.newSource = newSource;
    }

    public ClassFileTransformer createPlayer(PlayUnit unit) {
        if (category == PlayCategory.MethodLogging) {
            return new MethodLoggingPlayer(unit);
        } else {
            return null;
        }
    }
}