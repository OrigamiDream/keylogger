package studio.avis.keylogger.loggers;

import studio.avis.keylogger.Utils;
import studio.avis.keylogger.storage.KeyLogStorage;
import studio.avis.keylogger.storage.data.KeyData;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import studio.avis.juikit.Juikit;

import static org.jnativehook.keyboard.NativeKeyEvent.*;

public class KeyboardLogger implements KeyLogger, NativeKeyListener {

    private final Juikit juikit;

    public KeyboardLogger(Juikit juikit) {
        this.juikit = juikit;
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
        if(!juikit.data("RECORDING", boolean.class)) {
            return;
        }
        KeyData keyData = new KeyData();
        keyData.setCurrentTime(System.currentTimeMillis());
        keyData.setKeyCode(parseKeyCode(nativeKeyEvent));
        keyData.setPressed(true);
        KeyLogStorage.data.ifPresent(data -> {
            data.appendKey(keyData);
        });
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {
        if(!juikit.data("RECORDING", boolean.class)) {
            return;
        }
        KeyData keyData = new KeyData();
        keyData.setCurrentTime(System.currentTimeMillis());
        keyData.setKeyCode(parseKeyCode(nativeKeyEvent));
        keyData.setPressed(false);
        KeyLogStorage.data.ifPresent(data -> {
            data.appendKey(keyData);
        });
    }

    public int parseKeyCode(NativeKeyEvent nativeKeyEvent) {
        switch (nativeKeyEvent.getKeyCode()) {
            case VC_CONTROL: {
                return nativeKeyEvent.getKeyLocation() == KEY_LOCATION_LEFT ? VC_CONTROL : Utils.VC_CONTROL_RIGHT;
            }

            case VC_META: {
                return nativeKeyEvent.getKeyLocation() == KEY_LOCATION_LEFT ? VC_META : Utils.VC_META_RIGHT;
            }

            case VC_ALT: {
                return nativeKeyEvent.getKeyLocation() == KEY_LOCATION_LEFT ? VC_ALT : Utils.VC_ALT_RIGHT;
            }

            default:
                return nativeKeyEvent.getKeyCode();
        }
    }

}
