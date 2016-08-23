package integration.myapp;

public class MyLateLoadingClass {
    public static long whenLoaded = System.currentTimeMillis();

    public String lateLoadingFunction(String name) {
        return "lateLoadingFunction." + name;
    }
}
