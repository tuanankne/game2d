package io.github.some_example_name.screen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import io.github.some_example_name.Main;

public class PauseScreen {
    private static ShapeRenderer shapeRenderer;
    private static Texture backgroundTexture;
    private static Texture btnUpTexture;
    private static Texture btnDownTexture;
    private static Texture btnHoverTexture;
    private static BitmapFont titleFont;
    private static BitmapFont buttonFont;
    private static final float BUTTON_WIDTH = 400; // 200 * 2 - tăng gấp đôi kích thước nút
    private static final float BUTTON_HEIGHT = 120; // 60 * 2 - tăng gấp đôi kích thước nút
    private static final float BUTTON_SPACING = 20; // Tăng khoảng cách để phù hợp với nút to hơn
    private static final Color TEXT_COLOR = new Color(1, 1, 1, 1);

    public static void initialize() {
        shapeRenderer = new ShapeRenderer();
        backgroundTexture = new Texture("Menu/static.jpg");
        btnUpTexture = new Texture("Menu/btn_up.png");
        btnDownTexture = new Texture("Menu/btn_down.png");
        btnHoverTexture = new Texture("Menu/btn_hover.png");
        
        // Tạo fonts
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/menu.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        
        // Font cho tiêu đề
        parameter.size = 144; // 48 * 3
        parameter.color = Color.WHITE;
        titleFont = generator.generateFont(parameter);
        
        // Font cho buttons
        parameter.size = 48; // 24 * 2 - tăng gấp đôi kích thước font cho nút to hơn
        parameter.color = Color.WHITE;
        buttonFont = generator.generateFont(parameter);
        
        generator.dispose();
    }

    public static void render(Main game, BitmapFont font) {
        // Lưu lại blend function và color hiện tại
        int srcFunc = game.batch.getBlendSrcFunc();
        int dstFunc = game.batch.getBlendDstFunc();
        Color oldColor = game.batch.getColor().cpy();

        // Vẽ background chỉ chiếm 1/3 màn hình ở giữa (tăng độ cao)
        game.batch.setColor(1, 1, 1, 1);
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float bgWidth = screenWidth / 3f; // Giữ nguyên độ rộng
        float bgHeight = screenHeight / 2.5f; // Tăng độ cao từ 1/3 lên 1/2.5
        float bgX = (screenWidth - bgWidth) / 2f;
        float bgY = (screenHeight - bgHeight) / 2f;
        game.batch.draw(backgroundTexture, bgX, bgY, bgWidth, bgHeight);
        
        float centerX = Gdx.graphics.getWidth() / 2;
        float centerY = Gdx.graphics.getHeight() / 2;

        // Vẽ tiêu đề "PAUSED" trong vùng static.jpg
        titleFont.getData().setScale(0.5f); // Giảm kích thước font để vừa trong static.jpg
        GlyphLayout layout = new GlyphLayout(titleFont, "PAUSED");
        titleFont.setColor(TEXT_COLOR);
        titleFont.draw(game.batch, "PAUSED", centerX - layout.width/2, bgY + bgHeight - 20); // Đặt ở phía trên static.jpg

        // Vẽ các nút với viền to hơn trong vùng static.jpg
        game.batch.setColor(Color.WHITE);
        float buttonY = bgY + bgHeight/2 - BUTTON_HEIGHT/2; // Đặt nút ở giữa static.jpg
        
        // Vẽ viền to hơn cho Resume button
        float borderThickness = 16f; // 8 * 2 - tăng gấp đôi độ dày viền cho nút to hơn
        game.batch.setColor(0.4f, 0.2f, 0.1f, 1f); // Màu nâu đậm cho viền
        game.batch.draw(btnUpTexture, 
                       centerX - BUTTON_WIDTH/2 - borderThickness, 
                       buttonY - borderThickness, 
                       BUTTON_WIDTH + 2*borderThickness, 
                       BUTTON_HEIGHT + 2*borderThickness);
        
        // Vẽ nút Resume
        game.batch.setColor(Color.WHITE);
        game.batch.draw(btnUpTexture, centerX - BUTTON_WIDTH/2, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT);
        
        // Vẽ viền to hơn cho Menu button (đặt ở phía dưới Resume button)
        game.batch.setColor(0.4f, 0.2f, 0.1f, 1f); // Màu nâu đậm cho viền
        game.batch.draw(btnUpTexture, 
                       centerX - BUTTON_WIDTH/2 - borderThickness, 
                       buttonY - BUTTON_HEIGHT - BUTTON_SPACING - borderThickness,
                       BUTTON_WIDTH + 2*borderThickness, 
                       BUTTON_HEIGHT + 2*borderThickness);
        
        // Vẽ nút Menu
        game.batch.setColor(Color.WHITE);
        game.batch.draw(btnUpTexture, 
                       centerX - BUTTON_WIDTH/2, 
                       buttonY - BUTTON_HEIGHT - BUTTON_SPACING,
                       BUTTON_WIDTH, BUTTON_HEIGHT);

        // Vẽ text trên buttons
        buttonFont.getData().setScale(1.0f);
        buttonFont.setColor(Color.WHITE);
        
        // Resume button text
        GlyphLayout resumeLayout = new GlyphLayout(buttonFont, "Resume");
        buttonFont.draw(game.batch, "Resume", 
                       centerX - resumeLayout.width/2, 
                       buttonY + BUTTON_HEIGHT/2 + resumeLayout.height/2);
        
        // Menu button text
        GlyphLayout menuLayout = new GlyphLayout(buttonFont, "Back to Menu");
        buttonFont.draw(game.batch, "Back to Menu", 
                       centerX - menuLayout.width/2, 
                       buttonY - BUTTON_HEIGHT - BUTTON_SPACING + BUTTON_HEIGHT/2 + menuLayout.height/2);

        // Reset font scale và khôi phục blend function và color
        titleFont.getData().setScale(1.0f);
        buttonFont.getData().setScale(1.0f);
        game.batch.setColor(oldColor);
        game.batch.setBlendFunction(srcFunc, dstFunc);
    }

