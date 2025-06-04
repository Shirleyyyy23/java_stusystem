import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserLogin {

    //学生注册
    public boolean registerStudent(int studentId, String name, String major, String className, int age, String phone, String password, String hobbies, File photoFile) throws SQLException {
        //检查学生学号是否已存在于待审核表或学生表
        String checkQuery = "SELECT student_id FROM students WHERE student_id = ? UNION SELECT student_id FROM pending_students WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, studentId);
            checkStmt.setInt(2, studentId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                return false;  //学号已存在
            }
        }

        //学生不存在，将信息插入待审核表
        String insertQuery = "INSERT INTO pending_students (student_id, name, major, class, age, phone, password, hobbies, photo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertQuery);
             FileInputStream fis = new FileInputStream(photoFile)) {
            stmt.setInt(1, studentId);
            stmt.setString(2, name);
            stmt.setString(3, major);
            stmt.setString(4, className);
            stmt.setInt(5, age);
            stmt.setString(6, phone);
            stmt.setString(7, password);
            stmt.setString(8, hobbies);
            stmt.setBinaryStream(9, fis, (int) photoFile.length());
            stmt.executeUpdate();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false; //如果找不到文件，返回false
        } catch (IOException e) {
            e.printStackTrace();
            return false; //如果读取文件出错，返回false
        }
        return true;
    }

    //教师注册
    public boolean registerTeacher(int teacherId, String name, String phone, String password, File photoFile) throws SQLException {
        //检查教师工号是否已存在于待审核表或教师表
        String checkQuery = "SELECT teacher_id FROM teachers WHERE teacher_id = ? UNION SELECT teacher_id FROM pending_teachers WHERE teacher_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, teacherId);
            checkStmt.setInt(2, teacherId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                return false;  //工号已存在
            }
        }

        //教师不存在，将信息插入待审核表
        String insertQuery = "INSERT INTO pending_teachers (teacher_id, name, phone, password, photo) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertQuery);
             FileInputStream fis = new FileInputStream(photoFile)) {
            stmt.setInt(1, teacherId);
            stmt.setString(2, name);
            stmt.setString(3, phone);
            stmt.setString(4, password);
            stmt.setBinaryStream(5, fis, (int) photoFile.length());
            stmt.executeUpdate();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false; //如果找不到文件，返回false
        } catch (IOException e) {
            e.printStackTrace();
            return false; //如果读取文件出错，返回false
        }
        return true;
    }

    //学生登录
    public int studentLogin(String studentId, String password) throws SQLException {
        String query = "SELECT student_id FROM students WHERE student_id = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, studentId);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("student_id");  //返回学生学号
            }
        }
        return -1;
    }

    //教师登录
    public int teacherLogin(String teacherId, String password) throws SQLException {
        String query = "SELECT teacher_id FROM teachers WHERE teacher_id = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, teacherId);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("teacher_id");  //返回教师工号
            }
        }
        return -1;
    }

    //管理员登录
    public int adminLogin(String adminId, String password) throws SQLException {
        String query = "SELECT admin_id FROM admins WHERE admin_id = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, adminId);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("admin_id");  //返回管理员ID
            }
        }
        return -1;
    }
}
