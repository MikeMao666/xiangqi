package edu.sustech.xiangqi;

import edu.sustech.xiangqi.ui.GameFrame;

import javax.swing.*;

public class XiangqiApplication {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            JFrame loginFrame = new JFrame("登录");
            loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            loginFrame.setSize(400, 200);
            loginFrame.setLayout(null);
            JTextField textField = new JTextField( );
            textField.setSize(100, 50);
            textField.setLocation(50, 50);
            JButton login = new JButton("按钮");
            login.setLocation(50, 100);
            login.setSize(100, 50);
            login.addActionListener(e ->{
                //添加事件并加入判断条件
                GameFrame frame = new GameFrame("中国象棋");
                frame.setVisible(true);
                loginFrame.setVisible(false);

            });
            loginFrame.setLocationRelativeTo(null);
            loginFrame.add(login);
            loginFrame.add(textField);
            loginFrame.setVisible(true);

        });

    }
}
