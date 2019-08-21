package jackplay.bootstrap;

import java.util.Date;

// A Trace is data collected in a Spot on a Site where tracing happens
class Trace {
    Date when;
    long whenAsTimeMs;

    // mandatory for all entries
    Spot spot;
    Site site;
    long threadId;
    String threadName;
    String id;

    // for method entrance
    String[] arguments;

    // for method return or exception
    long elapsed = -1;
    int argumentsCount;

    String returnedValue;            // return only
    boolean returningVoid = false;   // return only

    String exceptionStackTrace;      // exception only

    Trace(Spot spot, Site site, String id) {
        this.when = new Date();
        this.whenAsTimeMs = System.currentTimeMillis();
        this.spot = spot;
        this.site = site;
        this.threadId = Thread.currentThread().getId();
        this.threadName = Thread.currentThread().getName();
        this.id = id;
    }
}
