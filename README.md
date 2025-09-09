# Game Tower Defense 2D

## Tổng quan
Đây là một game tower defense 2D được phát triển bằng libGDX. Game cho phép người chơi phòng thủ chống lại các đợt tấn công (wave) của quái vật với nhiều loại quái khác nhau.

## Cấu trúc Project
```
core/src/main/java/io/github/some_example_name/
├── Main.java           - Lớp chính của game
├── MenuScreen.java     - Màn hình menu
├── GameScreen.java     - Màn hình chơi game
├── Enemy.java          - Quản lý quái vật
├── EnemyType.java      - Định nghĩa các loại quái
├── Wave.java          - Quản lý một đợt tấn công
├── WaveManager.java    - Quản lý các đợt tấn công
└── MapConfig.java      - Cấu hình bản đồ và wave
```

## Luồng hoạt động

### 1. Khởi động game (Main.java)
- Game bắt đầu từ lớp `Main` kế thừa từ `Game` của libGDX
- Khởi tạo `SpriteBatch` và texture cơ bản
- Chuyển đến màn hình menu (`MenuScreen`)

### 2. Màn hình Menu (MenuScreen.java)
- Hiển thị giao diện menu với:
  - Logo/tiêu đề game (nếu có)
  - Nút "Play" để bắt đầu chơi
  - Nút "Settings" (chức năng chưa được triển khai)
- Khi người chơi nhấn "Play":
  - Gọi `game.startGame()`
  - Chuyển sang màn hình chơi game (`GameScreen`)

### 3. Màn hình Game (GameScreen.java)
#### Khởi tạo
- Tải bản đồ từ file TMX
- Thiết lập camera và viewport
- Tạo cấu hình game thông qua `MapConfig`
- Khởi tạo `WaveManager`
- Tải các đường đi của quái từ layer "path" trong bản đồ

#### Vòng lặp game
1. **Cập nhật camera**
   - Xử lý input từ người chơi (pan, zoom)
   - Giới hạn camera trong phạm vi bản đồ

2. **Quản lý Wave**
   - `WaveManager` kiểm tra và cập nhật trạng thái wave
   - Hiển thị thông báo khi bắt đầu wave mới
   - Sinh quái mới khi đến thời điểm

3. **Quản lý Quái**
   - Cập nhật vị trí và trạng thái của từng quái
   - Xóa quái khi đã đến đích
   - Vẽ quái và hiệu ứng debug (hướng di chuyển)

### 4. Hệ thống Wave
#### WaveManager.java
- Quản lý danh sách các wave
- Điều khiển thời gian giữa các wave
- Hiển thị thông báo wave
- Kiểm tra điều kiện kết thúc wave

#### Wave.java
- Quản lý một đợt tấn công cụ thể
- Điều khiển việc sinh quái
- Theo dõi số lượng quái đã sinh
- Kiểm tra điều kiện hoàn thành wave

### 5. Hệ thống Quái
#### Enemy.java
- Quản lý trạng thái của một quái
- Di chuyển quái theo đường đi định sẵn
- Xử lý animation và render
- Kiểm tra điều kiện đến đích

#### EnemyType.java
Định nghĩa 3 loại quái:
- **NORMAL**: Quái thường với chỉ số cân bằng
- **FAST**: Quái nhanh, ít máu
- **TANK**: Quái chậm, nhiều máu, kích thước lớn

### 6. Cấu hình (MapConfig.java)
- Định nghĩa cấu trúc bản đồ
- Cấu hình các wave:
  - Wave 1: 5 quái thường
  - Wave 2: 3 quái thường + 3 quái nhanh
  - Wave 3: 2 quái mỗi loại (thường, nhanh, tank)

## Các tính năng hiện có
1. Menu với nút chơi game và cài đặt
2. Bản đồ tile-based với đường đi được định nghĩa
3. Hệ thống wave với nhiều loại quái
4. Camera có thể di chuyển và zoom
5. Debug mode hiển thị hướng di chuyển của quái

## Các tính năng cần phát triển
1. Hệ thống tháp phòng thủ
2. Tương tác người chơi (đặt tháp, nâng cấp)
3. Hệ thống tiền tệ và điểm số
4. Hiệu ứng âm thanh và hình ảnh
5. Lưu trữ tiến độ game
6. Menu cài đặt hoàn chỉnh