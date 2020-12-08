package name.lurker.lowlevel;

import name.lurker.util.Log;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.os.UserHandle;
import android.view.Surface;

import name.lurker.lowlevel.wrappers.InputMethodManager;
import name.lurker.lowlevel.wrappers.IPackageManager;
import name.lurker.lowlevel.wrappers.IWindowManager;
import name.lurker.lowlevel.wrappers.Settings;

public class DeviceControl {
    private final IWindowManager      wm = IWindowManager.getInstance();
    private final IPackageManager     pm = IPackageManager.getInstance();
    private final InputMethodManager imm = InputMethodManager.getInstance();

    private final int initialDensity = wm.getInitialDisplayDensity();
    private final int baseDensity    = wm.getBaseDisplayDensity();
    private boolean densityChanged   = false;

    private final int oldRotation        = wm.getRotation();
    private final boolean rotationFrozen = wm.isRotationFrozen();
    private boolean orientationChanged   = false;

    private final Point initialSize = new Point(0, 0);
    private final Point baseSize    = new Point(0, 0);
    private boolean sizeChanged     = false;

    private final String baseBrightness = Settings.get(Settings.SYSTEM, Settings.SCREEN_BRIGHTNESS);
    private boolean brightnessChanged = false;

    private ScreenRotation screenRotation;

    private boolean disableHWOverlays = false;

    private String IMEMethod;
    private boolean disableIme = false;
    private boolean enabledIme = false;
    private String lastIMEMethod;

    public DeviceControl() {
        wm.getInitialDisplaySize(initialSize);
        wm.getBaseDisplaySize(baseSize);

        Log.i("DeviceControl started");
    }

    public void setIME(String ime) {
        IMEMethod = ime;
        if (IMEMethod != null && !IMEMethod.isEmpty() && pm.isPackageAvailable(packageOf(IMEMethod))) {
            lastIMEMethod = Settings.get(Settings.SECURE, Settings.DEFAULT_INPUT_METHOD);
            try {
                disableIme = !imm.setInputMethodEnabled(IMEMethod, true);
                enabledIme = imm.setInputMethod(IMEMethod);
                Log.i("IME: '"+IMEMethod+ "' enabled: "+enabledIme+" lastMethod: '"+lastIMEMethod+"'");
            } catch (Exception e) {
                Log.e("IME: ", e);
            }
        }
    }

    static String packageOf(String s) {
        int i = s.indexOf('/');
        if (i < 0) return s;
        return s.substring(0, i);
    }

    public boolean setDensity(final int density) {
        if (density <= 0) return false;
        Log.i("initialDensity: " + initialDensity + ", baseDensity: " + baseDensity + ", New density: " + density);
        wm.setForcedDisplayDensity(density);
        densityChanged = true;
        return true;
    }

    public boolean setSize(final Point size) {
        if (size.x <= 0 || size.y <= 0) return false;
        Log.i("initialSize: " + initialSize.x + "x" + initialSize.y + ", baseSize: " + baseSize.x + "x" + baseSize.y + " New size: " + size.x + "x" + size.y);
        wm.setForcedDisplaySize(size.x, size.y);
        sizeChanged = true;
        return true;
    }

    public void dimScreen() {
        // Don't change brightness if can't restore!
        if (!"".equals(baseBrightness)) {
            Log.i("baseBrightness: " + baseBrightness);
            Settings.put(Settings.SYSTEM, Settings.SCREEN_BRIGHTNESS, "8");
            brightnessChanged = true;
        }
    }

    public void setDisableHWOverlays(boolean value) {
        disableHWOverlays = value;
        if (disableHWOverlays)
            doDisableHWOverlays(true);
    }

    // Requires root rights and su!
    private void doDisableHWOverlays(boolean value) {
        Settings.setDisableHWOverlays(value);
    }

    public void setPortrait() {
        if (screenRotation == null)
            screenRotation = new ScreenRotation();

        screenRotation.set(ScreenRotation.PORTRAIT);
    }

    public void setLandscape() {
        if (screenRotation == null)
            screenRotation = new ScreenRotation();

        screenRotation.set(ScreenRotation.LANDSCAPE);
    }

    public void setLandscapeMode(boolean value) {
        if (value)
            setLandscape();
        else if (screenRotation != null) {
            screenRotation.stop();
            screenRotation = null;
        }
    }

    public void Finish() {
        if (brightnessChanged)
            Settings.put(Settings.SYSTEM, Settings.SCREEN_BRIGHTNESS, baseBrightness);

        if (screenRotation != null) {
            screenRotation.stop();
            screenRotation = null;
        }

        if (densityChanged) {
            if (baseDensity == initialDensity) wm.clearForcedDisplayDensity();
            else wm.setForcedDisplayDensity(baseDensity);
        }

        if (sizeChanged) {
            if (baseSize.x == initialSize.x && baseSize.y == initialSize.y)
                wm.clearForcedDisplaySize();
            else
                wm.setForcedDisplaySize(baseSize.x, baseSize.y);
        }

        if (disableHWOverlays)
           doDisableHWOverlays(false);

        if (enabledIme && lastIMEMethod != null && !lastIMEMethod.isEmpty())
            imm.setInputMethod(lastIMEMethod);
        if (disableIme)
            imm.setInputMethodEnabled(IMEMethod, false);

        Log.i("DeviceControl stopped");
    }
}
