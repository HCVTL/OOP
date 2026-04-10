package core;
import ui.*;
import entities.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class GameWindow extends JPanel implements ActionListener {
    enum GameState { MENU, PLAYING, LOADING, PAUSE }
    private GameState currentState = GameState.MENU;
    private Image menuBg;

    // --- THÊM BIẾN HÌNH ẢNH NHÂN VẬT ---
    private Image imgUp, imgDown, imgLeft, imgRight;
    private String facing = "DOWN"; // Hướng nhìn mặc định

    private int menuIndex = 0;
    private String[] menuOptions = {"New Game", "Continue", "Exit"};
    private int loadingProgress = 0;

    private double playerX = 250, playerY = 250;
    private final int PLAYER_SIZE = 40; // Tăng size một chút để nhìn rõ nhân vật
    private final double SPEED = 3.5; // Tăng tốc độ cho thám tử linh hoạt hơn

    private Inventory inventory;
    private boolean showInventory = true;
    private boolean up, down, left, right;
    private ArrayList<IEntity> evidenceList;
    private DialogueManager dialogueManager;

    public GameWindow() {
        inventory = new Inventory();
        evidenceList = new ArrayList<>();
        dialogueManager = new DialogueManager();

        try {
            // Nạp ảnh nền Menu
            menuBg = ImageIO.read(new File("src/assets/Gemini_Generated_Image_a3nhnba3nhnba3nh.png"));

            // --- NẠP 4 ẢNH NHÂN VẬT ---
            imgDown = ImageIO.read(new File("src/assets/behind.png"));
            imgUp = ImageIO.read(new File("src/assets/frontt.png"));
            imgLeft = ImageIO.read(new File("src/assets/lefft.png"));
            imgRight = ImageIO.read(new File("src/assets/right.png"));

        } catch (IOException e) {
            System.out.println("Lỗi: Không tìm thấy file ảnh trong src/assets/. Kiểm tra lại tên file!");
        }

        JFrame frame = new JFrame("Thám tử lừng danh Công An");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        if (gd.isFullScreenSupported()) {
            frame.setUndecorated(true);
            gd.setFullScreenWindow(frame);
        }

        // Khởi tạo màn chơi
        evidenceList.add(new Evidence("Dấu vân tay", 120, 150));
        evidenceList.add(new Evidence("Vỏ đạn", 450, 100));
        evidenceList.add(new NPC("Nghi phạm A", 100, 450, "Tôi không làm gì cả, thưa thám tử!"));
        evidenceList.add(new Wall(200, 100, 20, 300));
        evidenceList.add(new Wall(200, 100, 200, 20));

        this.setFocusable(true);
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) { updateKey(e.getKeyCode(), true); }
            @Override
            public void keyReleased(KeyEvent e) { updateKey(e.getKeyCode(), false); }
        });

        new Timer(16, this).start();
        frame.add(this);
        frame.setVisible(true);
    }

    private void updateKey(int code, boolean pressed) {
        // XỬ LÝ MENU
        if (currentState == GameState.MENU) {
            if (pressed) {
                if (code == KeyEvent.VK_W) menuIndex = (menuIndex - 1 + menuOptions.length) % menuOptions.length;
                if (code == KeyEvent.VK_S) menuIndex = (menuIndex + 1) % menuOptions.length;
                if (code == KeyEvent.VK_ENTER) handleMenuSelection();
            }
            return;
        }

        // XỬ LÝ PAUSE
        if (currentState == GameState.PAUSE) {
            if (pressed) {
                if (code == KeyEvent.VK_ESCAPE) currentState = GameState.PLAYING;
                if (code == KeyEvent.VK_ENTER) System.exit(0);
            }
            return;
        }

        // XỬ LÝ TRONG GAME
        if (currentState == GameState.PLAYING) {
            if (code == KeyEvent.VK_W) { up = pressed; if(pressed) facing = "UP"; }
            if (code == KeyEvent.VK_S) { down = pressed; if(pressed) facing = "DOWN"; }
            if (code == KeyEvent.VK_A) { left = pressed; if(pressed) facing = "LEFT"; }
            if (code == KeyEvent.VK_D) { right = pressed; if(pressed) facing = "RIGHT"; }

            if (pressed) {
                if (code == KeyEvent.VK_SPACE) interact();
                if (code == KeyEvent.VK_I) showInventory = !showInventory;
                if (code == KeyEvent.VK_ESCAPE) {
                    currentState = GameState.PAUSE;
                    up = down = left = right = false;
                }
            }
        }
    }

    private void handleMenuSelection() {
        if (menuIndex == 0) startLoading();
        else if (menuIndex == 1) JOptionPane.showMessageDialog(this, "Không có file lưu!");
        else if (menuIndex == 2) System.exit(0);
    }

    private void startLoading() {
        currentState = GameState.LOADING;
        loadingProgress = 0;
    }

    private void movePlayer() {
        double nx = playerX + (left ? -SPEED : (right ? SPEED : 0));
        if (nx >= 0 && nx <= getWidth() - PLAYER_SIZE && canMove(nx, playerY)) playerX = nx;

        double ny = playerY + (up ? -SPEED : (down ? SPEED : 0));
        if (ny >= 0 && ny <= getHeight() - PLAYER_SIZE && canMove(playerX, ny)) playerY = ny;
    }

    private boolean canMove(double nx, double ny) {
        Rectangle futureRect = new Rectangle((int)nx, (int)ny, PLAYER_SIZE, PLAYER_SIZE);
        for (IEntity e : evidenceList) {
            if (e instanceof Wall && futureRect.intersects(((Wall) e).getBounds())) return false;
        }
        return true;
    }

    private void interact() {
        if (dialogueManager.isVisible()) { dialogueManager.hide(); return; }
        Iterator<IEntity> it = evidenceList.iterator();
        while (it.hasNext()) {
            IEntity entity = it.next();
            if (entity.checkCollision((int)playerX, (int)playerY, PLAYER_SIZE)) {
                if (entity instanceof Evidence) {
                    dialogueManager.showDialogue("Hệ thống", "Đã nhặt: " + entity.getName());
                    inventory.add((Evidence) entity);
                    it.remove();
                } else if (entity instanceof NPC) {
                    dialogueManager.showDialogue(entity.getName(), ((NPC) entity).getDialogue());
                }
                break;
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (currentState == GameState.PLAYING) {
            if (!dialogueManager.isVisible()) movePlayer();
            for (IEntity en : evidenceList) {
                double d = Math.sqrt(Math.pow(playerX - en.getX(), 2) + Math.pow(playerY - en.getY(), 2));
                en.setHighlight(d < 80);
            }
        } else if (currentState == GameState.LOADING) {
            loadingProgress += 2;
            if (loadingProgress >= 100) currentState = GameState.PLAYING;
        }
        dialogueManager.update();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        switch (currentState) {
            case MENU: drawMenu(g); break;
            case LOADING: drawLoading(g); break;
            case PLAYING: drawGameContent(g); break;
            case PAUSE: drawGameContent(g); drawPauseMenu(g); break;
        }
    }

    private void drawGameContent(Graphics g) {
        g.setColor(new Color(30, 30, 30));
        g.fillRect(0, 0, getWidth(), getHeight());

        for (IEntity item : evidenceList) item.draw(g);

        // --- VẼ ẢNH NHÂN VẬT THEO HƯỚNG ---
        Image currentImg = imgDown;
        if (facing.equals("UP")) currentImg = imgUp;
        else if (facing.equals("LEFT")) currentImg = imgLeft;
        else if (facing.equals("RIGHT")) currentImg = imgRight;

        if (currentImg != null) {
            g.drawImage(currentImg, (int)playerX, (int)playerY, PLAYER_SIZE, PLAYER_SIZE, null);
        } else {
            g.setColor(Color.CYAN);
            g.fillRect((int)playerX, (int)playerY, PLAYER_SIZE, PLAYER_SIZE);
        }

        if (showInventory) drawInventoryUI(g);
        dialogueManager.draw(g, getWidth(), getHeight());
        g.setColor(Color.GRAY);
        g.drawString("I: Túi đồ | Space: Tương tác | ESC: Tạm dừng", 10, 20);
    }

    private void drawMenu(Graphics g) {
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        if (menuBg != null) g.drawImage(menuBg, 0, 0, getWidth(), getHeight(), null);
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setFont(new Font("Arial", Font.BOLD, 50));
        g.setColor(Color.DARK_GRAY);
        g.drawString("THÁM TỬ LỪNG DANH CÔNG AN", centerX - 350, centerY - 147);
        g.setColor(Color.CYAN);
        g.drawString("THÁM TỬ LỪNG DANH CÔNG AN", centerX - 347, centerY - 150);

        g.setFont(new Font("Arial", Font.PLAIN, 28));
        for (int i = 0; i < menuOptions.length; i++) {
            int optionX = centerX - 100;
            int optionY = centerY + (i * 60);
            if (i == menuIndex) {
                g.setColor(Color.YELLOW);
                g.drawString("> " + menuOptions[i], optionX - 40, optionY);
                g.fillRect(optionX - 40, optionY + 10, 200, 2);
            } else {
                g.setColor(Color.WHITE);
                g.drawString(menuOptions[i], optionX, optionY);
            }
        }
    }

    private void drawLoading(Graphics g) {
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        g.setColor(Color.WHITE);
        g.drawString("Loading: " + loadingProgress + "%", centerX - 60, centerY - 20);
        g.drawRect(centerX - 200, centerY + 10, 400, 25);
        g.fillRect(centerX - 200, centerY + 10, (loadingProgress * 400) / 100, 25);
    }

    private void drawPauseMenu(Graphics g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        g.drawString("TẠM DỪNG", getWidth() / 2 - 100, getHeight() / 2 - 50);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("Nhấn [ENTER] để Thoát Game", getWidth() / 2 - 130, getHeight() / 2 + 20);
        g.drawString("Nhấn [ESC] để Quay lại", getWidth() / 2 - 100, getHeight() / 2 + 60);
    }

    private void drawInventoryUI(Graphics g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRoundRect(10, 40, 160, 150, 10, 10);
        g.setColor(Color.YELLOW);
        g.drawString("INVENTORY:", 20, 60);
        g.setColor(Color.WHITE);
        int y = 85;
        for (Evidence e : inventory.getItems()) {
            g.drawString("• " + e.getName(), 25, y);
            y += 20;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameWindow());
    }
}