package studio.avis.keylogger;

import studio.avis.keylogger.loggers.KeyboardLogger;
import studio.avis.keylogger.loggers.MouseLogger;
import studio.avis.keylogger.storage.KeyLogStorage;
import studio.avis.keylogger.storage.data.Data;
import studio.avis.keylogger.storage.data.KeyData;
import studio.avis.keylogger.storage.data.MouseData;
import studio.avis.keylogger.storage.data.MouseWheelData;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import studio.avis.juikit.Juikit;
import studio.avis.juikit.internal.Button;
import studio.avis.juikit.internal.JuikitView;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.jnativehook.keyboard.NativeKeyEvent.*;

public class Main {

    enum ButtonInfo {

        START_RECORDING("기록 시작", WIDTH / 2 - BUTTON_WIDTH / 2 - (RECORDING_BUTTON_SPACE / 2), HEIGHT / 2 - BUTTON_HEIGHT / 2, BUTTON_WIDTH, BUTTON_HEIGHT,
                Color.GREEN, Color.GREEN.brighter(), Color.GREEN.darker()),

        STOP_RECORDING("기록 중지", WIDTH / 2 - BUTTON_WIDTH / 2 - (RECORDING_BUTTON_SPACE / 2), HEIGHT / 2 - BUTTON_HEIGHT / 2, BUTTON_WIDTH, BUTTON_HEIGHT,
                Color.RED, Color.RED.brighter(), Color.RED.darker()),

        START_REPLAY("재생 시작", WIDTH / 2 - BUTTON_WIDTH / 2 + (RECORDING_BUTTON_SPACE / 2), HEIGHT / 2 - BUTTON_HEIGHT / 2, BUTTON_WIDTH, BUTTON_HEIGHT,
                Color.BLUE, Color.BLUE.brighter(), Color.BLUE.darker()),

        STOP_REPLAY("재생 중지", WIDTH / 2 - BUTTON_WIDTH / 2 + (RECORDING_BUTTON_SPACE / 2), HEIGHT / 2 - BUTTON_HEIGHT / 2, BUTTON_WIDTH, BUTTON_HEIGHT,
                Color.YELLOW, Color.YELLOW.brighter(), Color.YELLOW.darker());

        private final String label;

        private final int x;
        private final int y;
        private final int width;
        private final int height;

        private final Color color;
        private final Color hoverColor;
        private final Color pressingColor;

        ButtonInfo(String label, int x, int y, int width, int height, Color color, Color hoverColor, Color pressingColor) {
            this.label = label;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.color = color;
            this.hoverColor = hoverColor;
            this.pressingColor = pressingColor;
        }

        public Button.Builder createButton(String id, BiConsumer<JuikitView, Graphics> consumer) {
            return Button.builder().id(id)
                    .background((juikit, graphics) -> drawButton(color, graphics))
                    .hover((juikit, graphics) -> drawButton(hoverColor, graphics))
                    .press((juikit, graphics) -> drawButton(pressingColor, graphics))
                    .sizeFixed(x, y, width, height)
                    .processPressed(consumer);
        }

        private void drawButton(Color color, Graphics graphics) {
            Font font = new Font(graphics.getFont().getName(), graphics.getFont().getStyle(), (int) (8 * SIZE_MULT));
            graphics.setFont(font);

            FontMetrics fontMetrics = graphics.getFontMetrics(graphics.getFont());
            int stringHeight = fontMetrics.getHeight();
            int stringWidth = fontMetrics.stringWidth(label);

            graphics.setColor(color);
            graphics.fillRect(x, y, width, height);

            graphics.setColor(Color.BLACK);
            graphics.drawString(label, x + (width / 2) - (stringWidth / 2), y + (height / 2) + (stringHeight / 2));
        }

    }

    public static final String KEY_LOG_DATA_FILE_EXTENSION = ".aviskeylog";

    public static final double SIZE_MULT = 2;

    public static final int WIDTH = (int) (250 * SIZE_MULT);
    public static final int HEIGHT = (int) (250 * SIZE_MULT);

    public static final int KEYBOARD_WIDTH = (int) (500 * SIZE_MULT);
    public static final int KEYBOARD_HEIGHT = (int) (250 * SIZE_MULT);

    public static final int BUTTON_WIDTH = (int) (40 * SIZE_MULT);
    public static final int BUTTON_HEIGHT = (int) (40 * SIZE_MULT);

    public static final int RECORDING_BUTTON_SPACE = (int) (140 * SIZE_MULT);

