package jackplay.model;

/**
 * A <code>Category</code> represents types of operations that are going to be performed by Jackplay agent.
 *
 * <p><code>TRACE</code> is for tracing method execution, what is input, output, and what exception is thrown.</p>
 * <p><code>REDEFINE</code> is for redefining method body in runtime, it is orthogonal to tracing.</p>
 */
public enum Category {
    TRACE,
    REDEFINE
}
