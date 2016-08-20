package jackplay;

import jackplay.bootstrap.Options;
import jackplay.bootstrap.TraceKeeper;
import jackplay.play.*;
import jackplay.play.performers.LeadPerformer;
import jackplay.web.BoxOffice;

import java.lang.instrument.*;

public class Theatre {
    static Theatre theatre;

    Options options;
    Instrumentation inst;
    ProgramManager pm;
    LeadPerformer leadPerformer;
    BoxOffice boxOffice;
    InfoCenter infoCenter;
    PlayCoordinator coordinator;

    public Theatre(Options options, Instrumentation inst, ProgramManager pm,
                   LeadPerformer leadPerformer, BoxOffice boxOffice,
                   InfoCenter infoCenter, PlayCoordinator coordinator) {
        this.options = options;
        this.inst = inst;
        this.pm = pm;
        this.leadPerformer = leadPerformer;
        this.boxOffice = boxOffice;
        this.infoCenter = infoCenter;
        this.coordinator = coordinator;
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        Logger.info("running JackPlay with arguments:" + (agentArgs == null ? "(no args, resort to default)" : agentArgs));
        Logger.debug("Instrumentation.isRetransformClassesSupported():" + inst.isRetransformClassesSupported());
        Logger.debug("Instrumentation.isRedefineClassesSupported():" + inst.isRedefineClassesSupported());

        Options options = Options.optionsMergedWithDefaults(agentArgs);
        Logger.info("After web server is started, point your browser to " +
                    (options.https() ? "https" : "http") +
                    "://yourserver:" + options.port());

        Logger.init(options);
        TraceKeeper.init(options);

        ProgramManager pm = new ProgramManager();
        LeadPerformer leader = new LeadPerformer();
        BoxOffice boxOffice = new BoxOffice();
        InfoCenter infoCenter = new InfoCenter();
        PlayCoordinator coordinator = new PlayCoordinator();

        theatre = new Theatre(options, inst, pm, leader, boxOffice, infoCenter, coordinator);
        theatre.init();
        theatre.prepare();
        theatre.start();
    }

    private void init() {
        leadPerformer.init(pm);
        infoCenter.init(inst, pm, options);
        coordinator.init(inst, options, pm, infoCenter, leadPerformer);
        boxOffice.init(options, coordinator, infoCenter);
    }

    private void prepare() {
        pm.addDefaultTrace(options.defaultTraceAsArray());
    }

    private void start() {
        this.boxOffice.start();
    }
}