    public static final int KEYBOARD_KEY_WIDTH = (int) (20 * SIZE_MULT);
    public static final int KEYBOARD_KEY_HEIGHT = (int) (20 * SIZE_MULT);
    public static final int KEYBOARD_KEY_WIDTH_SHIFT = (int) (50 * SIZE_MULT);
    public static final int KEYBOARD_KEY_WIDTH_ESCAPE = (int) (30 * SIZE_MULT);
    public static final int KEYBOARD_KEY_WIDTH_ENTER = (int) (40 * SIZE_MULT);
    public static final int KEYBOARD_KEY_WIDTH_CAPS_LOCK = (int) (35 * SIZE_MULT);
    public static final int KEYBOARD_KEY_WIDTH_SPACE = (int) (125 * SIZE_MULT);
    public static final int KEYBOARD_KEY_WIDTH_CONTROL = (int) (25 * SIZE_MULT);

    public static final int KEYBOARD_KEY_START_X = (int) (24 * SIZE_MULT);
    public static final int KEYBOARD_KEY_START_Y = (int) (12 * SIZE_MULT);

    public static final int KEYBOARD_KEY_SPACE = (int) (5 * SIZE_MULT);

    public static final int KEYBOARD_MOUSE_START_X = (int) (425 * SIZE_MULT);
    public static final int KEYBOARD_MOUSE_START_Y = (int) (80 * SIZE_MULT);

    public static final int KEYBOARD_MOUSE_WIDTH = (int) (15 * SIZE_MULT);
    public static final int KEYBOARD_MOUSE_HEIGHT = (int) (30 * SIZE_MULT);

    public static final int KEYBOARD_MOUSE_SCROLL_Y = (int) (10 * SIZE_MULT);
    public static final int KEYBOARD_MOUSE_SCROLL_WIDTH = (int) (3 * SIZE_MULT);
    public static final int KEYBOARD_MOUSE_SCROLL_HEIGHT = (int) (10 * SIZE_MULT);

    public static final int KEYBOARD_KEY_ARROW_UP_Y_ADJUST = (int) (2 * SIZE_MULT);
    public static final int KEYBOARD_KEY_ARROW_DOWN_Y_ADJUST = (int) (4 * SIZE_MULT);

    private static final AtomicBoolean REPLAYING = new AtomicBoolean(false);
    private static final AtomicBoolean RECORDING = new AtomicBoolean(false);
    private static final AtomicInteger RUNNING_REPLAYS = new AtomicInteger();

    public static String getKeyText(boolean macOS, int keyCode) {
        if(macOS) {
            return NativeKeyEvent.getKeyText(keyCode);
        }

        switch (keyCode) {
            case VC_BACKQUOTE:
                return "`";

            case VC_META:
                return "Win";

            case VC_MINUS:
                return "-";

            case VC_EQUALS:
                return "=";

            case VC_OPEN_BRACKET:
                return "[";

            case VC_CLOSE_BRACKET:
                return "]";

            case VC_BACK_SLASH:
                return "₩";

            case VC_SEMICOLON:
                return ";";

            case VC_QUOTE:
                return "'";

            case VC_COMMA:
                return ",";

            case VC_PERIOD:
                return ".";

            case VC_SLASH:
                return "/";

            case VC_ESCAPE:
                return "Esc";

            case VC_BACKSPACE:
                return "⌫";

            case VC_LEFT:
                return "←";

            case VC_RIGHT:
                return "→";

            case VC_UP:
                return "↑";

            case VC_DOWN:
                return "↓";

            default:
                return NativeKeyEvent.getKeyText(keyCode);
        }
    }

    public static void main(String[] args) {
        // Disable log
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.WARNING);
        logger.setUseParentHandlers(false);

