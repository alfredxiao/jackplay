package jackplay.bootstrap;

import java.util.Date;

public class TraceLog {
    public Date when;
    public long whenAsTimeMs;

    // mandatory for all entries
    public TracePoint tracePoint;
    public PlayGround pg;
    public long threadId;
    public String threadName;
    public String uuid;

    // for method entrance
    public String[] arguments;

    // for method return or exception
    public long elapsed = -1;
    public int argsLen;
    public String returnedValue;        // return only
    public String exceptionStackTrace;  // exception only

    public TraceLog(TracePoint tracePoint, PlayGround pg, String uuid) {
        this.when = new Date();
        this.whenAsTimeMs = System.currentTimeMillis();
        this.tracePoint = tracePoint;
        this.pg = pg;
        this.threadId = Thread.currentThread().getId();
        this.threadName = Thread.currentThread().getName();
        this.uuid = uuid;
    }
}