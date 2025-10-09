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
import io.github.some_example_name.utils.StarRating;
import io.github.some_example_name.utils.PlayerHealth;
import io.github.some_example_name.utils.GameSoundManager;
import io.github.some_example_name.config.map.MapProgress;
import io.github.some_example_name.config.map.MapType;
import io.github.some_example_name.screen.menu.MapSelectionScreen;

public class GameWinScreen implements Screen, InputProcessor {
    private final Main game;
    private final MapType currentMap;
    private BitmapFont titleFont;
    private BitmapFont statsFont;
    private BitmapFont buttonFont;
    private Texture backgroundTexture;
    private Texture btnUpTexture;
    private Texture btnDownTexture;
    private Texture btnHoverTexture;
    private Texture starTexture;
    private static final float BUTTON_WIDTH = 800;
    private static final float BUTTON_HEIGHT = 240;
    private static final Color TEXT_COLOR = new Color(1, 1, 1, 1);

    public GameWinScreen(final Main game, MapType currentMap) {
        this.game = game;
        this.currentMap = currentMap;

        // Load textures
        backgroundTexture = new Texture(Gdx.files.internal("Menu/static.jpg"));
        btnUpTexture = new Texture(Gdx.files.internal("Menu/btn_up.png"));
        btnDownTexture = new Texture(Gdx.files.internal("Menu/btn_down.png"));
        btnHoverTexture = new Texture(Gdx.files.internal("Menu/btn_hover.png"));
        starTexture = new Texture(Gdx.files.internal("Menu/star.png"));

        createFonts();
        Gdx.input.setInputProcessor(this);
    }

    private void createFonts() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/menu.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        // Font cho tiêu đề - to và nổi bật
        parameter.size = 120;
        parameter.color = Color.GOLD;
        parameter.borderWidth = 3;
        parameter.borderColor = new Color(0.6f, 0.4f, 0, 1);
        titleFont = generator.generateFont(parameter);

        // Font cho thống kê - dễ đọc
        parameter.size = 60;
        parameter.color = Color.WHITE;
        parameter.borderWidth = 2;
        parameter.borderColor = Color.BLACK;
        statsFont = generator.generateFont(parameter);

        // Font cho buttons
        parameter.size = 48;
        parameter.color = Color.WHITE;
        parameter.borderWidth = 0;
        buttonFont = generator.generateFont(parameter);

