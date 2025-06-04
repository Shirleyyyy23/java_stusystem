import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TeacherWindow {
    private JFrame frame;
    private JTextField teacherIdField, phoneField, passwordField, nameField;
    private int teacherId;
    private JTable searchResultTable;
    private DefaultTableModel tableModel;

    public TeacherWindow(int teacherId) {
        this.teacherId = teacherId;
        frame = new JFrame("教师信息界面");
        frame.setSize(800, 600);  //调整窗口大小以适应组件
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        //设置自定义图标
        ImageIcon icon = new ImageIcon("D:\\A-课程作业\\java大课设\\华农115周年.png");
        Image img = icon.getImage();
        frame.setIconImage(img); //设置窗口图标

        //信息字段
        JPanel infoPanel = new JPanel(new GridLayout(5, 2));
        frame.add(infoPanel, BorderLayout.NORTH);
        JLabel teacherIdLabel = new JLabel("工号：");
        teacherIdField = new JTextField();
        teacherIdField.setEditable(false); //不允许修改
        JLabel nameLabel = new JLabel("姓名：");
        nameField = new JTextField();
        nameField.setEditable(false);
        JLabel phoneLabel = new JLabel("手机号：");
        phoneField = new JTextField();
        JLabel passwordLabel = new JLabel("登录密码：");
        passwordField = new JTextField();
        infoPanel.add(teacherIdLabel);
        infoPanel.add(teacherIdField);
        infoPanel.add(nameLabel);
        infoPanel.add(nameField);
        infoPanel.add(phoneLabel);
        infoPanel.add(phoneField);
        infoPanel.add(passwordLabel);
        infoPanel.add(passwordField);

        //更新按钮
        JButton updateButton = new JButton("更新信息");
        infoPanel.add(updateButton);

        //退出按钮
        JButton logoutButton = new JButton("退出登录");
        frame.add(logoutButton, BorderLayout.SOUTH);

        //获取教师信息并显示
        loadTeacherInfo();

        //更新按钮事件
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (nameField.isEditable()) {
                    JOptionPane.showMessageDialog(frame, "更新失败，您不能修改姓名或工号。请联系管理员。");
                } else {
                    saveTeacherInfo();
                }
            }
        });

        //查询功能面板
        JPanel searchPanel = new JPanel(new GridBagLayout());
        frame.add(searchPanel, BorderLayout.CENTER);
        addSearchComponents(searchPanel);

        //退出按钮事件
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose(); //关闭当前窗口
                new MainWindow(); //返回登录界面
            }
        });

        frame.setVisible(true);
    }

    //加载教师个人信息
    private void loadTeacherInfo() {
        String query = "SELECT * FROM teachers WHERE teacher_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, teacherId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                teacherIdField.setText(String.valueOf(rs.getInt("teacher_id")));
                nameField.setText(rs.getString("name"));
                phoneField.setText(rs.getString("phone"));
                passwordField.setText(rs.getString("password"));
            } else {
                JOptionPane.showMessageDialog(frame, "未找到教师信息。");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //保存教师修改后的信息
    private void saveTeacherInfo() {
        String phone = phoneField.getText();
        String password = passwordField.getText();
        String query = "UPDATE teachers SET phone = ?, password = ? WHERE teacher_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, phone);
            stmt.setString(2, password);
            stmt.setInt(3, teacherId);
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

    //添加查询功能
    private void addSearchComponents(JPanel panel) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        //学号查询
        JLabel studentIdLabel = new JLabel("按学号查询学生：");
        JTextField studentIdField = new JTextField(20);  //增加宽度
        JButton searchByIdButton = new JButton("查询");
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(studentIdLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;  //扩展输入框
        panel.add(studentIdField, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0;
        panel.add(searchByIdButton, gbc);

        //班级查询
        JLabel classLabel = new JLabel("按班级查询学生：");
        JTextField classField = new JTextField(20);
        JButton searchByClassButton = new JButton("查询");
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(classLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(classField, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0;
        panel.add(searchByClassButton, gbc);

        //专业查询
        JLabel majorLabel = new JLabel("按专业查询学生：");
        JTextField majorField = new JTextField(20);
        JButton searchByMajorButton = new JButton("查询");
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(majorLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(majorField, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0;
        panel.add(searchByMajorButton, gbc);

        //查询结果表格
        tableModel = new DefaultTableModel() {
            //禁用表格的编辑功能
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; //所有单元格不可编辑
            }
        };

        searchResultTable = new JTable(tableModel);
        JScrollPane searchScrollPane = new JScrollPane(searchResultTable);
        tableModel.setColumnIdentifiers(new Object[]{"学号", "姓名", "专业", "班级", "年龄", "手机号", "密码", "兴趣爱好"});
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;  //让表格可以填充整个空间
        panel.add(searchScrollPane, gbc);

        //事件绑定学号查询
        searchByIdButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String studentId = studentIdField.getText();
                searchStudentByField("student_id", studentId);
            }
        });

        //事件绑定班级查询
        searchByClassButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String className = classField.getText();
                searchStudentByField("class", className);
            }
        });

        //事件绑定专业查询
        searchByMajorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String major = majorField.getText();
                searchStudentByField("major", major);
            }
        });
    }

    //查询学生信息
    private void searchStudentByField(String field, String value) {
        tableModel.setRowCount(0); //清空表格数据
        String query = "SELECT * FROM students WHERE " + field + " = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, value);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("student_id"),
                        rs.getString("name"),
                        rs.getString("major"),
                        rs.getString("class"),
                        rs.getInt("age"),
                        rs.getString("phone"),
                        rs.getString("password"),
                        rs.getString("hobbies")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
