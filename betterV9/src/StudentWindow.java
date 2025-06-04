import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentWindow {
    private JFrame frame;
    private JTextField studentIdField, phoneField, passwordField, nameField, majorField, classField, ageField;
    private JTextArea hobbiesArea;
    private int studentId;

    public StudentWindow(int studentId) {
        this.studentId = studentId;
        frame = new JFrame("学生信息界面");
        frame.setSize(500, 500);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());
        
        //设置自定义图标
        ImageIcon icon = new ImageIcon("D:\\A-课程作业\\java大课设\\华农115周年.png");
        Image img = icon.getImage();
        frame.setIconImage(img);  //设置窗口图标

        //面板显示个人信息
        JPanel infoPanel = new JPanel(new GridLayout(10, 2));
        frame.add(infoPanel, BorderLayout.CENTER);

        //信息字段
        JLabel studentIdLabel = new JLabel("学号：");
        studentIdField = new JTextField();
        studentIdField.setEditable(false);  //不允许修改
        JLabel nameLabel = new JLabel("姓名：");
        nameField = new JTextField();
        nameField.setEditable(false);
        JLabel majorLabel = new JLabel("专业：");
        majorField = new JTextField();
        majorField.setEditable(false);
        JLabel classLabel = new JLabel("班级：");
        classField = new JTextField();
        classField.setEditable(false);
        JLabel ageLabel = new JLabel("年龄：");
        ageField = new JTextField();
        ageField.setEditable(false);
        JLabel phoneLabel = new JLabel("手机号：");
        phoneField = new JTextField();
        JLabel passwordLabel = new JLabel("登录密码：");
        passwordField = new JTextField();  //显示明文密码
        JLabel hobbiesLabel = new JLabel("兴趣爱好：");
        hobbiesArea = new JTextArea(3, 15);

        infoPanel.add(studentIdLabel);
        infoPanel.add(studentIdField);
        infoPanel.add(nameLabel);
        infoPanel.add(nameField);
        infoPanel.add(majorLabel);
        infoPanel.add(majorField);
        infoPanel.add(classLabel);
        infoPanel.add(classField);
        infoPanel.add(ageLabel);
        infoPanel.add(ageField);
        infoPanel.add(phoneLabel);
        infoPanel.add(phoneField);
        infoPanel.add(passwordLabel);
        infoPanel.add(passwordField);
        infoPanel.add(hobbiesLabel);
        infoPanel.add(new JScrollPane(hobbiesArea));

        //更新按钮
        JButton updateButton = new JButton("更新信息");
        infoPanel.add(updateButton);

        //退出按钮
        JButton logoutButton = new JButton("退出登录");
        frame.add(logoutButton, BorderLayout.SOUTH);

        //获取学生信息并显示
        loadStudentInfo();

        //更新按钮事件
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (nameField.isEditable() || majorField.isEditable() || classField.isEditable() || ageField.isEditable()) {
                    JOptionPane.showMessageDialog(frame, "更新失败，您不能修改学号、姓名、专业、班级或年龄。请联系管理员。");
                } else {
                    saveStudentInfo();
                }
            }
        });

        //退出按钮事件
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();  //关闭当前窗口
                new MainWindow(); //返回登录界面
            }
        });

        frame.setVisible(true);
    }

    //加载学生个人信息
    private void loadStudentInfo() {
        String query = "SELECT * FROM students WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                studentIdField.setText(String.valueOf(rs.getInt("student_id")));
                nameField.setText(rs.getString("name"));
                majorField.setText(rs.getString("major"));
                classField.setText(rs.getString("class"));
                ageField.setText(String.valueOf(rs.getInt("age")));
                phoneField.setText(rs.getString("phone"));
                passwordField.setText(rs.getString("password"));
                hobbiesArea.setText(rs.getString("hobbies"));
            } else {
                JOptionPane.showMessageDialog(frame, "未找到学生信息。");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //保存学生修改后的信息
    private void saveStudentInfo() {
        String phone = phoneField.getText();
        String password = passwordField.getText();  //获取明文密码
        String hobbies = hobbiesArea.getText();

        String query = "UPDATE students SET phone = ?, password = ?, hobbies = ? WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, phone);
            stmt.setString(2, password);
            stmt.setString(3, hobbies);
            stmt.setInt(4, studentId);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(frame, "信息更新成功！");
            } else {
                JOptionPane.showMessageDialog(frame, "更新失败，请重试。");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
