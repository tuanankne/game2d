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
import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class MenuScreen implements Screen {
    final Main game;
    private SpriteBatch batch;
    private BitmapFont font;
    private Stage stage;
    private TextButton startButton;
    private Table table;
    private String title = "My Game";
    private String startText = "Tap to Start";
    private Texture background;
    private Label comingSoonLabel;

    public MenuScreen(final Main game) {
        this.game = game;
        batch = new SpriteBatch();
        font = new BitmapFont();
        background = new Texture(Gdx.files.internal("background.png"));
        stage = new Stage();
        Gdx.input.setInputProcessor(stage);
        // Tạo nút không dùng skin
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = font;
        // Tạo nền nổi bật cho nút bằng cách vẽ hình chữ nhật màu vàng
        style.up = new com.badlogic.gdx.scenes.scene2d.utils.Drawable() {
            @Override
            public void draw(com.badlogic.gdx.graphics.g2d.Batch batch, float x, float y, float width, float height) {
                batch.end();
                com.badlogic.gdx.graphics.glutils.ShapeRenderer shapeRenderer = new com.badlogic.gdx.graphics.glutils.ShapeRenderer();
                shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
                shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(1f, 0.85f, 0.2f, 1f); // vàng nổi bật
                shapeRenderer.rect(x, y, width, height);
                shapeRenderer.end();
                batch.begin();
            }
            @Override public float getLeftWidth() { return 0; }
            @Override public void setLeftWidth(float width) {}
            @Override public float getRightWidth() { return 0; }
            @Override public void setRightWidth(float width) {}
            @Override public float getTopHeight() { return 0; }
            @Override public void setTopHeight(float height) {}
            @Override public float getBottomHeight() { return 0; }
            @Override public void setBottomHeight(float height) {}
            @Override public float getMinWidth() { return 0; }
            @Override public void setMinWidth(float width) {}
            @Override public float getMinHeight() { return 0; }
            @Override public void setMinHeight(float height) {}
        };
        startButton = new TextButton("START GAME", style);
        // Nút to, chữ to, nổi bật
        startButton.setSize(700, 220);
        startButton.getLabel().setFontScale(3f);
        startButton.getLabel().setColor(0,0,0,1); // Chữ đen nổi bật trên nền vàng
        startButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                comingSoonLabel.setVisible(true);
                game.startGame();
            }
        });

        // Thêm label cho dòng chữ "Group Killler KMA"
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        Label groupLabel = new Label("Group Killler KMA", labelStyle);
        groupLabel.setFontScale(3.2f);
        groupLabel.setColor(1, 1, 1, 1); // Màu trắng

        // Label Coming Soon (ẩn mặc định)
        comingSoonLabel = new Label("ComingSoon....", new Label.LabelStyle(font, com.badlogic.gdx.graphics.Color.WHITE));
        comingSoonLabel.setFontScale(2.5f);
        comingSoonLabel.setVisible(false);

        table = new Table();
        table.setFillParent(true);
        table.center(); // căn giữa cả bảng
        table.add(groupLabel).padBottom(60f).center().row();
        table.add(startButton).width(750).height(250).center().row();
        table.add(comingSoonLabel).padTop(40f).center();
        stage.addActor(table);
    }

    @Override
    public void show() {}

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
    public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        stage.dispose();
        background.dispose();
    }
}
