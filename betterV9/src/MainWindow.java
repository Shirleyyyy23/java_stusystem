import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

public class MainWindow {
    private JFrame frame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private UserLogin userLogin;
    private JComboBox<String> roleComboBox;

    public MainWindow() {
        userLogin = new UserLogin();

        //创建窗口
        frame = new JFrame("学生管理系统");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 500);
        frame.setLocationRelativeTo(null);

        //设置自定义图标
        ImageIcon icon = new ImageIcon("D:\\A-课程作业\\java大课设\\华农115周年.png");
        Image img = icon.getImage();
        frame.setIconImage(img); //设置窗口图标

        //创建带有背景图片的面板
        BackgroundPanel backgroundPanel = new BackgroundPanel(new ImageIcon("D:\\A-课程作业\\java大课设\\一.jpg").getImage());

        //设置主窗口的布局
        backgroundPanel.setLayout(new GridBagLayout());
        frame.add(backgroundPanel);

        //将原有组件布局添加到背景面板上
        placeComponents(backgroundPanel);

        //显示窗口
        frame.setVisible(true);
    }

    private void placeComponents(JPanel panel) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        //用户角色选择框
        JLabel roleLabel = new JLabel("选择角色：");
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(roleLabel, gbc);

        String[] roles = {"管理员", "教师", "学生"};
        roleComboBox = new JComboBox<>(roles);
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(roleComboBox, gbc);

        //用户名输入
        JLabel userLabel = new JLabel("ID（学生/教师/管理员）：");
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(userLabel, gbc);

        usernameField = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(usernameField, gbc);

        //密码输入
        JLabel passwordLabel = new JLabel("密码：");
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(15); //使用JPasswordField隐藏密码
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(passwordField, gbc);

        //登录按钮
        JButton loginButton = new JButton("登录");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(loginButton, gbc);

        //注册按钮
        JButton registerButton = new JButton("注册");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(registerButton, gbc);

        //登录按钮事件
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String role = (String) roleComboBox.getSelectedItem();
                String userId = usernameField.getText();
                String password = new String(passwordField.getPassword());
                try {
                    if (role.equals("学生")) {
                        int studentId = userLogin.studentLogin(userId, password);
                        if (studentId != -1) {
                            new StudentWindow(studentId);
                            frame.dispose();
                        } else {
                            JOptionPane.showMessageDialog(frame, "学生登录信息无效，请重试。");
                        }
                    } else if (role.equals("教师")) {
                        int teacherId = userLogin.teacherLogin(userId, password);
                        if (teacherId != -1) {
                            new TeacherWindow(teacherId);
                            frame.dispose();
                        } else {
                            JOptionPane.showMessageDialog(frame, "教师登录信息无效，请重试。");
                        }
                    } else if (role.equals("管理员")) {
                        int adminId = userLogin.adminLogin(userId, password);
                        if (adminId != -1) {
                            new AdminWindow(adminId);
                            frame.dispose();
                        } else {
                            JOptionPane.showMessageDialog(frame, "管理员登录信息无效，请重试。");
                        }
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        //注册按钮事件
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String role = (String) roleComboBox.getSelectedItem();
                if (role.equals("管理员")) {
                    //如果用户选择管理员注册，弹出警告窗口
                    JOptionPane.showMessageDialog(frame, "管理员不能自行注册，请联系系统管理员创建账户。", "警告", JOptionPane.WARNING_MESSAGE);
                } else {
                    //对于教师和学生，继续正常的注册流程
                    new RegistrationWindow(role); //显示新的注册界面
                    frame.dispose(); //关闭当前登录窗口
                }
            }
        });
    }

    public static void main(String[] args) {
        new MainWindow();
    }

    //自定义面板用于绘制背景图片
    class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        public BackgroundPanel(Image image) {
            this.backgroundImage = image;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                //绘制背景图像
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }
}
