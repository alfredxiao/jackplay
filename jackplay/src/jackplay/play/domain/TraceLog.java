package jackplay.play.domain;

import java.util.Date;

public class TraceLog {
    public Date when;
    public TraceTriggerPoint triggerPoint;
    public PlayGround pg;
    public String log;
    public long elapsed;
    public String uuid;

    public TraceLog(TraceTriggerPoint triggerPoint, PlayGround pg, String log, String uuid) {
        this(triggerPoint, pg, log, uuid, 0);
    }

    public TraceLog(TraceTriggerPoint triggerPoint, PlayGround pg, String log, String uuid, long elapsed) {
        this.when = new Date();
        this.triggerPoint = triggerPoint;
        this.pg = pg;
        this.log = log;
        this.elapsed = elapsed;
        this.uuid = uuid;
    }
}