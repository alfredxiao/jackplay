public class Demo {
  public static void main(String[] args) throws Exception {
    Greeter greeter = new Greeter();
    greeter.greet("Alfred");

    while (true) {
      Thread.sleep(2000);
      Greeter greeter2 = new Greeter();
      greeter2.greet("Gary");
    }
  }
}
