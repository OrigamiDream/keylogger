package studio.avis.keylogger.storage.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class MouseWheelData implements Serializable {

    private static final long serialVersionUID = -5252433320905037646L;

    private long currentTime;
    private int wheelRotation;

    private MouseWheelData nextMouseWheel = null;
}
