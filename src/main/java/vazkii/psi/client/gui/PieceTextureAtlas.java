package vazkii.psi.client.gui;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

/**
 * Custom texture atlas for spell piece icons.
 * Stitches all piece textures into a single atlas for efficient rendering.
 */
public class PieceTextureAtlas {

    private static PieceTextureAtlas INSTANCE;

    private final DynamicTexture atlasTexture;
    private final ResourceLocation atlasLocation;
    private final Map<String, UVCoords> uvMap = new HashMap<>();

    private static final int ICON_SIZE = 18; // Icons are 18x18 with transparent borders
    private static final int ATLAS_WIDTH = 256;
    private static final int ATLAS_HEIGHT = 256;

    // All piece textures we need to stitch
    private static final String[] PIECE_NAMES = { "constant_number", "constant_string", "operator_sum",
        "selector_caster", "selector_raycast", "selector_entity_position", "trick_debug", "trick_break_block",
        "trick_explode" };

    private PieceTextureAtlas() {
        // Create a blank atlas texture
        BufferedImage atlasImage = new BufferedImage(ATLAS_WIDTH, ATLAS_HEIGHT, BufferedImage.TYPE_INT_ARGB);

        // Stitch all piece textures into the atlas
        int x = 0;
        int y = 0;
        int maxRowHeight = 0;

        for (String pieceName : PIECE_NAMES) {
            try {
                // Load the piece texture
                ResourceLocation pieceRes = new ResourceLocation("psi", "textures/spell/" + pieceName + ".png");
                BufferedImage pieceImage = ImageIO.read(
                    Minecraft.getMinecraft()
                        .getResourceManager()
                        .getResource(pieceRes)
                        .getInputStream());

                // Check if we need to move to next row
                if (x + pieceImage.getWidth() > ATLAS_WIDTH) {
                    x = 0;
                    y += maxRowHeight;
                    maxRowHeight = 0;
                }

                // Copy piece image into atlas
                atlasImage.getGraphics()
                    .drawImage(pieceImage, x, y, null);

                // Store UV coordinates (normalized 0-1)
                float u0 = (float) x / ATLAS_WIDTH;
                float v0 = (float) y / ATLAS_HEIGHT;
                float u1 = (float) (x + pieceImage.getWidth()) / ATLAS_WIDTH;
                float v1 = (float) (y + pieceImage.getHeight()) / ATLAS_HEIGHT;

                uvMap.put(
                    "psi:" + pieceName,
                    new UVCoords(u0, v0, u1, v1, x, y, pieceImage.getWidth(), pieceImage.getHeight()));

                // Move to next position
                x += pieceImage.getWidth();
                maxRowHeight = Math.max(maxRowHeight, pieceImage.getHeight());

            } catch (IOException e) {
                System.err.println("[Psi] Failed to load piece texture: " + pieceName);
                e.printStackTrace();
            }
        }

        // Upload atlas to GPU
        atlasTexture = new DynamicTexture(atlasImage);
        atlasLocation = Minecraft.getMinecraft()
            .getTextureManager()
            .getDynamicTextureLocation("psi_piece_atlas", atlasTexture);

        System.out.println("[Psi] Piece texture atlas created with " + uvMap.size() + " pieces");
    }

    public static PieceTextureAtlas getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PieceTextureAtlas();
        }
        return INSTANCE;
    }

    /**
     * Bind the atlas texture for rendering.
     */
    public void bindAtlas() {
        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(atlasLocation);
    }

    /**
     * Get UV coordinates for a piece.
     */
    public UVCoords getUV(String pieceId) {
        return uvMap.get(pieceId);
    }

    /**
     * Draw a piece icon using the atlas.
     */
    public void drawPiece(String pieceId, int x, int y) {
        UVCoords uv = uvMap.get(pieceId);
        if (uv == null) {
            System.err.println("[Psi] No UV coords for piece: " + pieceId + " (available: " + uvMap.keySet() + ")");
            // Fallback: draw gray square
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glColor4f(0.5f, 0.5f, 0.5f, 1.0f);
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex2d(x, y + ICON_SIZE);
            GL11.glVertex2d(x + ICON_SIZE, y + ICON_SIZE);
            GL11.glVertex2d(x + ICON_SIZE, y);
            GL11.glVertex2d(x, y);
            GL11.glEnd();
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            return;
        }

        // Bind atlas and draw using Tessellator (matches Minecraft's GUI rendering)
        bindAtlas();
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        net.minecraft.client.renderer.Tessellator tessellator = net.minecraft.client.renderer.Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + uv.height, 0, uv.u0, uv.v1); // Bottom-left
        tessellator.addVertexWithUV(x + uv.width, y + uv.height, 0, uv.u1, uv.v1); // Bottom-right
        tessellator.addVertexWithUV(x + uv.width, y, 0, uv.u1, uv.v0); // Top-right
        tessellator.addVertexWithUV(x, y, 0, uv.u0, uv.v0); // Top-left
        tessellator.draw();
    }

    /**
     * UV coordinates for a piece in the atlas.
     */
    public static class UVCoords {

        public final float u0, v0, u1, v1;
        public final int x, y, width, height;

        public UVCoords(float u0, float v0, float u1, float v1, int x, int y, int width, int height) {
            this.u0 = u0;
            this.v0 = v0;
            this.u1 = u1;
            this.v1 = v1;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
}
