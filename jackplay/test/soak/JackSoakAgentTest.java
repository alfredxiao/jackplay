package soak;

import jackplay.Logger;
import org.junit.Test;

public class JackSoakAgentTest {


    final long RUN_LENGTH_MS = 10 * 60*60*1000;
    int retransformationCount = 0;

    @Test
    public void runForOneHour() throws Exception {
        JackSoapCommon.prepareRunning();

        long start = System.currentTimeMillis();

        JackSoapCommon.startRunning();

        while (System.currentTimeMillis() - start < RUN_LENGTH_MS) {
            JackSoapCommon.jack.trace(JackSoapCommon.test1);
            restAndCount();

            JackSoapCommon.jack.trace(JackSoapCommon.test2);
            restAndCount();

            JackSoapCommon.jack.trace(JackSoapCommon.test3);
            restAndCount();

            JackSoapCommon.jack.trace(JackSoapCommon.lateLoading);
            restAndCount();

            JackSoapCommon.jack.redefine(JackSoapCommon.test1, "{ return \"REDEFINED\"; }");
            restAndCount();

            JackSoapCommon.jack.redefine(JackSoapCommon.test2, "{ int a = 2; }");
            restAndCount();

            try {
                JackSoapCommon.jack.redefine(JackSoapCommon.test1, "{ return no_such_thing; }");
            } catch(Exception ignore) {}
            restAndCount();

            try {
                JackSoapCommon.jack.redefine(JackSoapCommon.test3, "{ return 3; }");
            } catch(Exception ignore) {}
            restAndCount();

            JackSoapCommon.jack.redefine(JackSoapCommon.test2, "{ int c = 6; }");
            restAndCount();

            JackSoapCommon.jack.undoTrace(JackSoapCommon.test1);
            restAndCount();

            JackSoapCommon.jack.undoRedefine(JackSoapCommon.test1);
            restAndCount();

            Logger.info("soak-agent", "retransformationCount:" + retransformationCount);
        }

        JackSoapCommon.stopRunning();
    }

    private void restAndCount() {
        retransformationCount++;
//        System.gc();
        JackSoapCommon.sleepSmallRandom(5);
    }
}
