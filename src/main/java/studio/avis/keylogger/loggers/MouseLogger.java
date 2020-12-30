package studio.avis.keylogger.loggers;

import studio.avis.keylogger.storage.KeyLogStorage;
import studio.avis.keylogger.storage.data.MouseData;
import studio.avis.keylogger.storage.data.MouseWheelData;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;
import org.jnativehook.mouse.NativeMouseWheelEvent;
import org.jnativehook.mouse.NativeMouseWheelListener;
import studio.avis.juikit.Juikit;

public class MouseLogger implements KeyLogger, NativeMouseInputListener, NativeMouseWheelListener {

    private final Juikit juikit;

    public MouseLogger(Juikit juikit) {
        this.juikit = juikit;
    }

    @Override
    public void nativeMouseClicked(NativeMouseEvent nativeMouseEvent) {

    }

    @Override
    public void nativeMousePressed(NativeMouseEvent nativeMouseEvent) {
        if(!juikit.data("RECORDING", boolean.class)) {
            return;
        }
        MouseData mouseData = new MouseData();
        mouseData.setCurrentTime(System.currentTimeMillis());
        mouseData.setButtonId(nativeMouseEvent.getButton());
        mouseData.setPressed(true);
        KeyLogStorage.data.ifPresent(data -> {
            data.appendMouse(mouseData);
        });
    }

    @Override
    public void nativeMouseReleased(NativeMouseEvent nativeMouseEvent) {
        if(!juikit.data("RECORDING", boolean.class)) {
            return;
        }
        MouseData mouseData = new MouseData();
        mouseData.setCurrentTime(System.currentTimeMillis());
        mouseData.setButtonId(nativeMouseEvent.getButton());
        mouseData.setPressed(false);
        KeyLogStorage.data.ifPresent(data -> {
            data.appendMouse(mouseData);
        });
    }

    @Override
    public void nativeMouseMoved(NativeMouseEvent nativeMouseEvent) {

    }

    @Override
    public void nativeMouseDragged(NativeMouseEvent nativeMouseEvent) {

    }

    @Override
    public void nativeMouseWheelMoved(NativeMouseWheelEvent nativeMouseWheelEvent) {
        if(!juikit.data("RECORDING", boolean.class)) {
            return;
        }
        MouseWheelData mouseWheelData = new MouseWheelData();
        mouseWheelData.setCurrentTime(System.currentTimeMillis());
        mouseWheelData.setWheelRotation(nativeMouseWheelEvent.getWheelRotation());
        KeyLogStorage.data.ifPresent(data -> {
            data.appendMouseWheel(mouseWheelData);
        });
    }
}
