package BaiTapLon;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseHelper {
    private static final String URL = "jdbc:mysql://localhost:3306/quanlyluong?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root"; 
    private static final String PASS = "";     

    public static Connection getConnection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public static void initDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Các bảng cũ (Giữ nguyên)...
            stmt.execute("CREATE TABLE IF NOT EXISTS users (username VARCHAR(50) PRIMARY KEY, password VARCHAR(255), role VARCHAR(50), company_code VARCHAR(50), email VARCHAR(255), phone VARCHAR(50))");
            stmt.execute("CREATE TABLE IF NOT EXISTS employees (id VARCHAR(50) PRIMARY KEY, name VARCHAR(255), department VARCHAR(255), position VARCHAR(100), baseSalary DOUBLE, account_username VARCHAR(255), login_username VARCHAR(255), status VARCHAR(50))");
            
            // --- BẢNG CHAT MỚI ---
            stmt.execute("CREATE TABLE IF NOT EXISTS chat_groups (id INT AUTO_INCREMENT PRIMARY KEY, group_name VARCHAR(255), creator_id VARCHAR(50))");
            stmt.execute("CREATE TABLE IF NOT EXISTS group_members (group_id INT, employee_id VARCHAR(50))");
            stmt.execute("CREATE TABLE IF NOT EXISTS internal_messages (id INT AUTO_INCREMENT PRIMARY KEY, sender_id VARCHAR(50), receiver_id VARCHAR(50), group_id INT, content TEXT, sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, is_group_msg BOOLEAN)");

            System.out.println("✅ Khởi tạo hệ thống Chat thành công!");
        } catch (Exception e) { e.printStackTrace(); }
    }
}