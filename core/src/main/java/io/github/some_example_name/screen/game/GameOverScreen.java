package io.github.some_example_name.screen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

import io.github.some_example_name.Main;
import io.github.some_example_name.screen.menu.MenuScreen;
import io.github.some_example_name.utils.GameStats;
import io.github.some_example_name.config.map.MapType;

public class GameOverScreen implements Screen, InputProcessor {
    private final Main game;
    private final MapType currentMap;
    private BitmapFont titleFont;
    private BitmapFont statsFont;
    private BitmapFont buttonFont;
    private Texture backgroundTexture;
    private Texture btnUpTexture;
    private Texture btnDownTexture;
    private Texture btnHoverTexture;
    private static final float BUTTON_WIDTH = 800; // 400 * 2 - to gấp đôi
    private static final float BUTTON_HEIGHT = 240; // 120 * 2 - to gấp đôi
    private static final float BUTTON_SPACING = 40; // 20 * 2 - tăng khoảng cách
    private static final Color TEXT_COLOR = new Color(1, 1, 1, 1);

    public GameOverScreen(final Main game, MapType currentMap) {
        this.game = game;
        this.currentMap = currentMap;
        
        // Load textures
        backgroundTexture = new Texture(Gdx.files.internal("Menu/static.jpg"));
        btnUpTexture = new Texture(Gdx.files.internal("Menu/btn_up.png"));
        btnDownTexture = new Texture(Gdx.files.internal("Menu/btn_down.png"));
        btnHoverTexture = new Texture(Gdx.files.internal("Menu/btn_hover.png"));

        // Tạo fonts
        createFonts();
        
        // Thiết lập input processor
        Gdx.input.setInputProcessor(this);
    }

    private void createFonts() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/menu.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        
        // Font cho tiêu đề
        parameter.size = 144; // 48 * 3
        parameter.color = Color.RED;
        titleFont = generator.generateFont(parameter);
        
        // Font cho thống kê
        parameter.size = 72; // 24 * 3
        parameter.color = Color.WHITE;
        statsFont = generator.generateFont(parameter);
        
        // Font cho buttons
        parameter.size = 48; // 16 * 3
        parameter.color = Color.WHITE;
        buttonFont = generator.generateFont(parameter);
        
