package jackplay.play;

public class RedefinitionException extends PlayException {
    public RedefinitionException(String msg) {
        super(msg);
    }

    public RedefinitionException(Exception e) {
        super(e);
    }
}
