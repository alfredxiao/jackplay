package myapp.greeter;


import java.util.Calendar;

public class NiceGreeter implements Greeter {

    public String greet(String name) {
        return genWelcomeWords() + " " + name;
    }

    public String genWelcomeWords() {
        int hourOfNow = getHourOfNow();

        if (hourOfNow >= 6 && hourOfNow <= 11) {
            return "Good morning!";
        } else if (hourOfNow >= 13 && hourOfNow <= 17) {
            return "Good afternoon";
        } else if (hourOfNow >= 18 && hourOfNow <= 20) {
            return "Good evening";
        } else if (hourOfNow >= 22 && hourOfNow <= 1) {
            return "Good night";
        } else {
            return "Hello";
        }
    }

    public int getHourOfNow() {
        Calendar rightNow = Calendar.getInstance();
        return rightNow.get(Calendar.HOUR_OF_DAY);
    }
}
