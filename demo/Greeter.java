class Greeter {

  String greet(String name) {
    System.out.println("  in Greeter.greet()...");
    return "Hello, " + beautify(name);
  }

  String beautify(String name) {
    System.out.println("  in Greeter.beautify()...");
    return name.toUpperCase();
  }
}
