package jackplay;


import jackplay.core.InfoCenter;
import jackplay.core.Jack;

public class TheatreRep {
    public static InfoCenter getInfoCenter() {
        return JackplayAgent.infoCenter;
    }

    public static Jack getJack() {
        return JackplayAgent.jack;
    }
}
