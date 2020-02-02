package jackplay.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * A <code>Site</code> represents a target entity being traced.
 *
 * <p>So far, we trace methods only.</p>
 *
 * <p>Within a <code>Site</code>, there are few points that it can be traced, e.g. method entry and method exit. These
 * are represented as @see <code>Point</code></p>
 */
public class Site {
    public final String packageName;                    // com.abc
    public final String classFullName;                  // com.abc.MyService
    public final String methodFullName;                 // com.abc.MyService.myfunction(java.lang.String,int)
    public final String methodLongName;                 // com.abc.MyService.myfunction
    public final String methodShortName;                // myfunction
    public final String methodShortNameWithSignature;   // myfunction(java.lang.String,int)
    public final String parameters;                     // "java.lang.String,int"

    // [ "java.lang.String", "int" ]
    public final List<String> parameterList = new LinkedList<>();

    final static String MESSAGE_INVALID_FORMAT = "invalid format, correct format is className.methodName()";

    public Site(String methodFullName) {
        if (null == methodFullName
                || methodFullName.length() < 5
                || !methodFullName.endsWith(")")
                || methodFullName.indexOf(' ') >= 0) {
            throwInvalidFormatMessage(methodFullName);
        }

        int lastDot = methodFullName.lastIndexOf('.');
        if (lastDot <= 1 || methodFullName.endsWith(".")) throwInvalidFormatMessage(methodFullName);

        int firstParen = methodFullName.indexOf('(');
        if (firstParen <= 0) throwInvalidFormatMessage(methodFullName);
        int secondParen = methodFullName.indexOf(')');

        int dotBeforeMethodName = methodFullName.substring(0, firstParen).lastIndexOf('.');

        this.classFullName = methodFullName.substring(0, dotBeforeMethodName);

        int lastDotInClassFullName = classFullName.lastIndexOf('.');
        this.packageName = lastDotInClassFullName > 0 ? classFullName.substring(0, lastDotInClassFullName) : "";
        this.methodFullName = methodFullName;
        this.methodLongName = methodFullName.substring(0, firstParen);
        this.methodShortName = methodFullName.substring(dotBeforeMethodName + 1, firstParen);
        this.methodShortNameWithSignature = methodFullName.substring(dotBeforeMethodName + 1);

        this.parameters = methodFullName.substring(firstParen + 1, secondParen);
        String[] parametersArray = this.parameters.split(",");
        for (String param : parametersArray) {
            if (param != null && param.length() > 0) {
                parameterList.add(param);
            }
        }
    }

    private void throwInvalidFormatMessage(String methodFullName) {
        throw new IllegalArgumentException("[" + methodFullName + "] is " + MESSAGE_INVALID_FORMAT);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Site site = (Site) o;
        return Objects.equals(classFullName, site.classFullName) &&
                Objects.equals(methodFullName, site.methodFullName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(methodFullName);
    }

    @Override
    public String toString() {
        return "PlayGround{" +
                "classFullName='" + classFullName + '\'' +
                ", methodFullName='" + methodFullName + '\'' +
                ", methodLongName='" + methodLongName + '\'' +
                ", methodShortName='" + methodShortName + '\'' +
                '}';
    }
}