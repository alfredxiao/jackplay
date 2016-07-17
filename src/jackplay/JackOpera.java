package jackplay;

import jackplay.play.Composer;
import jackplay.play.ProgramManager;
import jackplay.play.performers.LeadPerformer;
import jackplay.web.BoxOffice;

import java.lang.instrument.*;

public class JackOpera implements IOpera {
    static JackOpera jackOpera;

    Composer composer;
    ProgramManager pm;
    LeadPerformer leader;
    BoxOffice boxOffice;

    public static void premain(String agentArgs, Instrumentation inst) {
        Logger.log("running JackOpera with arguments:" + agentArgs);
        Logger.debug("Instrumentation.isRetransformClassesSupported():" + inst.isRetransformClassesSupported());
        Logger.debug("Instrumentation.isRedefineClassesSupported():" + inst.isRedefineClassesSupported());

        Options options = Options.optionsMergedWithDefaults(agentArgs);
        Logger.initialise(options);

        jackOpera = new JackOpera();
        Composer composer = new Composer(jackOpera);
        ProgramManager pm = new ProgramManager();
        LeadPerformer leader = new LeadPerformer(jackOpera);
        BoxOffice boxOffice = new BoxOffice(jackOpera);

        jackOpera.composer = composer;
        jackOpera.pm = pm;
        jackOpera.leader = leader;
        jackOpera.boxOffice = boxOffice;

        jackOpera.start();
    }

    public Composer getComposer() {
        return composer;
    }

    public ProgramManager getProgramManager() {
        return pm;
    }

    public LeadPerformer getLeadPerformer() {
        return leader;
    }

    public void start() {
        this.boxOffice.start();
    }
}
