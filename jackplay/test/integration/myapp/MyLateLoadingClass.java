package integration.myapp;

public class MyLateLoadingClass {
    public static long whenLoaded = System.currentTimeMillis();

    public String myfunction11(String name) {
        return "myfunction11." + name;
    }
}
