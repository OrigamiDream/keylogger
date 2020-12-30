package studio.avis.keylogger.storage.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class Data implements Serializable {

    public static final Object KEYBOARD_LOCK = new Object();
    public static final Object MOUSE_LOCK = new Object();
    public static final Object MOUSE_WHEEL_LOCK = new Object();

    private static final long serialVersionUID = 5828609485111924497L;

    // Keyboard
    // TODO: StackOverflowError
    private KeyData keyDataHead;
    private transient KeyData latestKey;
    private transient Set<Integer> downKeys = new HashSet<>();

    // Mouse
    // TODO: StackOverflowError
    private MouseData mouseDataHead;
    private transient MouseData latestMouse;
    private transient Set<Integer> downButtons = new HashSet<>();

    // Mouse Wheel
    // TODO: StackOverflowError
    private MouseWheelData mouseWheelDataHead;
    private transient MouseWheelData latestMouseWheel;
    private transient int currentWheelRotation = 0;
    private transient long lastWheelRotationChanged = System.currentTimeMillis();

    private transient long recordingStartTime;

    private long elapsedTimeBeforeKeyInput;
    private long elapsedTimeBeforeMouseInput;
    private long elapsedTimeBeforeMouseWheelInput;

    public void appendKey(KeyData keyData) {
        synchronized (KEYBOARD_LOCK) {
            if(keyData.isPressed()) {
                downKeys.add(keyData.getKeyCode());
            } else {
                downKeys.remove(keyData.getKeyCode());
            }
        }

        if(keyDataHead == null) {
            elapsedTimeBeforeKeyInput = System.currentTimeMillis() - recordingStartTime;
            keyDataHead = keyData;
        } else {
            if(latestKey != null) {
                latestKey.setNextKey(keyData);
            }
        }
        latestKey = keyData;
    }

    public void appendMouse(MouseData mouseData) {
        synchronized (MOUSE_LOCK) {
            if(mouseData.isPressed()) {
                downButtons.add(mouseData.getButtonId());
            } else {
                downButtons.remove(mouseData.getButtonId());
            }
        }

        if(mouseDataHead == null) {
            elapsedTimeBeforeMouseInput = System.currentTimeMillis() - recordingStartTime;
            mouseDataHead = mouseData;
        } else {
            if(latestMouse != null) {
                latestMouse.setNextMouse(mouseData);
            }
        }
        latestMouse = mouseData;
    }

    public void appendMouseWheel(MouseWheelData mouseWheelData) {
        synchronized (MOUSE_WHEEL_LOCK) {
            currentWheelRotation = mouseWheelData.getWheelRotation();
            lastWheelRotationChanged = System.currentTimeMillis();
        }

        if(mouseWheelDataHead == null) {
            elapsedTimeBeforeMouseWheelInput = System.currentTimeMillis() - recordingStartTime;
            mouseWheelDataHead = mouseWheelData;
        } else {
            if(latestMouseWheel != null) {
                latestMouseWheel.setNextMouseWheel(mouseWheelData);
            }
        }
        latestMouseWheel = mouseWheelData;
    }

}
