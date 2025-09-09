package io.github.some_example_name;

// Import các thư viện cần thiết từ libGDX
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.MathUtils;

// Lớp quản lý đối tượng quái trong game
public class Enemy {
    // Vị trí hiện tại của quái
    private Vector2 position;
    // Điểm đích mà quái đang di chuyển tới
    private Vector2 target;
    // Vector hướng di chuyển của quái
    private Vector2 direction;
    // Tốc độ di chuyển
    private float speed;
    // Lượng máu hiện tại
    private float health;
    // Hình ảnh của quái
    private Texture texture;
    // Kích thước cơ bản của sprite (pixel)
    private static final float BASE_SIZE = 64;
    // Góc xoay của sprite (độ)
    private float rotation;
    // Tỷ lệ phóng to/thu nhỏ sprite
    private float scale;

    // Constructor khởi tạo quái với vị trí và loại quái cụ thể
    public Enemy(float x, float y, EnemyType type) {
        position = new Vector2(x, y);          // Khởi tạo vị trí
        target = new Vector2();                // Khởi tạo điểm đích
        direction = new Vector2();             // Khởi tạo vector hướng
        texture = new Texture(type.getTexturePath()); // Tải texture
        speed = type.getSpeed();              // Thiết lập tốc độ
        health = type.getHealth();            // Thiết lập máu
        scale = type.getScale();              // Thiết lập tỷ lệ kích thước
        rotation = 0;                         // Góc xoay ban đầu là 0
    }

    // Thiết lập điểm đích mới cho quái
    public void setTarget(float x, float y) {
        target.set(x, y);
    }

    // Các biến quản lý đường đi của quái
    private Vector2 currentWaypoint;     // Điểm đến hiện tại
    private int currentWaypointIndex;    // Chỉ số điểm đến trong đường đi
    private com.badlogic.gdx.utils.Array<Vector2> path;  // Mảng các điểm đến

    // Thiết lập đường đi mới cho quái
    public void setPath(com.badlogic.gdx.utils.Array<Vector2> newPath) {
        this.path = newPath;
        currentWaypointIndex = 0;  // Reset về điểm đầu tiên
        if (path != null && path.size > 0) {
            currentWaypoint = path.get(0);  // Lấy điểm đến đầu tiên
            target.set(currentWaypoint);    // Thiết lập làm mục tiêu
        }
    }

    // Cập nhật trạng thái của quái theo thời gian
    public void update(float delta) {
        if (path == null || path.size == 0) return;

        // Tính toán vector hướng đến điểm đích hiện tại
        direction.set(target).sub(position);
        float distance = direction.len();  // Tính khoảng cách đến đích

        // Di chuyển về phía đích nếu chưa đến nơi
        if (distance > 1) {
            direction.nor();  // Chuẩn hóa vector hướng
            // Di chuyển theo hướng với tốc độ và thời gian
            position.add(direction.x * speed * delta, direction.y * speed * delta);
            
            // Cập nhật góc xoay sprite theo hướng di chuyển
            rotation = (float) Math.toDegrees(Math.atan2(direction.y, direction.x));
        } else {
            // Đã đến điểm đích, chuyển sang điểm tiếp theo
            currentWaypointIndex++;
            if (currentWaypointIndex < path.size) {
                currentWaypoint = path.get(currentWaypointIndex);
                target.set(currentWaypoint);
            }
        }
    }

    // Lấy vị trí hiện tại của quái
    public Vector2 getPosition() {
        return position;
    }

    // Kiểm tra xem quái đã đi hết đường đi chưa
    public boolean hasReachedEnd() {
        return currentWaypointIndex >= path.size;
    }

    // Vẽ quái lên màn hình
    public void render(SpriteBatch batch) {
        float size = BASE_SIZE * scale;  // Tính kích thước thực tế
        batch.draw(texture, 
            position.x - size/2, position.y - size/2,  // Vị trí vẽ (căn giữa)
            size/2, size/2,                           // Điểm xoay (center)
            size, size,                               // Kích thước vẽ
            1, 1,                                     // Không scale thêm
            rotation,                                 // Góc xoay
            0, 0,                                     // Vị trí cắt texture
            texture.getWidth(), texture.getHeight(),  // Kích thước texture
            false, false);                           // Không lật ảnh
    }

    // Vẽ thông tin debug (hướng di chuyển)
    public void renderDebug(com.badlogic.gdx.graphics.glutils.ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(1, 0, 0, 1);  // Màu đỏ
        // Vẽ đường thẳng chỉ hướng di chuyển
        shapeRenderer.line(position.x, position.y, 
            position.x + direction.x * 30, 
            position.y + direction.y * 30);
    }

    // Giải phóng tài nguyên
    public void dispose() {
        texture.dispose();  // Giải phóng texture
    }
}