        System.out.println("Loading native hookers...");
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
            e.printStackTrace();
            System.exit(1);
        }

        if(!GlobalScreen.isNativeHookRegistered()) {
            System.out.println("Failed to register native hooks");
            System.exit(1);
        }

        File currentFolders = new File(".");
        List<File> logFiles = new ArrayList<>();
        Arrays.stream(Objects.requireNonNull(currentFolders.listFiles()))
                .filter(file -> file.getName().endsWith(KEY_LOG_DATA_FILE_EXTENSION))
                .forEach(logFiles::add);

        logFiles.sort(Comparator.comparing(File::getName));

        boolean loaded = false;
        if(!logFiles.isEmpty()) {
            System.out.println(logFiles.size() + " previous recorded log found. Choosing last one...");
            File latest = logFiles.get(logFiles.size() - 1);
            Optional<Data> data = KeyLogStorage.load(latest.getAbsolutePath());
            if(data.isPresent()) {
                System.out.println("Log(" + latest.getName() + ") available.");
                KeyLogStorage.data = data;
                loaded = true;
            }
        }

        if(!loaded) {
            Data data = new Data();
            data.setDownButtons(new HashSet<>());
            data.setDownKeys(new HashSet<>());
            data.setCurrentWheelRotation(0);
            KeyLogStorage.data = Optional.of(data);
        }

        Juikit handlingJuikit = Juikit.createFrame()
                .size(WIDTH, HEIGHT)
                .centerAlign()
                .closeOperation(WindowConstants.EXIT_ON_CLOSE)
                .antialiasing(true)
                .title("KeyLogger")
                .background(Color.BLACK)
                .repaintInterval(1)
                .resizable(false)

                .data("RECORDING", false)
                .data("REPLAYING", false)
                .data("STORING", false)

                .painter((juikit, graphics) -> {
                    graphics.setColor(Color.BLACK);
                    graphics.fillRect(0, 0, juikit.width(), juikit.height());

                    if(juikit.data("STORING", boolean.class)) {
                        String text = "저장 중...";

                        FontMetrics metrics = graphics.getFontMetrics(graphics.getFont());
                        int height = metrics.getHeight();
                        int width = metrics.stringWidth(text);

                        graphics.setColor(Color.WHITE);
                        graphics.drawString(text, juikit.width() / 2 - width / 2, juikit.height() / 2 + height / 2);
                    }
                })

                .visibility(true);

        List<List<Key>> keyboardLayouts = new ArrayList<>();

        int[][] layout;
        if(handlingJuikit.macOS()) {
            layout = Utils.KOREAN_MACOS_KEYBOARD_LAYOUT;
        } else {
            layout = Utils.KOREAN_WINDOWS_KEYBOARD_LAYOUT;
        }

        for(int[] line : layout) {
            List<Key> keyboardLine = new ArrayList<>();
            for(int keys : line) {
                int width = KEYBOARD_KEY_WIDTH;
                int height = KEYBOARD_KEY_HEIGHT;
                switch (keys) {
                    case VC_SHIFT:
                    case Utils.VC_SHIFT_RIGHT:
                        width = KEYBOARD_KEY_WIDTH_SHIFT;
                        break;

                    case VC_BACKSPACE:
                    case VC_TAB:
                    case VC_ESCAPE:
                        width = KEYBOARD_KEY_WIDTH_ESCAPE;
                        break;

                    case VC_ENTER:
                        width = KEYBOARD_KEY_WIDTH_ENTER;
                        break;

                    case VC_CAPS_LOCK:
                        width = KEYBOARD_KEY_WIDTH_CAPS_LOCK;
                        break;

                    case VC_SPACE:
                        width = KEYBOARD_KEY_WIDTH_SPACE;
                        break;

                    case VC_CONTROL:
                        width = KEYBOARD_KEY_WIDTH_CONTROL;
                        break;

                    case VC_LEFT:
                        // Add arrow keys
                        Key left = new Key(new int[] { VC_LEFT }, KEYBOARD_KEY_WIDTH, KEYBOARD_KEY_HEIGHT);
                        Key right = new Key(new int[] { VC_RIGHT }, KEYBOARD_KEY_WIDTH, KEYBOARD_KEY_HEIGHT);
                        Key mid = new Key(new int[] { VC_UP, VC_DOWN }, KEYBOARD_KEY_WIDTH, KEYBOARD_KEY_HEIGHT);
                        keyboardLine.add(left);
                        keyboardLine.add(mid);
                        keyboardLine.add(right);
                        continue;
                }
                Key key = new Key(new int[] { keys }, width, height);
                keyboardLine.add(key);
            }
            keyboardLayouts.add(keyboardLine);
        }

        Juikit layoutJuikit = Juikit.createFrame()
                .size(KEYBOARD_WIDTH, KEYBOARD_HEIGHT)
                .centerAlign()
                .closeOperation(WindowConstants.EXIT_ON_CLOSE)
                .antialiasing(true)
                .title("Keyboard / Mouse Layout")
                .background(Color.BLACK)
                .repaintInterval(5)
                .resizable(false)

                .data("KEYBOARD_LAYOUT", keyboardLayouts)

                .painter((juikit, graphics) -> {
                    graphics.setColor(Color.BLACK);
                    graphics.fillRect(0, 0, juikit.width(), juikit.height());
                    synchronized (Data.KEYBOARD_LOCK) {
                        Data data = KeyLogStorage.data.orElse(null);
                        if(data == null) {
                            return;
                        }
                        List<List<Key>> keyboard = juikit.data("KEYBOARD_LAYOUT");

                        int startX = KEYBOARD_KEY_START_X;
                        int startY = KEYBOARD_KEY_START_Y;

                        Font font = new Font(graphics.getFont().getName(), graphics.getFont().getStyle(), (int) (8 * SIZE_MULT * 0.8));
                        graphics.setFont(font);

                        FontMetrics metrics = graphics.getFontMetrics(font);
                        int stringHeight = metrics.getHeight();

                        int y = 0;
                        for(List<Key> line : keyboard) {
                            y++;

                            int height = startY + y * (KEYBOARD_KEY_HEIGHT + KEYBOARD_KEY_SPACE);

                            int width = 0;
                            for(Key key : line) {
                                if(key.getKeyCodes()[0] == VC_UP) {
                                    boolean pressedUp = data.getDownKeys().contains(VC_UP);
                                    boolean pressedDown = data.getDownKeys().contains(VC_DOWN);
                                    if(pressedUp) {
                                        graphics.setColor(Color.RED);
                                    } else {
                                        graphics.setColor(Color.WHITE);
                                    }
                                    String arrowUp = getKeyText(juikit.juikit().macOS(), VC_UP);
                                    graphics.fillRect(startX + width, height, key.getWidth(), key.getHeight() / 2 - 1);
                                    graphics.setColor(Color.BLACK);
                                    int arrowUpStringWidth = metrics.stringWidth(arrowUp);
                                    graphics.drawString(arrowUp, startX + width + (key.getWidth() / 2) - (arrowUpStringWidth / 2), height + (key.getHeight() / 4 - KEYBOARD_KEY_ARROW_UP_Y_ADJUST) + (stringHeight / 2));

                                    if(pressedDown) {
                                        graphics.setColor(Color.RED);
                                    } else {
                                        graphics.setColor(Color.WHITE);
                                    }
                                    String arrowDown = getKeyText(juikit.juikit().macOS(), VC_DOWN);
                                    graphics.fillRect(startX + width, height + (key.getHeight() / 2 + 1), key.getWidth(), key.getHeight() / 2 - 1);
                                    graphics.setColor(Color.BLACK);
                                    int arrowDownStringWidth = metrics.stringWidth(arrowDown);
                                    graphics.drawString(arrowDown, startX + width + (key.getWidth() / 2) - (arrowDownStringWidth / 2), height + (key.getHeight() / 2 + KEYBOARD_KEY_ARROW_DOWN_Y_ADJUST) + (stringHeight / 2));
                                } else {
                                    boolean pressed = false;
                                    for(int keyCode : key.getKeyCodes()) {
                                        if(data.getDownKeys().contains(keyCode)) {
                                            pressed = true;
                                            break;
                                        }
                                    }

                                    if(pressed) {
                                        graphics.setColor(Color.RED);
                                    } else {
                                        graphics.setColor(Color.WHITE);
                                    }
                                    graphics.fillRect(startX + width, height, key.getWidth(), key.getHeight());
                                    graphics.setColor(Color.BLACK);
                                    int keyCode;
                                    switch (key.getKeyCodes()[0]) {
                                        case Utils.VC_SHIFT_RIGHT:
                                            keyCode = VC_SHIFT;
                                            break;

                                        case Utils.VC_CONTROL_RIGHT:
                                            keyCode = VC_CONTROL;
                                            break;

                                        case Utils.VC_META_RIGHT:
                                            keyCode = VC_META;
                                            break;

                                        case Utils.VC_ALT_RIGHT:
                                            keyCode = VC_ALT;
                                            break;

                                        default:
                                            keyCode = key.getKeyCodes()[0];
                                            break;
                                    }
                                    String keyCodeString;
                                    if(keyCode == VC_KATAKANA) {
                                        keyCodeString = "한글";
                                    } else {
                                        keyCodeString = getKeyText(juikit.juikit().macOS(), keyCode);
                                    }
                                    int keyCodeStringWidth = metrics.stringWidth(keyCodeString);
                                    graphics.drawString(keyCodeString, startX + width + (key.getWidth() / 2) - (keyCodeStringWidth / 2), height + (key.getHeight() / 2) + (stringHeight / 2) - 2);
                                }

                                width += key.getWidth() + KEYBOARD_KEY_SPACE;
                            }
                        }
                    }
                    int mouseWheelTop;
                    int mouseWheelBottom;
                    int mouseWheelMidX;
                    synchronized (Data.MOUSE_LOCK) {
                        Data data = KeyLogStorage.data.orElse(null);
                        if(data == null) {
                            return;
                        }

                        int startX = KEYBOARD_MOUSE_START_X;
                        int startY = KEYBOARD_MOUSE_START_Y;

                        int mouseWidth = KEYBOARD_MOUSE_WIDTH;
                        int mouseHeight = KEYBOARD_MOUSE_HEIGHT;

                        // left button
                        if(data.getDownButtons().contains(Utils.VC_MOUSE_LEFT_BUTTON)) {
                            graphics.setColor(Color.RED);
                        } else {
                            graphics.setColor(Color.WHITE);
                        }
                        graphics.fillRect(startX, startY, mouseWidth, mouseHeight);

                        // right button
                        if(data.getDownButtons().contains(Utils.VC_MOUSE_RIGHT_BUTTON)) {
                            graphics.setColor(Color.RED);
                        } else {
                            graphics.setColor(Color.WHITE);
                        }
                        graphics.fillRect(startX + mouseWidth + KEYBOARD_KEY_SPACE, startY, mouseWidth, mouseHeight);

                        graphics.setColor(Color.WHITE);
                        graphics.fillRect(startX, startY + mouseHeight, mouseWidth * 2 + KEYBOARD_KEY_SPACE, mouseHeight);

                        if(data.getDownButtons().contains(Utils.VC_MOUSE_MIDDLE_BUTTON)) {
                            graphics.setColor(Color.RED);
                        } else {
                            graphics.setColor(Color.WHITE);
                        }
                        graphics.fillRect(startX + mouseWidth + (KEYBOARD_KEY_SPACE / 2) - (KEYBOARD_MOUSE_SCROLL_WIDTH / 2), startY + KEYBOARD_MOUSE_SCROLL_Y, KEYBOARD_MOUSE_SCROLL_WIDTH, KEYBOARD_MOUSE_SCROLL_HEIGHT);

                        mouseWheelTop = startY + KEYBOARD_MOUSE_SCROLL_Y;
                        mouseWheelBottom = startY + KEYBOARD_MOUSE_SCROLL_Y + KEYBOARD_MOUSE_SCROLL_HEIGHT;
                        mouseWheelMidX = startX + mouseWidth + (KEYBOARD_KEY_SPACE / 2);
                    }
                    synchronized (Data.MOUSE_WHEEL_LOCK) {
                        Data data = KeyLogStorage.data.orElse(null);
                        if(data == null) {
                            return;
                        }
                        int wheelRotation;
                        if(juikit.juikit().macOS()) {
                            wheelRotation = data.getCurrentWheelRotation();
                        } else {
                            wheelRotation = data.getCurrentWheelRotation() * 10; // Windows
                        }
                        wheelRotation *= SIZE_MULT;
                        Graphics2D g2 = (Graphics2D) graphics;
                        // std in macOS (natural scroll)
                        // bound to bottom = positive
                        // bound to top = negative
                        Point2D from = null;
                        Point2D to = null;
                        if(wheelRotation < 0) {
                            from = new Point2D.Float(mouseWheelMidX, mouseWheelTop);
                            to = new Point2D.Float(mouseWheelMidX + 0.5f, mouseWheelTop + (wheelRotation * 2));
                        } else if(wheelRotation > 0) {
                            from = new Point2D.Float(mouseWheelMidX, mouseWheelBottom);
                            to = new Point2D.Float(mouseWheelMidX + 0.5f, mouseWheelBottom + (wheelRotation * 2));
                        }

                        if(from != null) { // to is also not null.
                            float arrowSize = Math.abs(wheelRotation) / 2f;
                            g2.setColor(Color.RED);
                            drawArrow(g2, from, to, new BasicStroke((float) (arrowSize / 6 * SIZE_MULT)), new BasicStroke((float) (arrowSize * 2)), (float) (arrowSize * 2));
                        }
                    }
                })

                .visibility(true);


        createStartRecordingButton(handlingJuikit);
        if(loaded) {
            createStartReplayingButton(handlingJuikit);
        }

        System.out.println("Keyboard logger");
        GlobalScreen.addNativeKeyListener(new KeyboardLogger(handlingJuikit));

        MouseLogger mouseLogger = new MouseLogger(handlingJuikit);
        GlobalScreen.addNativeMouseListener(mouseLogger);
