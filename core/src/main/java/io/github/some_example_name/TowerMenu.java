package io.github.some_example_name;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

// Menu hiển thị các lựa chọn tháp để xây dựng
public class TowerMenu {
    private Texture tower1Texture;    // Texture cho tháp loại 1
    private Texture tower2Texture;    // Texture cho tháp loại 2
    private Texture tower3Texture;    // Texture cho tháp loại 3
    private Texture cancelTexture;    // Texture cho nút hủy
    private Vector2 position;         // Vị trí hiển thị menu
    private boolean isVisible;        // Trạng thái hiển thị menu
    private float iconSize = 128f;     // Kích thước icon
    private float padding = 5f;       // Khoảng cách giữa các icon

    public TowerMenu() {
        // Tải texture cho các icon
        tower1Texture = new Texture("map1/towerDefense_tile249.png");
        tower2Texture = new Texture("map1/towerDefense_tile203.png");
        tower3Texture = new Texture("map1/towerDefense_tile206.png");
        cancelTexture = new Texture("map1/towerDefense_tile017.png");
        position = new Vector2();
        isVisible = false;
    }

    // Hiển thị menu tại vị trí được chỉ định
    public void show(float x, float y) {
        position.set(x, y);
        isVisible = true;
    }

    // Ẩn menu
    public void hide() {
        isVisible = false;
    }

    // Kiểm tra xem có đang hiển thị menu không
    public boolean isVisible() {
        return isVisible;
    }

    private int optionType; // 0: tower1, 1: tower2, 2: tower3, 3: cancel

    public void setOptionType(int type) {
        this.optionType = type;
    }

    // Vẽ menu
    public void render(SpriteBatch batch) {
        if (!isVisible) return;

        // Vẽ icon tương ứng với loại được chọn
        switch (optionType) {
            case 0:
                batch.draw(tower1Texture, position.x, position.y, iconSize, iconSize);
                break;
            case 1:
                batch.draw(tower2Texture, position.x, position.y, iconSize, iconSize);
                break;
            case 2:
                batch.draw(tower3Texture, position.x, position.y, iconSize, iconSize);
                break;
            case 3:
                batch.draw(cancelTexture, position.x, position.y, iconSize, iconSize);
                break;
        }
    }

    // Kiểm tra xem người dùng có click vào icon không
    public int checkClick(float screenX, float screenY) {
        if (!isVisible) return -1;

        // Chuyển đổi tọa độ màn hình thành tọa độ local của menu
        float localX = screenX - position.x;
        float localY = screenY - position.y;

        // Kiểm tra xem có click vào icon không
        if (localX >= 0 && localX <= iconSize && localY >= 0 && localY <= iconSize) {
            return optionType;  // Trả về loại của icon này
        }

        return -1;  // Không click vào icon
    }

    // Giải phóng tài nguyên
    public void dispose() {
        tower1Texture.dispose();
        tower2Texture.dispose();
        tower3Texture.dispose();
        cancelTexture.dispose();
    }
}
