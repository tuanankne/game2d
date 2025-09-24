package io.github.some_example_name.screen.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

public class AnimatedBackground {
    private Array<Texture> frames;
    private float frameTime; // Thời gian mỗi frame
    private float currentFrameTime;
    private int currentFrameIndex;
    private float width, height;

    public AnimatedBackground(String[] frameFiles, float frameTime) {
        frames = new Array<>();
        this.frameTime = frameTime;
        this.currentFrameTime = 0;
        this.currentFrameIndex = 0;

        // Load tất cả các texture
        for (String file : frameFiles) {
            Texture texture = new Texture(Gdx.files.internal(file));
            frames.add(texture);
        }

        // Lấy kích thước màn hình
        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();
    }

    public void update(float delta) {
        currentFrameTime += delta;
        if (currentFrameTime >= frameTime) {
            currentFrameTime = 0;
            currentFrameIndex = (currentFrameIndex + 1) % frames.size;
        }
    }

    public void render(SpriteBatch batch) {
        if (frames.size > 0) {
            batch.draw(frames.get(currentFrameIndex), 0, 0, width, height);
        }
    }

    public void resize(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public void dispose() {
        for (Texture texture : frames) {
            texture.dispose();
        }
    }
}
