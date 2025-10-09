package io.github.some_example_name.entities.tower;


/**
 * Enum này là trung tâm dữ liệu, định nghĩa TẤT CẢ các loại tháp trong game.
 * Mỗi mục enum chứa đường dẫn đến các file ảnh cho 3 cấp độ.
 * Nếu một tháp không có bộ phận nào đó, hãy dùng 'null'.
 */
public enum TowerType {
    STONE_TOWER(
        // basePaths (level1, level2, level3)
        new String[]{"towers/stone/level1/3.png", "towers/stone/level2/6.png", "towers/stone/level3/7.png"},
        // cannonPaths
//        new String[]{"towers/stone/level1/27.png", "towers/stone/level2/cannon.png", "towers/stone/level3/cannon.png"},
        null,
        // holderBackPaths
        new String[]{"towers/stone/level1/1.png", "towers/stone/level2/1.png", "towers/stone/level3/4.png"},
        // holderFrontPaths
        new String[]{"towers/stone/level1/2.png", "towers/stone/level2/2.png", "towers/stone/level3/5.png"},
        // projectilePaths
        new String[]{"towers/stone/level1/40.png", "towers/stone/level2/40.png", "towers/stone/level3/40.png"},
        // armPaths (không có)
        null,
        // clampTopPaths (không có)
        null,
        // clampBottomPaths (không có)
        null
    ),

    FIRE_TOWER(
        // basePaths (level1, level2, level3)
        new String[]{"towers/fire/level1/12.png", "towers/fire/level2/13.png", "towers/fire/level3/14.png"},
        // cannonPaths (không có)
        null,
        // holderBackPaths
        new String[]{"towers/fire/level1/8.png", "towers/fire/level2/10.png", "towers/fire/level3/10.png"},
        // holderFrontPaths
        new String[]{"towers/fire/level1/9.png", "towers/fire/level2/11.png", "towers/fire/level3/11.png"},
        // projectilePaths
        new String[]{"towers/fire/level1/35.png", "towers/fire/level2/35.png", "towers/fire/level3/40.png"},
        // armPaths (không có)
        null,
        // clampTopPaths (không có)
        null,
        // clampBottomPaths (không có)
        null
    ),
    BIGLAND_TOWER(
        // basePaths (dùng ảnh 24.png)
        new String[]{"towers/bigLand/level1/24.png", "towers/bigLand/level2/25.png", "towers/bigLand/level3/26.png"},
        // cannonPaths (không có)
        null,
        // holderBackPaths (không có)
        null,
        // holderFrontPaths (không có)
        null,
        // projectilePaths (dùng ảnh 45.png)
        new String[]{"towers/bigLand/level1/45.png", "towers/bigLand/level2/45.png", "towers/bigLand/level3/45.png"},
        // armPaths (dùng ảnh 28.png)
        new String[]{"towers/bigLand/level1/28.png", "towers/bigLand/level2/27.png", "towers/bigLand/level3/27.png"},
        // clampTopPaths (không có)
        null,
        // clampBottomPaths (không có)
        null
    ),
    LAND_TOWER(
        // basePaths (dùng ảnh 21.png)
        new String[]{"towers/land/level1/15.png", "towers/land/level2/16.png", "towers/land/level3/17.png"},
        // cannonPaths (không có)
        null,
        // holderBackPaths
        new String[]{"towers/land/level1/20.png", "towers/land/level2/22.png", "towers/land/level3/18.png"},
        // holderFrontPaths
        new String[]{"towers/land/level1/21.png", "towers/land/level2/23.png", "towers/land/level3/19.png"},
        // projectilePaths (dùng ảnh 44.png)
        new String[]{"towers/land/level1/29.png", "towers/land/level2/29.png", "towers/land/level3/29.png"},
        // armPaths (không có)
        null,
        // clampTopPaths (không có)
        null,
        // clampBottomPaths (không có)
        null
    );

    // Thêm CATAPULT và các loại khác ở đây...

    private final String[] basePaths, cannonPaths, holderBackPaths, holderFrontPaths, projectilePaths, armPaths, clampTopPaths, clampBottomPaths;

    TowerType(String[] basePaths, String[] cannonPaths, String[] holderBackPaths, String[] holderFrontPaths, String[] projectilePaths, String[] armPaths, String[] clampTopPaths, String[] clampBottomPaths) {
        this.basePaths = basePaths;
        this.cannonPaths = cannonPaths;
        this.holderBackPaths = holderBackPaths;
        this.holderFrontPaths = holderFrontPaths;
        this.projectilePaths = projectilePaths;
        this.armPaths = armPaths;
        this.clampTopPaths = clampTopPaths;
        this.clampBottomPaths = clampBottomPaths;
    }

    // Các hàm getter an toàn (kiểm tra null) để lấy đường dẫn ảnh cho một cấp độ cụ thể
    public String getPathFor(String component, int level) {
        if (level < 1 || level > 3) return null;
        String[] paths = null;
        switch (component) {
            case "base": paths = basePaths; break;
            case "cannon": paths = cannonPaths; break;
            case "holderBack": paths = holderBackPaths; break;
            case "holderFront": paths = holderFrontPaths; break;
            case "projectile": paths = projectilePaths; break;
            case "arm": paths = armPaths; break;
            case "clampTop": paths = clampTopPaths; break;
            case "clampBottom": paths = clampBottomPaths; break;
        }
        if (paths == null || paths.length < level) return null;
        return paths[level - 1];
    }
}
