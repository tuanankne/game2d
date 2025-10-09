package io.github.some_example_name.screen.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.audio.Music;

import io.github.some_example_name.Main;
import io.github.some_example_name.screen.ui.ScrollingBackground;
import io.github.some_example_name.screen.ui.MenuButton;
import io.github.some_example_name.screen.ui.SoundManager;
import io.github.some_example_name.screen.ui.MusicManager;

public class SettingsScreen implements Screen {
    private final Main game;
    private final SpriteBatch batch;
    private final BitmapFont titleFont;
    private final BitmapFont settingFont;
    private final BitmapFont backButtonFont;
    private final Texture buttonTexture;
    private final ScrollingBackground background;
    private final MenuButton backButton;
    private final MenuButton musicToggleButton;
    private final MenuButton soundEffectToggleButton;
    private final SoundManager soundManager;
    private final MusicManager musicManager;

    private static final float BACK_BUTTON_SIZE = 400;
    private static final float SETTING_BUTTON_WIDTH = 200;
    private static final float SETTING_BUTTON_HEIGHT = 80;

    public SettingsScreen(final Main game) {
        this.game = game;
        this.batch = game.batch;
        this.soundManager = SoundManager.getInstance();
        this.musicManager = MusicManager.getInstance();

        // Khởi tạo font cho tiêu đề
        FreeTypeFontGenerator titleGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/menu.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter titleParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        titleParameter.size = 80;
        titleParameter.color = Color.WHITE;
        titleParameter.borderWidth = 3;
        titleParameter.borderColor = Color.BLACK;
        this.titleFont = titleGenerator.generateFont(titleParameter);
        titleGenerator.dispose();

        // Khởi tạo font cho setting
        FreeTypeFontGenerator settingGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/menu.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter settingParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        settingParameter.size = 50;
        settingParameter.color = Color.WHITE;
        settingParameter.borderWidth = 2;
        settingParameter.borderColor = Color.BLACK;
        this.settingFont = settingGenerator.generateFont(settingParameter);
        settingGenerator.dispose();

        // Khởi tạo font cho nút back
        FreeTypeFontGenerator backButtonGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/menu.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter backButtonParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        backButtonParameter.size = 60;
        backButtonParameter.color = Color.WHITE;
        backButtonParameter.borderWidth = 2;
        backButtonParameter.borderColor = Color.BLACK;
        this.backButtonFont = backButtonGenerator.generateFont(backButtonParameter);
        backButtonGenerator.dispose();

        // Khởi tạo textures
        this.buttonTexture = new Texture(Gdx.files.internal("Menu/btn_up.png"));

        // Khởi tạo scrolling background
        background = new ScrollingBackground("Menu/bg.jpg");

        // Khởi tạo các nút
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();

        // Nút Back ở góc trái dưới
        float backBtnWidth = Math.min(screenW * 0.3f, BACK_BUTTON_SIZE);
        float backBtnHeight = Math.min(screenH * 0.15f, BACK_BUTTON_SIZE * 0.4f);
        backButton = new MenuButton(buttonTexture, 20, 20, backBtnWidth, backBtnHeight, "BACK");

        // Nút Music ở giữa màn hình
        float musicBtnX = (screenW - SETTING_BUTTON_WIDTH) / 2;
        float musicBtnY = screenH * 0.6f;
        musicToggleButton = new MenuButton(buttonTexture, musicBtnX, musicBtnY, 
            SETTING_BUTTON_WIDTH, SETTING_BUTTON_HEIGHT, 
            soundManager.isMusicEnabled() ? "ON" : "OFF");

        // Nút Sound Effects
        float soundBtnX = (screenW - SETTING_BUTTON_WIDTH) / 2;
        float soundBtnY = screenH * 0.4f;
        soundEffectToggleButton = new MenuButton(buttonTexture, soundBtnX, soundBtnY, 
            SETTING_BUTTON_WIDTH, SETTING_BUTTON_HEIGHT, 
            soundManager.isSoundEffectEnabled() ? "ON" : "OFF");

        // Khởi tạo nhạc nền
        musicManager.playMusic();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Cập nhật scrolling background
        background.update(delta);

        // Cập nhật trạng thái các nút
        backButton.update();
        musicToggleButton.update();
        soundEffectToggleButton.update();

        // Xử lý click
        if (Gdx.input.justTouched()) {
            float touchX = Gdx.input.getX();
            float touchY = Gdx.graphics.getHeight() - Gdx.input.getY();

            // Kiểm tra click nút back
            if (backButton.isClicked()) {
                game.setScreen(new MenuScreen(game));
                dispose();
                return;
            }

            // Kiểm tra click nút âm thanh
            if (musicToggleButton.isClicked()) {
                soundManager.toggleMusic();
                musicToggleButton.setText(soundManager.isMusicEnabled() ? "ON" : "OFF");
                musicManager.updateVolume();
            }

            // Kiểm tra click nút hiệu ứng âm thanh
            if (soundEffectToggleButton.isClicked()) {
                soundManager.toggleSoundEffect();
                soundEffectToggleButton.setText(soundManager.isSoundEffectEnabled() ? "ON" : "OFF");
            }
        }

        batch.begin();

        // Vẽ background
        background.render(batch);

        // Vẽ tiêu đề
        titleFont.setColor(Color.WHITE);
        String title = "SETTINGS";
        GlyphLayout titleLayout = new GlyphLayout(titleFont, title);
        titleFont.draw(batch, title,
            (Gdx.graphics.getWidth() - titleLayout.width) / 2,
            Gdx.graphics.getHeight() * 0.9f);

        // Vẽ nhãn "Music:"
        settingFont.setColor(Color.WHITE);
        String musicLabel = "MUSIC:";
        GlyphLayout musicLabelLayout = new GlyphLayout(settingFont, musicLabel);
        settingFont.draw(batch, musicLabel,
            (Gdx.graphics.getWidth() - musicLabelLayout.width) / 2,
            Gdx.graphics.getHeight() * 0.7f);

        // Vẽ nhãn "Sound Effects:"
        String soundLabel = "SOUND EFFECTS:";
        GlyphLayout soundLabelLayout = new GlyphLayout(settingFont, soundLabel);
        settingFont.draw(batch, soundLabel,
            (Gdx.graphics.getWidth() - soundLabelLayout.width) / 2,
            Gdx.graphics.getHeight() * 0.5f);

        // Vẽ các nút
        musicToggleButton.draw(batch, settingFont);
        soundEffectToggleButton.draw(batch, settingFont);
        backButton.draw(batch, backButtonFont);

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        background.resize(width, height);
    }

    @Override
    public void show() {
        musicManager.playMusic();
    }

    @Override
    public void hide() {
        // Không pause nhạc để nhạc tiếp tục chạy khi chuyển sang màn hình khác
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        titleFont.dispose();
        settingFont.dispose();
        backButtonFont.dispose();
        buttonTexture.dispose();
        background.dispose();
        // Không dispose musicManager vì nó được dùng chung
    }
}
