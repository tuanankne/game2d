import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class JavaAnimation extends JFrame {
    private AnimationPanel animationPanel;

    public JavaAnimation() {
        setTitle("Java Animation Example");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        animationPanel = new AnimationPanel();
        add(animationPanel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JavaAnimation frame = new JavaAnimation();
            frame.setVisible(true);
        });
    }
}

class AnimationPanel extends JPanel implements ActionListener {
    private Image[] frames;
    private Timer timer;
    private int currentFrame;

    public AnimationPanel() {
        // Load các ảnh background
        frames = new Image[3];
        for (int i = 0; i < 3; i++) {
            frames[i] = new ImageIcon("Menu/background" + (i+1) + ".png").getImage();
        }

        // Tạo timer để update animation mỗi 100ms
        timer = new Timer(100, this);
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Vẽ frame hiện tại
        if (currentFrame < frames.length && frames[currentFrame] != null) {
            g.drawImage(frames[currentFrame], 0, 0, getWidth(), getHeight(), this);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Chuyển sang frame tiếp theo
        currentFrame = (currentFrame + 1) % frames.length;
        // Yêu cầu vẽ lại
        repaint();
    }
}