    public static int checkClick(float x, float y) {
        float centerX = Gdx.graphics.getWidth() / 2;
        float screenHeight = Gdx.graphics.getHeight();
        float bgHeight = screenHeight / 2.5f; // Cập nhật để phù hợp với độ cao mới
        float bgY = (screenHeight - bgHeight) / 2f;
        float buttonY = bgY + bgHeight/2 - BUTTON_HEIGHT/2; // Vị trí nút trong static.jpg
        float borderThickness = 16f; // 8 * 2 - tăng gấp đôi độ dày viền

        // Check resume button (bao gồm cả viền)
        if (x >= centerX - BUTTON_WIDTH/2 - borderThickness && 
            x <= centerX + BUTTON_WIDTH/2 + borderThickness &&
            y >= buttonY - borderThickness && 
            y <= buttonY + BUTTON_HEIGHT + borderThickness) {
            return 1; // Resume
        }

        // Check menu button (bao gồm cả viền)
        if (x >= centerX - BUTTON_WIDTH/2 - borderThickness && 
            x <= centerX + BUTTON_WIDTH/2 + borderThickness &&
            y >= buttonY - BUTTON_HEIGHT - BUTTON_SPACING - borderThickness &&
            y <= buttonY - BUTTON_SPACING + borderThickness) {
            return 2; // Menu
        }

        return 0; // No button clicked
    }

    public static void dispose() {
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (btnUpTexture != null) btnUpTexture.dispose();
        if (btnDownTexture != null) btnDownTexture.dispose();
        if (btnHoverTexture != null) btnHoverTexture.dispose();
        if (titleFont != null) titleFont.dispose();
        if (buttonFont != null) buttonFont.dispose();
    }
}