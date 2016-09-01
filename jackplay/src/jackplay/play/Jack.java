package jackplay.play;

import jackplay.Logger;
import jackplay.bootstrap.Genre;
import static jackplay.bootstrap.Genre.*;

import jackplay.bootstrap.Options;
import jackplay.bootstrap.PlayGround;
import jackplay.play.performers.LeadPerformer;
import jackplay.play.performers.Performer;
import jackplay.play.performers.RedefinePerformer;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class Jack {
    Instrumentation inst;
    ProgramManager pm;
    Options options;
    InfoCenter infoCenter;
    LeadPerformer leadPerformer;

    public void init(Instrumentation inst, Options options, ProgramManager pm, InfoCenter infoCenter, LeadPerformer leadPerformer) {
        this.inst = inst;
        this.pm = pm;
        this.options = options;
        this.infoCenter = infoCenter;
        this.leadPerformer = leadPerformer;
        this.inst.addTransformer(leadPerformer, true);
    }

    private boolean verifyPlayability(Method method) throws PlayException {
        String packageName = getPackageName(method.getDeclaringClass().getName());
        if (!options.packageAllowed(packageName)) {
            throw new PlayException("package not allowed: " + packageName);
        }

        String className = method.getDeclaringClass().getName();
        if (!infoCenter.hasMethodBody(method)) {
            throw new PlayException("method has no body (either native or abstract): " +
                    className + "." + method.getName());
        }

        if (!inst.isModifiableClass(method.getDeclaringClass())) throw new PlayException("class not modifiable:" + className);

        if (!inst.isRetransformClassesSupported()) throw new PlayException("RetransformClass not supported");

        return true;
    }

    private String getPackageName(String className) {
        int lastDot = className.lastIndexOf('.');
        return lastDot < 0 ? "" : className.substring(0, lastDot);
    }

    public void handleRetransformationError(Throwable t, Class clazz, Genre genre, PlayGround pg, String previousBody) throws PlayException {
        // if an agenda causes problem, we do our best by removing it and
        // re-transform with this agenda removed - in other words, undo it

        Logger.error("jack", t);
        pm.removeAgenda(genre, pg);

        if (previousBody != null) {
            pm.addAgenda(METHOD_REDEFINE, pg, previousBody);
        }

        try {
            Logger.debug("jack", "attempting to undo retransformation for class:" + pg.classFullName);
            inst.retransformClasses(clazz);
        } catch(Exception betterEffort) {
            Logger.error("jack", betterEffort);
            if (genre == METHOD_REDEFINE && previousBody != null) {
                // this means even the previously working method redefinition does not work,
                // we have to remove it completely, unfortunately
                pm.removeAgenda(METHOD_REDEFINE, pg);
                try {
                    Logger.debug("jack", "attempting to restore retransformation for class:" + pg.classFullName);
                    inst.retransformClasses(clazz);
                } catch(Exception bestEffort) {
                    Logger.error("jack", bestEffort);
                }
            }
        }

        throw new PlayException("An " + t.getClass().getName() + " error occurred while attempting to retransform "
                + pg.classFullName + ": " + t.getMessage());
    }

    private synchronized void play(Genre genre, PlayGround pg, String newBody) throws PlayException {
        String previousBody = null;
        if (genre == METHOD_REDEFINE) {
            RedefinePerformer performer = (RedefinePerformer) pm.existingPerformer(genre, pg.classFullName, pg.methodFullName);
            previousBody = performer == null ? null : performer.getNewBody();
        }

        if (pm.addAgenda(genre, pg, newBody)) {
            boolean matched = false;
            List<Class> loadedMatchedClasses = infoCenter.findLoadedModifiableClasses(pg.classFullName);
            for (Class clazz : loadedMatchedClasses) {
                Method method = infoCenter.findMatchingMethod(clazz, pg);
                if (method != null && verifyPlayability(method)) {
                    matched = true;
                    try {
                        leadPerformer.transformSuccess = false;
                        leadPerformer.transformFailure = null;

                        Logger.debug("jack", "starts retransforming class:" + pg.classFullName);
                        inst.retransformClasses(clazz);

                        if (leadPerformer.transformSuccess) {
                            Logger.info("jack", "finished retransforming class:" + pg.classFullName);
                        } else {
                            handleRetransformationError(leadPerformer.transformFailure, clazz, genre, pg, previousBody);
                        }
                    } catch(Throwable t) {
                        handleRetransformationError(t, clazz, genre, pg, previousBody);
                    }
                }
            }

            if (!matched) {
                Logger.info("jack", "agenda " + genre + ", " + pg.methodFullName + " added but not played yet");
            }
        }
    }

    public synchronized void undoPlay(Genre genre, PlayGround pg) throws PlayException {
        if (pm.removeAgenda(genre, pg)) {
            List<Class> loadedMatchedClasses = infoCenter.findLoadedModifiableClasses(pg.classFullName);
            for (Class clazz : loadedMatchedClasses) {
                try {
                    inst.retransformClasses(clazz);
                } catch (Throwable t) {
                    throw new PlayException("An " + t.getClass().getName() + " error occurred while attempting to retransform "
                            + pg.classFullName + ": " + t.getMessage());
                }
            }
        }
    }

    public void trace(PlayGround pg) throws PlayException {
        this.play(METHOD_TRACE, pg, null);
    }

    public void undoTrace(PlayGround pg) throws PlayException {
        this.undoPlay(METHOD_TRACE, pg);
    }

    public void redefine(PlayGround pg, String newBody) throws PlayException {
        this.play(METHOD_REDEFINE, pg, newBody);
    }

    public void undoRedefine(PlayGround pg) throws PlayException {
        this.undoPlay(METHOD_REDEFINE, pg);
    }

    public void undoClass(Genre genre, String className) throws PlayException {
        Map<Genre, Map<String, Performer>> plays = pm.agendaForClass(className);
        if (plays != null && !plays.isEmpty()) {
            for (String methodFullName : plays.get(genre).keySet()) {
                this.undoPlay(genre, new PlayGround(methodFullName));
            }
        }
    }

    public void undoAll(Genre genre) throws PlayException {
        Map<String,?> traces = pm.agendaOfGenre(genre);
        if (!traces.isEmpty()) {
            for (String className : traces.keySet()) {
                this.undoClass(genre, className);
            }
        }
    }

    public void undoAll() throws PlayException {
        this.undoAll(METHOD_TRACE);
        this.undoAll(METHOD_REDEFINE);
    }
}