        generator.dispose();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!game.batch.isDrawing()) {
            game.batch.begin();
        }

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float centerX = screenWidth / 2;

        // Vẽ tiêu đề "LEVEL COMPLETE!" ở vị trí cao hơn
        float titleY = screenHeight - 150;
        titleFont.getData().setScale(1.5f);
        GlyphLayout titleLayout = new GlyphLayout(titleFont, "LEVEL COMPLETE!");
        titleFont.setColor(Color.GOLD);
        titleFont.draw(game.batch, "LEVEL COMPLETE!", centerX - titleLayout.width/2, titleY);

        // Cập nhật thời gian chơi
        GameStats.updatePlayTime();

        // Vẽ số sao - ngay dưới tiêu đề với khoảng cách đẹp
        float starsY = titleY - 180;
        drawStars(centerX, starsY);

        // Vẽ thống kê với khoảng cách rõ ràng
        float statsStartY = starsY - 150;
        drawStatistics(centerX, statsStartY);

        // Vẽ nút Continue ở dưới cùng
        float buttonY = 150;
        drawContinueButton(centerX, buttonY);

        if (game.batch.isDrawing()) {
            game.batch.end();
        }
    }

    private void drawStars(float centerX, float y) {
        int stars = MapProgress.getInstance().getMapStars(currentMap);
        float starSize = 100f;
        float starSpacing = 140f;
        float starsStartX = centerX - starSpacing;

        // Vẽ background cho các sao (tạo hiệu ứng nổi)
        game.batch.setColor(0, 0, 0, 0.3f);
        for (int i = 0; i < 3; i++) {
            float starX = starsStartX + i * starSpacing;
            game.batch.draw(starTexture,
                starX - starSize/2 - 5,
                y - starSize/2 - 5,
                starSize + 10,
                starSize + 10);
        }

        // Vẽ các sao
        for (int i = 0; i < 3; i++) {
            float starX = starsStartX + i * starSpacing;

            if (i < stars) {
                // Sao đạt được - vàng rực rỡ
                game.batch.setColor(1f, 0.9f, 0.1f, 1f);
                game.batch.draw(starTexture,
                    starX - starSize/2,
                    y - starSize/2,
                    starSize,
                    starSize);
            } else {
                // Sao chưa đạt - xám mờ
                game.batch.setColor(0.3f, 0.3f, 0.3f, 0.5f);
                game.batch.draw(starTexture,
                    starX - starSize/2,
                    y - starSize/2,
                    starSize,
                    starSize);
            }
        }
        game.batch.setColor(Color.WHITE);
    }

    private void drawStatistics(float centerX, float startY) {
        statsFont.getData().setScale(1.0f);
        statsFont.setColor(Color.WHITE);

        String enemyStats = GameStats.getEnemyStatsString();
        String timeStats = GameStats.getTimeString();

        // Vẽ với khoảng cách rõ ràng giữa các dòng
        float lineSpacing = 80f;

        GlyphLayout enemyLayout = new GlyphLayout(statsFont, enemyStats);
        statsFont.draw(game.batch, enemyStats,
            centerX - enemyLayout.width/2,
            startY);

        GlyphLayout timeLayout = new GlyphLayout(statsFont, timeStats);
        statsFont.draw(game.batch, timeStats,
            centerX - timeLayout.width/2,
            startY - lineSpacing);
    }

    private void drawContinueButton(float centerX, float buttonY) {
        float borderThickness = 16f;

        // Vẽ bóng nút (hiệu ứng depth)
        game.batch.setColor(0, 0, 0, 0.4f);
        game.batch.draw(btnUpTexture,
            centerX - BUTTON_WIDTH/2 + 8,
            buttonY - 8,
            BUTTON_WIDTH,
            BUTTON_HEIGHT);

        // Vẽ viền nút
        game.batch.setColor(0.4f, 0.2f, 0.1f, 1f);
        game.batch.draw(btnUpTexture,
            centerX - BUTTON_WIDTH/2 - borderThickness,
            buttonY - borderThickness,
            BUTTON_WIDTH + 2*borderThickness,
            BUTTON_HEIGHT + 2*borderThickness);

        // Vẽ nút chính
        game.batch.setColor(Color.WHITE);
        game.batch.draw(btnUpTexture,
            centerX - BUTTON_WIDTH/2,
            buttonY,
            BUTTON_WIDTH,
            BUTTON_HEIGHT);

        // Vẽ text trên nút
        buttonFont.getData().setScale(2.0f);
        buttonFont.setColor(Color.WHITE);
        GlyphLayout buttonLayout = new GlyphLayout(buttonFont, "Continue");
        buttonFont.draw(game.batch, "Continue",
            centerX - buttonLayout.width/2,
            buttonY + BUTTON_HEIGHT/2 + buttonLayout.height/2);
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void show() {
        // Phát âm thanh chiến thắng
        GameSoundManager.playCongratulationSound();
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
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
        starTexture.dispose();
    }

    public int checkClick(float x, float y) {
        float centerX = Gdx.graphics.getWidth() / 2;
        float buttonY = 150;
        float borderThickness = 16f;

        if (x >= centerX - BUTTON_WIDTH/2 - borderThickness &&
            x <= centerX + BUTTON_WIDTH/2 + borderThickness &&
            y >= buttonY - borderThickness &&
            y <= buttonY + BUTTON_HEIGHT + borderThickness) {
            return 1;
        }

        return 0;
    }

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
            game.setScreen(new io.github.some_example_name.screen.menu.MapSelectionScreen(game));
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
