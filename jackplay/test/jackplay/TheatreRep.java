package jackplay;


import jackplay.play.InfoCenter;

public class TheatreRep {
    public static Theatre getTheatre() {
        return Theatre.theatre;
    }

    public static InfoCenter getInfoCenter() {
        return getTheatre().infoCenter;
    }
}
