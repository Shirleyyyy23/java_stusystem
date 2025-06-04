import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.SQLException;

public class RegistrationWindow {
    private JFrame frame;
    private JTextField studentIdField, nameField, majorField, classField, ageField, phoneField, passwordField;
    private JTextArea hobbiesArea;
    private JLabel photoLabel;
    private File selectedPhotoFile = null;
    private UserLogin userLogin = new UserLogin();

    public RegistrationWindow(String role) {
        //创建主窗口
        frame = new JFrame(role + " 注册");
        frame.setSize(400, 600);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        //设置自定义图标
        ImageIcon icon = new ImageIcon("D:\\A-课程作业\\java大课设\\华农115周年.png");
        Image img = icon.getImage();
        frame.setIconImage(img); //设置窗口图标

        //创建内容面板
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(10, 2, 10, 10));
        frame.add(panel);

        //添加各个信息输入字段
        JLabel studentIdLabel = new JLabel("学号/工号:");
        studentIdLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(studentIdLabel);
        studentIdField = new JTextField();
        panel.add(studentIdField);

        JLabel nameLabel = new JLabel("姓名:");
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(nameLabel);
        nameField = new JTextField();
        panel.add(nameField);

        if (role.equals("学生")) {
            JLabel majorLabel = new JLabel("专业:");
            majorLabel.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(majorLabel);
            majorField = new JTextField();
            panel.add(majorField);

            JLabel classLabel = new JLabel("班级:");
            classLabel.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(classLabel);
            classField = new JTextField();
            panel.add(classField);

            JLabel ageLabel = new JLabel("年龄:");
            ageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(ageLabel);
            ageField = new JTextField();
            panel.add(ageField);

            JLabel hobbiesLabel = new JLabel("兴趣爱好:");
            hobbiesLabel.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(hobbiesLabel);
            hobbiesArea = new JTextArea(3, 20);
            panel.add(new JScrollPane(hobbiesArea));
        }

        JLabel phoneLabel = new JLabel("手机号:");
        phoneLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(phoneLabel);
        phoneField = new JTextField();
        panel.add(phoneField);

        JLabel passwordLabel = new JLabel("密码:");
        passwordLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(passwordLabel);
        passwordField = new JTextField();
        panel.add(passwordField);

        //添加照片选择按钮
        JButton choosePhotoButton = new JButton("选择照片");
        photoLabel = new JLabel("未选择照片");
        panel.add(choosePhotoButton);
        panel.add(photoLabel);

        //选择照片按钮事件
        JFileChooser fileChooser = new JFileChooser();
        choosePhotoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    selectedPhotoFile = fileChooser.getSelectedFile();
                    photoLabel.setText(selectedPhotoFile.getName());
                }
            }
        });

        //提交按钮
        JButton submitButton = new JButton("提交");
        panel.add(new JLabel()); //空标签占位
        panel.add(submitButton);

        //提交按钮事件
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (role.equals("学生")) {
                    registerStudent();
                } else if (role.equals("教师")) {
                    registerTeacher();
                }
            }
        });

        frame.setVisible(true);
    }

    //学生注册
    private void registerStudent() {
        int studentId = Integer.parseInt(studentIdField.getText());
        String name = nameField.getText();
        String major = majorField.getText();
        String className = classField.getText();
        int age = Integer.parseInt(ageField.getText());
        String phone = phoneField.getText();
        String password = passwordField.getText();
        String hobbies = hobbiesArea.getText();

        if (selectedPhotoFile == null) {
            JOptionPane.showMessageDialog(frame, "请选择照片进行注册。");
            return;
        }

        try {
            boolean registered = userLogin.registerStudent(studentId, name, major, className, age, phone, password, hobbies, selectedPhotoFile);
            if (registered) {
                JOptionPane.showMessageDialog(frame, "学生注册成功，等待管理员审核！");
                frame.dispose(); //注册成功后关闭窗口
            } else {
                JOptionPane.showMessageDialog(frame, "学号已存在，注册失败。");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    //教师注册
    private void registerTeacher() {
        int teacherId = Integer.parseInt(studentIdField.getText()); //用studentIdField来获取工号
        String name = nameField.getText();
        String phone = phoneField.getText();
        String password = passwordField.getText();

        if (selectedPhotoFile == null) {
            JOptionPane.showMessageDialog(frame, "请选择照片进行注册。");
            return;
        }

        try {
            boolean registered = userLogin.registerTeacher(teacherId, name, phone, password, selectedPhotoFile);
            if (registered) {
                JOptionPane.showMessageDialog(frame, "教师注册成功，等待管理员审核！");
                frame.dispose(); //注册成功后关闭窗口
            } else {
                JOptionPane.showMessageDialog(frame, "工号已存在，注册失败。");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
