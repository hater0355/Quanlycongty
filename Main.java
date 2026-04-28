package BaiTapLon;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        
        // GỌI HÀM KÍCH HOẠT GIAO DIỆN SIÊU VIP VỪA TẠO
        ThemeManager.applyAdvancedFlatLaf();

        // Chạy ứng dụng trên Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            new LoginUI();
        });
    }
}