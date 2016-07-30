package jackplay.play.domain;

import java.util.Date;

public class TraceLog {
    public Date when;
    public TraceTriggerPoint triggerPoint;
    public PlayGround pg;
    public String log;
    public long elapsed;

    public TraceLog(TraceTriggerPoint triggerPoint, PlayGround pg, String log) {
        this(triggerPoint, pg, log, 0);
    }

    public TraceLog(TraceTriggerPoint triggerPoint, PlayGround pg, String log, long elapsed) {
        this.when = new Date();
        this.triggerPoint = triggerPoint;
        this.pg = pg;
        this.log = log;
        this.elapsed = elapsed;
    }
}