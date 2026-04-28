package BaiTapLon;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Window;
import java.awt.Dimension;

public class ThemeManager {
    public static boolean isDarkMode = false;
    
    public static Color COLOR_ACCENT = new Color(59, 130, 246); 
    
    public static Color BG_SIDEBAR = new Color(243, 244, 246);
    public static Color BG_MAIN = new Color(255, 255, 255);
    public static Color BG_CARD = new Color(249, 250, 251);
    public static Color TEXT_PRIMARY = new Color(17, 24, 39);
    public static Color TEXT_SECONDARY = new Color(107, 114, 128);

    // ========================================================
    // GÓI TÚT LẠI NHAN SẮC TOÀN DIỆN CHO JAVA SWING
    // ========================================================
    public static void applyAdvancedFlatLaf() {
        // 1. Font chữ hiện đại: Dùng Segoe UI (Của Win 11) mỏng, thanh và rõ nét hơn Tahoma
        UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 14));
        
        // 2. Bo tròn toàn cục (Biến mọi ô nhập, ComboBox, nút bấm mặc định thành bo tròn)
        UIManager.put("Button.arc", 15);
        UIManager.put("Component.arc", 15);
        UIManager.put("TextComponent.arc", 15);
        UIManager.put("ProgressBar.arc", 15);
        
        // 3. Thanh cuộn (Scrollbar) chuẩn MacOS: Tàng hình nút mũi tên, bo tròn, có đệm
        UIManager.put("ScrollBar.showButtons", false);
        UIManager.put("ScrollBar.thumbArc", 10);
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
        
        // 4. Bỏ viền đứt nét thô kệch khi click chuột vào nút bấm
        UIManager.put("Button.focus", new Color(0,0,0,0));
        UIManager.put("Component.focusWidth", 1); // Viền phát sáng mỏng tinh tế
        
        // 5. Tút lại Bảng (JTable) sang trọng như giao diện Web
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false); // Bỏ cột dọc rườm rà
        UIManager.put("Table.intercellSpacing", new Dimension(0, 1));
        UIManager.put("Table.selectionBackground", new Color(59, 130, 246, 50)); // Màu chọn dòng xanh trong suốt
        UIManager.put("Table.rowHeight", 40); // Hàng cao, thoáng mắt
        
        // 6. Áp dụng Theme
        toggleDarkMode(isDarkMode);
    }

    public static void setAccentColor(Color color) {
        COLOR_ACCENT = color;
        UIManager.put("Component.accentColor", color);
        updateAllUI();
    }

    public static void toggleDarkMode() {
        toggleDarkMode(!isDarkMode);
    }
    
    private static void toggleDarkMode(boolean dark) {
        isDarkMode = dark;
        
        if (isDarkMode) {
            try { UIManager.setLookAndFeel(new FlatDarkLaf()); } catch (Exception e) {}
            BG_SIDEBAR = new Color(30, 30, 30);
            BG_MAIN = new Color(18, 18, 18);
            BG_CARD = new Color(43, 43, 43);
            TEXT_PRIMARY = new Color(243, 244, 246);
            TEXT_SECONDARY = new Color(156, 163, 175);
            UIManager.put("Table.selectionForeground", Color.WHITE);
        } else {
            try { UIManager.setLookAndFeel(new FlatLightLaf()); } catch (Exception e) {}
            BG_SIDEBAR = new Color(243, 244, 246);
            BG_MAIN = new Color(255, 255, 255);
            BG_CARD = new Color(249, 250, 251);
            TEXT_PRIMARY = new Color(17, 24, 39);
            TEXT_SECONDARY = new Color(107, 114, 128);
            UIManager.put("Table.selectionForeground", Color.BLACK);
        }
        
        UIManager.put("Component.accentColor", COLOR_ACCENT);
        updateAllUI();
    }

    private static void updateAllUI() {
        for (Window window : Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
        }
    }
}