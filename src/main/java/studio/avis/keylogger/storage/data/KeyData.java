package studio.avis.keylogger.storage.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class KeyData implements Serializable {

    private static final long serialVersionUID = -1742866059105534295L;

    private long currentTime;
    private int keyCode;
    private boolean pressed;

    private KeyData nextKey = null;

}
