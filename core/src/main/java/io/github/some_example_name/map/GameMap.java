package io.github.some_example_name.map;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.github.some_example_name.entities.tower.Tower;
import io.github.some_example_name.entities.enemy.Enemy;
import io.github.some_example_name.mechanics.wave.Wave;


/**
 * Interface định nghĩa các phương thức cần thiết cho một map trong game
 */
public interface GameMap {
    /**
     * Khởi tạo map với các tài nguyên cần thiết
     */
    void initialize();

    /**
     * Cập nhật trạng thái của map
     * @param delta Thời gian giữa 2 frame
     */
    void update(float delta);

    /**
     * Vẽ map và các đối tượng trong map
     * @param batch SpriteBatch để vẽ
     */
    void render(SpriteBatch batch);

    /**
     * Lấy danh sách các điểm spawn quái
     * @return Array chứa các điểm spawn
     */
    Array<Vector2> getSpawnPoints();

    /**
     * Lấy đường đi của quái
     * @param spawnPointIndex Index của điểm spawn
     * @return Array chứa các điểm trên đường đi
     */
    Array<Vector2> getPath(int spawnPointIndex);

    /**
     * Kiểm tra xem có thể đặt tháp tại vị trí này không
     * @param x Tọa độ x
     * @param y Tọa độ y
     * @return true nếu có thể đặt tháp
     */
    boolean canPlaceTower(float x, float y);

    /**
     * Thêm tháp vào map
     * @param tower Tháp cần thêm
     * @param x Tọa độ x
     * @param y Tọa độ y
     * @return true nếu thêm thành công
     */
    boolean addTower(Tower tower, float x, float y);

    /**
     * Thêm quái vào map
     * @param enemy Quái cần thêm
     */
    void addEnemy(Enemy enemy);

    /**
     * Lấy danh sách quái đang có trên map
     * @return Array chứa các quái
     */
    Array<Enemy> getEnemies();

    /**
     * Lấy danh sách tháp đang có trên map
     * @return Array chứa các tháp
     */
    Array<Tower> getTowers();

    /**
     * Lấy wave hiện tại
     * @return Wave hiện tại
     */
    Wave getCurrentWave();

    /**
     * Chuyển sang wave tiếp theo
     * @return true nếu còn wave tiếp theo, false nếu đã hết wave
     */
    boolean nextWave();

    /**
     * Giải phóng tài nguyên
     */
    void dispose();
}
