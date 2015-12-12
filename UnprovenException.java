
package logic;


public class UnprovenException extends RuntimeException {

    /**
     * Creates a new instance of <code>UnprovenException</code> without detail
     * message.
     */
    public UnprovenException() {
    }

    /**
     * Constructs an instance of <code>UnprovenException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public UnprovenException(String msg) {
        super(msg);
    }
}
