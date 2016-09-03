package jackplay.bootstrap;

import java.util.Date;

class TraceLog {
    Date when;
    long whenAsTimeMs;

    // mandatory for all entries
    TracePoint tracePoint;
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

    TraceLog(TracePoint tracePoint, PlayGround pg, String uuid) {
        this.when = new Date();
        this.whenAsTimeMs = System.currentTimeMillis();
        this.tracePoint = tracePoint;
        this.pg = pg;
        this.threadId = Thread.currentThread().getId();
        this.threadName = Thread.currentThread().getName();
        this.uuid = uuid;
    }
}