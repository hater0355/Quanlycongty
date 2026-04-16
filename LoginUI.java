package BaiTapLon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LoginUI extends JFrame {
    
    private CardLayout cardLayout;
    private JPanel mainPanel;

    public LoginUI() {
        setTitle("Hệ Thống Quản Lý Lương & Nhân Sự");
        setSize(420, 550); 
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(createLoginCard(), "Login");
        mainPanel.add(createRegisterCard(), "Register");

        add(mainPanel);
        setVisible(true);
    }

    // ==========================================
    // 1. MÀN HÌNH ĐĂNG NHẬP
    // ==========================================
    private JPanel createLoginCard() {
        JPanel panel = new JPanel(null);
        panel.setBackground(new Color(18, 25, 35));

        JLabel title = new JLabel("ĐĂNG NHẬP", SwingConstants.CENTER);
        title.setBounds(0, 40, 420, 40);
        title.setForeground(new Color(245, 158, 11)); 
        title.setFont(new Font("Tahoma", Font.BOLD, 28));

        JLabel lblUser = new JLabel("Tên tài khoản:");
        lblUser.setForeground(Color.LIGHT_GRAY);
        lblUser.setBounds(60, 110, 300, 20);
        JTextField txtUser = new JTextField();
        txtUser.setBounds(60, 130, 300, 40);
        
        JLabel lblPass = new JLabel("Mật khẩu:");
        lblPass.setForeground(Color.LIGHT_GRAY);
        lblPass.setBounds(60, 180, 300, 20);
        JPasswordField txtPass = new JPasswordField();
        txtPass.setBounds(60, 200, 300, 40);

        JButton btnLogin = new JButton("Đăng nhập ngay");
        btnLogin.setBounds(60, 270, 300, 45);
        btnLogin.setBackground(new Color(245, 158, 11));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Tahoma", Font.BOLD, 14));
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblSwitch = new JLabel("Chưa có tài khoản? Đăng ký tại đây.", SwingConstants.CENTER);
        lblSwitch.setBounds(0, 330, 420, 20);
        lblSwitch.setForeground(new Color(59, 130, 246)); 
        lblSwitch.setCursor(new Cursor(Cursor.HAND_CURSOR));

        lblSwitch.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cardLayout.show(mainPanel, "Register");
            }
        });

        btnLogin.addActionListener(e -> {
            String u = txtUser.getText().trim();
            String p = new String(txtPass.getPassword());

            if (u.isEmpty() || p.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Lấy chức vụ sau khi xác thực
            String role = EmployeeManager.getInstance().authenticateUser(u, p);
            
            if (role != null) {
                if (role.equals("ADMIN")) {
                    new DashboardUI(); 
                    dispose(); 
                } else {
                    // KIỂM TRA MẬT KHẨU MẶC ĐỊNH
                    if (p.equals("123")) {
                        JPasswordField pf = new JPasswordField();
                        int okCxl = JOptionPane.showConfirmDialog(this, pf, 
                            "⚠️ YÊU CẦU BẢO MẬT\nĐây là tài khoản do Công ty cấp phát.\nVui lòng nhập mật khẩu mới của riêng bạn để tiếp tục:", 
                            JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                        
                        if (okCxl == JOptionPane.OK_OPTION) {
                            String newPass = new String(pf.getPassword());
                            if (newPass.isEmpty() || newPass.equals("123")) {
                                JOptionPane.showMessageDialog(this, "Mật khẩu mới không được để trống hoặc giống mật khẩu mặc định!");
                                EmployeeManager.getInstance().logoutUser(); 
                                return; // Vẫn ở lại màn hình Login
                            }
                            // Đổi mật khẩu
                            EmployeeManager.getInstance().changePassword(u, newPass);
                            JOptionPane.showMessageDialog(this, "✅ Đổi mật khẩu thành công! Đang vào hệ thống...");
                        } else {
                            EmployeeManager.getInstance().logoutUser();
                            return; // Người dùng bấm Hủy -> Không cho vào hệ thống
                        }
                    }
                    
                    new EmployeeDashboardUI();
                    dispose(); 
                }
            } else {
                JOptionPane.showMessageDialog(this, "Sai tài khoản hoặc mật khẩu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(title);
        panel.add(lblUser); panel.add(txtUser);
        panel.add(lblPass); panel.add(txtPass);
        panel.add(btnLogin);
        panel.add(lblSwitch);

        return panel;
    }

    // ==========================================
    // 2. MÀN HÌNH ĐĂNG KÝ (Có chọn loại tài khoản)
    // ==========================================
    private JPanel createRegisterCard() {
        JPanel panel = new JPanel(null);
        panel.setBackground(new Color(18, 25, 35));

        JLabel title = new JLabel("TẠO TÀI KHOẢN", SwingConstants.CENTER);
        title.setBounds(0, 20, 420, 40);
        title.setForeground(new Color(16, 185, 129)); 
        title.setFont(new Font("Tahoma", Font.BOLD, 28));

        JLabel lblRole = new JLabel("Bạn là ai?");
        lblRole.setForeground(Color.LIGHT_GRAY);
        lblRole.setBounds(60, 80, 300, 20);
        
        JComboBox<String> cbRole = new JComboBox<>(new String[]{"Người Quản Lý (Tạo công ty)", "Nhân Viên (Xin việc)"});
        cbRole.setBounds(60, 100, 300, 35);

        JLabel lblUser = new JLabel("Tên tài khoản (Viết liền không dấu):");
        lblUser.setForeground(Color.LIGHT_GRAY);
        lblUser.setBounds(60, 140, 300, 20);
        JTextField txtUser = new JTextField();
        txtUser.setBounds(60, 160, 300, 35);
        
        JLabel lblFullName = new JLabel("Họ và Tên thật (Dành cho Nhân viên):");
        lblFullName.setForeground(Color.GRAY);
        lblFullName.setBounds(60, 200, 300, 20);
        JTextField txtFullName = new JTextField();
        txtFullName.setBounds(60, 220, 300, 35);
        txtFullName.setEnabled(false); // Khóa lại vì mặc định đang chọn Quản lý

        JLabel lblCode = new JLabel("Mã công ty (Dành cho Nhân viên):");
        lblCode.setForeground(Color.GRAY);
        lblCode.setBounds(60, 260, 300, 20);
        JTextField txtCode = new JTextField();
        txtCode.setBounds(60, 280, 300, 35);
        txtCode.setEnabled(false); // Khóa lại vì mặc định đang chọn Quản lý
        
        JLabel lblPass = new JLabel("Mật khẩu:");
        lblPass.setForeground(Color.LIGHT_GRAY);
        lblPass.setBounds(60, 320, 300, 20);
        JPasswordField txtPass = new JPasswordField();
        txtPass.setBounds(60, 340, 300, 35);

        // --- SỰ KIỆN: Khi thay đổi Role (Quản lý / Nhân viên) ---
        cbRole.addActionListener(e -> {
            boolean isEmployee = cbRole.getSelectedIndex() == 1;
            txtFullName.setEnabled(isEmployee);
            txtCode.setEnabled(isEmployee);
            
            // Đổi màu chữ label để báo hiệu ô nào đang được dùng
            if (isEmployee) {
                lblFullName.setForeground(Color.LIGHT_GRAY);
                lblCode.setForeground(Color.LIGHT_GRAY);
            } else {
                lblFullName.setForeground(Color.GRAY);
                lblCode.setForeground(Color.GRAY);
                txtFullName.setText(""); // Xóa chữ nếu chuyển lại về quản lý
                txtCode.setText("");
            }
        });

        JButton btnRegister = new JButton("Hoàn tất đăng ký");
        btnRegister.setBounds(60, 400, 300, 45);
        btnRegister.setBackground(new Color(16, 185, 129));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFont(new Font("Tahoma", Font.BOLD, 14));
        btnRegister.setFocusPainted(false);
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblSwitch = new JLabel("Đã có tài khoản? Quay lại Đăng nhập.", SwingConstants.CENTER);
        lblSwitch.setBounds(0, 460, 420, 20);
        lblSwitch.setForeground(new Color(59, 130, 246));
        lblSwitch.setCursor(new Cursor(Cursor.HAND_CURSOR));

        lblSwitch.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cardLayout.show(mainPanel, "Login");
            }
        });

        btnRegister.addActionListener(e -> {
            String role = cbRole.getSelectedIndex() == 0 ? "ADMIN" : "EMPLOYEE";
            String u = txtUser.getText().trim();
            String p = new String(txtPass.getPassword());

            if (u.isEmpty() || p.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng điền đủ Tên tài khoản và Mật khẩu!");
                return;
            }

            if (role.equals("ADMIN")) {
                // Xử lý tạo Admin
                if(EmployeeManager.getInstance().registerAdmin(u, p)) {
                    JOptionPane.showMessageDialog(this, "Tạo tài khoản Quản lý thành công!\n(Bạn có thể lấy Mã công ty ở góc dưới bên trái màn hình làm việc)");
                    cardLayout.show(mainPanel, "Login"); // Chuyển về màn login
                } else {
                    JOptionPane.showMessageDialog(this, "Tên tài khoản này đã có người sử dụng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // Xử lý tạo Nhân viên xin việc
                String name = txtFullName.getText().trim();
                String code = txtCode.getText().trim();
                
                if (name.isEmpty() || code.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Nhân viên bắt buộc phải điền đủ Họ tên và Mã công ty giới thiệu!");
                    return;
                }

                String result = EmployeeManager.getInstance().registerEmployee(u, p, name, code);
                if (result.equals("SUCCESS")) {
                    JOptionPane.showMessageDialog(this, "Đăng ký thành công!\n\nHồ sơ xin việc của bạn đã được gửi. Vui lòng chờ Giám đốc duyệt mới có thể hiển thị trong công ty.");
                    cardLayout.show(mainPanel, "Login"); // Chuyển về màn login
                } else {
                    JOptionPane.showMessageDialog(this, result, "Lỗi đăng ký", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        panel.add(title);
        panel.add(lblRole); panel.add(cbRole);
        panel.add(lblUser); panel.add(txtUser);
        panel.add(lblFullName); panel.add(txtFullName);
        panel.add(lblCode); panel.add(txtCode);
        panel.add(lblPass); panel.add(txtPass);
        panel.add(btnRegister);
        panel.add(lblSwitch);

        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginUI());
    }
}