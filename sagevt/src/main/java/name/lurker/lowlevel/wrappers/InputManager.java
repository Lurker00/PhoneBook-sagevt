package name.lurker.lowlevel.wrappers;

import android.os.IInterface;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class InputManager extends Wrapper {

    public static final int INJECT_INPUT_EVENT_MODE_ASYNC           = 0;
    public static final int INJECT_INPUT_EVENT_MODE_WAIT_FOR_RESULT = 1;
    public static final int INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH = 2;

    private final Method injectInputEventMethod;

    public boolean injectInputEvent(InputEvent inputEvent, int mode) {
        if (injectInputEventMethod == null) return false;
        try {
            return (Boolean) injectInputEventMethod.invoke(manager, inputEvent, mode);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    public boolean injectEvent(InputEvent event) {
        return injectInputEvent(event, InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
    }

    public boolean injectKeyEvent(int action, int keyCode, int repeat, int metaState, long now) {
        KeyEvent event = new KeyEvent(now, now, action, keyCode, repeat, metaState, KeyCharacterMap.VIRTUAL_KEYBOARD, 0, 0,
                InputDevice.SOURCE_KEYBOARD);
        return injectEvent(event);
    }

    public boolean injectKeycode(int keyCode) {
        final long now = SystemClock.uptimeMillis();
        return injectKeyEvent(KeyEvent.ACTION_DOWN, keyCode, 0, 0, now)
                && injectKeyEvent(KeyEvent.ACTION_UP, keyCode, 0, 0, now);
    }

    private InputManager(IInterface manager) {
        super(manager);
        this.injectInputEventMethod = getMethod(manager.getClass(), "injectInputEvent", InputEvent.class, int.class);
    }

    static private InputManager instance;
    static public InputManager getInstance() {
        synchronized (InputManager.class) {
            if (instance == null) {
                instance = new InputManager(getService("input", "android.hardware.input.IInputManager"));
            }
            return instance;
        }
    }
}
