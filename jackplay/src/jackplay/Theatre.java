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
    Composer composer;
    ProgramManager pm;
    LeadPerformer leadPerformer;
    BoxOffice boxOffice;
    InfoCenter infoCenter;

    public Theatre(Options options, Instrumentation inst, Composer composer, ProgramManager pm,
                   LeadPerformer leadPerformer, BoxOffice boxOffice, InfoCenter infoCenter) {
        this.options = options;
        this.inst = inst;
        this.composer = composer;
        this.pm = pm;
        this.leadPerformer = leadPerformer;
        this.boxOffice = boxOffice;
        this.infoCenter = infoCenter;
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        Logger.info("running JackPlay with arguments:" + (agentArgs == null ? "(no args, resort to default)" : agentArgs));
        Logger.debug("Instrumentation.isRetransformClassesSupported():" + inst.isRetransformClassesSupported());
        Logger.debug("Instrumentation.isRedefineClassesSupported():" + inst.isRedefineClassesSupported());

        Options options = Options.optionsMergedWithDefaults(agentArgs);
        Logger.info("open your browser and point to " +
                    (options.https() ? "https" : "http") +
                    "://yourserver:" + options.port());

        Logger.init(options);
        TraceKeeper.init(options);

        Composer composer = new Composer();
        ProgramManager pm = new ProgramManager();
        LeadPerformer leader = new LeadPerformer();
        BoxOffice boxOffice = new BoxOffice();
        InfoCenter infoCenter = new InfoCenter();

        theatre = new Theatre(options, inst, composer, pm, leader, boxOffice, infoCenter);
        theatre.init();
        theatre.start();
    }

    public void init() {
        composer.init(options, inst, pm, leadPerformer);
        pm.init(composer, options, infoCenter);
        leadPerformer.init(composer, pm);
        boxOffice.init(options, pm, infoCenter);
        infoCenter.init(inst, pm, options);
    }

    public void start() {
        this.boxOffice.start();
    }
}
