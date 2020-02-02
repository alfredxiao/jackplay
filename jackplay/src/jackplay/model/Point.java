package jackplay.model;

/**
 * A <code>Point</code> refers to a specific place within a <code>Site</code> where tracing happens and information
 * being collected.
 *
 * <p><code>MethodEntrance</code> refers to execution entering a method body.</p>
 * <p><code>MethodExit</code> refers to execution leaving a method body without any exception.</p>
 * <p><code>MethodTermination</code> refers to execution terminated during the course of running method body and
 * ends up with an <code>Throwable</code> being thrown.</p>
 */
public enum Point {
    MethodEntrance,
    MethodExit,
    MethodTermination
}