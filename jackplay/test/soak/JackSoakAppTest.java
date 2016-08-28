package soak;

import org.junit.Test;

import static soak.JackSoakCommon.changeOnce;

public class JackSoakAppTest {

    final long RUN_LENGTH_MS = 10 * 60*60*1000;

    @Test
    public void runAppSoapTest() throws Exception {
        JackSoakCommon.prepareRunning();

        changeOnce();

        long start = System.currentTimeMillis();

        JackSoakCommon.startRunning();

        while (System.currentTimeMillis() - start < RUN_LENGTH_MS) {
            Thread.sleep(3000);
        }

        JackSoakCommon.stopRunning();
    }
}