        generator.dispose();
    }

    @Override
    public void render(float delta) {
        // Vẽ nền
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Vẽ nền đen đơn giản
        // Kiểm tra và đảm bảo batch không đang active
        if (!game.batch.isDrawing()) {
            game.batch.begin();
        }
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        
        float centerX = screenWidth / 2;
        float centerY = screenHeight / 2;

        // Vẽ tiêu đề "GAME OVER"
        titleFont.getData().setScale(1.5f);
        GlyphLayout titleLayout = new GlyphLayout(titleFont, "GAME OVER");
        titleFont.setColor(Color.RED);
        titleFont.draw(game.batch, "GAME OVER", centerX - titleLayout.width/2, centerY + 300);

        // Cập nhật thời gian chơi trước khi hiển thị
        GameStats.updatePlayTime();
        
        // Vẽ thống kê
        statsFont.getData().setScale(1.2f);
        statsFont.setColor(Color.WHITE);
        
        String enemyStats = GameStats.getEnemyStatsString();
        String timeStats = GameStats.getTimeString();
        
        GlyphLayout enemyLayout = new GlyphLayout(statsFont, enemyStats);
        GlyphLayout timeLayout = new GlyphLayout(statsFont, timeStats);
        
        statsFont.draw(game.batch, enemyStats, centerX - enemyLayout.width/2, centerY + 150);
        statsFont.draw(game.batch, timeStats, centerX - timeLayout.width/2, centerY + 100);

        // Vẽ các nút - đặt xa hơn để không bị đè
        float buttonY = centerY - 100;
        
        // Vẽ viền nút
        float borderThickness = 16f;
        game.batch.setColor(0.4f, 0.2f, 0.1f, 1f); // Màu nâu đậm cho viền
        
        // Nút Play Again
        game.batch.draw(btnUpTexture, 
                       centerX - BUTTON_WIDTH/2 - borderThickness, 
                       buttonY - borderThickness, 
                       BUTTON_WIDTH + 2*borderThickness, 
                       BUTTON_HEIGHT + 2*borderThickness);
        
        // Nút Back to Menu
        game.batch.draw(btnUpTexture, 
                       centerX - BUTTON_WIDTH/2 - borderThickness, 
                       buttonY - BUTTON_HEIGHT - BUTTON_SPACING - borderThickness,
                       BUTTON_WIDTH + 2*borderThickness, 
                       BUTTON_HEIGHT + 2*borderThickness);
        
        // Vẽ nút
        game.batch.setColor(Color.WHITE);
        game.batch.draw(btnUpTexture, centerX - BUTTON_WIDTH/2, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT);
        game.batch.draw(btnUpTexture, 
                       centerX - BUTTON_WIDTH/2, 
                       buttonY - BUTTON_HEIGHT - BUTTON_SPACING,
                       BUTTON_WIDTH, BUTTON_HEIGHT);

        // Vẽ text trên nút
        buttonFont.getData().setScale(2.0f); // Tăng kích thước font cho nút to
        buttonFont.setColor(Color.WHITE);
        
        // Text nút Play Again
        GlyphLayout playAgainLayout = new GlyphLayout(buttonFont, "Play Again");
        buttonFont.draw(game.batch, "Play Again", 
                       centerX - playAgainLayout.width/2, 
                       buttonY + BUTTON_HEIGHT/2 + playAgainLayout.height/2);
        
        // Text nút Back to Menu
        GlyphLayout menuLayout = new GlyphLayout(buttonFont, "Back to Menu");
        buttonFont.draw(game.batch, "Back to Menu", 
                       centerX - menuLayout.width/2, 
                       buttonY - BUTTON_HEIGHT - BUTTON_SPACING + BUTTON_HEIGHT/2 + menuLayout.height/2);

        // Chỉ end batch nếu chúng ta đã begin nó
        if (game.batch.isDrawing()) {
            game.batch.end();
        }
    }

    @Override
    public void resize(int width, int height) {
        // Không cần xử lý resize
    }

    @Override
    public void show() {
        // Màn hình được hiển thị
    }

    @Override
    public void hide() {
        // Màn hình bị ẩn
    }

    @Override
    public void pause() {
        // Game bị tạm dừng
    }

    @Override
    public void resume() {
        // Game được tiếp tục
    }

    @Override
    public void dispose() {
        titleFont.dispose();
        statsFont.dispose();
        buttonFont.dispose();
        backgroundTexture.dispose();
        btnUpTexture.dispose();
        btnDownTexture.dispose();
        btnHoverTexture.dispose();
    }
    
    // Phương thức kiểm tra click (cần được gọi từ bên ngoài)
    public int checkClick(float x, float y) {
        float centerX = Gdx.graphics.getWidth() / 2;
        float centerY = Gdx.graphics.getHeight() / 2;
        float buttonY = centerY - 100; // Cập nhật vị trí nút mới
        float borderThickness = 16f;

        // Check play again button
        if (x >= centerX - BUTTON_WIDTH/2 - borderThickness && 
            x <= centerX + BUTTON_WIDTH/2 + borderThickness &&
            y >= buttonY - borderThickness && 
            y <= buttonY + BUTTON_HEIGHT + borderThickness) {
            return 1; // Play Again
        }

        // Check back to menu button
        if (x >= centerX - BUTTON_WIDTH/2 - borderThickness && 
            x <= centerX + BUTTON_WIDTH/2 + borderThickness &&
            y >= buttonY - BUTTON_HEIGHT - BUTTON_SPACING - borderThickness &&
            y <= buttonY - BUTTON_SPACING + borderThickness) {
            return 2; // Back to Menu
        }

        return 0; // No button clicked
    }
    
    // InputProcessor methods
    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        int result = checkClick(screenX, Gdx.graphics.getHeight() - screenY);
        if (result == 1) {
            // Play Again button clicked - chơi lại map hiện tại
            game.setScreen(new io.github.some_example_name.screen.game.GameScreen(game, currentMap));
            dispose();
            return true;
        } else if (result == 2) {
            // Back to Menu button clicked
            game.setScreen(new MenuScreen(game));
            dispose();
            return true;
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }
}