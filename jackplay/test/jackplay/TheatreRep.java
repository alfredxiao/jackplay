package jackplay;


import jackplay.core.InfoCenter;
import jackplay.core.Jack;

public class TheatreRep {
    public static InfoCenter getInfoCenter() {
        return Agent.infoCenter;
    }

    public static Jack getJack() {
        return Agent.jack;
    }
}
