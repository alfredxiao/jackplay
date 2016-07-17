package jackplay.play;

import jackplay.JackLogger;
import jackplay.JackOptions;
import javassist.CtMethod;
import javassist.NotFoundException;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.*;

// singleton
public class Composer {
    JackOptions options;
    Instrumentation inst;
    Map<String, Map<String, Map<Genre, Performer>>> program;
    LeadPerformer leadPerformer;

    public Composer(JackOptions options, Instrumentation inst) {
        this.options = options;
        this.inst =  inst;
        this.leadPerformer = new LeadPerformer(this);
        this.inst.addTransformer(leadPerformer, true);
        this.program = new HashMap<String, Map<String, Map<Genre, Performer>>>();
        PlayLogger.initialise(options);
    }

    public void logMethod(PlayGround playGround) throws Exception {
        if (isNewAndValidPlay(playGround, Genre.METHOD_LOGGING)) {
            addPlayToProgram(playGround, Genre.METHOD_LOGGING, null);
            this.performPlay(playGround.className);
        }
    }

    public void redefineMethod(PlayGround playGround, String methodSource) throws Exception {
        if (isNewAndValidPlay(playGround, Genre.METHOD_REDEFINE)) {
            addPlayToProgram(playGround, Genre.METHOD_REDEFINE, methodSource);
            this.performPlay(playGround.className);
        }
    }

    private static void lookupMethod(PlayGround pg) throws NotFoundException {
        CtMethod m = pg.findMethod();
    }

    private boolean isNewAndValidPlay(PlayGround playGround, Genre genre) throws NotFoundException {
        lookupMethod(playGround);
        boolean playExists =
                program.containsKey(playGround.className)
                && program.get(playGround.className).containsKey(playGround.methodLongName)
                && program.get(playGround.className).get(playGround.methodLongName).containsKey(genre);
        return !playExists || (genre == Genre.METHOD_REDEFINE);
    }

    private void addPlayToProgram(PlayGround playGround, Genre genre, String methodSource) {
        prepareProgram(playGround.className, playGround.methodLongName);
        Performer performer = createPerformer(playGround, genre, methodSource);
        program.get(playGround.className).get(playGround.methodLongName).put(genre, performer);
    }

    private Performer createPerformer(PlayGround playGround, Genre genre, String methodSource) {
        switch (genre) {
            case METHOD_LOGGING:
                return new LoggingPerformer(playGround);
            case METHOD_REDEFINE:
                return new RedefinePerformer(playGround, methodSource);
            default:
                return null;
        }
    }

    private void prepareProgram(String className, String methodLongName) {
        if (!program.containsKey(className)) {
            program.put(className, new HashMap<String, Map<Genre, Performer>>());
        }

        if (!program.get(className).containsKey(methodLongName)) {
            program.get(className).put(methodLongName, new HashMap<Genre, Performer>());
        }
    }

    private void performPlay(String className) throws Exception {
        Class c = Class.forName(className);
        if (c.getName().equals(className)) {
            if (inst.isModifiableClass(c) && inst.isRetransformClassesSupported()) {
                leadPerformer.setClassToPlay(c);
                try {
                    inst.retransformClasses(c);
                } catch(VerifyError ve) {
                    JackLogger.error(ve);
                    JackLogger.log("can't verify a class, will reset its method body (while keep tracing if any)");
                    UndoClassRedefinition(c);
                    // todo: remove redefine performer
                }

                if (!leadPerformer.getExceptionsDuringPerformance().isEmpty()) {
                    throw new Exception("error in performing class redefinition", leadPerformer.getExceptionsDuringPerformance().get(0));
                }
            } else {
                throw new Exception("class not modifiable:" + className);
            }
        }
    }

    private void UndoClassRedefinition(Class c) throws UnmodifiableClassException {
        removeRedefinePerformers(c);
        inst.retransformClasses(c);
    }

    private void removeRedefinePerformers(Class c) {
        Map<String, Map<Genre, Performer>> methodMap = program.get(c.getName());
        for (Map<Genre, Performer> performerMap : methodMap.values()) {
            if (performerMap.containsKey(Genre.METHOD_REDEFINE)) {
                performerMap.remove(Genre.METHOD_REDEFINE);
            }
        }
    }

    public List<Performer> findPerformers(String className) {
        List<Performer> performers = new ArrayList<Performer>();
        Map<String, Map<Genre, Performer>> methodMap = program.get(className);
        for (Map<Genre, Performer> performerMap : methodMap.values()) {
            if (performerMap.containsKey(Genre.METHOD_REDEFINE)) {
                performers.add(performerMap.get(Genre.METHOD_REDEFINE));
            }
            if (performerMap.containsKey(Genre.METHOD_LOGGING)) {
                performers.add(performerMap.get(Genre.METHOD_LOGGING));
            }
        }

        return performers;
    }
}

