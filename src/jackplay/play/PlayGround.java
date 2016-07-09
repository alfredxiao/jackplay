package jackplay.play;

import java.util.Objects;

public class PlayGround {
    final String className;
    final String methodName;

    public PlayGround(String playGround) {
        int lastDot = playGround.lastIndexOf('.');
        if (lastDot <= 1 || playGround.endsWith(".")) throw new RuntimeException(playGround + " : Invalid format which should conform to className.methodName");
        this.className = playGround.substring(0, lastDot);
        this.methodName = playGround.substring(lastDot + 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayGround playGround = (PlayGround) o;
        return Objects.equals(className, playGround.className) &&
                Objects.equals(methodName, playGround.methodName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, methodName);
    }

    @Override
    public String toString() {
        return "PlayGround{" +
                "className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                '}';
    }
}
