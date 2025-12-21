package edu.sustech.xiangqi.ui;

import edu.sustech.xiangqi.model.user.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegisterDialog extends JDialog {
    private UserManager userManager;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JLabel statusLabel;
    private boolean registeredSuccessfully = false;
    private String registeredUsername;

    public RegisterDialog(JFrame parent, UserManager userManager) {
        super(parent, "用户注册", true);
        this.userManager = userManager;
        initializeUI();
        setLocationRelativeTo(parent);
    }

    private void initializeUI() {
        setLayout(new GridBagLayout());
        setSize(600, 450);
        setResizable(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 标题
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("用户注册", JLabel.CENTER);
        titleLabel.setFont(new Font("楷体", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0, 100, 0));
        add(titleLabel, gbc);

        // 用户名
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        JLabel usernameLabel = new JLabel("用户名:");
        usernameLabel.setFont(new Font("宋体", Font.PLAIN, 14));
        add(usernameLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        usernameField = new JTextField(15);
        usernameField.setFont(new Font("宋体", Font.PLAIN, 14));
        add(usernameField, gbc);

        // 密码
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel passwordLabel = new JLabel("密码:");
        passwordLabel.setFont(new Font("宋体", Font.PLAIN, 14));
        add(passwordLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("宋体", Font.PLAIN, 14));
        add(passwordField, gbc);

        // 确认密码
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel confirmPasswordLabel = new JLabel("确认密码:");
        confirmPasswordLabel.setFont(new Font("宋体", Font.PLAIN, 14));
        add(confirmPasswordLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 3;
        confirmPasswordField = new JPasswordField(15);
        confirmPasswordField.setFont(new Font("宋体", Font.PLAIN, 14));
        add(confirmPasswordField, gbc);

        // 状态标签
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        statusLabel = new JLabel(" ", JLabel.CENTER);
        statusLabel.setForeground(Color.RED);
        statusLabel.setFont(new Font("宋体", Font.PLAIN, 12));
        add(statusLabel, gbc);

        // 按钮面板
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton registerButton = new JButton("注册");
        JButton cancelButton = new JButton("取消");

        // 设置按钮样式
        Font buttonFont = new Font("宋体", Font.BOLD, 14);
        registerButton.setFont(buttonFont);
        cancelButton.setFont(buttonFont);

        registerButton.setBackground(new Color(60, 179, 113));
        registerButton.setForeground(Color.WHITE);
        cancelButton.setBackground(new Color(220, 100, 100));
        cancelButton.setForeground(Color.WHITE);

        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, gbc);

        // 注册说明
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        JTextArea infoArea = new JTextArea(
                "注册要求:\n" +
                        "• 用户名: 3-20个字符\n" +
                        "• 密码: 至少6个字符\n" +
                        "• 两次输入的密码必须一致"
        );
        infoArea.setEditable(false);
        infoArea.setBackground(new Color(240, 240, 240));
        infoArea.setFont(new Font("宋体", Font.PLAIN, 12));
        infoArea.setForeground(Color.DARK_GRAY);
        add(infoArea, gbc);

        // 注册按钮事件
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                attemptRegistration();
            }
        });

        // 取消按钮事件
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registeredSuccessfully = false;
                dispose();
            }
        });

        // 回车键注册
        ActionListener enterListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                attemptRegistration();
            }
        };

        usernameField.addActionListener(enterListener);
        passwordField.addActionListener(enterListener);
        confirmPasswordField.addActionListener(enterListener);
    }

    private void attemptRegistration() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        // 输入验证
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            statusLabel.setText("请填写所有字段");
            return;
        }

        if (username.length() < 3 || username.length() > 20) {
            statusLabel.setText("用户名长度应为3-20个字符");
            return;
        }

        if (password.length() < 6) {
            statusLabel.setText("密码长度至少6个字符");
            return;
        }

        if (!password.equals(confirmPassword)) {
            statusLabel.setText("两次输入的密码不一致");
            confirmPasswordField.setText("");
            return;
        }

        // 检查用户名是否包含非法字符
        if (!username.matches("^[a-zA-Z0-9_\\u4e00-\\u9fa5]+$")) {
            statusLabel.setText("用户名只能包含字母、数字、下划线和中文字符");
            return;
        }

        // 尝试注册
        if (userManager.userRegister(username, password)) {
            registeredSuccessfully = true;
            registeredUsername = username;
            statusLabel.setText("注册成功！");

            // 延迟关闭对话框，让用户看到成功消息
            Timer timer = new Timer(1000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });
            timer.setRepeats(false);
            timer.start();
        } else {
            statusLabel.setText("用户名已存在");
            usernameField.setText("");
            passwordField.setText("");
            confirmPasswordField.setText("");
        }
    }

    public boolean isRegisteredSuccessfully() {
        return registeredSuccessfully;
    }

    public String getRegisteredUsername() {
        return registeredUsername;
    }
}