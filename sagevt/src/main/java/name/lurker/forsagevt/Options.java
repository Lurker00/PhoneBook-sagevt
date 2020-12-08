package name.lurker.forsagevt;

import name.lurker.util.Log;

import android.graphics.Point;
import android.graphics.Rect;

public class Options {
    private int density               = 0;
    private final Point size          = new Point(0,0);
    private boolean landscapeMode     = false;
    private boolean dimScreen         = false;
    private int pid                   = -1;
    private boolean disableHWOverlays = false;
    private boolean sendBack          = false;
    private String ime                = "";

    public int getDensity() { return density; }

    public Point getSize() { return size; }

    public boolean getLandscapeMode() { return landscapeMode; }

    public boolean getDimScreen() { return dimScreen; }

    public int watchPid() { return pid; }

    public boolean getDisableHWOverlays() { return disableHWOverlays; }

    public boolean getSendBack() { return sendBack; }

    public String getIME() { return ime; }

    private void setOption(final String option) {
        String[] pair = option.split("=");
        if (pair.length != 2) {
           Log.e("Expected key=value pair ("+option+")");
            return;
        }

        if ("density".equals(pair[0])) {
            density = Integer.parseInt(pair[1]);
        } else if ("size".equals(pair[0])) {
            String[] value=pair[1].split(":");
            if (value.length != 2) value=pair[1].split("x");
            if (value.length != 2) {
                Log.e("Expected size=width:height ("+option+")");
                return;
            }
            size.x = Integer.parseInt(value[0]);
            size.y = Integer.parseInt(value[1]);
        } else if ("landscape".equals(pair[0])) {
            landscapeMode = Boolean.parseBoolean(pair[1]);
        } else if ("dim".equals(pair[0])) {
            dimScreen = Boolean.parseBoolean(pair[1]);
        } else if ("pid".equals(pair[0])) {
            pid = Integer.parseInt(pair[1]);
        } else if("disableHWOverlays".equals(pair[0])) {
            disableHWOverlays = Boolean.parseBoolean(pair[1]);
        } else if("sendback".equals(pair[0])) {
            sendBack = Boolean.parseBoolean(pair[1]);
        } else if("ime".equals(pair[0])) {
            ime = pair[1];
        } else if("debug".equals(pair[0])) {
            Log.debug = Boolean.parseBoolean(pair[1]);
        } else {
            Log.i("Unknown option: "+option);
        }
    }

    public Options(String[] args) {
        for (int i=0; i<args.length; ++i)
            setOption(args[i]);
    }
}
