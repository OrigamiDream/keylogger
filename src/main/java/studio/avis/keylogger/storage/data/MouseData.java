package studio.avis.keylogger.storage.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class MouseData implements Serializable {

    private static final long serialVersionUID = 8481823538547482186L;

    private long currentTime;
    private int buttonId;
    private boolean pressed;

    private MouseData nextMouse = null;
}
