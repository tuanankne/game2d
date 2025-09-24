package io.github.some_example_name.screen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;

import io.github.some_example_name.Main;
import io.github.some_example_name.screen.menu.MenuScreen;

public class GameOverScreen implements Screen {
    private final Main game;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private Stage stage;
    private BitmapFont font;
    private BitmapFont titleFont;

    public GameOverScreen(final Main game) {
        this.game = game;
        this.batch = new SpriteBatch();
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, 800, 480);

        // Tạo viewport và stage
        FitViewport viewport = new FitViewport(800, 480, camera);
        stage = new Stage(viewport, batch);
        Gdx.input.setInputProcessor(stage);

        // Tạo font cho tiêu đề
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/menu.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 48;
        parameter.color = Color.RED;
        titleFont = generator.generateFont(parameter);

        // Tạo font cho buttons
        parameter.size = 24;
        parameter.color = Color.WHITE;
        font = generator.generateFont(parameter);
        generator.dispose();

        createUI();
    }

    private void createUI() {
        Table table = new Table();
        table.setFillParent(true);

        // Tạo style cho label
        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, Color.RED);
        Label gameOverLabel = new Label("GAME OVER", titleStyle);

        // Tạo style cho buttons
        TextButtonStyle buttonStyle = new TextButtonStyle();
        buttonStyle.font = font;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.downFontColor = Color.LIGHT_GRAY;

        // Tạo các buttons
        TextButton restartButton = new TextButton("Chơi lại", buttonStyle);
        TextButton menuButton = new TextButton("Trở về Menu", buttonStyle);

        // Thêm listeners cho buttons
        restartButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.startGame();
                dispose();
            }
        });

        menuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new MenuScreen(game));
                dispose();
            }
        });

        // Thêm các elements vào table
        table.add(gameOverLabel).padBottom(50).row();
        table.add(restartButton).padBottom(20).row();
        table.add(menuButton).row();

        stage.addActor(table);
    }

    @Override
    public void render(float delta) {
        // Xóa màn hình với màu đen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
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
        batch.dispose();
        stage.dispose();
        font.dispose();
        titleFont.dispose();
    }
}
