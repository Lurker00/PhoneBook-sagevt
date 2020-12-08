package com.sagetech.sagevt;

import name.lurker.lowlevel.wrappers.InputManager;

import android.os.SystemClock;
import android.view.InputDevice;
import android.view.MotionEvent;

import java.io.File;

public class AdbIO implements IOInterface {
    public static final int VERSION = 1;

    private static final int MAX_NUM_POINTER = 15;

    private final InputManager im = InputManager.getInstance();

    private class PointerInfo {
        MotionEvent.PointerCoords coor;
        int eventAction;
        int id;
        String name;
        MotionEvent.PointerProperties property;

        private PointerInfo() {
            this.name = null;
            this.coor = null;
            this.property = null;
            this.eventAction = 0;
        }
    }
    private PointerInfo[] pointers = new PointerInfo[MAX_NUM_POINTER];

    private AdbIO() {
        for (int i = 0; i < MAX_NUM_POINTER; i++) {
            this.pointers[i] = new PointerInfo();
        }
    }

    private static AdbIO instance;
    public static AdbIO getInstance() {
        if (instance == null) {
            instance = new AdbIO();
        }
        return instance;
    }

    private PointerInfo getPointerInfo(String name) {
        for (int i = 0; i < MAX_NUM_POINTER; i++) {
            if (this.pointers[i].name != null && this.pointers[i].name.equals(name)) {
                return this.pointers[i];
            }
        }
        return null;
    }

    private int getFreePointerIndex() {
        for (int i = 0; i < MAX_NUM_POINTER; i++) {
            if (this.pointers[i].name == null) {
                return i;
            }
        }
        return -1;
    }

    private int getPressedCount() {
        int count = 0;
        for (int i = 0; i < MAX_NUM_POINTER; i++) {
            if (this.pointers[i].name != null) {
                count++;
            }
        }
        return count;
    }

    private MotionEvent.PointerCoords[] getPointerCoords() {
        MotionEvent.PointerCoords[] array = new MotionEvent.PointerCoords[getPressedCount()];
        int off = 0;
        int i = 0;
        while (true) {
            int off2 = off;
            if (i >= MAX_NUM_POINTER) {
                return array;
            }
            if (this.pointers[i].name != null) {
                off = off2 + 1;
                array[off2] = this.pointers[i].coor;
            } else {
                off = off2;
            }
            i++;
        }
    }

    private MotionEvent.PointerProperties[] getPointerProperties() {
        MotionEvent.PointerProperties[] array = new MotionEvent.PointerProperties[getPressedCount()];
        int off = 0;
        int i = 0;
        while (true) {
            int off2 = off;
            if (i >= MAX_NUM_POINTER) {
                return array;
            }
            if (this.pointers[i].name != null) {
                off = off2 + 1;
                array[off2] = this.pointers[i].property;
            } else {
                off = off2;
            }
            i++;
        }
    }

    private boolean injectTouch(long firstDownTime, long currTime, int action, int pointcount, MotionEvent.PointerCoords[] pointerCoords, MotionEvent.PointerProperties[] properties) {
        MotionEvent event = MotionEvent.obtain(firstDownTime, currTime, action, pointcount, properties, pointerCoords
                                             , 0, 0, 1.0f, 1.0f, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0);
        boolean ret = im.injectEvent(event);
        event.recycle();
        return ret;
    }

    public PointerInfo[] getCleanPoints() {
        int off;
        PointerInfo[] ps = new PointerInfo[getPressedCount()];
        PointerInfo[] pointerInfoArr = this.pointers;
        int length = pointerInfoArr.length;
        int i = 0;
        int off2 = 0;
        while (i < length) {
            PointerInfo pi = pointerInfoArr[i];
            if (pi.name != null) {
                off = off2 + 1;
                ps[off2] = pi;
            } else {
                off = off2;
            }
            i++;
            off2 = off;
        }
        return ps;
    }

    public void releaseAll() {
        for (int i = 0; i < MAX_NUM_POINTER; i++) {
            if (this.pointers[i].name != null) {
                sendTouch(this.pointers[i].name, -1, -1, false);
            }
        }
    }

    public int sendTouch(String name, int x, int y, boolean isDown) {
        int eventAction = -1;
        boolean remove = false;
        PointerInfo pointerInfo = getPointerInfo(name);
        if (!isDown) {
            PointerInfo[] cpoints = getCleanPoints();
            if (pointerInfo == null || cpoints.length < 1) {
                return 0;
            }
            if (cpoints.length == 1) {
                eventAction = 1;
            } else if (cpoints.length <= 1) {
                return 0;
            } else {
                int i = 0;
                while (true) {
                    if (i >= cpoints.length) {
                        break;
                    } else if (cpoints[i].name.equals(name)) {
                        eventAction = (i << 8) | MotionEvent.ACTION_POINTER_UP;
                        break;
                    } else {
                        i++;
                    }
                }
            }
            remove = true;
        } else if (pointerInfo == null) {
            int index = getFreePointerIndex();
            int npressed = getPressedCount();
            pointerInfo = this.pointers[index];
            pointerInfo.name = name;
            pointerInfo.id = index;
            pointerInfo.coor = new MotionEvent.PointerCoords();
            pointerInfo.coor.pressure = 0.0f;
            pointerInfo.coor.size = 0.0f;
            pointerInfo.coor.x = (float) x;
            pointerInfo.coor.y = (float) y;
            pointerInfo.property = new MotionEvent.PointerProperties();
            pointerInfo.property.toolType = MotionEvent.TOOL_TYPE_FINGER;
            pointerInfo.property.id = index;
            if (npressed < 1) {
                eventAction = 0;
            } else {
                eventAction = (pointerInfo.id << 8) | MotionEvent.ACTION_POINTER_DOWN;
            }
        } else if (pointerInfo.eventAction == 0) {
            pointerInfo.coor.x = (float) x;
            pointerInfo.coor.y = (float) y;
            eventAction = 2;
        } else {
            SGVTLog.msg("error event actions.");
        }
        boolean ret = false;
        if (!SGVTLog.forPC) {
            ret = injectTouch(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), eventAction, getPressedCount(), getPointerCoords(), getPointerProperties());
        }
        if (remove || !ret) {
            pointerInfo.name = null;
        }
        return 0;
    }

    public int getVersion() {
        return VERSION;
    }

    public int cleanOld() {
        new File("/data/local/tmp/.sagevt/instance.pid").delete();
        new File("/data/local/tmp/.sagevt/sagevt-daemon").delete();
//        new File("/data/local/tmp/.sagevt/sagevt.jar").delete();
        return 0;
    }
}
