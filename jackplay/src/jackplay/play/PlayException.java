package jackplay.play;

public class PlayException extends Exception {
    public PlayException(String msg) {
        super(msg);
    }

    public PlayException(Exception e) {
        super(e);
        super.initCause(e);
    }
}
