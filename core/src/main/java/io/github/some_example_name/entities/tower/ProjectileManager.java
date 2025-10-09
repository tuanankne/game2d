package io.github.some_example_name.entities.tower;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

public class ProjectileManager {

    // Danh sách TẤT CẢ các viên đạn đang bay trong game
    private static Array<Projectile> projectiles = new Array<>();

    /**
     * Thêm một viên đạn mới vào hệ thống để quản lý.
     * @param projectile Viên đạn được tạo ra bởi một Tháp.
     */
    public static void addProjectile(Projectile projectile) {
        projectiles.add(projectile);
    }

    /**
     * Cập nhật logic cho tất cả các viên đạn (di chuyển, kiểm tra va chạm).
     * Hàm này nên được gọi MỘT LẦN trong hàm update() chính của GameScreen.
     * @param delta Thời gian giữa các frame.
     */
    public static void update(float delta) {
        for (int i = projectiles.size - 1; i >= 0; i--) {
            Projectile p = projectiles.get(i);
            p.update(delta);
            if (!p.isActive()) {
                projectiles.removeIndex(i);
                p.dispose(); // Giải phóng tài nguyên của viên đạn
            }
        }
    }

    /**
     * Vẽ tất cả các viên đạn lên màn hình.
     * Hàm này nên được gọi MỘT LẦN trong hàm render() chính của GameScreen.
     * @param batch Dùng chung SpriteBatch của game.
     */
    public static void render(SpriteBatch batch) {
        for (Projectile p : projectiles) {
            p.render(batch);
        }
    }

    /**
     * Giải phóng tài nguyên của tất cả các viên đạn còn lại khi thoát game.
     */
    public static void dispose() {
        for (Projectile p : projectiles) {
            p.dispose();
        }
        projectiles.clear();
    }
}
