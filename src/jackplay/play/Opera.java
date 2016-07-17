package jackplay.play;

import jackplay.Options;
import jackplay.play.performers.LeadPerformer;

import java.lang.instrument.Instrumentation;

public interface Opera {
    Options getOptions();
    Instrumentation getInstrumentation();
    Composer getComposer();
    ProgramManager getProgramManager();
    LeadPerformer getLeadPerformer();
}
