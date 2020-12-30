package studio.avis.keylogger;

public class Key {

    private final int[] keyCodes;
    private final int width;
    private final int height;

    public Key(int[] keyCodes, int width, int height) {
        this.keyCodes = keyCodes;
        this.width = width;
        this.height = height;
    }

    public int[] getKeyCodes() {
        return keyCodes;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
