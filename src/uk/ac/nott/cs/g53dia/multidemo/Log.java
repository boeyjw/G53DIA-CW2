/**
 * Utility class to display logs in the console
 */
public class Log {
    private boolean doLog;

    /**
     * Allow logging
     * @param doLog If true, display logs
     */
    public Log(boolean doLog) {
        this.doLog = doLog;
    }

    /**
     * Defaults to no logs
     */
    public Log() {
        this(false);
    }

    /**
     * Display a log line with newline, using System.out.println
     * @param p String to be printed
     */
    public void d(String p) {
        if(doLog)
            System.out.println(p);
    }

    /**
     * Display a log line without newline, using System.out.print
     * @param p String to be printed
     */
    public void dc(String p) {
        if(doLog)
            System.out.print(p);
    }
}
