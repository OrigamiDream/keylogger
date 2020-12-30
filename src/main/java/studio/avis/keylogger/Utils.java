package studio.avis.keylogger;

import static org.jnativehook.keyboard.NativeKeyEvent.*;

public class Utils {

    public static final int VC_SHIFT_RIGHT = 3638;
    public static final int VC_CONTROL_RIGHT = 1000000;
    public static final int VC_META_RIGHT = 1000001;
    public static final int VC_ALT_RIGHT = 1000002;

    public static final int VC_MOUSE_LEFT_BUTTON = 1;
    public static final int VC_MOUSE_RIGHT_BUTTON = 2;
    public static final int VC_MOUSE_MIDDLE_BUTTON = 3;

    public static final int[][] KOREAN_WINDOWS_KEYBOARD_LAYOUT = {
            { VC_ESCAPE, VC_F1, VC_F2, VC_F3, VC_F4, VC_F5, VC_F6, VC_F7, VC_F8, VC_F9, VC_F10, VC_F11, VC_F12, VC_MEDIA_EJECT },
            { VC_BACKQUOTE, VC_1, VC_2, VC_3, VC_4, VC_5, VC_6, VC_7, VC_8, VC_9, VC_0, VC_MINUS, VC_EQUALS, VC_BACKSPACE },
            { VC_TAB, VC_Q, VC_W, VC_E, VC_R, VC_T, VC_Y, VC_U, VC_I, VC_O, VC_P, VC_OPEN_BRACKET, VC_CLOSE_BRACKET, VC_BACK_SLASH },
            { VC_CAPS_LOCK, VC_A, VC_S, VC_D, VC_F, VC_G, VC_H, VC_J, VC_K, VC_L, VC_SEMICOLON, VC_QUOTE, VC_ENTER },
            { VC_SHIFT, VC_Z, VC_X, VC_C, VC_V, VC_B, VC_N, VC_M, VC_COMMA, VC_PERIOD, VC_SLASH, VC_SHIFT_RIGHT },
            { VC_CONTROL, VC_META, VC_ALT, VC_SPACE, VC_KATAKANA, VC_ALT_RIGHT, VC_META_RIGHT, VC_LEFT }
    };

    public static final int[][] KOREAN_MACOS_KEYBOARD_LAYOUT = {
            { VC_ESCAPE, VC_F1, VC_F2, VC_F3, VC_F4, VC_F5, VC_F6, VC_F7, VC_F8, VC_F9, VC_F10, VC_F11, VC_F12, VC_MEDIA_EJECT },
            { VC_BACKQUOTE, VC_1, VC_2, VC_3, VC_4, VC_5, VC_6, VC_7, VC_8, VC_9, VC_0, VC_MINUS, VC_EQUALS, VC_BACKSPACE },
            { VC_TAB, VC_Q, VC_W, VC_E, VC_R, VC_T, VC_Y, VC_U, VC_I, VC_O, VC_P, VC_OPEN_BRACKET, VC_CLOSE_BRACKET, VC_BACK_SLASH },
            { VC_CAPS_LOCK, VC_A, VC_S, VC_D, VC_F, VC_G, VC_H, VC_J, VC_K, VC_L, VC_SEMICOLON, VC_QUOTE, VC_ENTER },
            { VC_SHIFT, VC_Z, VC_X, VC_C, VC_V, VC_B, VC_N, VC_M, VC_COMMA, VC_PERIOD, VC_SLASH, VC_SHIFT_RIGHT },
            { VC_CONTROL, VC_ALT, VC_META, VC_SPACE, VC_META_RIGHT, VC_ALT_RIGHT, VC_CONTROL_RIGHT, VC_LEFT }
    };

}
