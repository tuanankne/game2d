package io.github.some_example_name.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Color;

public class GameControls {
    private static ShapeRenderer shapeRenderer;
    private static final float BUTTON_SIZE = 40;
    private static final float PADDING = 10;
    public static boolean isPaused = false;
    private static int speedIndex = 1; // 0: 0.5x, 1: 1x, 2: 2x, 3: 3x
    private static final float[] SPEED_VALUES = {0.5f, 1.0f, 2.0f, 3.0f};
    private static final String[] SPEED_TEXTS = {"0.5X", "1X", "2X", "3X"};
    private static final Color BUTTON_COLOR = new Color(0.2f, 0.2f, 0.2f, 0.8f);
    private static final Color BUTTON_HOVER_COLOR = new Color(0.3f, 0.3f, 0.3f, 0.9f);
    private static final Color BUTTON_ACTIVE_COLOR = new Color(0.4f, 0.4f, 0.4f, 1f);
    private static final Color PAUSE_SYMBOL_COLOR = new Color(1f, 0.8f, 0.2f, 1f);
    private static final Color SPEED_SYMBOL_COLOR = new Color(0.2f, 1f, 0.4f, 1f);

    public static void initialize() {
        shapeRenderer = new ShapeRenderer();
    }

    public static void render(SpriteBatch batch, BitmapFont font) {
        float screenWidth = Gdx.graphics.getWidth();
        float y = Gdx.graphics.getHeight() - BUTTON_SIZE - PADDING;
        float pauseX = screenWidth - 2 * (BUTTON_SIZE + PADDING);
        float speedX = screenWidth - (BUTTON_SIZE + PADDING);

        // Kết thúc SpriteBatch để vẽ shapes
        batch.end();

        // Bật blend
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        Gdx.gl.glBlendFunc(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Vẽ nút pause
        shapeRenderer.setColor(isPaused ? BUTTON_ACTIVE_COLOR : BUTTON_COLOR);
        shapeRenderer.rect(pauseX, y, BUTTON_SIZE, BUTTON_SIZE);

        // Vẽ biểu tượng pause
        shapeRenderer.setColor(PAUSE_SYMBOL_COLOR);
        if (isPaused) {
            // Vẽ biểu tượng play khi đang pause
            float triangleSize = BUTTON_SIZE * 0.5f;
            float centerX = pauseX + BUTTON_SIZE / 2;
            float centerY = y + BUTTON_SIZE / 2;
            float[] trianglePoints = new float[] {
                centerX - triangleSize/3, centerY - triangleSize/2,
                centerX - triangleSize/3, centerY + triangleSize/2,
                centerX + triangleSize/2, centerY
            };
            shapeRenderer.triangle(
                trianglePoints[0], trianglePoints[1],
                trianglePoints[2], trianglePoints[3],
                trianglePoints[4], trianglePoints[5]
            );
        } else {
            // Vẽ biểu tượng pause
            float barWidth = BUTTON_SIZE * 0.15f;
            float barHeight = BUTTON_SIZE * 0.5f;
            float spacing = BUTTON_SIZE * 0.2f;
            shapeRenderer.rect(pauseX + (BUTTON_SIZE - spacing - 2*barWidth)/2, y + (BUTTON_SIZE - barHeight)/2, barWidth, barHeight);
            shapeRenderer.rect(pauseX + (BUTTON_SIZE + spacing)/2, y + (BUTTON_SIZE - barHeight)/2, barWidth, barHeight);
        }

        // Vẽ nút tốc độ
        shapeRenderer.setColor(BUTTON_COLOR);
        shapeRenderer.rect(speedX, y, BUTTON_SIZE, BUTTON_SIZE);

        // Vẽ biểu tượng tốc độ (mũi tên)
        shapeRenderer.setColor(SPEED_SYMBOL_COLOR);
        float arrowSize = BUTTON_SIZE * 0.4f;
        float centerX = speedX + BUTTON_SIZE / 2;
        float centerY = y + BUTTON_SIZE / 2;

        // Vẽ số mũi tên tương ứng với tốc độ
        for (int i = 0; i <= speedIndex; i++) {
            float offset = (i - speedIndex/2f) * (arrowSize/2);
            float[] arrowPoints = new float[] {
                centerX - arrowSize/2 + offset, centerY,
                centerX + offset, centerY + arrowSize/2,
                centerX + offset, centerY - arrowSize/2
            };
            shapeRenderer.triangle(
                arrowPoints[0], arrowPoints[1],
                arrowPoints[2], arrowPoints[3],
                arrowPoints[4], arrowPoints[5]
            );
        }

        shapeRenderer.end();

        // Tắt blend
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);

        // Bật lại SpriteBatch để vẽ text
        batch.begin();

        // Vẽ text tốc độ
        font.setColor(Color.WHITE);
        font.draw(batch, SPEED_TEXTS[speedIndex],
                 screenWidth - (BUTTON_SIZE + PADDING) + BUTTON_SIZE/4,
                 y + BUTTON_SIZE + PADDING);
    }

    public static int handleClick(float x, float y) {
        float screenWidth = Gdx.graphics.getWidth();
        float buttonY = Gdx.graphics.getHeight() - BUTTON_SIZE - PADDING;

        // Check pause button
        if (x >= screenWidth - 2 * (BUTTON_SIZE + PADDING) &&
            x <= screenWidth - (BUTTON_SIZE + 2 * PADDING) &&
            y >= buttonY && y <= buttonY + BUTTON_SIZE) {
            isPaused = !isPaused;
            // Pause/Resume nhạc nền
            if (isPaused) {
                GameSoundManager.pauseBackgroundMusic();
            } else {
                GameSoundManager.resumeBackgroundMusic();
            }
            return 1; // Pause button clicked
        }

        // Check speed button
        if (x >= screenWidth - (BUTTON_SIZE + PADDING) &&
            x <= screenWidth - PADDING &&
            y >= buttonY && y <= buttonY + BUTTON_SIZE) {
            speedIndex = (speedIndex + 1) % SPEED_VALUES.length;
            return 2; // Speed button clicked
        }

        return 0; // No button clicked
    }

    public static float getGameSpeed() {
        return isPaused ? 0 : SPEED_VALUES[speedIndex];
    }

    public static boolean isPaused() {
        return isPaused;
    }

    public static void setPaused(boolean paused) {
        isPaused = paused;
    }

    public static void resetSpeed() {
        speedIndex = 1; // Reset về tốc độ bình thường (1x)
    }

    public static void increaseSpeed() {
        if (speedIndex < SPEED_VALUES.length - 1) {
            speedIndex++;
        }
    }

    public static void decreaseSpeed() {
        if (speedIndex > 0) {
            speedIndex--;
        }
    }

    public static float getCurrentSpeedValue() {
        return SPEED_VALUES[speedIndex];
    }

    public static String getCurrentSpeedText() {
        return SPEED_TEXTS[speedIndex];
    }

    public static void dispose() {
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
            shapeRenderer = null;
        }
    }
}
