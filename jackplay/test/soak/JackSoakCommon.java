package soak;

import jackplay.Logger;
import jackplay.TheatreRep;
import jackplay.bootstrap.PlayGround;
import jackplay.bootstrap.TraceKeeper;
import jackplay.play.Jack;
import testedapp.myapp.MyBaseClass;
import testedapp.myapp.MyClass;
import testedapp.myapp.MyLateLoadingClass;

import java.util.concurrent.ThreadLocalRandom;

public class JackSoakCommon {
    static Jack jack = TheatreRep.getJack();
    static PlayGround test1 = new PlayGround("testedapp.myapp.MyBaseClass.test1(int,java.lang.String)");
    static PlayGround test2 = new PlayGround("testedapp.myapp.MyBaseClass.test2(java.lang.Object,java.util.List)");
    static PlayGround test3 = new PlayGround("testedapp.myapp.MyBaseClass.test3(java.lang.Object[],int[][])");
    static PlayGround test4 = new PlayGround("testedapp.myapp.MyBaseClass.test4()");
    static PlayGround test5 = new PlayGround("testedapp.myapp.MyBaseClass.test5(java.lang.String)");
    static PlayGround lateLoading = new PlayGround("testedapp.myapp.MyLateLoadingClass.lateLoadingFunction(java.lang.String)");
    static MyClass myObj = new MyClass();
    static int runCount = 0;
    static boolean stoppable = false;

    static void changeOnce() throws Exception {
        jack.trace(test1);
        jack.trace(test2);
        jack.trace(test3);
        jack.trace(test4);
        jack.trace(test5);
        jack.trace(lateLoading);
        jack.redefine(test1, "{ return \"REDEFINED\"; }");
        jack.redefine(test2, "{ int a = 2; }");
        try {
            jack.redefine(test1, "{ return no_such_thing; }");
        } catch(Exception ignore) {}
        try {
            jack.redefine(test3, "{ return 3; }");
        } catch(Exception ignore) {}
        jack.redefine(test2, "{ int c = 6; }");

        jack.undoRedefine(test3);
    }

    static void runOnce(String who) {

        try {
            myObj.test2(null, null);
        } catch(NullPointerException ignore) {}
        myObj.test3(new Object[]{}, null);
        MyLateLoadingClass late = new MyLateLoadingClass();
        late.lateLoadingFunction("ha");

        runCount ++;
        Logger.info(who, "runCount:" + runCount);
    }

    static void prepareRunning() throws Exception {
        JackSoakCommon.jack.undoAll();
        TraceKeeper.clearLogHistory();
        Class myAbstractClass = MyBaseClass.class;
        Class myClass = MyClass.class;
    }

    public static void startRunning() {
        startTest1();
        startTest2();
        startTest3();
        startTest4();
        startTest5();
    }

    private static void startTest1() {
        new Thread(new Runnable() {
            public void run() {
                while (!stoppable) {
                    myObj.test1(nextInt(0, 10000), "AString-" + nextInt(0, 20000));
                    myObj.test1(nextInt(0, 10000), "BString-" + nextInt(0, 20000));
                    myObj.test1(nextInt(0, 10000), "CString-" + nextInt(0, 20000));
                    sleepSmallRandom();
                }
            }
        }).start();
    }

    private static void startTest2() {
        new Thread(new Runnable() {
            public void run() {
                while (!stoppable) {
                    try {
                        myObj.test2(null, null);
                    } catch(NullPointerException ignore) {}
                    sleepSmallRandom();
                }
            }
        }).start();
    }

    private static void startTest3() {
        new Thread(new Runnable() {
            public void run() {
                while (!stoppable) {
                    myObj.test3(new Object[]{}, null);
                    sleepSmallRandom();
                    MyLateLoadingClass late = new MyLateLoadingClass();
                    late.lateLoadingFunction("ha");
                }
            }
        }).start();
    }

    private static void startTest4() {
        new Thread(new Runnable() {
            public void run() {
                while (!stoppable) {
                    myObj.test4();
                    sleepSmallRandom();
                }
            }
        }).start();
    }

    private static void startTest5() {
        new Thread(new Runnable() {
            public void run() {
                while (!stoppable) {
                    try {
                        myObj.test5("haha");
                    } catch(Exception e) {

                    }
                    sleepSmallRandom();
                }
            }
        }).start();
    }

    public static void stopRunning() {
        stoppable = true;
    }

    static void sleepSmallRandom() {
        long r = nextInt(50, 200);
        try {
            Thread.sleep(r);
        } catch(Exception e) {}
    }

    static void sleepSmallRandom(int max) {
        long r = nextInt(0, max);
        try {
            Thread.sleep(r);
        } catch(Exception e) {}
    }

    synchronized static int nextInt(int start, int endExclusive) {
        return ThreadLocalRandom.current().nextInt(start, endExclusive);
    }
}
