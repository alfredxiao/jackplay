package jackplay.play;

import jackplay.JackLogger;
import jackplay.JackOptions;

import java.lang.instrument.Instrumentation;
import java.util.*;

// singleton
public class Composer {
    JackOptions options;
    Instrumentation inst;
    Map<String, Map<String, Map<Genre, Performer>>> program;

    public Composer(JackOptions options, Instrumentation inst) {
        this.options = options;
        this.inst =  inst;
        LeadPerformer leadPerformer = new LeadPerformer(this);
        this.inst.addTransformer(leadPerformer, true);
        this.program = new HashMap<String, Map<String, Map<Genre, Performer>>>();
    }

    public void logMethod(PlayGround playGround) throws Exception {
        if (isNewPlay(playGround, Genre.METHOD_LOGGING)) {
            JackLogger.debug("isNewPlay for " + playGround);
            addPlayToProgram(playGround, Genre.METHOD_LOGGING, null);
            JackLogger.debug("program:" + program);
            this.performPlay(playGround.className);
        }
    }

    public void redefineMethod(PlayGround playGround, String methodSource) throws Exception {
        if (isNewPlay(playGround, Genre.METHOD_REDEFINE)) {
            addPlayToProgram(playGround, Genre.METHOD_REDEFINE, methodSource);
            this.performPlay(playGround.className);
        }
    }

    private boolean isNewPlay(PlayGround playGround, Genre genre) {
        boolean playExists =
                program.containsKey(playGround.className)
                && program.get(playGround.className).containsKey(playGround.methodName)
                && program.get(playGround.className).get(playGround.methodName).containsKey(genre);
        return !playExists || (genre == Genre.METHOD_REDEFINE);
    }

    private void addPlayToProgram(PlayGround playGround, Genre genre, String methodSource) {
        prepareProgram(playGround.className, playGround.methodName);
        Performer performer = createPerformer(playGround.methodName, genre, methodSource);
        program.get(playGround.className).get(playGround.methodName).put(genre, performer);
    }

    private Performer createPerformer(String methodName, Genre genre, String methodSource) {
        if (genre == Genre.METHOD_LOGGING) {
            return new LoggingPerformer(methodName);
        } else {
            return null;
        }
    }

    private void prepareProgram(String className, String methodName) {
        if (!program.containsKey(className)) {
            program.put(className, new HashMap<String, Map<Genre, Performer>>());
        }

        if (!program.get(className).containsKey(methodName)) {
            program.get(className).put(methodName, new HashMap<Genre, Performer>());
        }
    }

    private void performPlay(String className) throws Exception {
        Class c = Class.forName(className);
        if  (inst.isModifiableClass(c) && inst.isRetransformClassesSupported()) {
            JackLogger.debug("class is modifiable, let's do it");
            inst.retransformClasses(c);
        } else {
            throw new Exception("class not modifiable:" + className);
        }
    }

    public List<Performer> findPerformers(String className) {
        List<Performer> performers = new ArrayList<Performer>();
        Map<String, Map<Genre, Performer>> methodMap = program.get(className);
        for (Map<Genre, Performer> genrePerformerMap : methodMap.values()) {
            performers.addAll(genrePerformerMap.values());
        }

        return performers;
    }
}

