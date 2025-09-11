package io.github.some_example_name.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.Gdx;

public class MenuButton {
    private Texture texture;
    private float x, y, width, height;
    private String text;
    private Rectangle bounds;
    private boolean isHovered;
    private GlyphLayout layout;

    public MenuButton(Texture texture, float x, float y, float width, float height, String text) {
        this.texture = texture;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.text = text;
        this.bounds = new Rectangle(x, y, width, height);
        this.layout = new GlyphLayout();
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        this.bounds.x = x;
        this.bounds.y = y;
    }

    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
        this.bounds.width = width;
        this.bounds.height = height;
    }

    public void update() {
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
        isHovered = bounds.contains(mouseX, mouseY);
    }

    public void draw(SpriteBatch batch, BitmapFont font) {
        // Vẽ nền nút
        batch.draw(texture, x, y, width, height);

        // Tính toán kích thước text và vẽ
        layout.setText(font, text);
        float offsetY = height * 0.04f;
        font.draw(batch, text,
                x + (width - layout.width) / 2,
                y + (height + layout.height) / 2 + offsetY);
    }

    public boolean isClicked() {
        return isHovered && Gdx.input.justTouched();
    }

    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }
}
