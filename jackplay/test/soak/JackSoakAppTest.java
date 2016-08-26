package soak;

import org.junit.Test;

import static soak.JackSoapCommon.changeOnce;

public class JackSoakAppTest {

    final long RUN_LENGTH_MS = 10 * 60*60*1000;

    @Test
    public void runAppSoapTest() throws Exception {
        JackSoapCommon.prepareRunning();

        changeOnce();

        long start = System.currentTimeMillis();

        JackSoapCommon.startRunning();

        while (System.currentTimeMillis() - start < 10 * RUN_LENGTH_MS) {
            Thread.sleep(3000);
        }

        JackSoapCommon.stopRunning();
    }
}
