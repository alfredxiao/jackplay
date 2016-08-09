package jackplay.play;

import jackplay.bootstrap.Genre;
import jackplay.bootstrap.PlayGround;
import jackplay.play.performers.TracingPerformer;
import jackplay.play.performers.Performer;
import jackplay.javassist.NotFoundException;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProgramManager {
    Composer composer;
    Map<Genre, Map<String, Map<String, Performer>>> program;

    public void wireUp(Composer composer) {
        program = new ConcurrentHashMap<>();
        this.composer = composer;
    }

    public void addPlayAsTracing(String methodFullName) throws Exception {
       this.addPlay(methodFullName, Genre.METHOD_TRACE, null);
    }

    public void addPlayAsRedefinition(String methodFullName, String src) throws Exception {
        this.addPlay(methodFullName, Genre.METHOD_REDEFINE, src);
    }

    private void addPlay(String methodFullName, Genre genre, String src) throws Exception {
        PlayGround pg = new PlayGround(methodFullName);     // basic format validation
        validateMethodLocation(pg);                         // validate method existence
        if (genre == Genre.METHOD_REDEFINE || !existsPlay(pg, genre)) {
            addToProgram(pg, genre, src);
            try {
                composer.performPlay(pg.classFullName);
            } catch(Exception e) {
                removeProgrammedMethod(genre, pg.methodFullName);
                throw e;
            }
        }
    }

    private static void validateMethodLocation(PlayGround pg) throws NotFoundException {
        InfoCenter.locateMethod(pg, pg.methodFullName, pg.methodShortName);
    }

    public void removeRedefinition(String className, String methodFullName) {
        program.get(Genre.METHOD_REDEFINE).get(className).remove(methodFullName);
    }

    public void removeProgrammedMethod(Genre genre, String methodFullName) throws Exception {
        PlayGround pg = new PlayGround(methodFullName);
        program.get(genre).get(pg.classFullName).remove(methodFullName);
        if (program.get(genre).get(pg.classFullName).isEmpty()) {
            program.get(genre).remove(pg.classFullName);
            if (program.get(genre).isEmpty()) {
                program.remove(genre);
            }
        }
        composer.performPlay(pg.classFullName);
    }

    public void removeProgrammedClass(Genre genre, String className) throws Exception {
        program.get(genre).remove(className);
        composer.performPlay(className);
        if (program.get(genre).isEmpty()) program.remove(genre);
    }

    // called when verifier error
    void removeRedefinitions(String className) {
        program.get(Genre.METHOD_REDEFINE).remove(className);
    }

    private boolean existsPlay(PlayGround pg, Genre genre) throws NotFoundException {
        try {
            return program.get(genre).get(pg.classFullName).containsKey(pg.methodFullName);
        } catch(NullPointerException npe) {
            return false;
        }
    }

    private synchronized void addToProgram(PlayGround pg, Genre genre, String methodSource) {
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
                return new jackplay.play.performers.RedefinePerformer(pg, methodSource);
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
}
