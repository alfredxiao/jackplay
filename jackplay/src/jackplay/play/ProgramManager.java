package jackplay.play;

import jackplay.Logger;
import jackplay.bootstrap.Genre;
import jackplay.bootstrap.Options;
import jackplay.bootstrap.PlayGround;
import jackplay.play.performers.RedefinePerformer;
import jackplay.play.performers.TracingPerformer;
import jackplay.play.performers.Performer;
import jackplay.javassist.NotFoundException;
import jackplay.javassist.CtClass;
import jackplay.javassist.ClassPool;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProgramManager {
    Composer composer;
    Map<Genre, Map<String, Map<String, Performer>>> program;
    private Options options;
    private InfoCenter infoCenter;

    public void init(Composer composer, Options options, InfoCenter infoCenter) {
        program = new ConcurrentHashMap<>();
        this.options = options;
        this.composer = composer;
        this.infoCenter = infoCenter;
    }

    public void submitMethodTrace(String methodFullName) throws Exception {
       this.submitPlayToProgram(methodFullName, Genre.METHOD_TRACE, null);
    }

    public void submitMethodRedefinition(String methodFullName, String src) throws Exception {
        this.submitPlayToProgram(methodFullName, Genre.METHOD_REDEFINE, src);
    }

    private void submitPlayToProgram(String methodFullName, Genre genre, String src) throws Exception {
        PlayGround pg = new PlayGround(methodFullName);     // basic format validation
        checkValidity(pg);                         // validate method existence
        if (genre == Genre.METHOD_REDEFINE || !existsPlay(pg, genre)) {
            addPlayToProgram(pg, genre, src);
            try {
                composer.retransformClass(pg.classFullName);
            } catch(Exception e) {
                removeMethodFromProgram(genre, pg.methodFullName);
                throw e;
            }
        }
    }

    private void checkValidity(PlayGround pg) throws Exception {
        if (!this.canPlayOn(pg)) {
            throw new Exception(pg.methodFullName + " is not allowed.");
        }
    }

    private boolean canPlayOn(PlayGround pg) throws NotFoundException {
        ClassPool cp = ClassPool.getDefault();
        CtClass cc = cp.get(pg.classFullName);

        String packageName = cc.getPackageName();
        return options.canPlayPackage(packageName) &&
                infoCenter.canPlayMethod(InfoCenter.locateMethod(pg, pg.methodFullName, pg.methodShortName));
    }

    public void removeMethodRedefinition(String className, String methodFullName) {
        program.get(Genre.METHOD_REDEFINE).get(className).remove(methodFullName);
    }

    public void removeMethodFromProgramAndReplay(Genre genre, String methodFullName) throws Exception {
        removeMethodFromProgram(genre, methodFullName);

        composer.retransformClass(new PlayGround(methodFullName).classFullName);
    }

    private void removeMethodFromProgram(Genre genre, String methodFullName) throws Exception {
        PlayGround pg = new PlayGround(methodFullName);
        program.get(genre).get(pg.classFullName).remove(methodFullName);
        if (program.get(genre).get(pg.classFullName).isEmpty()) {
            program.get(genre).remove(pg.classFullName);
            if (program.get(genre).isEmpty()) {
                program.remove(genre);
            }
        }
    }

    public void removeClassFromProgramAndReplay(Genre genre, String className) throws Exception {
        removeClassFromProgram(genre, className);
        composer.retransformClass(className);
    }

    // called when verifier error occurs
    void removeClassFromProgram(Genre genre, String className) {
        program.get(genre).remove(className);
        if (program.get(genre).isEmpty()) program.remove(genre);
    }

    private boolean existsPlay(PlayGround pg, Genre genre) throws NotFoundException {
        try {
            return program.get(genre).get(pg.classFullName).containsKey(pg.methodFullName);
        } catch(NullPointerException npe) {
            return false;
        }
    }

    private synchronized void addPlayToProgram(PlayGround pg, Genre genre, String methodSource) {
        prepareProgram(genre, pg.classFullName);
        Performer performer = createPerformer(pg, genre, methodSource);
        program.get(genre).get(pg.classFullName).put(pg.methodFullName, performer);
    }

    private synchronized void prepareProgram(Genre genre, String className) {
        prepareGenreMap(genre);
        prepareClassMap(genre, className);
    }

    private void prepareGenreMap(Genre genre) {
        if (!program.containsKey(genre))  program.put(genre, new ConcurrentHashMap<String, Map<String, jackplay.play.performers.Performer>>());
    }

    private void prepareClassMap(Genre genre, String className) {
        if (!program.get(genre).containsKey(className)) program.get(genre).put(className, new ConcurrentHashMap<String, jackplay.play.performers.Performer>());
    }

    private Performer createPerformer(PlayGround pg, Genre genre, String methodSource) {
        switch (genre) {
            case METHOD_TRACE:
                return new TracingPerformer(pg);
            case METHOD_REDEFINE:
                return new RedefinePerformer(pg, methodSource);
            default:
                throw new RuntimeException("unknown genre " + genre.toString());
        }
    }

    public Collection<Performer> findPerformers(Genre genre, String className) {
        if (program.containsKey(genre) && program.get(genre).containsKey(className)) {
            return program.get(genre).get(className).values();
        } else {
            return null;
        }
    }

    public Map<Genre, Map<String, Map<String, Performer>>> getCurrentProgram() {
        return this.program;
    }

    public void submitMethodTraceBatch(String[] methodFullNames) {
        for (String mfn : methodFullNames) {
            try {
                this.submitMethodTrace(mfn);
            } catch (Exception e) {
                Logger.error(e);
            }
        }
    }
}
