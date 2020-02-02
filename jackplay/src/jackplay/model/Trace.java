package jackplay.model;

import java.util.Date;

/**
 * A <code>Trace</code> is a data record collected in a Spot on a Site where tracing happens
 */
public class Trace {
    public Date when;
    public long whenAsTimeMs;

    // mandatory for all entries
    public Point point;
    public Site site;
    public long threadId;
    public String threadName;
    public String id;

    // for method entrance
    public String[] arguments;

    // for method return or exception
    public long elapsed = -1;
    public int argumentsCount;

    public String returnedValue;            // return only
    public boolean returningVoid = false;   // return only

    public String exceptionStackTrace;      // exception only

    public Trace(Point point, Site site, String id) {
        this.when = new Date();
        this.whenAsTimeMs = System.currentTimeMillis();
        this.point = point;
        this.site = site;
        this.threadId = Thread.currentThread().getId();
        this.threadName = Thread.currentThread().getName();
        this.id = id;
    }
}
