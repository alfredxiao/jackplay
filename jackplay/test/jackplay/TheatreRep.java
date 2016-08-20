package jackplay;


import jackplay.play.InfoCenter;
import jackplay.play.Jack;

public class TheatreRep {
    public static Theatre getTheatre() {
        return Theatre.theatre;
    }

    public static InfoCenter getInfoCenter() {
        return getTheatre().infoCenter;
    }

    public static Jack getJack() {
        return getTheatre().jack;
    }
}
