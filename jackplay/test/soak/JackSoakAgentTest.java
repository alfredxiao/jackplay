package soak;

import jackplay.JackplayLogger;
import org.junit.Test;

public class JackSoakAgentTest {


    final long RUN_LENGTH_MS = 10 * 60*60*1000;
    int retransformationCount = 0;

    @Test
    public void runForOneHour() throws Exception {
        JackSoakCommon.prepareRunning();

        long start = System.currentTimeMillis();

        JackSoakCommon.startRunning();

        while (System.currentTimeMillis() - start < RUN_LENGTH_MS) {
            JackSoakCommon.jack.trace(JackSoakCommon.test1);
            restAndCount();

            JackSoakCommon.jack.trace(JackSoakCommon.test2);
            restAndCount();

            JackSoakCommon.jack.trace(JackSoakCommon.test3);
            restAndCount();

            JackSoakCommon.jack.trace(JackSoakCommon.lateLoading);
            restAndCount();

            JackSoakCommon.jack.redefine(JackSoakCommon.test1, "{ return \"REDEFINED\"; }");
            restAndCount();

            JackSoakCommon.jack.redefine(JackSoakCommon.test2, "{ int a = 2; }");
            restAndCount();

            try {
                JackSoakCommon.jack.redefine(JackSoakCommon.test1, "{ return no_such_thing; }");
            } catch(Exception ignore) {}
            restAndCount();

            try {
                JackSoakCommon.jack.redefine(JackSoakCommon.test3, "{ return 3; }");
            } catch(Exception ignore) {}
            restAndCount();

            JackSoakCommon.jack.redefine(JackSoakCommon.test2, "{ int c = 6; }");
            restAndCount();

            JackSoakCommon.jack.undoTrace(JackSoakCommon.test1);
            restAndCount();

            JackSoakCommon.jack.undoRedefine(JackSoakCommon.test1);
            restAndCount();

            JackplayLogger.info("soak-agent", "retransformationCount:" + retransformationCount);
        }

        JackSoakCommon.stopRunning();
    }

    private void restAndCount() {
        retransformationCount++;
//        System.gc();
        JackSoakCommon.sleepSmallRandom(5);
    }
}
