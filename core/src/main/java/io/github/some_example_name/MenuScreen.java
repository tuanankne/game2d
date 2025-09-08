package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.audio.Music;

public class MenuScreen implements Screen {
    final Main game;
    private SpriteBatch batch;
    private BitmapFont font;
    private BitmapFont buttonFont;
    private Stage stage;
    private TextButton playButton;
    private TextButton settingsButton;
    private Table table;
    private Texture background;
    private java.util.List<Texture> buttonTextures;
    private Texture titleTexture;
    private Image titleImage;
    private Music menuMusic;

    public MenuScreen(final Main game) {
        this.game = game;
        batch = new SpriteBatch();
        font = loadUiFont();
        buttonFont = loadButtonFont();
        background = loadBackground();
        stage = new Stage();
        Gdx.input.setInputProcessor(stage);
        // Style nút: ưu tiên PNG (Menu/btn_up.png, btn_over.png, btn_down.png); nếu thiếu thì dùng style phẳng
        TextButton.TextButtonStyle buttonStyle = createButtonStyleFromPng(
                "Menu/btn_up.png",
                "Menu/btn_over.png",
                "Menu/btn_down.png"
        );
        if (buttonStyle == null) {
            buttonStyle = new TextButton.TextButtonStyle();
            buttonStyle.font = buttonFont != null ? buttonFont : font;
            TextureRegionDrawable up = singleColor(0.15f, 0.15f, 0.18f, 0.90f);
            TextureRegionDrawable over = singleColor(0.20f, 0.20f, 0.24f, 0.95f);
            TextureRegionDrawable down = singleColor(0.10f, 0.10f, 0.12f, 1.00f);
            buttonStyle.up = up;
            buttonStyle.over = over;
            buttonStyle.down = down;
        } else {
            buttonStyle.font = buttonFont != null ? buttonFont : font;
        }

        playButton = new TextButton("Play", buttonStyle);
        settingsButton = new TextButton("Settings", buttonStyle);

        // Kích thước nút lớn, cân đối theo màn hình
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();
        // Giảm một nửa chiều rộng, tăng gấp đôi chiều cao
        float btnWidth = Math.max(Math.min(screenW * 0.49f, 1000f), 500f);
        float btnHeight = Math.max(Math.min(screenH * 0.36f, 520f), 280f);
        playButton.getLabel().setFontScale(1.25f);
        settingsButton.getLabel().setFontScale(1.25f);

        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.startGame();
            }
        });

        // Bỏ label tiêu đề chữ, ưu tiên ảnh title nếu có

        // Ảnh tiêu đề nếu có
        if (Gdx.files.internal("Menu/title.png").exists()) {
            titleTexture = new Texture(Gdx.files.internal("Menu/title.png"));
            titleTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            titleImage = new Image(new TextureRegionDrawable(new TextureRegion(titleTexture)));
            titleImage.setScaling(Scaling.fit);
        }

        table = new Table();
        table.setFillParent(true);
        table.top(); // căn từ trên xuống như mock
        table.padTop(screenH * 0.02f).padBottom(screenH * 1.5f);
        // Tính lại chiều cao title để luôn hiển thị hết trên màn hình
        float minSpacing = btnHeight * 0.05f; // đệm tối thiểu giữa các phần
        float neededForButtons = btnHeight * 3f + minSpacing * 2f; // 2 nút + đệm
        float titleReservedH = Math.min(Math.max(screenH - neededForButtons, screenH * 0.28f), screenH * 0.40f);
        if (titleImage != null) {
            float titleMaxW = Math.min(screenW * 0.95f, 1600f);
            table.add(titleImage).width(titleMaxW).height(titleReservedH).center().row();
        } else {
            // nếu không có ảnh title, dành chỗ rỗng tương đương
            table.add().height(titleReservedH).center().row();
        }
        // Spacer giữa title và nút đầu
        table.add().height(minSpacing * 0.02f).row();
        // Hai nút đặt giữa theo chiều ngang
        table.add(playButton).width(btnWidth).height(btnHeight).center().row();
        // Spacer giữa hai nút (kéo nút dưới lên sát nhất có thể)
        table.add().height(minSpacing * 0.002f ).row();
        table.add(settingsButton).width(btnWidth).height(btnHeight).center().row();
        stage.addActor(table);

        // Load and setup menu music
        if (Gdx.files.internal("Music/Menu.mp3").exists()) {
            menuMusic = Gdx.audio.newMusic(Gdx.files.internal("Music/Menu.mp3"));
            menuMusic.setLooping(true);
            menuMusic.setVolume(10f);
            menuMusic.play();
        }
    }

    @Override
    public void show() {
        if (menuMusic != null) {
            menuMusic.play();
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        if (menuMusic != null) {
            menuMusic.pause();
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        if (font != null) font.dispose();
        if (buttonFont != null) buttonFont.dispose();
        stage.dispose();
        background.dispose();
        if (titleTexture != null) titleTexture.dispose();
        if (buttonTextures != null) {
            for (Texture t : buttonTextures) t.dispose();
        }
        if (menuMusic != null) {
            menuMusic.stop();
            menuMusic.dispose();
        }
    }

    private Texture loadBackground() {
        if (Gdx.files.internal("background.png").exists()) {
            return new Texture(Gdx.files.internal("background.png"));
        }
        if (Gdx.files.internal("Menu/background.jpg").exists()) {
            return new Texture(Gdx.files.internal("Menu/background.jpg"));
        }
        return new Texture(Gdx.files.internal("libgdx.png"));
    }

    private BitmapFont loadUiFont() {
        try {
            if (Gdx.files.internal("fonts/menu.ttf").exists()) {
                FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/menu.ttf"));
                FreeTypeFontParameter p = new FreeTypeFontParameter();
                p.size = Math.round(Math.min(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()) * 0.05f);
                p.characters = FreeTypeFontGenerator.DEFAULT_CHARS;
                BitmapFont f = gen.generateFont(p);
                gen.dispose();
                return f;
            }
        } catch (Throwable ignored) {}
        return new BitmapFont();
    }

    private BitmapFont loadButtonFont() {
        try {
            if (Gdx.files.internal("fonts/menu.ttf").exists()) {
                FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/menu.ttf"));
                FreeTypeFontParameter p = new FreeTypeFontParameter();
                p.size = Math.round(Math.min(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()) * 0.065f);
                p.characters = FreeTypeFontGenerator.DEFAULT_CHARS;
                BitmapFont f = gen.generateFont(p);
                gen.dispose();
                return f;
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private TextButton.TextButtonStyle createButtonStyleFromPng(String upPath, String overPath, String downPath) {
        try {
            boolean upExists = Gdx.files.internal(upPath).exists();
            boolean overExists = Gdx.files.internal(overPath).exists();
            boolean downExists = Gdx.files.internal(downPath).exists();
            if (!upExists && !downExists && !overExists) return null;

            TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
            if (buttonTextures == null) buttonTextures = new java.util.ArrayList<Texture>();
            if (upExists) {
                Texture t = new Texture(Gdx.files.internal(upPath));
                t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                buttonTextures.add(t);
                style.up = new TextureRegionDrawable(new TextureRegion(t));
            }
            if (overExists) {
                Texture t = new Texture(Gdx.files.internal(overPath));
                t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                buttonTextures.add(t);
                style.over = new TextureRegionDrawable(new TextureRegion(t));
            }
            if (downExists) {
                Texture t = new Texture(Gdx.files.internal(downPath));
                t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                buttonTextures.add(t);
                style.down = new TextureRegionDrawable(new TextureRegion(t));
            }
            if (style.over == null) style.over = style.down != null ? style.down : style.up;
            if (style.down == null) style.down = style.up;
            return style;
        } catch (Throwable t) {
            return null;
        }
    }

    private TextureRegionDrawable singleColor(float r, float g, float b, float a) {
        com.badlogic.gdx.graphics.Pixmap pm = new com.badlogic.gdx.graphics.Pixmap(8, 8, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pm.setColor(r, g, b, a);
        pm.fill();
        Texture tex = new Texture(pm);
        pm.dispose();
        return new TextureRegionDrawable(new TextureRegion(tex));
    }
}
