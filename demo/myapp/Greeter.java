package myapp;

class Greeter {

  String greet(String name) {
    System.out.println("  in myapp.Greeter.greet()...");
    return "Hello, " + beautify(name);
  }

  String beautify(String name) {
    System.out.println("  in myapp.Greeter.beautify()...");
    return name.toUpperCase();
  }
}
