// STUB(R.Chen): LWJGL 2 Keyboard → 1.20.1 uses GLFW key codes via InputUtil or Screen.keyPressed.
package org.lwjgl.input;

import org.lwjgl.glfw.GLFW;

public class Keyboard {
    public static final int KEY_BACK = GLFW.GLFW_KEY_BACKSPACE;
    public static final int KEY_RETURN = GLFW.GLFW_KEY_ENTER;
    public static final int KEY_ESCAPE = GLFW.GLFW_KEY_ESCAPE;
    public static final int KEY_TAB = GLFW.GLFW_KEY_TAB;
    public static final int KEY_LSHIFT = GLFW.GLFW_KEY_LEFT_SHIFT;
    public static final int KEY_RSHIFT = GLFW.GLFW_KEY_RIGHT_SHIFT;
    public static final int KEY_LCONTROL = GLFW.GLFW_KEY_LEFT_CONTROL;
    public static final int KEY_RCONTROL = GLFW.GLFW_KEY_RIGHT_CONTROL;
    public static final int KEY_A = GLFW.GLFW_KEY_A;
    public static final int KEY_C = GLFW.GLFW_KEY_C;
    public static final int KEY_V = GLFW.GLFW_KEY_V;
    public static final int KEY_X = GLFW.GLFW_KEY_X;
    public static final int KEY_Z = GLFW.GLFW_KEY_Z;
    public static final int KEY_UP = GLFW.GLFW_KEY_UP;
    public static final int KEY_DOWN = GLFW.GLFW_KEY_DOWN;
    public static final int KEY_LEFT = GLFW.GLFW_KEY_LEFT;
    public static final int KEY_RIGHT = GLFW.GLFW_KEY_RIGHT;
    public static final int KEY_HOME = GLFW.GLFW_KEY_HOME;
    public static final int KEY_END = GLFW.GLFW_KEY_END;
    public static final int KEY_DELETE = GLFW.GLFW_KEY_DELETE;
    public static final int KEY_INSERT = GLFW.GLFW_KEY_INSERT;
    public static final int KEY_PRIOR = GLFW.GLFW_KEY_PAGE_UP;
    public static final int KEY_NEXT = GLFW.GLFW_KEY_PAGE_DOWN;

    public static boolean isKeyDown(int key) { return false; }
    public static int getEventKey() { return 0; }
    public static char getEventCharacter() { return 0; }
    public static boolean getEventKeyState() { return false; }
}
