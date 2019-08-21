package jackplay.play;

import jackplay.JackplayLogger;
import jackplay.bootstrap.Genre;
import static jackplay.bootstrap.Genre.*;

import jackplay.bootstrap.Options;
import jackplay.bootstrap.Site;
import jackplay.play.performers.Transformer;
import jackplay.play.performers.Performer;
import jackplay.play.performers.RedefinePerformer;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class Jack {
    Instrumentation inst;
    Registry pm;
    Options options;
    InfoCenter infoCenter;
    Transformer transformer;

    public Jack(Instrumentation inst, Options options, Registry pm, InfoCenter infoCenter, Transformer transformer) {
        this.inst = inst;
        this.pm = pm;
        this.options = options;
        this.infoCenter = infoCenter;
        this.transformer = transformer;
        this.inst.addTransformer(transformer, true);
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

    private void handleRetransformationError(Throwable t, Class clazz, Genre genre, Site pg, String previousBody) throws PlayException {
        // if an agenda causes problem, we do our best by removing it and
        // re-transform with this agenda removed - in other words, undo it

        JackplayLogger.error("jack", t);
        pm.unregister(genre, pg);

        if (previousBody != null) {
            pm.register(REDEFINE, pg, previousBody);
        }

        try {
            JackplayLogger.debug("jack", "attempting to undo retransformation for class:" + pg.classFullName);
            inst.retransformClasses(clazz);
        } catch(Exception betterEffort) {
            JackplayLogger.error("jack", betterEffort);
            if (genre == REDEFINE && previousBody != null) {
                // this means even the previously working method redefinition does not work,
                // we have to remove it completely, unfortunately
                pm.unregister(REDEFINE, pg);
                try {
                    JackplayLogger.debug("jack", "attempting to restore retransformation for class:" + pg.classFullName);
                    inst.retransformClasses(clazz);
                } catch(Exception bestEffort) {
                    JackplayLogger.error("jack", bestEffort);
                }
            }
        }

        throw new PlayException("An " + t.getClass().getName() + " error occurred while attempting to retransform "
                + pg.classFullName + ": " + t.getMessage());
    }

    private synchronized void play(Genre genre, Site pg, String newBody) throws PlayException {
        String previousBody = null;
        if (genre == REDEFINE) {
            RedefinePerformer performer = (RedefinePerformer) pm.existingPerformer(genre, pg.classFullName, pg.methodFullName);
            previousBody = performer == null ? null : performer.getNewBody();
        }

        if (pm.register(genre, pg, newBody)) {
            boolean matched = false;
            List<Class> loadedMatchedClasses = infoCenter.findLoadedModifiableClasses(pg.classFullName);
            for (Class clazz : loadedMatchedClasses) {
                Method method = infoCenter.findMatchingMethod(clazz, pg);
                if (method != null && verifyPlayability(method)) {
                    matched = true;
                    try {
                        transformer.transformSuccess = false;
                        transformer.transformFailure = null;

                        JackplayLogger.debug("jack", "starts retransforming class:" + pg.classFullName);
                        inst.retransformClasses(clazz);

                        if (transformer.transformSuccess) {
                            JackplayLogger.info("jack", "finished retransforming class:" + pg.classFullName);
                        } else {
                            handleRetransformationError(transformer.transformFailure, clazz, genre, pg, previousBody);
                        }
                    } catch(Throwable t) {
                        handleRetransformationError(t, clazz, genre, pg, previousBody);
                    }
                }
            }

            if (!matched) {
                JackplayLogger.info("jack", "agenda " + genre + ", " + pg.methodFullName + " added but not played yet");
            }
        }
    }

    public synchronized void undoPlay(Genre genre, Site pg) throws PlayException {
        if (pm.unregister(genre, pg)) {
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

    public void trace(Site pg) throws PlayException {
        this.play(TRACE, pg, null);
    }

    public void undoTrace(Site pg) throws PlayException {
        this.undoPlay(TRACE, pg);
    }

    public void redefine(Site pg, String newBody) throws PlayException {
        this.play(REDEFINE, pg, newBody);
    }

    public void undoRedefine(Site pg) throws PlayException {
        this.undoPlay(REDEFINE, pg);
    }

    public void undoClass(Genre genre, String className) throws PlayException {
        Map<Genre, Map<String, Performer>> plays = pm.agendaForClass(className);
        if (plays != null && !plays.isEmpty()) {
            for (String methodFullName : plays.get(genre).keySet()) {
                this.undoPlay(genre, new Site(methodFullName));
            }
        }
    }

    private void undoAll(Genre genre) throws PlayException {
        Map<String,?> traces = pm.agendaOfGenre(genre);
        if (!traces.isEmpty()) {
            for (String className : traces.keySet()) {
                this.undoClass(genre, className);
            }
        }
    }

    public void undoAll() throws PlayException {
        this.undoAll(TRACE);
        this.undoAll(REDEFINE);
    }
}