//        GlobalScreen.addNativeMouseMotionListener(mouseLogger);
        GlobalScreen.addNativeMouseWheelListener(mouseLogger);
    }

    private static void createStartRecordingButton(Juikit juikit) {
        juikit.button(ButtonInfo.START_RECORDING.createButton("startRecording", (jk, graphics) -> startRecording(jk.juikit())));
    }

    private static void createStopRecordingButton(Juikit juikit) {
        juikit.button(ButtonInfo.STOP_RECORDING.createButton("stopRecording", (jk, graphics) -> stopRecording(jk.juikit())));
    }

    private static void createStartReplayingButton(Juikit juikit) {
        juikit.button(ButtonInfo.START_REPLAY.createButton("startReplaying", (jk, graphics) -> {
            stopRecording(jk.juikit());

            // Remove all of recording stuffs
            jk.removeButton("startRecording");
            jk.removeButton("stopRecording");

            jk.data("REPLAYING", true);
            jk.removeButton("startReplaying");

            REPLAYING.set(true);

            // Keyboard
            new Thread(() -> {
                RUNNING_REPLAYS.incrementAndGet();
                Data data = KeyLogStorage.data.orElse(null);
                if(data == null) {
                    System.out.println("Loaded data is not available.");
                    RUNNING_REPLAYS.decrementAndGet();
                    return;
                }

                long keyReplayTimestamp = 0;
                boolean keyboardAvailable = false;
                if(data.getKeyDataHead() != null) {
                    keyReplayTimestamp = data.getKeyDataHead().getCurrentTime();
                    keyboardAvailable = true;
                } else {
                    System.out.println("Keyboard records are not available.");
                }

                try {
                    Thread.sleep(Math.max(data.getElapsedTimeBeforeKeyInput() - 5, 0));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                KeyData lastKeyData = null; // will be searched as soon as possible.
                long currentTimestamp = System.currentTimeMillis();

                while(REPLAYING.get()) {
                    try {
                        Thread.sleep(5L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    long now = System.currentTimeMillis();
                    long timeDiff = now - currentTimestamp;
                    currentTimestamp = now;

                    if(keyboardAvailable) {
                        long keyReplayTimeTo = keyReplayTimestamp + timeDiff;

                        KeyData keyData;
                        if(lastKeyData == null) {
                            keyData = data.getKeyDataHead();
                        } else {
                            keyData = lastKeyData;
                        }

                        while(keyData.getCurrentTime() <= keyReplayTimeTo) {
                            replayKeyEvent(keyData);

                            KeyData next = keyData.getNextKey();

                            keyData = next;
                            lastKeyData = next;
                            if(next == null) {
                                break;
                            }
                        }

                        keyReplayTimestamp = keyReplayTimeTo;

                        if(lastKeyData == null) {
                            keyboardAvailable = false;
                        }
                    }

                    if(!keyboardAvailable) {
                        break;
                    }
                }

                synchronized (Data.KEYBOARD_LOCK) {
                    data.getDownKeys().clear();
                }

                RUNNING_REPLAYS.decrementAndGet();
            }).start();

            // Mouse
            new Thread(() -> {
                RUNNING_REPLAYS.incrementAndGet();
                Data data = KeyLogStorage.data.orElse(null);
                if(data == null) {
                    System.out.println("Loaded data is not available.");
                    RUNNING_REPLAYS.decrementAndGet();
                    return;
                }
                long mouseReplayTimestamp = 0;
                boolean mouseAvailable = false;
                if(data.getMouseDataHead() != null) {
                    mouseReplayTimestamp = data.getMouseDataHead().getCurrentTime();
                    mouseAvailable = true;
                } else {
                    System.out.println("Mouse records are not available.");
                }

                try {
                    Thread.sleep(Math.max(data.getElapsedTimeBeforeMouseInput() - 5, 0));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                MouseData lastMouseData = null;
                long currentTimestamp = System.currentTimeMillis();

                while(REPLAYING.get()) {
                    try {
                        Thread.sleep(5L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    long now = System.currentTimeMillis();
                    long timeDiff = now - currentTimestamp;
                    currentTimestamp = now;

                    if(mouseAvailable) {
                        long mouseReplayTimeTo = mouseReplayTimestamp + timeDiff;

                        MouseData mouseData;
                        if(lastMouseData == null) {
                            mouseData = data.getMouseDataHead();
                        } else {
                            mouseData = lastMouseData;
                        }

                        while(mouseData.getCurrentTime() <= mouseReplayTimeTo) {
                            replayMouseEvent(mouseData);

                            MouseData next = mouseData.getNextMouse();

                            mouseData = next;
                            lastMouseData = next;
                            if(next == null) {
                                break;
                            }
                        }

                        mouseReplayTimestamp = mouseReplayTimeTo;

                        if(lastMouseData == null) {
                            mouseAvailable = false;
                        }
                    }

                    if(!mouseAvailable) {
                        break;
                    }
                }

                synchronized (Data.MOUSE_LOCK) {
                    data.getDownButtons().clear();
                }

                RUNNING_REPLAYS.decrementAndGet();
            }).start();

            // Mouse Wheel
            new Thread(() -> {
                RUNNING_REPLAYS.incrementAndGet();
                Data data = KeyLogStorage.data.orElse(null);
                if(data == null) {
                    System.out.println("Loaded data is not available.");
                    RUNNING_REPLAYS.decrementAndGet();
                    return;
                }

                long mouseWheelReplayTimeStamp = 0;
                boolean mouseWheelAvailable = false;
                if(data.getMouseWheelDataHead() != null) {
                    mouseWheelReplayTimeStamp = data.getMouseWheelDataHead().getCurrentTime();
                    mouseWheelAvailable = true;
                } else {
                    System.out.println("Mouse Wheel records are not available.");
                }

                try {
                    Thread.sleep(Math.max(data.getElapsedTimeBeforeMouseWheelInput() - 5, 0));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                MouseWheelData lastMouseWheelData = null; // will be searched
                long currentTimestamp = System.currentTimeMillis();

                while(REPLAYING.get()) {
                    try {
                        Thread.sleep(5L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    long now = System.currentTimeMillis();
                    long timeDiff = now - currentTimestamp;
                    currentTimestamp = now;

                    // Mouse Wheel
                    if(mouseWheelAvailable) {
                        long mouseWheelReplayTimeTo = mouseWheelReplayTimeStamp + timeDiff;

                        MouseWheelData mouseWheelData;
                        if(lastMouseWheelData == null) {
                            mouseWheelData = data.getMouseWheelDataHead();
                        } else {
                            mouseWheelData = lastMouseWheelData;
                        }

                        while(mouseWheelData.getCurrentTime() <= mouseWheelReplayTimeTo) {
                            replayMouseWheelEvent(mouseWheelData);

                            MouseWheelData next = mouseWheelData.getNextMouseWheel();

                            mouseWheelData = next;
                            lastMouseWheelData = next;
                            if(next == null) {
                                break;
                            }
                        }

                        mouseWheelReplayTimeStamp = mouseWheelReplayTimeTo;

                        if(lastMouseWheelData == null) {
                            mouseWheelAvailable = false;
                        }
                    }

                    if(!mouseWheelAvailable) {
                        break;
                    }
                }
                synchronized (Data.MOUSE_WHEEL_LOCK) {
                    data.setCurrentWheelRotation(0);
                }

                RUNNING_REPLAYS.decrementAndGet();
            }).start();

            new Thread(() -> {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                while(RUNNING_REPLAYS.get() > 0);
                stopReplaying(jk.juikit());
            }).start();

            createStopReplayingButton(jk.juikit());
        }));
    }

    private static void replayKeyEvent(KeyData keyData) {
        Data data = KeyLogStorage.data.orElse(null);
        if(data == null) {
            System.out.println("Loaded data is not available.");
            return;
        }
        synchronized (Data.KEYBOARD_LOCK) {
            if(keyData.isPressed()) {
                data.getDownKeys().add(keyData.getKeyCode());
            } else {
                data.getDownKeys().remove(keyData.getKeyCode());
            }
        }
    }

    private static void replayMouseEvent(MouseData mouseData) {
        Data data = KeyLogStorage.data.orElse(null);
        if(data == null) {
            System.out.println("Loaded data is not available.");
            return;
        }
        synchronized (Data.MOUSE_LOCK) {
            if(mouseData.isPressed()) {
                data.getDownButtons().add(mouseData.getButtonId());
            } else {
                data.getDownButtons().remove(mouseData.getButtonId());
            }
        }
    }

    private static void replayMouseWheelEvent(MouseWheelData mouseWheelData) {
        Data data = KeyLogStorage.data.orElse(null);
        if(data == null) {
            System.out.println("Loaded data is not available.");
            return;
        }
        synchronized (Data.MOUSE_WHEEL_LOCK) {
            data.setCurrentWheelRotation(mouseWheelData.getWheelRotation());
        }
    }

    private static void createStopReplayingButton(Juikit juikit) {
        juikit.button(ButtonInfo.STOP_REPLAY.createButton("stopReplaying", (jk, graphics) -> stopReplaying(jk.juikit())));
    }

    private static void stopReplaying(Juikit jk) {
        jk.data("REPLAYING", false);
        jk.removeButton("stopReplaying");

        REPLAYING.set(false);

        createStartRecordingButton(jk);
        createStartReplayingButton(jk);
    }

    private static void startRecording(Juikit jk) {
        if(jk.data("RECORDING", boolean.class)) {
            return;
        }
        RECORDING.set(true);
        jk.data("RECORDING", true);
        System.out.println("Click in: " + System.currentTimeMillis());
        Data data = new Data();
        data.setRecordingStartTime(System.currentTimeMillis());
        KeyLogStorage.data = Optional.of(data);
        jk.removeButton("startRecording");

        if(!jk.macOS()) {
            new Thread(() -> {
                while(RECORDING.get()) {
                    if(data.getCurrentWheelRotation() == 0) {
                        continue;
                    }

                    if(System.currentTimeMillis() - data.getLastWheelRotationChanged() >= 250) {
                        synchronized (Data.MOUSE_WHEEL_LOCK) {
                            data.setCurrentWheelRotation(0);
                        }
                    }
                }
            }).start();
        }

        createStopRecordingButton(jk);
    }

    private static void stopRecording(Juikit jk) {
        if(!jk.data("RECORDING", boolean.class)) {
            return;
        }
        jk.data("STORING", true);
        RECORDING.set(false);
        System.out.println("Click out: " + System.currentTimeMillis());
        jk.data("RECORDING", false);
        new Thread(() -> {
            KeyLogStorage.data.ifPresent(data -> {
                String filename = System.currentTimeMillis() + KEY_LOG_DATA_FILE_EXTENSION;
                KeyLogStorage.save(filename, data);

                File file = new File(filename);
                System.out.println("Saved " + filename + " at " + file.getAbsolutePath());
            });
            Data data = KeyLogStorage.data.orElse(null);
            if(data != null) {
                synchronized (Data.KEYBOARD_LOCK) {
                    data.getDownKeys().clear();
                }
                synchronized (Data.MOUSE_LOCK) {
                    data.getDownButtons().clear();
                }
                synchronized (Data.MOUSE_WHEEL_LOCK) {
                    data.setCurrentWheelRotation(0);
                }
            }
            jk.removeButton("stopRecording");
            createStartRecordingButton(jk);
            createStartReplayingButton(jk);
            jk.data("STORING", false);
        }).start();
    }

    public static void drawArrow(final Graphics2D gfx, final Point2D start, final Point2D end, final Stroke lineStroke, final Stroke arrowStroke, final float arrowSize) {

        final double startx = start.getX();
        final double starty = start.getY();

        gfx.setStroke(arrowStroke);
        final double deltax = startx - end.getX();
        final double result;
        if (deltax == 0.0d) {
            result = Math.PI / 2;
        } else {
            result = Math.atan((starty - end.getY()) / deltax) + (startx < end.getX() ? Math.PI : 0);
        }

        final double angle = result;

        final double arrowAngle = Math.PI / 12.0d;

        final double x1 = arrowSize * Math.cos(angle - arrowAngle);
        final double y1 = arrowSize * Math.sin(angle - arrowAngle);
        final double x2 = arrowSize * Math.cos(angle + arrowAngle);
        final double y2 = arrowSize * Math.sin(angle + arrowAngle);

        final double cx = (arrowSize / 2.0f) * Math.cos(angle);
        final double cy = (arrowSize / 2.0f) * Math.sin(angle);

        final GeneralPath polygon = new GeneralPath();
        polygon.moveTo(end.getX(), end.getY());
        polygon.lineTo(end.getX() + x1, end.getY() + y1);
        polygon.lineTo(end.getX() + x2, end.getY() + y2);
        polygon.closePath();
        gfx.fill(polygon);

        gfx.setStroke(lineStroke);
        gfx.drawLine((int) startx, (int) starty, (int) (end.getX() + cx), (int) (end.getY() + cy));
    }

}
