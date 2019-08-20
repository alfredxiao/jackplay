package jackplay.bootstrap;

import java.util.Date;

class Trace {
    Date when;
    long whenAsTimeMs;

    // mandatory for all entries
    Site site;
    PlayGround pg;
    long threadId;
    String threadName;
    String uuid;

    // for method entrance
    String[] arguments;

    // for method return or exception
    long elapsed = -1;
    int argumentsCount;

    String returnedValue;            // return only
    boolean returningVoid = false;   // return only

    String exceptionStackTrace;      // exception only

    Trace(Site site, PlayGround pg, String uuid) {
        this.when = new Date();
        this.whenAsTimeMs = System.currentTimeMillis();
        this.site = site;
        this.pg = pg;
        this.threadId = Thread.currentThread().getId();
        this.threadName = Thread.currentThread().getName();
        this.uuid = uuid;
    }
}

enum Site {
    MethodEntrance,
    MethodExit,
    MethodTermination
}