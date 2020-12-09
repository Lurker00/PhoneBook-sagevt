package com.sagetech.sagevt;

import name.lurker.lowlevel.wrappers.InputManager;
import name.lurker.lowlevel.wrappers.Process;
import name.lurker.forsagevt.DeviceControl;
import name.lurker.forsagevt.Options;

import android.os.SystemClock;
import android.view.KeyEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {
    private static void loop(int pid) {
        ApkSession session = ApkSession.getInstance();
        while (true) {
            if (!session.isAlive()) {
                SGVTLog.msg("session closed, so do I");
                return;
            }

            SystemClock.sleep(1000);
            if (Process.getUidForPid(pid) <= 0) {
                SGVTLog.msg("parent was killed. quit myself now.");
                session.interrupt();
                try { session.join(); } catch (Exception e) {}
                return;
            }
        }
    }

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                SGVTLog.err("Exception in thread " + t);
                SGVTLog.exception(e);
            }
        });

        SGVTLog.msg("starting: "+android.os.Process.myPid());
        Options options = new Options(args);
        if (options.watchPid() <= 0) {
            // su is required because Anyware app kills the process instead,
            // making impossible to restore the device state
            forkMe(args, options.useSu());
            // It will not come here on USB disconnect
            SGVTLog.msg("stopped: "+android.os.Process.myPid());
            return;
        }

        SGVTLog.msg("started: "+android.os.Process.myPid()+"<-"+options.watchPid());
        Process.setArgV0("sagevt.i");

        // Early session start, to let the app to connect while we are initializing
        ApkSession session = ApkSession.getInstance();

        DeviceControl.getInstance().setOptions(options);

        if (options.getSendBack()) { // close Anyware app
            InputManager.getInstance().injectKeycode(KeyEvent.KEYCODE_BACK);
            InputManager.getInstance().injectKeycode(KeyEvent.KEYCODE_BACK);
        }

        loop(options.watchPid());

        DeviceControl.getInstance().Finish();

        if (options.getSendBack()) { // close Anyware app
            SystemClock.sleep(1000);
            InputManager.getInstance().injectKeycode(KeyEvent.KEYCODE_BACK);
            InputManager.getInstance().injectKeycode(KeyEvent.KEYCODE_BACK);
        }

        SGVTLog.msg("stopped");

        // Call exit to ensure full stop
        System.exit(0);
    }

    public static String[] getEnv() {
        Map<String, String> myenv = System.getenv();

        String[] key = myenv.keySet().toArray(new String[0]);
        String[] val = myenv.values().toArray(new String[0]);

        String[] env = new String[myenv.size()];
        for (int i=0; i<env.length; ++i)
            env[i] = key[i]+"="+val[i];

        return env;
    }

    private static java.lang.Process doFork(final String[] args, boolean su) throws IOException {
        List<String> cmd = new ArrayList<String>();
        if (su) {
            cmd.add("su"); cmd.add("-c");
        }
        cmd.add("app_process");
        cmd.add("/"); // unused parameter
        cmd.add(Main.class.getName());
        cmd.add("pid="+Process.myPid());
        for (int i=0; i<args.length; ++i)
            cmd.add(args[i]);

        return Runtime.getRuntime().exec(cmd.toArray(new String[cmd.size()]), getEnv());
    }

    private static void forkMe(final String[] args, boolean su) {
        SGVTLog.msg("sagevt is forking: "+android.os.Process.myPid());

        try {
            final java.lang.Process fork = doFork(args, su);
            SGVTLog.msg("sagevt forked");
            if (SGVTLog.debug) { // Redirect child output to console
                BufferedReader reader = new BufferedReader(new InputStreamReader(fork.getInputStream(),"UTF-8"));
                try {
                    String line;
                    while((line=reader.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch(Exception e) {
                }
                reader.close();
            } else {
                fork.waitFor();
            }
        }
        catch (Exception e) {
            SGVTLog.err("Can't fork!");
            SGVTLog.exception(e);
        }
    }
}
