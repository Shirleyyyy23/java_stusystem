import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class AdminWindow {
    private JFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
    private boolean showingStudents = true;  //当前是否显示学生列表或学生待审核列表
    private boolean reviewingPending = false; //当前是否在审核待审核列表

    //新增的四个操作按钮
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton viewButton;

    public AdminWindow(int adminId) {
        frame = new JFrame("管理员界面");
        frame.setSize(1000, 600);  //设置更大窗口以容纳更多按钮
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());
        
        //设置自定义图标
        ImageIcon icon = new ImageIcon("D:\\A-课程作业\\java大课设\\华农115周年.png");
        Image img = icon.getImage();
        frame.setIconImage(img);  //设置窗口图标

        //顶部按钮面板
        JPanel topPanel = new JPanel();
        JButton studentListButton = new JButton("查看学生列表");
        JButton teacherListButton = new JButton("查看教师列表");
        JButton pendingStudentButton = new JButton("查看待审核学生");
        JButton pendingTeacherButton = new JButton("查看待审核教师");
        JButton approveButton = new JButton("审核通过");
        JButton rejectButton = new JButton("拒绝");

        topPanel.add(studentListButton);
        topPanel.add(teacherListButton);
        topPanel.add(pendingStudentButton);
        topPanel.add(pendingTeacherButton);
        topPanel.add(approveButton);
        topPanel.add(rejectButton);

        frame.add(topPanel, BorderLayout.NORTH);

        //表格数据禁止直接编辑
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; //禁用表格的编辑功能
            }
        };
        table = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(table);
        frame.add(scrollPane, BorderLayout.CENTER);

        //创建操作按钮面板
        JPanel operationPanel = new JPanel();
        operationPanel.setLayout(new BoxLayout(operationPanel, BoxLayout.Y_AXIS)); //垂直布局
        addButton = new JButton("增加");
        editButton = new JButton("修改");
        deleteButton = new JButton("删除");
        viewButton = new JButton("查看");

        operationPanel.add(viewButton);
        operationPanel.add(addButton);
        operationPanel.add(editButton);
        operationPanel.add(deleteButton);

        //创建底部面板用于存放操作按钮和退出按钮
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS)); //垂直布局

        //退出按钮
        JButton logoutButton = new JButton("退出登录");

        //将操作按钮和退出按钮依次添加到底部面板
        bottomPanel.add(operationPanel);  //先放操作按钮
        bottomPanel.add(Box.createRigidArea(new Dimension(0, 10)));  //添加间距
        bottomPanel.add(logoutButton);  //最后放退出按钮

        frame.add(bottomPanel, BorderLayout.SOUTH);  //将底部面板放在南部

        //隐藏操作按钮（默认不显示，只有查看学生或教师时显示）
        toggleOperationButtons(false);
        //初始显示学生信息
        loadStudentData();
        //切换显示学生列表
        studentListButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showingStudents = true;
                reviewingPending = false;  //不是在审核状态
                loadStudentData();
                toggleOperationButtons(true); //显示操作按钮
            }
        });
        //切换显示教师列表
        teacherListButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showingStudents = false;
                reviewingPending = false;  //不是在审核状态
                loadTeacherData();
                toggleOperationButtons(true); //显示操作按钮
            }
        });

        //查看待审核学生
        pendingStudentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showingStudents = true;
                reviewingPending = true;  //设置为审核状态
                loadPendingStudentData();
                toggleOperationButtons(false); //隐藏操作按钮
            }
        });

        //查看待审核教师
        pendingTeacherButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showingStudents = false;
                reviewingPending = true;  //设置为审核状态
                loadPendingTeacherData();
                toggleOperationButtons(false); //隐藏操作按钮
            }
        });

        //审核通过
        approveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                approveUser();
            }
        });

        //审核拒绝
        rejectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rejectUser();
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

        //增加按钮事件
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addUser();
            }
        });

        //修改按钮事件
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editUser();
            }
        });

        //删除按钮事件
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteUser();
            }
        });

        frame.setVisible(true);
    }

    //审核通过用户
    private void approveUser() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "请先选择要审核的用户！");
            return;
        }

        if (reviewingPending) {
            if (showingStudents) {
                int studentId = (int) tableModel.getValueAt(selectedRow, 0);
                try (Connection conn = DBConnection.getConnection()) {
                    //将待审核学生插入到正式学生表中
                    String insertQuery = "INSERT INTO students (student_id, name, major, class, age, phone, password, hobbies) SELECT student_id, name, major, class, age, phone, password, hobbies FROM pending_students WHERE student_id = ?";
                    String deleteQuery = "DELETE FROM pending_students WHERE student_id = ?";

                    PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                    insertStmt.setInt(1, studentId);
                    insertStmt.executeUpdate();

                    PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);
                    deleteStmt.setInt(1, studentId);
                    deleteStmt.executeUpdate();

                    JOptionPane.showMessageDialog(frame, "学生审核通过！");
                    loadPendingStudentData();  //重新加载待审核学生列表
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                int teacherId = (int) tableModel.getValueAt(selectedRow, 0);
                try (Connection conn = DBConnection.getConnection()) {
                    //将待审核教师插入到正式教师表中
                    String insertQuery = "INSERT INTO teachers (teacher_id, name, phone, password) SELECT teacher_id, name, phone, password FROM pending_teachers WHERE teacher_id = ?";
                    String deleteQuery = "DELETE FROM pending_teachers WHERE teacher_id = ?";

                    PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                    insertStmt.setInt(1, teacherId);
                    insertStmt.executeUpdate();

                    PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);
                    deleteStmt.setInt(1, teacherId);
                    deleteStmt.executeUpdate();

                    JOptionPane.showMessageDialog(frame, "教师审核通过！");
                    loadPendingTeacherData();  //重新加载待审核教师列表
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //拒绝用户申请
    private void rejectUser() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "请先选择要拒绝的用户！");
            return;
        }

        if (reviewingPending) {
            if (showingStudents) {
                int studentId = (int) tableModel.getValueAt(selectedRow, 0);
                try (Connection conn = DBConnection.getConnection()) {
                    String deleteQuery = "DELETE FROM pending_students WHERE student_id = ?";
                    PreparedStatement stmt = conn.prepareStatement(deleteQuery);
                    stmt.setInt(1, studentId);
                    stmt.executeUpdate();

                    JOptionPane.showMessageDialog(frame, "学生申请已拒绝！");
                    loadPendingStudentData();  //重新加载待审核学生列表
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                int teacherId = (int) tableModel.getValueAt(selectedRow, 0);
                try (Connection conn = DBConnection.getConnection()) {
                    String deleteQuery = "DELETE FROM pending_teachers WHERE teacher_id = ?";
                    PreparedStatement stmt = conn.prepareStatement(deleteQuery);
                    stmt.setInt(1, teacherId);
                    stmt.executeUpdate();

                    JOptionPane.showMessageDialog(frame, "教师申请已拒绝！");
                    loadPendingTeacherData();  //重新加载待审核教师列表
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //加载学生信息到表格
    private void loadStudentData() {
        tableModel.setRowCount(0);  //清除表格数据
        tableModel.setColumnIdentifiers(new Object[]{"学号", "姓名", "专业", "班级", "年龄", "手机号", "密码", "兴趣爱好"});

        String query = "SELECT * FROM students";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

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

    //加载教师信息到表格
    private void loadTeacherData() {
        tableModel.setRowCount(0);  //清除表格数据
        tableModel.setColumnIdentifiers(new Object[]{"工号", "姓名", "手机号", "密码"});

        String query = "SELECT * FROM teachers";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("teacher_id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("password")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //加载待审核学生信息到表格
    private void loadPendingStudentData() {
        tableModel.setRowCount(0);  //清除表格数据
        tableModel.setColumnIdentifiers(new Object[]{"学号", "姓名", "专业", "班级", "年龄", "手机号", "密码", "兴趣爱好"});

        String query = "SELECT * FROM pending_students";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

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

    //加载待审核教师信息到表格
    private void loadPendingTeacherData() {
        tableModel.setRowCount(0);  //清除表格数据
        tableModel.setColumnIdentifiers(new Object[]{"工号", "姓名", "手机号", "密码"});

        String query = "SELECT * FROM pending_teachers";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("teacher_id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("password")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //添加新用户
    private void addUser() {
        //创建输入框让管理员填写新用户信息
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField passwordField = new JTextField();
        JTextField majorField = new JTextField();
        JTextField classField = new JTextField();
        JTextField ageField = new JTextField();
        JTextField hobbiesField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(0, 2));
        panel.add(new JLabel("学号/工号:"));
        panel.add(idField);
        panel.add(new JLabel("姓名:"));
        panel.add(nameField);
        panel.add(new JLabel("手机号:"));
        panel.add(phoneField);
        panel.add(new JLabel("密码:"));
        panel.add(passwordField);

        if (showingStudents) {
            //如果是学生，还需要输入专业、班级等信息
            panel.add(new JLabel("专业:"));
            panel.add(majorField);
            panel.add(new JLabel("班级:"));
            panel.add(classField);
            panel.add(new JLabel("年龄:"));
            panel.add(ageField);
            panel.add(new JLabel("兴趣爱好:"));
            panel.add(hobbiesField);
        }

        int result = JOptionPane.showConfirmDialog(null, panel, "增加用户", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try (Connection conn = DBConnection.getConnection()) {
                String query;
                if (showingStudents) {
                    //学生信息插入语句
                    query = "INSERT INTO students (student_id, name, major, class, age, phone, password, hobbies) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                } else {
                    //教师信息插入语句
                    query = "INSERT INTO teachers (teacher_id, name, phone, password) VALUES (?, ?, ?, ?)";
                }

                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, Integer.parseInt(idField.getText()));
                stmt.setString(2, nameField.getText());
                stmt.setString(3, phoneField.getText());
                stmt.setString(4, passwordField.getText());

                if (showingStudents) {
                    stmt.setString(3, majorField.getText());
                    stmt.setString(4, classField.getText());
                    stmt.setInt(5, Integer.parseInt(ageField.getText()));
                    stmt.setString(6, phoneField.getText());
                    stmt.setString(7, passwordField.getText());
                    stmt.setString(8, hobbiesField.getText());
                }

                stmt.executeUpdate();
                JOptionPane.showMessageDialog(frame, "用户增加成功！");
                if (showingStudents) {
                    loadStudentData();
                } else {
                    loadTeacherData();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    //修改用户信息
    private void editUser() {
        //获取选中的行
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "请先选择要修改的用户！");
            return;
        }

        //创建输入框
        JTextField idField = new JTextField(tableModel.getValueAt(selectedRow, 0).toString());
        JTextField nameField = new JTextField(tableModel.getValueAt(selectedRow, 1).toString());
        JTextField phoneField = new JTextField(tableModel.getValueAt(selectedRow, showingStudents ? 5 : 2).toString());  //学生是第6列，教师是第3列
        JTextField passwordField = new JTextField(tableModel.getValueAt(selectedRow, showingStudents ? 6 : 3).toString());  //学生是第7列，教师是第4列

        JPanel panel = new JPanel(new GridLayout(0, 2));

        panel.add(new JLabel("学号/工号:"));
        panel.add(idField);
        panel.add(new JLabel("姓名:"));
        panel.add(nameField);
        panel.add(new JLabel("手机号:"));
        panel.add(phoneField);
        panel.add(new JLabel("密码:"));
        panel.add(passwordField);

        if (showingStudents) {
            JTextField majorField = new JTextField(tableModel.getValueAt(selectedRow, 2).toString());
            JTextField classField = new JTextField(tableModel.getValueAt(selectedRow, 3).toString());
            JTextField ageField = new JTextField(tableModel.getValueAt(selectedRow, 4).toString());
            JTextField hobbiesField = new JTextField(tableModel.getValueAt(selectedRow, 7).toString());

            panel.add(new JLabel("专业:"));
            panel.add(majorField);
            panel.add(new JLabel("班级:"));
            panel.add(classField);
            panel.add(new JLabel("年龄:"));
            panel.add(ageField);
            panel.add(new JLabel("兴趣爱好:"));
            panel.add(hobbiesField);
        }

        int result = JOptionPane.showConfirmDialog(null, panel, "修改用户信息", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try (Connection conn = DBConnection.getConnection()) {
                String query;
                if (showingStudents) {
                    query = "UPDATE students SET name = ?, major = ?, class = ?, age = ?, phone = ?, password = ?, hobbies = ? WHERE student_id = ?";
                } else {
                    query = "UPDATE teachers SET name = ?, phone = ?, password = ? WHERE teacher_id = ?";
                }

                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, nameField.getText());
                stmt.setString(2, phoneField.getText());
                stmt.setString(3, passwordField.getText());

                if (showingStudents) {
                    stmt.setString(2, tableModel.getValueAt(selectedRow, 2).toString());
                    stmt.setString(3, tableModel.getValueAt(selectedRow, 3).toString());
                    stmt.setInt(4, Integer.parseInt(tableModel.getValueAt(selectedRow, 4).toString()));
                    stmt.setString(5, phoneField.getText());
                    stmt.setString(6, passwordField.getText());
                    stmt.setString(7, tableModel.getValueAt(selectedRow, 7).toString());
                    stmt.setInt(8, Integer.parseInt(idField.getText()));
                } else {
                    stmt.setInt(4, Integer.parseInt(idField.getText()));
                }

                stmt.executeUpdate();
                JOptionPane.showMessageDialog(frame, "用户信息修改成功！");
                if (showingStudents) {
                    loadStudentData();
                } else {
                    loadTeacherData();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    //删除用户
    private void deleteUser() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "请先选择要删除的用户！");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(frame, "确认要删除此用户吗？", "删除用户", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection()) {
                String query;
                if (showingStudents) {
                    query = "DELETE FROM students WHERE student_id = ?";
                } else {
                    query = "DELETE FROM teachers WHERE teacher_id = ?";
                }

                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, (int) tableModel.getValueAt(selectedRow, 0));
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(frame, "用户删除成功！");
                if (showingStudents) {
                    loadStudentData();
                } else {
                    loadTeacherData();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    //控制操作按钮的显示与隐藏
    private void toggleOperationButtons(boolean visible) {
        addButton.setVisible(visible);
        editButton.setVisible(visible);
        deleteButton.setVisible(visible);
        viewButton.setVisible(visible);
    }
}
