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
    Map<Genre, Map<String, Map<String, Performer>>> program;

    public ProgramManager() {
        this.program = new ConcurrentHashMap<>();
        this.prepareGenre(METHOD_TRACE);
        this.prepareGenre(METHOD_REDEFINE);
    }

    public boolean addAgenda(Genre genre, PlayGround pg, String newBody) {
        if (this.existsAgenda(genre, pg)) {
            return false;
        } else {
            this.createNewAgenda(genre, pg, newBody);
            return true;
        }
    }

    public boolean removeAgenda(Genre genre, PlayGround pg) {
        if (!this.existsAgenda(genre, pg)) {
            return false;
        } else {
            this.deleteExistingAgenda(genre, pg);
            return true;
        }
    }

    private synchronized void createNewAgenda(Genre genre, PlayGround pg, String newBody) {
        prepareClass(genre, pg.classFullName);

        Performer performer = createPerformer(pg, genre, newBody);
        // todo, use method shortname + argslist instead of method full name
        program.get(genre).get(pg.classFullName).put(pg.methodFullName, performer);

        Logger.debug("program-manager", "created new agenda:" + genre + ", " + pg.methodFullName);
    }

    private void deleteExistingAgenda(Genre genre, PlayGround pg) {
        if (program.get(genre).containsKey(pg.classFullName)) {

            program.get(genre).get(pg.classFullName).remove(pg.methodFullName);
            Logger.debug("program-manager", "deleted existing agenda:" + genre + ", " + pg.methodFullName);

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
            case METHOD_TRACE:
                return new TracingPerformer(pg);
            case METHOD_REDEFINE:
                return new RedefinePerformer(pg, methodSource);
            default:
                throw new RuntimeException("unknown genre " + genre.toString());
        }
    }

    public Map<Genre, Map<String, Performer>> agendaForClass(String classFullName) {
        Map<Genre, Map<String, Performer>> agenda = new HashMap<>();
        agenda.put(METHOD_TRACE, this.program.get(METHOD_TRACE).get(classFullName));
        agenda.put(METHOD_REDEFINE, this.program.get(METHOD_REDEFINE).get(classFullName));

        return agenda;
    }

    public void addTraces(String[] methodFullNames) {
        if (methodFullNames == null || methodFullNames.length == 0) return;

        for (String mfn : methodFullNames) {
            if (mfn == null) continue;

            String trimmed = mfn.trim();
            if (trimmed.length() == 0) continue;

            this.addAgenda(METHOD_TRACE, new PlayGround(mfn), null);
        }
    }
}
