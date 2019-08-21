package jackplay;


import jackplay.play.InfoCenter;
import jackplay.play.Jack;

public class TheatreRep {
    public static InfoCenter getInfoCenter() {
        return JackplayAgent.infoCenter;
    }

    public static Jack getJack() {
        return JackplayAgent.jack;
    }
}
