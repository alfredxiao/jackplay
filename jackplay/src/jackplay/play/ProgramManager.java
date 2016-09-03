package jackplay.play;

import jackplay.Logger;
import jackplay.bootstrap.Genre;
import jackplay.bootstrap.PlayGround;
import static jackplay.bootstrap.Genre.*;
import jackplay.play.performers.RedefinePerformer;
import jackplay.play.performers.TracingPerformer;
import jackplay.play.performers.Performer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// singleton
public class ProgramManager {
    private Map<Genre, Map<String, Map<String, Performer>>> program;

    public ProgramManager() {
        this.program = new ConcurrentHashMap<>();
        this.prepareGenre(TRACE);
        this.prepareGenre(REDEFINE);
    }

    public synchronized boolean addAgenda(Genre genre, PlayGround pg, String newBody) {
        if (TRACE == genre && this.existsAgenda(genre, pg)) {
            Logger.debug("program-manager", "not create new agenda as it already exists:" + pg.methodFullName);
            return false;
        } else {
            this.createNewAgenda(genre, pg, newBody);
            return true;
        }
    }

    public synchronized boolean removeAgenda(Genre genre, PlayGround pg) {
        if (!this.existsAgenda(genre, pg)) {
            return false;
        } else {
            this.deleteExistingAgenda(genre, pg);
            return true;
        }
    }

    private void createNewAgenda(Genre genre, PlayGround pg, String newBody) {
        prepareClass(genre, pg.classFullName);

        Performer performer = createPerformer(pg, genre, newBody);

        // todo, use method shortname + argslist instead of method full name
        program.get(genre).get(pg.classFullName).put(pg.methodFullName, performer);

        Logger.info("program-manager", "created new agenda:" + genre + ", " + pg.methodFullName);
    }

    private void deleteExistingAgenda(Genre genre, PlayGround pg) {
        if (program.get(genre).containsKey(pg.classFullName)) {

            program.get(genre).get(pg.classFullName).remove(pg.methodFullName);
            Logger.info("program-manager", "deleted existing agenda:" + genre + ", " + pg.methodFullName);

            if (program.get(genre).get(pg.classFullName).isEmpty()) {
                program.get(genre).remove(pg.classFullName);
            }
        }
    }

    private boolean existsAgenda(Genre genre, PlayGround pg) {
        try {
            return program.get(genre).get(pg.classFullName).containsKey(pg.methodFullName);
        } catch(NullPointerException npe) {
            return false;
        }
    }

    private void prepareGenre(Genre genre) {
        if (!program.containsKey(genre)) {
            program.put(genre, new ConcurrentHashMap<>());
        }
    }

    private void prepareClass(Genre genre, String className) {
        if (!program.get(genre).containsKey(className)) program.get(genre).put(className, new ConcurrentHashMap<String, jackplay.play.performers.Performer>());
    }

    private Performer createPerformer(PlayGround pg, Genre genre, String methodSource) {
        switch (genre) {
            case TRACE:
                return new TracingPerformer(pg);
            case REDEFINE:
                return new RedefinePerformer(pg, methodSource);
            default:
                throw new RuntimeException("unknown genre " + genre.toString());
        }
    }

    public synchronized Map<Genre, Map<String, Performer>> agendaForClass(String classFullName) {
        Map<Genre, Map<String, Performer>> agenda = new HashMap<>();
        agenda.put(TRACE, this.program.get(TRACE).get(classFullName));
        agenda.put(REDEFINE, this.program.get(REDEFINE).get(classFullName));

        return agenda;
    }

    public synchronized void addTraces(String[] methodFullNames) {
        if (methodFullNames == null || methodFullNames.length == 0) return;

        for (String mfn : methodFullNames) {
            if (mfn == null) continue;

            String trimmed = mfn.trim();
            if (trimmed.length() == 0) continue;

            this.addAgenda(TRACE, new PlayGround(mfn), null);
        }
    }

    synchronized Performer existingPerformer(Genre genre, String classFullName, String methodFullName) {
        try {
            return program.get(genre).get(classFullName).get(methodFullName);
        } catch (NullPointerException npe) {
            return null;
        }
    }

    synchronized Map<String, ?> agendaOfGenre(Genre genre) {
        return this.program.get(genre);
    }

    synchronized Map<Genre, Map<String, Map<String, Performer>>> copyOfCurrentProgram() {
        Map<Genre, Map<String, Map<String, Performer>>> copy = new HashMap<>();
        deepMapCopy(program, copy);

        return copy;
    }

    @SuppressWarnings("unchecked")
    private void deepMapCopy(Map source, Map target) {
        for (Object key : source.keySet()) {
            Object value = source.get(key);
            if (value instanceof Map) {
                Map valueCopy = new HashMap();
                deepMapCopy((Map) value, valueCopy);

                target.put(key, valueCopy);
            } else {
                target.put(key, value);
            }
        }
    }
}
