package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class PauseScreen {
    private static ShapeRenderer shapeRenderer;
    private static Texture resumeTexture;
    private static Texture menuTexture;
    private static final float BUTTON_WIDTH = 200;
    private static final float BUTTON_HEIGHT = 60;
    private static final float BUTTON_SPACING = 20;
    private static final Color OVERLAY_COLOR = new Color(0, 0, 0, 0.7f);
    private static final Color BUTTON_COLOR = new Color(0.2f, 0.2f, 0.2f, 1);
    private static final Color BUTTON_HOVER_COLOR = new Color(0.3f, 0.3f, 0.3f, 1);
    private static final Color TEXT_COLOR = new Color(1, 1, 1, 1);

    public static void initialize() {
        shapeRenderer = new ShapeRenderer();
        resumeTexture = new Texture("map1/towerDefense_tile272.png");
        menuTexture = new Texture("Menu/btn_up.png");
    }

    public static void render(Main game, BitmapFont font) {
        // Lưu lại blend function và color hiện tại
        int srcFunc = game.batch.getBlendSrcFunc();
        int dstFunc = game.batch.getBlendDstFunc();
        Color oldColor = game.batch.getColor().cpy();

        // Vẽ overlay nửa trong suốt
        game.batch.setColor(OVERLAY_COLOR);
        game.batch.draw(menuTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        float centerX = Gdx.graphics.getWidth() / 2;
        float centerY = Gdx.graphics.getHeight() / 2;

        // Vẽ tiêu đề "PAUSED"
        font.getData().setScale(2.0f);
        GlyphLayout layout = new GlyphLayout(font, "PAUSED");
        font.setColor(TEXT_COLOR);
        font.draw(game.batch, "PAUSED", centerX - layout.width/2, centerY + 100);

        // Vẽ các nút
        game.batch.setColor(Color.WHITE);
        float buttonY = centerY - BUTTON_HEIGHT/2;
        game.batch.draw(resumeTexture, centerX - BUTTON_WIDTH/2, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT);
        game.batch.draw(menuTexture, centerX - BUTTON_WIDTH/2, buttonY - BUTTON_HEIGHT - BUTTON_SPACING, 
                       BUTTON_WIDTH, BUTTON_HEIGHT);

        // Reset font scale và khôi phục blend function và color
        font.getData().setScale(1.0f);
        game.batch.setColor(oldColor);
        game.batch.setBlendFunction(srcFunc, dstFunc);
    }

    public static int checkClick(float x, float y) {
        float centerX = Gdx.graphics.getWidth() / 2;
        float centerY = Gdx.graphics.getHeight() / 2;
        float buttonY = centerY - BUTTON_HEIGHT/2;

        // Check resume button
        if (x >= centerX - BUTTON_WIDTH/2 && x <= centerX + BUTTON_WIDTH/2 &&
            y >= buttonY && y <= buttonY + BUTTON_HEIGHT) {
            return 1; // Resume
        }

        // Check menu button
        if (x >= centerX - BUTTON_WIDTH/2 && x <= centerX + BUTTON_WIDTH/2 &&
            y >= buttonY - BUTTON_HEIGHT - BUTTON_SPACING && 
            y <= buttonY - BUTTON_SPACING) {
            return 2; // Menu
        }

        return 0; // No button clicked
    }

    public static void dispose() {
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (resumeTexture != null) resumeTexture.dispose();
        if (menuTexture != null) menuTexture.dispose();
    }
}
