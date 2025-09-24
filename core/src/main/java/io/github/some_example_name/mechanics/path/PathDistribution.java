package io.github.some_example_name.mechanics.path;

import com.badlogic.gdx.utils.Array;
import io.github.some_example_name.entities.enemy.Enemy;
import io.github.some_example_name.entities.enemy.EnemyDistribution;

/**
 * Lớp quản lý phân bổ quái cho từng đường đi
 */
public class PathDistribution {
    private Array<EnemyDistribution> pathDistributions; // Phân bổ quái cho mỗi đường
    private Array<Integer> currentNormalCounts; // Số lượng quái thường đã spawn
    private Array<Integer> currentFastCounts;   // Số lượng quái nhanh đã spawn
    private Array<Integer> currentTankCounts;   // Số lượng quái tank đã spawn
    private int currentPathIndex; // Đường đi hiện tại
    private Enemy.Type lastSpawnedType; // Loại quái cuối cùng được spawn

    /**
     * Khởi tạo phân bổ quái với thông tin cụ thể cho từng đường
     * @param distributions Mảng chứa thông tin phân bổ quái cho từng đường
     */
    public PathDistribution(Array<EnemyDistribution> distributions) {
        this.pathDistributions = distributions;
        this.currentNormalCounts = new Array<>();
        this.currentFastCounts = new Array<>();
        this.currentTankCounts = new Array<>();
        this.currentPathIndex = 0;
        this.lastSpawnedType = null;

        // Khởi tạo số lượng đã spawn cho mỗi loại quái trên mỗi đường
        for (int i = 0; i < distributions.size; i++) {
            currentNormalCounts.add(0);
            currentFastCounts.add(0);
            currentTankCounts.add(0);
        }
    }

    /**
     * Lấy thông tin quái tiếp theo cần spawn
     * @return Mảng gồm [chỉ số đường, loại quái], hoặc null nếu đã spawn đủ
     */
    public Object[] getNextSpawn() {
        for (int i = 0; i < pathDistributions.size; i++) {
            int pathIndex = (currentPathIndex + i) % pathDistributions.size;
            EnemyDistribution dist = pathDistributions.get(pathIndex);
            
            // Thử spawn các loại quái theo thứ tự ưu tiên
            Enemy.Type type = null;
            
            // Ưu tiên loại quái chưa được spawn gần đây nhất
            if (lastSpawnedType != Enemy.Type.NORMAL && 
                currentNormalCounts.get(pathIndex) < dist.getNormalCount()) {
                type = Enemy.Type.NORMAL;
                currentNormalCounts.set(pathIndex, currentNormalCounts.get(pathIndex) + 1);
            }
            else if (lastSpawnedType != Enemy.Type.FAST && 
                     currentFastCounts.get(pathIndex) < dist.getFastCount()) {
                type = Enemy.Type.FAST;
                currentFastCounts.set(pathIndex, currentFastCounts.get(pathIndex) + 1);
            }
            else if (lastSpawnedType != Enemy.Type.TANK && 
                     currentTankCounts.get(pathIndex) < dist.getTankCount()) {
                type = Enemy.Type.TANK;
                currentTankCounts.set(pathIndex, currentTankCounts.get(pathIndex) + 1);
            }
            // Nếu không thể spawn loại khác, spawn bất kỳ loại nào còn lại
            else if (currentNormalCounts.get(pathIndex) < dist.getNormalCount()) {
                type = Enemy.Type.NORMAL;
                currentNormalCounts.set(pathIndex, currentNormalCounts.get(pathIndex) + 1);
            }
            else if (currentFastCounts.get(pathIndex) < dist.getFastCount()) {
                type = Enemy.Type.FAST;
                currentFastCounts.set(pathIndex, currentFastCounts.get(pathIndex) + 1);
            }
            else if (currentTankCounts.get(pathIndex) < dist.getTankCount()) {
                type = Enemy.Type.TANK;
                currentTankCounts.set(pathIndex, currentTankCounts.get(pathIndex) + 1);
            }

            if (type != null) {
                lastSpawnedType = type;
                currentPathIndex = (pathIndex + 1) % pathDistributions.size;
                float[] stats = dist.getStats(type);
                return new Object[]{pathIndex, type, stats[0], stats[1]};
            }
        }
        return null; // Không còn quái nào để spawn
    }

    /**
     * Kiểm tra xem đã spawn đủ số lượng quái chưa
     */
    public boolean isComplete() {
        for (int i = 0; i < pathDistributions.size; i++) {
            EnemyDistribution dist = pathDistributions.get(i);
            if (currentNormalCounts.get(i) < dist.getNormalCount() ||
                currentFastCounts.get(i) < dist.getFastCount() ||
                currentTankCounts.get(i) < dist.getTankCount()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Lấy thông tin phân bổ quái của một đường
     */
    public EnemyDistribution getDistribution(int pathIndex) {
        if (pathIndex >= 0 && pathIndex < pathDistributions.size) {
            return pathDistributions.get(pathIndex);
        }
        return null;
    }
}
