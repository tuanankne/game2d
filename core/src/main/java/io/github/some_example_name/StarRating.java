package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class StarRating {
    private static ShapeRenderer shapeRenderer;
    private static float animationTime = 0;
    private static final float ANIMATION_DURATION = 1.0f; // Thời gian hiệu ứng
    private static final int STAR_POINTS = 5; // Số cánh của sao
    private static final Color STAR_COLOR = new Color(1, 0.8f, 0, 1); // Màu vàng
    private static final Color STAR_OUTLINE = new Color(1, 0.6f, 0, 1); // Màu viền
    private static final Color EMPTY_STAR_COLOR = new Color(0.3f, 0.3f, 0.3f, 1); // Màu xám

    public static void initialize() {
        shapeRenderer = new ShapeRenderer();
    }

    public static int calculateStars(int currentHealth, int maxHealth) {
        float healthPercentage = (float) currentHealth / maxHealth * 100;
        if (healthPercentage >= 100) {
            return 3;
        } else if (healthPercentage >= 50) {
            return 2;
        } else {
            return 1;
        }
    }

    private static void drawStar(float centerX, float centerY, float size, boolean filled, float alpha) {
        float outerRadius = size / 2;
        float innerRadius = outerRadius * 0.4f;
        float glowRadius = outerRadius * 1.2f;
        
        // Tính toán các điểm của sao
        float[] vertices = new float[STAR_POINTS * 4];
        for (int i = 0; i < STAR_POINTS * 2; i += 2) {
            float angle = i * MathUtils.PI / STAR_POINTS - MathUtils.PI / 2;
            float radius = (i % 4 == 0) ? outerRadius : innerRadius;
            vertices[i] = centerX + MathUtils.cos(angle) * radius;
            vertices[i + 1] = centerY + MathUtils.sin(angle) * radius;
        }

        // Vẽ sao với hiệu ứng
        if (filled) {
            // Vẽ hiệu ứng glow
            Color glowColor = new Color(1f, 0.8f, 0f, alpha * 0.3f);
            shapeRenderer.setColor(glowColor);
            shapeRenderer.begin(ShapeType.Filled);
            for (int i = 0; i < STAR_POINTS * 2; i += 2) {
                int nextIndex = (i + 2) % (STAR_POINTS * 2);
                float angle = i * MathUtils.PI / STAR_POINTS - MathUtils.PI / 2;
                float nextAngle = ((i + 2) % (STAR_POINTS * 2)) * MathUtils.PI / STAR_POINTS - MathUtils.PI / 2;
                
                float x1 = centerX + MathUtils.cos(angle) * glowRadius;
                float y1 = centerY + MathUtils.sin(angle) * glowRadius;
                float x2 = centerX + MathUtils.cos(nextAngle) * glowRadius;
                float y2 = centerY + MathUtils.sin(nextAngle) * glowRadius;
                
                shapeRenderer.triangle(centerX, centerY, x1, y1, x2, y2);
            }
            shapeRenderer.end();

            // Vẽ phần trong của sao
            Color fillColor = STAR_COLOR.cpy();
            fillColor.a = alpha;
            shapeRenderer.setColor(fillColor);
            shapeRenderer.begin(ShapeType.Filled);
            for (int i = 0; i < STAR_POINTS * 2; i += 2) {
                int nextIndex = (i + 2) % (STAR_POINTS * 2);
                shapeRenderer.triangle(
                    centerX, centerY,
                    vertices[i], vertices[i + 1],
                    vertices[nextIndex], vertices[nextIndex + 1]
                );
            }
            shapeRenderer.end();

            // Vẽ viền sao
            Color outlineColor = STAR_OUTLINE.cpy();
            outlineColor.a = alpha;
            shapeRenderer.setColor(outlineColor);
            shapeRenderer.begin(ShapeType.Line);
            for (int i = 0; i < STAR_POINTS * 2; i += 2) {
                int nextIndex = (i + 2) % (STAR_POINTS * 2);
                shapeRenderer.line(vertices[i], vertices[i + 1], 
                                 vertices[nextIndex], vertices[nextIndex + 1]);
            }
            shapeRenderer.end();

            // Vẽ điểm sáng
            float sparkleSize = size * 0.1f;
            Color sparkleColor = new Color(1f, 1f, 1f, alpha * 0.8f);
            shapeRenderer.setColor(sparkleColor);
            shapeRenderer.begin(ShapeType.Filled);
            shapeRenderer.circle(centerX - size/4, centerY + size/4, sparkleSize);
            shapeRenderer.end();
        } else {
            // Vẽ sao không được fill với hiệu ứng mờ
            Color emptyColor = EMPTY_STAR_COLOR.cpy();
            emptyColor.a = alpha * 0.6f;
            shapeRenderer.setColor(emptyColor);
            shapeRenderer.begin(ShapeType.Line);
            for (int i = 0; i < STAR_POINTS * 2; i += 2) {
                int nextIndex = (i + 2) % (STAR_POINTS * 2);
                shapeRenderer.line(vertices[i], vertices[i + 1], 
                                 vertices[nextIndex], vertices[nextIndex + 1]);
            }
            shapeRenderer.end();
        }
    }

    public static void render(SpriteBatch batch, int stars, float centerX, float centerY, float size, boolean animate) {
        if (animate) {
            animationTime += Gdx.graphics.getDeltaTime();
        }
        
        float spacing = size * 1.5f;
        float totalWidth = spacing * 3;
        float startX = centerX - totalWidth / 2 + size / 2;

        // Bật blend để có hiệu ứng trong suốt
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        Gdx.gl.glBlendFunc(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA);

        // Thiết lập projection matrix cho shapeRenderer
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());

        // Vẽ các sao
        for (int i = 0; i < 3; i++) {
            float alpha = animate ? 
                MathUtils.clamp((animationTime - i * 0.3f) / ANIMATION_DURATION, 0, 1) : 1;
            drawStar(startX + spacing * i, centerY, size, i < stars, alpha);
        }

        // Tắt blend
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);
    }

    public static void dispose() {
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }

    public static void resetAnimation() {
        animationTime = 0;
    }
}