package jackplay.core;

import jackplay.JackplayLogger;
import jackplay.model.Genre;
import jackplay.model.Options;
import jackplay.model.Site;
import static jackplay.model.Genre.*;

import jackplay.core.performers.RedefinePerformer;
import jackplay.core.performers.TracingPerformer;
import jackplay.core.performers.Performer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Registry manages trace requests
public class Registry {
    private Map<Genre, Map<String, Map<String, Performer>>> registry;

    public Registry(Options options) {
        this.registry = new ConcurrentHashMap<>();
        this.prepareGenre(TRACE);
        this.prepareGenre(REDEFINE);
        this.addTraces(options.defaultTraceAsArray());
    }

    public synchronized boolean register(Genre genre, Site site, String newBody) {
        if (TRACE == genre && this.wasRegistered(TRACE, site)) {
            return false;
        }

        prepareClass(genre, site.classFullName);
        Performer performer = createPerformer(site, genre, newBody);

        // todo, use method shortname + argslist instead of method full name
        registry.get(genre).get(site.classFullName).put(site.methodFullName, performer);

        JackplayLogger.info("registry", "has registered:" + genre + ", " + site.methodFullName);
        return true;
    }

    public synchronized boolean unregister(Genre genre, Site site) {
        if (!this.wasRegistered(genre, site)) {
            return false;
        }

        if (registry.get(genre).containsKey(site.classFullName)) {

            registry.get(genre).get(site.classFullName).remove(site.methodFullName);
            JackplayLogger.info("program-manager", "deleted existing agenda:" + genre + ", " + site.methodFullName);

            if (registry.get(genre).get(site.classFullName).isEmpty()) {
                registry.get(genre).remove(site.classFullName);
            }
        }

        return true;
    }

    private boolean wasRegistered(Genre genre, Site pg) {
        try {
            return registry.get(genre).get(pg.classFullName).containsKey(pg.methodFullName);
        } catch(NullPointerException npe) {
            return false;
        }
    }

    private void prepareGenre(Genre genre) {
        if (!registry.containsKey(genre)) {
            registry.put(genre, new ConcurrentHashMap<>());
        }
    }

    private void prepareClass(Genre genre, String className) {
        if (!registry.get(genre).containsKey(className)) registry.get(genre).put(className, new ConcurrentHashMap<String, jackplay.core.performers.Performer>());
    }

    private Performer createPerformer(Site pg, Genre genre, String methodSource) {
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
        agenda.put(TRACE, this.registry.get(TRACE).get(classFullName));
        agenda.put(REDEFINE, this.registry.get(REDEFINE).get(classFullName));

        return agenda;
    }

    public synchronized void addTraces(String[] methodFullNames) {
        if (methodFullNames == null || methodFullNames.length == 0) return;

        for (String mfn : methodFullNames) {
            if (mfn == null) continue;

            String trimmed = mfn.trim();
            if (trimmed.length() == 0) continue;

            this.register(TRACE, new Site(mfn), null);
        }
    }

    synchronized Performer existingPerformer(Genre genre, String classFullName, String methodFullName) {
        try {
            return registry.get(genre).get(classFullName).get(methodFullName);
        } catch (NullPointerException npe) {
            return null;
        }
    }

    synchronized Map<String, ?> agendaOfGenre(Genre genre) {
        return this.registry.get(genre);
    }

    synchronized Map<Genre, Map<String, Map<String, Performer>>> copyOfCurrentProgram() {
        Map<Genre, Map<String, Map<String, Performer>>> copy = new HashMap<>();
        deepMapCopy(registry, copy);

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
