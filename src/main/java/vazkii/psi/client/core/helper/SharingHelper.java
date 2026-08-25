package vazkii.psi.client.core.helper;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.util.Base64;
import javax.imageio.ImageIO;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

/** The 1.7.10 equivalent of modern Psi's Imgur/Reddit sharing helper. */
public final class SharingHelper {
    private static final String CLIENT_ID = "d5d2258f3526156";
    private SharingHelper() {}

    public static void uploadAndOpen(final String title, final String spellCode, final String screenshot) {
        new Thread("Psi Imgur Upload") { @Override public void run() { open(upload(title, spellCode, screenshot)); } }.start();
    }
    public static void uploadAndShare(final String title, final String spellCode, final String screenshot) {
        new Thread("Psi Reddit Share") { @Override public void run() {
            String imageUrl = upload(title, spellCode, screenshot);
            if (imageUrl == null) return;
            try {
                String text = "## " + title + "\n\n### [Image + Code](" + imageUrl + ")\n\n*Replace this with a description of your spell.*";
                open("https://old.reddit.com/r/psispellcompendium/submit?title=" + URLEncoder.encode(title, "UTF-8")
                    + "&text=" + URLEncoder.encode(text, "UTF-8"));
            } catch (Exception ignored) {}
        } }.start();
    }
    private static String upload(String title, String spellCode, String screenshot) {
        try {
            String form = "type=base64&image=" + URLEncoder.encode(screenshot, "UTF-8")
                + "&name=" + URLEncoder.encode(title, "UTF-8")
                + "&description=" + URLEncoder.encode("Spell Code:\n\n" + spellCode, "UTF-8");
            HttpURLConnection connection = (HttpURLConnection) new URI("https://api.imgur.com/3/image").toURL().openConnection();
            connection.setRequestMethod("POST"); connection.setDoOutput(true);
            connection.setRequestProperty("Authorization", "Client-ID " + CLIENT_ID);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            connection.getOutputStream().write(form.getBytes("UTF-8"));
            InputStream input = connection.getResponseCode() / 100 == 2 ? connection.getInputStream() : connection.getErrorStream();
            ByteArrayOutputStream response = new ByteArrayOutputStream(); byte[] chunk = new byte[1024];
            for (int read; input != null && (read = input.read(chunk)) != -1;) response.write(chunk, 0, read);
            String json = new String(response.toByteArray(), "UTF-8");
            java.util.regex.Matcher id = java.util.regex.Pattern.compile("\\\"id\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"").matcher(json);
            return id.find() ? "https://imgur.com/" + id.group(1) : null;
        } catch (Exception ignored) { return null; }
    }
    public static String takeScreenshot() throws Exception {
        int width = Display.getWidth(), height = Display.getHeight(); ByteBuffer pixels = BufferUtils.createByteBuffer(width * height * 4);
        GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) for (int x = 0; x < width; x++) {
            int i = (x + width * y) * 4, r = pixels.get(i) & 255, g = pixels.get(i + 1) & 255, b = pixels.get(i + 2) & 255, a = pixels.get(i + 3) & 255;
            image.setRGB(x, height - 1 - y, (a << 24) | (r << 16) | (g << 8) | b);
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream(); ImageIO.write(image, "png", output);
        return Base64.getEncoder().encodeToString(output.toByteArray());
    }
    private static void open(String url) {
        try { if (url != null && Desktop.isDesktopSupported()) Desktop.getDesktop().browse(new URI(url)); } catch (Exception ignored) {}
    }
}
