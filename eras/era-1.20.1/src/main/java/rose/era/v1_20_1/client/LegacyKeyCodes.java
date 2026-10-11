package rose.era.v1_20_1.client;

/**
 * 1.20.1 key codes (GLFW key codes and mouse buttons) as 26.3 key codes (SDL scancodes, mouse buttons from 1). The
 * table pairs the {@code InputConstants.KEY_*} and {@code MOUSE_BUTTON_*} constants of both versions by name
 * (1.20.1 KEY_LWIN/KEY_RWIN are 26.3 KEY_LGUI/KEY_RGUI; F25 has no 26.3 key).
 */
public final class LegacyKeyCodes {
    /** The 26.3 keyboard key for a 1.20.1 GLFW key code; unknown keys (GLFW -1, F25) are 26.3's unknown key, 0. */
    public static int key(int glfw) {
        return switch (glfw) {
            case 32 -> 44; // SPACE
            case 39 -> 52; // APOSTROPHE
            case 44 -> 54; // COMMA
            case 45 -> 45; // MINUS
            case 46 -> 55; // PERIOD
            case 47 -> 56; // SLASH
            case 48 -> 39; // 0
            case 49 -> 30; // 1
            case 50 -> 31; // 2
            case 51 -> 32; // 3
            case 52 -> 33; // 4
            case 53 -> 34; // 5
            case 54 -> 35; // 6
            case 55 -> 36; // 7
            case 56 -> 37; // 8
            case 57 -> 38; // 9
            case 59 -> 51; // SEMICOLON
            case 61 -> 46; // EQUALS
            case 65 -> 4; // A
            case 66 -> 5; // B
            case 67 -> 6; // C
            case 68 -> 7; // D
            case 69 -> 8; // E
            case 70 -> 9; // F
            case 71 -> 10; // G
            case 72 -> 11; // H
            case 73 -> 12; // I
            case 74 -> 13; // J
            case 75 -> 14; // K
            case 76 -> 15; // L
            case 77 -> 16; // M
            case 78 -> 17; // N
            case 79 -> 18; // O
            case 80 -> 19; // P
            case 81 -> 20; // Q
            case 82 -> 21; // R
            case 83 -> 22; // S
            case 84 -> 23; // T
            case 85 -> 24; // U
            case 86 -> 25; // V
            case 87 -> 26; // W
            case 88 -> 27; // X
            case 89 -> 28; // Y
            case 90 -> 29; // Z
            case 91 -> 47; // LBRACKET
            case 92 -> 49; // BACKSLASH
            case 93 -> 48; // RBRACKET
            case 96 -> 53; // GRAVE
            case 256 -> 41; // ESCAPE
            case 257 -> 40; // RETURN
            case 258 -> 43; // TAB
            case 259 -> 42; // BACKSPACE
            case 260 -> 73; // INSERT
            case 261 -> 76; // DELETE
            case 262 -> 79; // RIGHT
            case 263 -> 80; // LEFT
            case 264 -> 81; // DOWN
            case 265 -> 82; // UP
            case 266 -> 75; // PAGEUP
            case 267 -> 78; // PAGEDOWN
            case 268 -> 74; // HOME
            case 269 -> 77; // END
            case 280 -> 57; // CAPSLOCK
            case 281 -> 71; // SCROLLLOCK
            case 282 -> 83; // NUMLOCK
            case 283 -> 70; // PRINTSCREEN
            case 284 -> 72; // PAUSE
            case 290 -> 58; // F1
            case 291 -> 59; // F2
            case 292 -> 60; // F3
            case 293 -> 61; // F4
            case 294 -> 62; // F5
            case 295 -> 63; // F6
            case 296 -> 64; // F7
            case 297 -> 65; // F8
            case 298 -> 66; // F9
            case 299 -> 67; // F10
            case 300 -> 68; // F11
            case 301 -> 69; // F12
            case 302 -> 104; // F13
            case 303 -> 105; // F14
            case 304 -> 106; // F15
            case 305 -> 107; // F16
            case 306 -> 108; // F17
            case 307 -> 109; // F18
            case 308 -> 110; // F19
            case 309 -> 111; // F20
            case 310 -> 112; // F21
            case 311 -> 113; // F22
            case 312 -> 114; // F23
            case 313 -> 115; // F24
            case 320 -> 98; // NUMPAD0
            case 321 -> 89; // NUMPAD1
            case 322 -> 90; // NUMPAD2
            case 323 -> 91; // NUMPAD3
            case 324 -> 92; // NUMPAD4
            case 325 -> 93; // NUMPAD5
            case 326 -> 94; // NUMPAD6
            case 327 -> 95; // NUMPAD7
            case 328 -> 96; // NUMPAD8
            case 329 -> 97; // NUMPAD9
            case 330 -> 220; // NUMPADCOMMA
            case 332 -> 85; // MULTIPLY
            case 334 -> 87; // ADD
            case 335 -> 88; // NUMPADENTER
            case 336 -> 103; // NUMPADEQUALS
            case 340 -> 225; // LSHIFT
            case 341 -> 224; // LCONTROL
            case 342 -> 226; // LALT
            case 343 -> 227; // LWIN
            case 344 -> 229; // RSHIFT
            case 345 -> 228; // RCONTROL
            case 346 -> 230; // RALT
            case 347 -> 231; // RWIN
            default -> 0;
        };
    }

    /** The 26.3 mouse button for a 1.20.1 (GLFW, from 0) mouse button. */
    public static int mouseButton(int glfw) {
        return switch (glfw) {
            case 0 -> 1; // LEFT
            case 1 -> 3; // RIGHT
            case 2 -> 2; // MIDDLE
            default -> glfw + 1;
        };
    }

    private LegacyKeyCodes() {}
}
