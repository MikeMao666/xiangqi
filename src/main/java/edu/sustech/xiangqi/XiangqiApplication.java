package edu.sustech.xiangqi;

import edu.sustech.xiangqi.model.user.*;
import edu.sustech.xiangqi.ui.GameFrame;
import edu.sustech.xiangqi.ui.RegisterDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class XiangqiApplication {
    private static User currentUser;
    private static UserManager userManager;
    private static JFrame loginFrame;
    private static JTextField usernameField;
    private static JPasswordField passwordField;
    private static JLabel statusLabel;

    public static void main(String[] args) {
        // 初始化用户管理器
        userManager = new UserManager();

        SwingUtilities.invokeLater(() -> {
            createLoginFrame();
        });
    }

    private static void createLoginFrame() {
        loginFrame = new JFrame("中国象棋 - 登录");
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setSize(400, 350);
        loginFrame.setLayout(new GridBagLayout());
        loginFrame.setLocationRelativeTo(null);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 标题
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("中国象棋", JLabel.CENTER);
        titleLabel.setFont(new Font("楷体", Font.BOLD, 24));
        titleLabel.setForeground(new Color(180, 0, 0));
        loginFrame.add(titleLabel, gbc);

        // 用户名标签和输入框
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        JLabel usernameLabel = new JLabel("用户名:");
        usernameLabel.setFont(new Font("宋体", Font.PLAIN, 14));
        loginFrame.add(usernameLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        usernameField = new JTextField(15);
        usernameField.setFont(new Font("宋体", Font.PLAIN, 14));
        loginFrame.add(usernameField, gbc);

        // 密码标签和输入框
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel passwordLabel = new JLabel("密码:");
        passwordLabel.setFont(new Font("宋体", Font.PLAIN, 14));
        loginFrame.add(passwordLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("宋体", Font.PLAIN, 14));
        loginFrame.add(passwordField, gbc);

        // 按钮面板
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(new Color(240, 240, 240));

        JButton loginButton = new JButton("登录");
        JButton registerButton = new JButton("注册");
        JButton guestButton = new JButton("游客模式");

        // 设置按钮样式
        Font buttonFont = new Font("宋体", Font.BOLD, 14);
        loginButton.setFont(buttonFont);
        registerButton.setFont(buttonFont);
        guestButton.setFont(buttonFont);

        loginButton.setBackground(new Color(70, 130, 180));
        loginButton.setForeground(Color.WHITE);
        registerButton.setBackground(new Color(60, 179, 113));
        registerButton.setForeground(Color.WHITE);
        guestButton.setBackground(new Color(205, 133, 63));
        guestButton.setForeground(Color.WHITE);

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        buttonPanel.add(guestButton);
        loginFrame.add(buttonPanel, gbc);

        // 状态标签
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        statusLabel = new JLabel(" ", JLabel.CENTER);
        statusLabel.setForeground(Color.RED);
        statusLabel.setFont(new Font("宋体", Font.PLAIN, 12));
        loginFrame.add(statusLabel, gbc);

        // 功能说明
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        JTextArea infoArea = new JTextArea(
                "功能说明:\n" +
                        "• 注册用户: 可以保存和加载游戏\n" +
                        "• 游客模式: 可以直接游戏，但无法存档"
        );
        infoArea.setEditable(false);
        infoArea.setBackground(new Color(240, 240, 240));
        infoArea.setFont(new Font("宋体", Font.PLAIN, 12));
        infoArea.setForeground(Color.DARK_GRAY);
        loginFrame.add(infoArea, gbc);

        // 登录按钮事件
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });

        // 注册按钮事件 - 现在打开注册对话框
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleRegistration();
            }
        });

        // 游客模式按钮事件
        guestButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentUser = userManager.guestLogin();
                statusLabel.setText("游客模式登录");
                startGame();
            }
        });

        // 回车键快捷登录
        ActionListener enterListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        };

        usernameField.addActionListener(enterListener);
        passwordField.addActionListener(enterListener);

        loginFrame.setVisible(true);
    }

    private static void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("请输入用户名和密码");
            return;
        }

        User user = userManager.userLogin(username, password);
        if (user != null) {
            currentUser = user;
            statusLabel.setText("登录成功，正在进入游戏...");
            startGame();
        } else {
            statusLabel.setText("用户名或密码错误");
            passwordField.setText("");
        }
    }

    private static void handleRegistration() {
        // 打开注册对话框
        RegisterDialog registerDialog = new RegisterDialog(loginFrame, userManager);
        registerDialog.setVisible(true);

        // 处理注册结果
        if (registerDialog.isRegisteredSuccessfully()) {
            String registeredUsername = registerDialog.getRegisteredUsername();
            usernameField.setText(registeredUsername);
            passwordField.setText("");
            statusLabel.setText("注册成功！请输入密码登录");
        }
    }

    private static void startGame() {
        // 创建游戏主界面
        GameFrame gameFrame = new GameFrame("中国象棋", currentUser);

        // 设置游戏窗口关闭时的行为
        gameFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // 添加窗口监听器
        gameFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                gameFrame.checkAndSaveOnExit();
                System.exit(0);
            }
        });

        gameFrame.setVisible(true);
        loginFrame.dispose();
    }


    public static User getCurrentUser() {
        return currentUser;
    }
}