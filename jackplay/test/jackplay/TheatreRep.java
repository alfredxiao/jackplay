package jackplay;


import jackplay.play.InfoCenter;
import jackplay.play.PlayCoordinator;

public class TheatreRep {
    public static Theatre getTheatre() {
        return Theatre.theatre;
    }

    public static InfoCenter getInfoCenter() {
        return getTheatre().infoCenter;
    }

    public static PlayCoordinator getPlayCoordinator() {
        return getTheatre().coordinator;
    }
}
