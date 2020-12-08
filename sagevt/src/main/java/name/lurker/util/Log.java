package name.lurker.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Log {
    private static String TAG = "lurker";
    public static boolean debug = false;

    public static class init {
        public init(String tag) {
            TAG = tag;
        }
    }

    public static void i(String str) {
        android.util.Log.i(TAG, str);
        if (debug) System.out.println(str);
    }

    public static void e(String str) {
        android.util.Log.e(TAG, str);
        // Use out for easy redirection!
        if (debug) System.out.println(str);
    }

    public static void e(String str, Throwable t) {
        e(str);
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        e(sw.toString());
    }
}
