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
            
            String sqlUsers = "CREATE TABLE IF NOT EXISTS users (" +
                         "username VARCHAR(255) PRIMARY KEY, " +
                         "password VARCHAR(255), " +
                         "role VARCHAR(50), " +
                         "company_code VARCHAR(50), " +
                         "email VARCHAR(255), " +
                         "phone VARCHAR(50))";
            stmt.execute(sqlUsers);

            try { stmt.execute("ALTER TABLE users ADD COLUMN email VARCHAR(255)"); } catch (Exception ignored) {}
            try { stmt.execute("ALTER TABLE users ADD COLUMN phone VARCHAR(50)"); } catch (Exception ignored) {}

            String sqlEmp = "CREATE TABLE IF NOT EXISTS employees (" +
                         "id VARCHAR(50) PRIMARY KEY, " +
                         "name VARCHAR(255), " +
                         "department VARCHAR(255), " +
                         "position VARCHAR(100), " +
                         "baseSalary DOUBLE, " +
                         "account_username VARCHAR(255), " +
                         "login_username VARCHAR(255), " +
                         "status VARCHAR(50))";
            stmt.execute(sqlEmp);

            String sqlTask = "CREATE TABLE IF NOT EXISTS tasks (" +
                         "id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "title VARCHAR(255), " +             
                         "description TEXT, " +               
                         "assignee_id VARCHAR(50), " +        
                         "creator_username VARCHAR(255), " +  
                         "deadline DATE, " +                  
                         "status VARCHAR(50) DEFAULT 'Chờ xử lý')"; 
            stmt.execute(sqlTask);
            
            // --- ĐÃ THÊM: BẢNG LƯU TRỮ ĐIỂM KPI ---
            String sqlKPI = "CREATE TABLE IF NOT EXISTS kpi_records (" +
                         "employee_id VARCHAR(50), " +
                         "kpi_month INT, " +
                         "kpi_year INT, " +
                         "score INT, " +
                         "note VARCHAR(255), " +
                         "PRIMARY KEY (employee_id, kpi_month, kpi_year))";
            stmt.execute(sqlKPI);
            
            System.out.println("✅ Khởi tạo cơ sở dữ liệu (Bao gồm bảng KPI) thành công!");
        } catch (Exception e) {
            System.out.println("❌ Lỗi khởi tạo DB: " + e.getMessage());
        }
    }
}