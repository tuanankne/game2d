package io.github.some_example_name.screen.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.Gdx;

public class MenuButton {
    private Texture btnUpTexture;
    private Texture btnDownTexture;
    private Texture btnHoverTexture;
    private float x, y, width, height;
    private String text;
    private Rectangle bounds;
    private boolean isHovered;
    private boolean isPressed;
    private GlyphLayout layout;

    public MenuButton(Texture btnUpTexture, float x, float y, float width, float height, String text) {
        this.btnUpTexture = btnUpTexture;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.text = text;
        this.bounds = new Rectangle(x, y, width, height);
        this.layout = new GlyphLayout();
        this.isHovered = false;
        this.isPressed = false;
        
        // Load các texture khác
        try {
            this.btnDownTexture = new Texture(Gdx.files.internal("Menu/btn_down.png"));
            this.btnHoverTexture = new Texture(Gdx.files.internal("Menu/btn_hover.png"));
        } catch (Exception e) {
            Gdx.app.log("MenuButton", "Could not load btn_down.png or btn_hover.png, using btn_up.png for all states");
            this.btnDownTexture = btnUpTexture;
            this.btnHoverTexture = btnUpTexture;
        }
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        this.bounds.x = x;
        this.bounds.y = y;
        Gdx.app.log("MenuButton", "Button '" + text + "' position set to: " + x + ", " + y + " bounds: " + bounds);
    }

    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
        this.bounds.width = width;
        this.bounds.height = height;
    }
    
    // Phương thức để set bounds riêng biệt (nhỏ hơn kích thước thực)
    public void setBounds(float x, float y, float width, float height) {
        this.bounds.x = x;
        this.bounds.y = y;
        this.bounds.width = width;
        this.bounds.height = height;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void update() {
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
        isHovered = bounds.contains(mouseX, mouseY);
        
        // Kiểm tra trạng thái nhấn
        if (isHovered && Gdx.input.isTouched()) {
            isPressed = true;
        } else {
            isPressed = false;
        }
    }

    public void draw(SpriteBatch batch, BitmapFont font) {
        // Chọn texture dựa trên trạng thái
        Texture currentTexture = btnUpTexture;
        if (isPressed) {
            currentTexture = btnDownTexture;
        } else if (isHovered) {
            currentTexture = btnHoverTexture;
        }
        
        // Vẽ nền nút với texture phù hợp
        batch.draw(currentTexture, x, y, width, height);

        // Tính toán kích thước text và vẽ
        layout.setText(font, text);
        float offsetY = height * 0.04f;
        font.draw(batch, text,
                x + (width - layout.width) / 2,
                y + (height + layout.height) / 2 + offsetY);
    }

    public boolean isClicked() {
        if (Gdx.input.justTouched()) {
            float mouseX = Gdx.input.getX();
            float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
            
            // Kiểm tra click trong vùng bounds
            boolean inBounds = mouseX >= bounds.x && mouseX <= bounds.x + bounds.width &&
                              mouseY >= bounds.y && mouseY <= bounds.y + bounds.height;
            
            if (inBounds) {
                Gdx.app.log("MenuButton", "Button '" + text + "' clicked at (" + mouseX + ", " + mouseY + ") bounds: " + bounds);
                return true;
            }
        }
        return false;
    }

    public void dispose() {
        if (btnUpTexture != null) {
            btnUpTexture.dispose();
        }
        if (btnDownTexture != null && btnDownTexture != btnUpTexture) {
            btnDownTexture.dispose();
        }
        if (btnHoverTexture != null && btnHoverTexture != btnUpTexture) {
            btnHoverTexture.dispose();
        }
    }
}
