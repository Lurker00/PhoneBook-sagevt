package name.lurker.lowlevel;

import name.lurker.lowlevel.wrappers.IDisplayManager;
import name.lurker.lowlevel.wrappers.IWindowManager;
import name.lurker.lowlevel.wrappers.Process;
import name.lurker.util.Log;

import android.content.ContextWrapper;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

@SuppressWarnings("deprecation")
public class ScreenRotation extends ContextWrapper {
    static private final String SERVICE_NAME = "name.lurker.ScreenRotation";

    private final WindowManager   wm = IWindowManager.getWindowManager(this);
    private final DisplayManager  dm = IDisplayManager.getDisplayManager(this);
    private final IWindowManager iwm = IWindowManager.getInstance();

    static public final int PORTRAIT    = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    static public final int LANDSCAPE   = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    static public final int UNSPECIFIED = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;

    private volatile LinearLayout orientationChanger;
    private volatile WindowManager.LayoutParams orientationLayout;
    private volatile int currentRotation = UNSPECIFIED;
    private volatile boolean rotationLocked = false;

    private volatile HandlerThread  serviceThread;
    private volatile ServiceHandler serviceHandler;

    private final Runnable periodicTask = new Runnable() {
		@Override
		public void run() {
            serviceHandler.removeCallbacks(this);
//            Log.i("Periodic task");
		    if (!set(currentRotation))
            	serviceHandler.postDelayed(this, 1000);
		}
	};

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
//            Log.i("handleMessage: "+msg.arg1+" "+msg);
            if (orientationChanger == null) return;
            removeCallbacks(periodicTask);
            int orientation = msg.what;
            try {
                if (rotationLocked) {
                    rotationLocked = false;
                    wm.removeView(orientationChanger);
                }
                if (orientation == PORTRAIT || orientation == LANDSCAPE) {
                    orientationLayout.screenOrientation = orientation;
                    wm.addView(orientationChanger, orientationLayout);
                    orientationChanger.setVisibility(View.VISIBLE);
                    currentRotation = orientation;
                    rotationLocked = true;
                    postDelayed(periodicTask, 1000);
                }
                Log.i("Orientation: "+orientation);
            } catch (IllegalStateException e) {
                Log.e("ScreenRotate.Set:", e);
                try {
                    rotationLocked = false;
                    wm.removeView(orientationChanger);
                } catch (Exception e1) {
                   Log.e("ScreenRotate.Set removeView:", e1);
                }
            } catch (Exception e) {
                Log.e("ScreenRotate.Set:", e);
            }
        }
    }

    public ScreenRotation() {
        super(null);

        try { Looper.prepareMainLooper(); } catch (IllegalStateException e) {}
        try {
            serviceThread = new HandlerThread(SERVICE_NAME);
            serviceThread.start();
            serviceHandler = new ServiceHandler(serviceThread.getLooper());

            orientationChanger = new LinearLayout(this);
            orientationLayout = new WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
                    , 0
                    | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    , PixelFormat.RGBA_8888);

            Log.i("ScreenRotation started");

        } catch (Exception e) {
            Log.e("ScreenRotate:", e);
        }
    }

    public void stop() {
        serviceHandler.removeCallbacks(periodicTask);
        if (rotationLocked) {
            set(UNSPECIFIED);
        }
        try {
            serviceThread.quitSafely();
        } catch (Exception e) {
            Log.e("Looper.quit:", e);
        }
        orientationChanger = null;
        Log.i("ScreenRotation stopped");
    }

    private boolean sameOrientation(int r) {
        if (currentRotation != r) return false;
        int co = iwm.getRotation();
        if (currentRotation == PORTRAIT  && (co == Surface.ROTATION_0  || co == Surface.ROTATION_180)) return true;
        if (currentRotation == LANDSCAPE && (co == Surface.ROTATION_90 || co == Surface.ROTATION_270)) return true;
        return false;
    }

    public boolean set(int orientation) {
        if (orientationChanger == null)   return false;
        if (sameOrientation(orientation)) return false;

        serviceHandler.removeCallbacks(periodicTask);
        switch(orientation) {
            case PORTRAIT:
            case LANDSCAPE:
            case UNSPECIFIED:
                serviceHandler.sendEmptyMessage(orientation);
                return true;
            default:
                Log.i("Invalid orientation: "+orientation);
                break;
        }
        return false;
    }

    /*************************************************************************
         The minimum Context implementation required for this particular task
    **************************************************************************/
    @Override
    public Resources getResources() {
        return Resources.getSystem();
    }

    @Override
    public Object getSystemService(String name) {
        if (android.content.Context.WINDOW_SERVICE.equals(name))
            return wm;
        if (android.content.Context.DISPLAY_SERVICE.equals(name))
            return dm;
        Log.e("getSystemService unknown: "+name);
        return null;
    }

//    @Override // Missed in class description, but is called
    android.view.Display getDisplay() {
        return IDisplayManager.getDefaultDisplay();
    }

//    @Override // It is @hide
    public int getDisplayId() {
        // This works
//        return android.view.Display.DEFAULT_DISPLAY;
        // But this is consistent
        return getDisplay().getDisplayId();
    }

    @Override
    public ApplicationInfo getApplicationInfo() {
        ApplicationInfo ai  = new ApplicationInfo();
        ai.packageName      = getBasePackageName();
        ai.minSdkVersion    = 9;
        ai.targetSdkVersion = 21;
        ai.uid              = Process.getUidForPid(Process.myPid());
        return ai;
    }

    @Override
    public Resources.Theme getTheme() {
        return getResources().newTheme();
    }

    @Override
    public int checkCallingOrSelfPermission(String permission) {
//        Log.i("Granted permission: "+permission);
        return PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public Looper getMainLooper() {
        return serviceThread.getLooper();
    }

//    @Override // Hidden method
    public String getBasePackageName() {
        return SERVICE_NAME;
    }
}
