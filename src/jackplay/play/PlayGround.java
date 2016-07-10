package jackplay.play;

import java.util.Objects;

public class PlayGround {
    final String className;
    final String methodLongName;
    final String methodShortName;

    public PlayGround(String methodLongName) {
        int lastDot = methodLongName.lastIndexOf('.');
        if (lastDot <= 1 || methodLongName.endsWith(".")) throw new RuntimeException(methodLongName + " : Invalid format which should conform to className.methodLongName");

        int firstParen = methodLongName.indexOf('(');
        int dotBeforeMethodName = methodLongName.substring(0, firstParen).lastIndexOf('.');

        this.className = methodLongName.substring(0, dotBeforeMethodName);
        this.methodLongName = methodLongName;
        this.methodShortName = findMethodShortName(methodLongName);
    }

    private static String findMethodShortName(String methodLongName) {
        int firstParen = methodLongName.indexOf('(');
        int dotBeforeMethodName = methodLongName.substring(0, firstParen).lastIndexOf('.');
        return methodLongName.substring(dotBeforeMethodName + 1, firstParen);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayGround playGround = (PlayGround) o;
        return Objects.equals(className, playGround.className) &&
                Objects.equals(methodLongName, playGround.methodLongName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, methodLongName);
    }

    @Override
    public String toString() {
        return "PlayGround{" +
                "className='" + className + '\'' +
                ", methodLongName='" + methodLongName + '\'' +
                '}';
    }
}
