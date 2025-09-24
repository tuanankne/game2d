package io.github.some_example_name.screen.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

// Lớp quản lý hiển thị overlay và menu khi chọn ô
public class TileSelector {
    private Texture selectedTileTexture;  // Texture cho ô được chọn
    private Vector2 selectedTile;         // Vị trí ô được chọn
    private boolean isVisible;            // Trạng thái hiển thị
    private int tileWidth;               // Chiều rộng của một ô
    private int tileHeight;              // Chiều cao của một ô

    public TileSelector(int tileWidth, int tileHeight) {
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        selectedTileTexture = new Texture("map1/towerDefense_tile018.png"); // Ảnh overlay màu xanh nhạt
        selectedTile = new Vector2(-1, -1);
        isVisible = false;
    }

    // Hiển thị overlay tại ô được chọn
    public void selectTile(int tileX, int tileY) {
        selectedTile.set(tileX, tileY);
        isVisible = true;
    }

    // Lấy tọa độ X của ô đã chọn
    public float getSelectedTileX() {
        return selectedTile.x;
    }

    // Lấy tọa độ Y của ô đã chọn
    public float getSelectedTileY() {
        return selectedTile.y;
    }

    // Ẩn overlay
    public void hide() {
        isVisible = false;
    }

    // Lấy vị trí hiển thị menu cho mỗi hướng
    public Vector2 getMenuPosition(int direction) {
        if (!isVisible) return null;

        float worldX = selectedTile.x * tileWidth;
        float worldY = selectedTile.y * tileHeight;
        float offset = tileWidth / 2;  // Khoảng cách từ tâm ô đến menu

        switch (direction) {
            case 0: // Phía trên
                return new Vector2(worldX + tileWidth/2 - offset/2, worldY + tileHeight + 5);
            case 1: // Bên phải
                return new Vector2(worldX + tileWidth + 5, worldY + tileHeight/2 - offset/2);
            case 2: // Phía dưới
                return new Vector2(worldX + tileWidth/2 - offset/2, worldY - tileHeight - 5);
            case 3: // Bên trái
                return new Vector2(worldX - tileWidth - 5, worldY + tileHeight/2 - offset/2);
            default:
                return null;
        }
    }

    // Vẽ overlay
    public void render(SpriteBatch batch) {
        if (!isVisible) return;
        batch.draw(selectedTileTexture,
            selectedTile.x * tileWidth,
            selectedTile.y * tileHeight,
            tileWidth, tileHeight);
    }

    // Giải phóng tài nguyên
    public void dispose() {
        if (selectedTileTexture != null) {
            selectedTileTexture.dispose();
        }
    }
}
