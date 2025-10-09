package io.github.some_example_name.screen.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import io.github.some_example_name.entities.tower.Tower;
import io.github.some_example_name.entities.tower.TowerType;
import io.github.some_example_name.utils.Currency;
import io.github.some_example_name.utils.NumberRenderer;

public class TowerMenu {
    private Vector2 position;
    private boolean visible;
    private Texture[] options;
    private float width;
    private float height;
    private float iconSize;
    private float padding;
    private int optionType;  // 0: top, 1: right, 2: bottom, 3: left

    private TowerType towerType;  // Loại tháp cho menu này
    private BitmapFont font;  // Font để vẽ text

    public TowerMenu(BitmapFont font) {
        this.font = font;
        position = new Vector2();
        visible = false;
        iconSize = 80;  // Tăng kích thước icon
        padding = 15;  // Tăng padding
        width = iconSize + 2 * padding;
        height = iconSize + 2 * padding;

        // Load textures
        options = new Texture[1];  // Chỉ 1 lựa chọn cho mỗi hướng
    }

    public void show(float x, float y) {
        position.set(x, y);
        visible = true;
    }

    public void hide() {
        visible = false;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setOptionType(int type) {
        this.optionType = type;
        // Thiết lập loại tháp dựa vào hướng
        switch (type) {
            case 0: // Top - Stone Tower
                towerType = TowerType.STONE_TOWER;
                options[0] = new Texture("towers/stone/level1/3.png");
                break;
            case 1: // Right - Fire Tower
                towerType = TowerType.FIRE_TOWER;
                options[0] = new Texture("towers/fire/level1/12.png");
                break;
            case 2: // Bottom - BigLand Tower
                towerType = TowerType.BIGLAND_TOWER;
                options[0] = new Texture("towers/bigLand/level1/24.png");
                break;
            case 3: // Left - Land Tower
                towerType = TowerType.LAND_TOWER;
                options[0] = new Texture("towers/land/level1/15.png");
                break;
        }
    }

    public TowerType getTowerType() {
        return towerType;
    }

    public void render(SpriteBatch batch) {
        if (!visible) return;

        if (options[0] != null) {
            float x = position.x + padding;
            float y = position.y + padding;

            // Vẽ icon
            batch.setColor(1, 1, 1, 1);  // Màu trắng cho icon
            batch.draw(options[0], x, y, iconSize, iconSize);

            // Chỉ hiển thị giá tiền nếu không phải là option bên trái (optionType != 3)
            if (optionType != 3) {
                // Lấy giá tiền dựa vào loại tháp
                int cost = Currency.getCost(towerType);

                // Vẽ giá tiền bên dưới icon
                float coinSize = 20;
                float textOffset = 5;

                // Vẽ icon coin từ lề trái
                batch.setColor(1, 1, 1, 1);  // Màu trắng cho icon
                batch.draw(Currency.getCoinTexture(), x, y - 25, coinSize, coinSize);

                // Đổi màu text dựa vào khả năng mua
                if (Currency.canAfford(towerType)) {
                    batch.setColor(0, 1, 0, 1);  // Màu xanh nếu đủ tiền
                } else {
                    batch.setColor(1, 0, 0, 1);  // Màu đỏ nếu không đủ tiền
                }

                // Vẽ số tiền bằng texture, cùng kích thước với coin
                NumberRenderer.drawNumber(batch, cost, x + coinSize - 5, y - 25, coinSize);
                batch.setColor(1, 1, 1, 1);  // Reset màu
            }
        }
    }

    public int checkClick(float x, float y) {
        if (!visible) return -1;

        // Kiểm tra xem click có trong menu không
        if (x < position.x || x > position.x + width ||
            y < position.y || y > position.y + height) {
            return -1;
        }

        // Kiểm tra xem click có trong vùng icon không
        float iconX = position.x + padding;
        float iconY = position.y + padding;
        if (x >= iconX && x <= iconX + iconSize &&
            y >= iconY && y <= iconY + iconSize) {
            return optionType;  // Trả về loại menu (hướng) nếu click vào icon
        }

        return -1;
    }

    public void dispose() {
        if (options != null) {
            for (Texture texture : options) {
                if (texture != null) {
                    texture.dispose();
                }
            }
        }
    }
}
