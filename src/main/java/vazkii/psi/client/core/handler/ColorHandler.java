package vazkii.psi.client.core.handler;

/** Color interpolation helpers used by animated CAD colorizers. */
public final class ColorHandler {

    private ColorHandler() {}

    public static int slideColor(int[] color, float speed) {
        int n = color.length;
        double t = (ClientTickHandler.total * speed * n / Math.PI) % n;
        int phase = (int) t;
        double dt = t - phase;
        if (dt == 0) return color[phase];
        return slideColorTime(color[phase], color[(phase + 1) % n], (float) (dt * Math.PI));
    }

    public static int pulseColor(int source, float multiplier, float speed, int magnitude) {
        int add = (int) (Math.sin(ClientTickHandler.ticksInGame * speed) * magnitude);
        int red = (0x00FF0000 & source) >> 16;
        int green = (0x0000FF00 & source) >> 8;
        int blue = 0x000000FF & source;
        int addedRed = clamp((int) (multiplier * (red + add)));
        int addedGreen = clamp((int) (multiplier * (green + add)));
        int addedBlue = clamp((int) (multiplier * (blue + add)));
        return 0xFF000000 | (addedRed << 16) | (addedGreen << 8) | addedBlue;
    }

    public static int slideColorTime(int color, int secondColor, float t) {
        float shift = (1 - (float) Math.cos(t)) / 2;
        if (shift == 0) return color;
        if (shift == 1) return secondColor;
        int redA = (0x00FF0000 & color) >> 16;
        int greenA = (0x0000FF00 & color) >> 8;
        int blueA = 0x000000FF & color;
        int redB = (0x00FF0000 & secondColor) >> 16;
        int greenB = (0x0000FF00 & secondColor) >> 8;
        int blueB = 0x000000FF & secondColor;
        int newRed = (int) (redA * (1 - shift) + redB * shift);
        int newGreen = (int) (greenA * (1 - shift) + greenB * shift);
        int newBlue = (int) (blueA * (1 - shift) + blueB * shift);
        return 0xFF000000 | (newRed << 16) | (newGreen << 8) | newBlue;
    }

    private static int clamp(int v) {
        return v < 0 ? 0 : v > 255 ? 255 : v;
    }
}
