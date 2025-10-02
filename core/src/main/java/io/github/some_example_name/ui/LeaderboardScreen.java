package io.github.some_example_name.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.some_example_name.Main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Màn hình xếp hạng hiển thị 8 điểm cao nhất theo thời gian
 */
public class LeaderboardScreen implements Screen {
    private final Main game;
    private final SpriteBatch batch;
    private final OrthographicCamera camera;
    private final Viewport viewport;

    private Texture backgroundTexture;
    private Texture backButtonTexture;

    // Danh sách điểm cao
    private List<Float> highScores;
    private float currentScore;

    // Nút back
    private float backButtonX, backButtonY, backButtonW, backButtonH;

    public LeaderboardScreen(Main game, float currentScore) {
        this.game = game;
        this.currentScore = currentScore;
        this.batch = new SpriteBatch();
        this.camera = new OrthographicCamera();
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        this.viewport = new FitViewport(w, h, camera);
        this.viewport.apply(true);
        camera.position.set(w / 2f, h / 2f, 0);

        // Load textures
        backgroundTexture = Gdx.files.internal("thuchanh/background5.jpg").exists()
                ? new Texture(Gdx.files.internal("thuchanh/background5.jpg"))
                : new Texture(Gdx.files.internal("thuchanh/taudich1.png"));
        backButtonTexture = Gdx.files.internal("thuchanh/home.png").exists()
                ? new Texture(Gdx.files.internal("thuchanh/home.png")) : null;

        // Load và cập nhật high scores
        loadHighScores();
        updateHighScores();
        saveHighScores();

        // Thiết lập nút back
        backButtonW = 120f;
        backButtonH = 120f;
        backButtonX = 50f;
        backButtonY = 50f;
    }

    private void loadHighScores() {
        highScores = new ArrayList<>();
        // Tạm thời khởi tạo với một số điểm mẫu
        highScores.add(120.5f);
        highScores.add(95.2f);
        highScores.add(87.8f);
        highScores.add(76.3f);
        highScores.add(65.1f);
        highScores.add(54.7f);
        highScores.add(43.9f);
        highScores.add(32.4f);

        // TODO: Load từ file hoặc database thực tế
    }

    private void updateHighScores() {
        highScores.add(currentScore);
        Collections.sort(highScores, Collections.reverseOrder()); // Sắp xếp giảm dần
        if (highScores.size() > 8) {
            highScores = highScores.subList(0, 8); // Chỉ giữ 8 điểm cao nhất
        }
    }

    private void saveHighScores() {
        // TODO: Lưu vào file hoặc database thực tế
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Vẽ nền
        float bgW = viewport.getWorldWidth();
        float bgH = viewport.getWorldHeight();
        batch.draw(backgroundTexture, 0, 0, bgW, bgH);

        // Vẽ tiêu đề
        game.font.getData().setScale(3f);
        String title = "HIGH SCORES";
        GlyphLayout titleLayout = new GlyphLayout();
        titleLayout.setText(game.font, title);
        float titleWidth = titleLayout.width;
        float titleX = (viewport.getWorldWidth() - titleWidth) / 2f;
        float titleY = viewport.getWorldHeight() - 100f;
        game.font.draw(batch, title, titleX, titleY);

        // Vẽ danh sách điểm
        game.font.getData().setScale(2f);
        float startY = titleY - 80f;
        float lineHeight = 60f;

        for (int i = 0; i < highScores.size(); i++) {
            float score = highScores.get(i);
            String scoreText = String.format("%d. %s", i + 1, formatTime(score));

            // Highlight điểm hiện tại
            if (Math.abs(score - currentScore) < 0.1f) {
                batch.setColor(1f, 1f, 0f, 1f); // Màu vàng
            } else {
                batch.setColor(1f, 1f, 1f, 1f); // Màu trắng
            }

            GlyphLayout scoreLayout = new GlyphLayout();
            scoreLayout.setText(game.font, scoreText);
            float scoreX = (viewport.getWorldWidth() - scoreLayout.width) / 2f;
            float scoreY = startY - i * lineHeight;
            game.font.draw(batch, scoreText, scoreX, scoreY);
        }
        batch.setColor(1f, 1f, 1f, 1f); // Reset màu

        // Vẽ nút back
        if (backButtonTexture != null) {
            batch.draw(backButtonTexture, backButtonX, backButtonY, backButtonW, backButtonH);
        }

        batch.end();

        // Xử lý input
        if (Gdx.input.justTouched()) {
            Vector3 tp = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            Vector3 world = camera.unproject(tp);
            float x = world.x, y = world.y;

            if (inside(x, y, backButtonX, backButtonY, backButtonW, backButtonH)) {
                game.setScreen(new io.github.some_example_name.screen.menu.MenuScreen(game));
            }
        }
    }

    private boolean inside(float x, float y, float rx, float ry, float rw, float rh) {
        return x >= rx && x <= rx + rw && y >= ry && y <= ry + rh;
    }

    private String formatTime(float seconds) {
        int total = Math.max(0, (int)seconds);
        int m = total / 60;
        int s = total % 60;
        return (m < 10 ? "0"+m : String.valueOf(m)) + ":" + (s < 10 ? "0"+s : String.valueOf(s));
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        batch.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (backButtonTexture != null) backButtonTexture.dispose();
    }
}

