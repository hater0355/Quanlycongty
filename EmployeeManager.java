package BaiTapLon;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime; 
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Random; 
import java.time.format.DateTimeFormatter;

public class EmployeeManager {
    private static EmployeeManager instance;

    private EmployeeManager() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ Driver MySQL đã được nạp thành công!");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Lỗi: Không tìm thấy Driver MySQL (.jar).");
        }
    }

    public static EmployeeManager getInstance() {
        if (instance == null) {
            instance = new EmployeeManager();
        }
        return instance;
    }

    public List<Employee> getAllEmployees() {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT * FROM employees WHERE account_username = ? AND status = 'APPROVED'";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currentUsername);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Employee(
                        rs.getString("id"), rs.getString("name"), rs.getString("department"), 
                        rs.getString("position"), rs.getDouble("baseSalary")
                    ));
                }
            }
        } catch (Exception e) { }
        return list;
    }

    public void addEmployee(Employee emp) {
        String sql = "INSERT INTO employees (id, name, department, position, baseSalary, account_username, login_username, status) VALUES (?, ?, ?, ?, ?, ?, ?, 'APPROVED')";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, emp.getId()); pstmt.setString(2, emp.getName()); pstmt.setString(3, emp.getDepartment()); 
            pstmt.setString(4, emp.getPosition()); pstmt.setDouble(5, emp.getBaseSalary()); pstmt.setString(6, currentUsername); 
            pstmt.setString(7, emp.getId()); pstmt.executeUpdate();
        } catch (Exception e) { }
    }

    public void updateSalary(String id, double newSalary) {
        String sql = "UPDATE employees SET baseSalary = ? WHERE id = ? AND account_username = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newSalary); pstmt.setString(2, id); pstmt.setString(3, currentUsername); pstmt.executeUpdate();
        } catch (Exception e) { }
    }

    public void updateEmployeeDeptPos(String id, String dep, String pos) {
        String sql = "UPDATE employees SET department = ?, position = ? WHERE id = ? AND account_username = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, dep); pstmt.setString(2, pos); pstmt.setString(3, id); pstmt.setString(4, currentUsername); pstmt.executeUpdate();
        } catch (Exception e) { }
    }

    public void deleteEmployee(String id) {
        String sql = "DELETE FROM employees WHERE id = ? AND account_username = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id); pstmt.setString(2, currentUsername); pstmt.executeUpdate();
        } catch (Exception e) { }
    }

    public String generateEmployeeId() {
        int year = LocalDate.now().getYear() % 100; String prefix = String.format("%02d", year);
        String newId; boolean exists = true; Random rand = new Random();
        do { newId = prefix + String.format("%03d", rand.nextInt(999) + 1); exists = checkIdExists(newId); } while (exists);
        return newId;
    }

    private boolean checkIdExists(String id) {
        String sql = "SELECT 1 FROM employees WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id); return pstmt.executeQuery().next();
        } catch (Exception e) { return true; } 
    }

    public void importEmployeeFromExcel(String name, String dep, String pos, double salary) {
        String empId = generateEmployeeId();
        String insertUserSql = "INSERT INTO users (username, password, role) VALUES (?, ?, 'EMPLOYEE')";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(insertUserSql)) {
            pstmt.setString(1, empId); pstmt.setString(2, "123"); pstmt.executeUpdate();
        } catch(Exception e) {}
        addEmployee(new Employee(empId, name, dep, pos, salary));
    }

    public void checkIn(String empId, LocalDate date, LocalTime time) {
        String sql = "INSERT INTO timekeeping (employee_id, work_date, check_in) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE check_in = VALUES(check_in)";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt =prepareStatement(conn, sql)) {
            pstmt.setString(1, empId); pstmt.setDate(2, java.sql.Date.valueOf(date)); pstmt.setTime(3, java.sql.Time.valueOf(time)); pstmt.executeUpdate();
        } catch (Exception e) {}
    }

    private PreparedStatement prepareStatement(Connection conn, String sql) throws SQLException {
        return conn.prepareStatement(sql);
    }

    public void checkOut(String empId, LocalDate date, LocalTime time) {
        String sql = "UPDATE timekeeping SET check_out = ? WHERE employee_id = ? AND work_date = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTime(1, java.sql.Time.valueOf(time)); pstmt.setString(2, empId); pstmt.setDate(3, java.sql.Date.valueOf(date)); pstmt.executeUpdate();
        } catch (Exception e) {}
    }

    public String[] getAttendanceRecord(String empId, LocalDate date) {
        String sql = "SELECT check_in, check_out FROM timekeeping WHERE employee_id = ? AND work_date = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) { 
            pstmt.setString(1, empId); pstmt.setDate(2, java.sql.Date.valueOf(date)); ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Time in = rs.getTime("check_in"); Time out = rs.getTime("check_out");
                if (in != null && out == null && date.isBefore(LocalDate.now())) {
                    out = java.sql.Time.valueOf("23:59:00");
                    String updateSql = "UPDATE timekeeping SET check_out = ? WHERE employee_id = ? AND work_date = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) { updateStmt.setTime(1, out); updateStmt.setString(2, empId); updateStmt.setDate(3, java.sql.Date.valueOf(date)); updateStmt.executeUpdate(); } catch (Exception ignored) {}
                }
                return new String[]{ (in != null) ? in.toString().substring(0, 5) : null, (out != null) ? out.toString().substring(0, 5) : null };
            }
        } catch (Exception e) { } return new String[]{null, null};
    }

    public int[] getAttendanceCount(String empId, int month, int year) {
        int[] counts = new int[]{0, 0}; 
        String sql = "SELECT work_date FROM timekeeping WHERE employee_id = ? AND MONTH(work_date) = ? AND YEAR(work_date) = ? AND check_in IS NOT NULL";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, empId); pstmt.setInt(2, month); pstmt.setInt(3, year); ResultSet rs = pstmt.executeQuery();
            while (rs.next()) { DayOfWeek day = rs.getDate("work_date").toLocalDate().getDayOfWeek(); if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) counts[1]++; else counts[0]++; }
        } catch (Exception e) { } return counts;
    }

    public void addDepartment(String name) {
        String sql = "INSERT INTO departments (name, account_username) VALUES (?, ?)";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name); pstmt.setString(2, currentUsername); pstmt.executeUpdate();
        } catch (Exception e) { }
    }

    public List<String> getAllDepartments() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT name FROM departments WHERE account_username = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currentUsername); ResultSet rs = pstmt.executeQuery();
            while (rs.next()) { list.add(rs.getString("name")); }
        } catch (Exception e) {}
        String sqlOld = "SELECT DISTINCT department FROM employees WHERE account_username = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sqlOld)) {
            pstmt.setString(1, currentUsername); ResultSet rs = pstmt.executeQuery();
            while (rs.next()) { String d = rs.getString("department"); if (d != null && !d.isEmpty() && !list.contains(d)) list.add(d); }
        } catch (Exception e) {}
        if (list.isEmpty()) list.add("Chung"); return list;
    }

    private String currentUsername = null;
    private String currentUserRole = null; 
    public String getCurrentUsername() { return currentUsername; }
    public String getCurrentUserRole() { return currentUserRole; }
    public void logoutUser() { currentUsername = null; currentUserRole = null; }

    public String registerAdmin(String username, String password, String fullName, String email, String phone) {
        String checkSql = "SELECT * FROM users WHERE username = ?";
        String insertSql = "INSERT INTO users (username, password, role, company_code, email, phone) VALUES (?, ?, 'ADMIN', ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement checkStmt = conn.prepareStatement(checkSql); PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            checkStmt.setString(1, username); if (checkStmt.executeQuery().next()) return "Tên tài khoản đã tồn tại!"; 
            String compCode = "COMP" + (1000 + new Random().nextInt(9000));
            insertStmt.setString(1, username); insertStmt.setString(2, password); insertStmt.setString(3, compCode); insertStmt.setString(4, email); insertStmt.setString(5, phone); insertStmt.executeUpdate();
            return "SUCCESS";
        } catch (Exception e) { return "Lỗi hệ thống: " + e.getMessage(); }
    }

    public String registerEmployee(String username, String password, String fullName, String companyCode, String email, String phone) {
        String findBossSql = "SELECT username FROM users WHERE company_code = ? AND role = 'ADMIN'";
        String checkUserSql = "SELECT * FROM users WHERE username = ?";
        String insertUserSql = "INSERT INTO users (username, password, role, email, phone) VALUES (?, ?, 'EMPLOYEE', ?, ?)";
        String insertEmpSql = "INSERT INTO employees (id, name, account_username, login_username, status) VALUES (?, ?, ?, ?, 'PENDING')";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement findBossStmt = conn.prepareStatement(findBossSql); PreparedStatement checkUserStmt = conn.prepareStatement(checkUserSql); PreparedStatement insertUserStmt = conn.prepareStatement(insertUserSql); PreparedStatement insertEmpStmt = conn.prepareStatement(insertEmpSql)) {
            findBossStmt.setString(1, companyCode); ResultSet rsBoss = findBossStmt.executeQuery(); if (!rsBoss.next()) return "Mã công ty không tồn tại!";
            String bossUsername = rsBoss.getString("username");
            checkUserStmt.setString(1, username); if (checkUserStmt.executeQuery().next()) return "Tên tài khoản đã có người sử dụng!";
            insertUserStmt.setString(1, username); insertUserStmt.setString(2, password); insertUserStmt.setString(3, email); insertUserStmt.setString(4, phone); insertUserStmt.executeUpdate();
            String randomId = generateEmployeeId(); insertEmpStmt.setString(1, randomId); insertEmpStmt.setString(2, fullName); insertEmpStmt.setString(3, bossUsername); insertEmpStmt.setString(4, username); insertEmpStmt.executeUpdate();
            return "SUCCESS";
        } catch (Exception e) { return "Lỗi hệ thống: " + e.getMessage(); }
    }

    public String authenticateUser(String username, String password) {
        String sql = "SELECT password, role FROM users WHERE username = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username); ResultSet rs = pstmt.executeQuery();
            if (rs.next()) { if (password.equals(rs.getString("password"))) { currentUsername = username; currentUserRole = rs.getString("role"); return currentUserRole; } }
        } catch (Exception e) { } return null; 
    }
    
    public void changePassword(String username, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE username = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newPassword); pstmt.setString(2, username); pstmt.executeUpdate();
        } catch (Exception e) {}
    }

    public boolean resetPassword(String username, String email, String newPassword) {
        String sql = "SELECT * FROM users WHERE username = ? AND email = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username); pstmt.setString(2, email); ResultSet rs = pstmt.executeQuery();
            if (rs.next()) { changePassword(username, newPassword); return true; }
        } catch (Exception e) {} return false;
    }
    
    public String getMyCompanyCode() {
        String sql = "SELECT company_code FROM users WHERE username = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currentUsername); ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("company_code");
        } catch (Exception e) {} return "N/A";
    }

    public String applyNewJob(String fullName, String companyCode) { return "SUCCESS"; } 

    public Employee getCurrentEmployeeProfile() {
        String sql = "SELECT * FROM employees WHERE login_username = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currentUsername); ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return new Employee(rs.getString("id"), rs.getString("name"), rs.getString("department"), rs.getString("position"), rs.getDouble("baseSalary"));
        } catch (Exception e) {} return null;
    }

    public String getCurrentEmployeeStatus() {
        String sql = "SELECT status FROM employees WHERE login_username = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currentUsername); ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("status");
        } catch (Exception e) {} return "NO_JOB"; 
    }

    public void sendNotification(String message) {
        String sql = "INSERT INTO notifications (account_username, message, created_at) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currentUsername); pstmt.setString(2, message); pstmt.setDate(3, java.sql.Date.valueOf(LocalDate.now())); pstmt.executeUpdate();
        } catch (Exception e) {}
    }

    public List<String[]> getNotifications(String adminUsername) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT message, created_at FROM notifications WHERE account_username = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, adminUsername); ResultSet rs = pstmt.executeQuery();
            while(rs.next()){ list.add(new String[]{ rs.getDate("created_at").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), rs.getString("message") }); }
        } catch (Exception e) {} return list;
    }

    public String getMyAdminUsername() {
        String sql = "SELECT account_username FROM employees WHERE login_username = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currentUsername); ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("account_username");
        } catch (Exception e) {} return null;
    }

    public void saveSchedule(String empId, LocalDate date, String shift) {
        String sql = "INSERT INTO schedules (employee_id, work_date, shift) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE shift = VALUES(shift)";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, empId); pstmt.setDate(2, java.sql.Date.valueOf(date)); pstmt.setString(3, shift); pstmt.executeUpdate();
        } catch (Exception e) {}
    }

    public String getSchedule(String empId, LocalDate date) {
        String sql = "SELECT shift FROM schedules WHERE employee_id = ? AND work_date = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, empId); pstmt.setDate(2, java.sql.Date.valueOf(date)); ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("shift");
        } catch (Exception e) {} return "Chưa đăng ký";
    }

    public void reviewLeaveRequest(String empId, LocalDate date, boolean isApproved) {
        String currentShift = getSchedule(empId, date);
        if (currentShift != null && (currentShift.startsWith("Chờ duyệt nghỉ: ") || currentShift.startsWith("Xin nghỉ: "))) {
            String reason = currentShift.replace("Chờ duyệt nghỉ: ", "").replace("Xin nghỉ: ", "");
            saveSchedule(empId, date, isApproved ? "Đã duyệt nghỉ: " + reason : "Từ chối nghỉ: " + reason);
        }
    }

    public List<String[]> getDailyAbsenceReport(LocalDate date) {
        List<String[]> report = new ArrayList<>(); List<Employee> emps = getAllEmployees();
        for (Employee e : emps) {
            String shift = getSchedule(e.getId(), date); String[] time = getAttendanceRecord(e.getId(), date);
            if (shift.startsWith("Chờ duyệt nghỉ") || shift.startsWith("Xin nghỉ")) { report.add(new String[]{e.getId(), e.getName(), "Chờ duyệt", shift.replace("Chờ duyệt nghỉ: ", "").replace("Xin nghỉ: ", "")}); } 
            else if (shift.startsWith("Đã duyệt nghỉ")) { report.add(new String[]{e.getId(), e.getName(), "Nghỉ CÓ phép", shift.replace("Đã duyệt nghỉ: ", "")}); } 
            else if (shift.startsWith("Từ chối nghỉ")) { report.add(new String[]{e.getId(), e.getName(), "Bị từ chối nghỉ", shift.replace("Từ chối nghỉ: ", "")}); } 
            else if (!shift.equals("Nghỉ") && !shift.equals("Chưa đăng ký")) { if (time[0] == null && !date.isAfter(LocalDate.now())) { report.add(new String[]{e.getId(), e.getName(), "Nghỉ KHÔNG phép", "Bỏ ca: " + shift}); } }
        } return report;
    }

    public void addTask(String title, String description, String assigneeId, LocalDate deadline) {
        String sql = "INSERT INTO tasks (title, description, assignee_id, creator_username, deadline, status) VALUES (?, ?, ?, ?, ?, 'Chờ xử lý')";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title); pstmt.setString(2, description); pstmt.setString(3, assigneeId); pstmt.setString(4, currentUsername); pstmt.setDate(5, java.sql.Date.valueOf(deadline)); pstmt.executeUpdate();
        } catch (Exception e) {}
    }

    public List<Task> getTasksForEmployee(String empId) {
        List<Task> list = new ArrayList<>(); String sql = "SELECT * FROM tasks WHERE assignee_id = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, empId); ResultSet rs = pstmt.executeQuery();
            while (rs.next()) { list.add(new Task(rs.getInt("id"), rs.getString("title"), rs.getString("description"), rs.getString("assignee_id"), rs.getString("creator_username"), rs.getDate("deadline").toLocalDate(), rs.getString("status"))); }
        } catch (Exception e) {} return list;
    }

    public List<Task> getAllTasksByAdmin() {
        List<Task> list = new ArrayList<>(); String sql = "SELECT * FROM tasks WHERE creator_username = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currentUsername); ResultSet rs = pstmt.executeQuery();
            while (rs.next()) { list.add(new Task(rs.getInt("id"), rs.getString("title"), rs.getString("description"), rs.getString("assignee_id"), rs.getString("creator_username"), rs.getDate("deadline").toLocalDate(), rs.getString("status"))); }
        } catch (Exception e) {} return list;
    }

    public void updateTaskStatus(int taskId, String status) {
        String sql = "UPDATE tasks SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status); pstmt.setInt(2, taskId); pstmt.executeUpdate();
        } catch (Exception e) {}
    }
    
    public void deleteTask(int taskId) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, taskId); pstmt.executeUpdate();
        } catch (Exception e) {}
    }

    // =========================================================
    // 12. TÍNH NĂNG MỚI: QUẢN LÝ KPI VÀ TÍNH THƯỞNG
    // =========================================================
    public void saveKPI(String empId, int month, int year, int score, String note) {
        String sql = "INSERT INTO kpi_records (employee_id, kpi_month, kpi_year, score, note) VALUES (?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE score = VALUES(score), note = VALUES(note)";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, empId);
            pstmt.setInt(2, month);
            pstmt.setInt(3, year);
            pstmt.setInt(4, score);
            pstmt.setString(5, note);
            pstmt.executeUpdate();
        } catch (Exception e) { System.err.println("Lỗi lưu KPI: " + e.getMessage()); }
    }

    public int getKPI(String empId, int month, int year) {
        String sql = "SELECT score FROM kpi_records WHERE employee_id = ? AND kpi_month = ? AND kpi_year = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, empId);
            pstmt.setInt(2, month);
            pstmt.setInt(3, year);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("score");
        } catch (Exception e) {}
        return 0; // Trả về 0 nếu chưa chấm
    }
}