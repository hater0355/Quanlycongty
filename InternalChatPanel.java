package BaiTapLon;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

public class InternalChatPanel extends JPanel {
    private String myId;
    private String targetId = null; 
    private int targetGroupId = -1; 
    private boolean isGroupMode = false;
    private int lastMessageCount = 0;

    private DefaultListModel<String> listModelContacts = new DefaultListModel<>();
    private JList<String> listContacts = new JList<>(listModelContacts);
    
    // ĐÃ THAY: JPanel chứa các tin nhắn dạng component riêng biệt thay vì JEditorPane Concatenated HTML
    private JPanel messagesContainer = new JPanel();
    private JScrollPane scrollChatArea;
    private JTextField txtInput = new RoundedTextField(20);
    
    private JButton btnImage = new JButton(new CameraIcon()); 
    private JButton btnSend = new RoundedButton("Gửi");
    private JLabel lblChatTitle = new JLabel("Hãy chọn một cuộc hội thoại");

    public InternalChatPanel(String myId) {
        this.myId = myId;
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(243, 244, 246)); 
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // ==========================================
        // 1. CỘT BÊN TRÁI: DANH BẠ 
        // ==========================================
        JPanel leftPanel = new JPanel(new BorderLayout(0, 10));
        leftPanel.setPreferredSize(new Dimension(280, 0));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
            new EmptyBorder(15, 10, 15, 10)
        ));
        
        JLabel lblLeftTitle = new JLabel("Tin nhắn");
        lblLeftTitle.setFont(new Font("Tahoma", Font.BOLD, 22));
        lblLeftTitle.setForeground(new Color(17, 24, 39));
        leftPanel.add(lblLeftTitle, BorderLayout.NORTH);
        
        listContacts.setCellRenderer(new ContactListRenderer());
        listContacts.setBackground(Color.WHITE);
        listContacts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listContacts.setBorder(null);
        loadContacts();
        
        JScrollPane scrollContacts = new JScrollPane(listContacts);
        scrollContacts.setBorder(null);
        scrollContacts.getVerticalScrollBar().setPreferredSize(new Dimension(5, 0));
        leftPanel.add(scrollContacts, BorderLayout.CENTER);
        
        JButton btnNewGroup = new RoundedButton("+ Tạo Nhóm Chat");
        btnNewGroup.setBackground(new Color(16, 185, 129));
        btnNewGroup.setForeground(Color.WHITE);
        btnNewGroup.setFont(new Font("Tahoma", Font.BOLD, 14));
        btnNewGroup.setPreferredSize(new Dimension(0, 45));
        btnNewGroup.addActionListener(e -> createGroupDialog());
        leftPanel.add(btnNewGroup, BorderLayout.SOUTH);

        // ==========================================
        // 2. KHUNG CHAT Ở GIỮA (Đã Đại Tu Toàn Bộ)
        // ==========================================
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235), 1));
        
        // Header
        JPanel chatHeader = new JPanel(new BorderLayout());
        chatHeader.setBackground(Color.WHITE);
        chatHeader.setBorder(new EmptyBorder(15, 20, 15, 20));
        chatHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 231, 235)));
        lblChatTitle.setFont(new Font("Tahoma", Font.BOLD, 18));
        lblChatTitle.setForeground(new Color(17, 24, 39));
        chatHeader.add(lblChatTitle, BorderLayout.WEST);
        
        // Messages Container (BoxLayout Xếp chồng Y-axis)
        messagesContainer.setLayout(new BoxLayout(messagesContainer, BoxLayout.Y_AXIS));
        messagesContainer.setBackground(new Color(249, 250, 251)); 
        messagesContainer.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        scrollChatArea = new JScrollPane(messagesContainer);
        scrollChatArea.setBorder(null);
        scrollChatArea.getVerticalScrollBar().setUnitIncrement(16); // Scroll mượt
        
        // Vùng nhập liệu
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        txtInput.setFont(new Font("Tahoma", Font.PLAIN, 15));
        txtInput.setPreferredSize(new Dimension(0, 45));
        
        btnImage.setContentAreaFilled(false); btnImage.setBorderPainted(false); btnImage.setFocusPainted(false);
        btnImage.setCursor(new Cursor(Cursor.HAND_CURSOR)); btnImage.setPreferredSize(new Dimension(50, 45));
        
        btnSend.setBackground(new Color(59, 130, 246)); btnSend.setForeground(Color.WHITE);
        btnSend.setFont(new Font("Tahoma", Font.BOLD, 15)); btnSend.setPreferredSize(new Dimension(90, 45));

        JPanel rightInputGrp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        rightInputGrp.setBackground(Color.WHITE); rightInputGrp.add(btnImage); rightInputGrp.add(btnSend);

        inputPanel.add(txtInput, BorderLayout.CENTER);
        inputPanel.add(rightInputGrp, BorderLayout.EAST);

        centerPanel.add(chatHeader, BorderLayout.NORTH);
        centerPanel.add(scrollChatArea, BorderLayout.CENTER);
        centerPanel.add(inputPanel, BorderLayout.SOUTH);

        add(leftPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);

        // ==========================================
        // 3. XỬ LÝ SỰ KIỆN
        // ==========================================
        listContacts.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = listContacts.getSelectedValue();
                if (selected == null) return;

                if (selected.startsWith("[NHÓM]")) {
                    isGroupMode = true;
                    try {
                        String idStr = selected.substring(selected.lastIndexOf("ID:") + 3, selected.length() - 1);
                        targetGroupId = Integer.parseInt(idStr.trim());
                        lblChatTitle.setText("Nhóm: " + selected.split(" \\(ID")[0].replace("[NHÓM] ", ""));
                    } catch (Exception ex) {}
                } else {
                    isGroupMode = false;
                    targetId = selected.split(" - ")[0];
                    lblChatTitle.setText("Trò chuyện với: " + selected.split(" - ")[1]);
                }
                refreshChatHistory(true); // Force scroll to bottom when changing contact
            }
        });

        btnSend.addActionListener(e -> sendMessage());
        txtInput.addActionListener(e -> sendMessage());
        btnImage.addActionListener(e -> sendImage());

        // Tự động refresh chat mỗi 2 giây, không force scroll để user rảnh tay đọc tin cũ
        new Timer(2000, e -> refreshChatHistory(false)).start();
    }

    private void loadContacts() {
        listModelContacts.clear();
        for (String[] g : EmployeeManager.getInstance().getMyGroups(myId)) {
            listModelContacts.addElement("[NHÓM] " + g[1] + " (ID:" + g[0] + ")");
        }
        for (Employee e : EmployeeManager.getInstance().getAllEmployees()) {
            if (!e.getId().equals(myId)) {
                listModelContacts.addElement(e.getId() + " - " + e.getName());
            }
        }
    }

    private void sendMessage() {
        String msg = txtInput.getText().trim();
        if (msg.isEmpty()) return;
        if (targetId == null && !isGroupMode) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn người nhận!"); return;
        }

        if (isGroupMode && targetGroupId != -1) {
            EmployeeManager.getInstance().sendGroupMessage(myId, targetGroupId, msg);
        } else if (targetId != null) {
            EmployeeManager.getInstance().sendPrivateMessage(myId, targetId, msg);
        }
        txtInput.setText("");
        refreshChatHistory(true);
    }

    private void sendImage() {
        if (targetId == null && !isGroupMode) { JOptionPane.showMessageDialog(this, "Vui lòng chọn người để gửi ảnh!"); return; }
        JFileChooser fileChooser = new JFileChooser(); fileChooser.setDialogTitle("Chọn ảnh (JPG, PNG)"); fileChooser.setFileFilter(new FileNameExtensionFilter("Hình ảnh", "jpg", "jpeg", "png"));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File selectedFile = fileChooser.getSelectedFile(); BufferedImage original = ImageIO.read(selectedFile);
                if (original == null) { JOptionPane.showMessageDialog(this, "File ảnh không hợp lệ!"); return; }
                int newWidth = 220; int newHeight = (newWidth * original.getHeight()) / original.getWidth();
                Image smoothImg = original.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                BufferedImage thumb = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = thumb.createGraphics(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setClip(new RoundRectangle2D.Float(0, 0, newWidth, newHeight, 15, 15)); g2.drawImage(smoothImg, 0, 0, null); g2.dispose();
                File dir = new File("ChatFiles/Images"); if (!dir.exists()) dir.mkdirs();
                String newFileName = System.currentTimeMillis() + "_thumb.png"; File destFile = new File(dir, newFileName); ImageIO.write(thumb, "png", destFile);
                String msgPath = "[IMG]" + destFile.getAbsolutePath();
                if (isGroupMode && targetGroupId != -1) { EmployeeManager.getInstance().sendGroupMessage(myId, targetGroupId, msgPath); } 
                else if (targetId != null) { EmployeeManager.getInstance().sendPrivateMessage(myId, targetId, msgPath); }
                refreshChatHistory(true);
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Có lỗi khi xử lý ảnh: " + ex.getMessage()); }
        }
    }

    // ==========================================
    // REFRESH CHAT: ĐẠI TU COMPONENT-BASED
    // ==========================================
    private void refreshChatHistory(boolean forceScroll) {
        if (!isGroupMode && targetId != null) {
            List<String[]> logs = EmployeeManager.getInstance().getPrivateChatHistory(myId, targetId);
            
            // Chỉ redraw nếu số lượng tin nhắn thay đổi (tránh nhấp nháy UI)
            if (messagesContainer.getComponentCount() != logs.size()) {
                messagesContainer.removeAll();
                
                for (String[] line : logs) {
                    messagesContainer.add(new MessageBubblePanel(line));
                    messagesContainer.add(Box.createRigidArea(new Dimension(0, 15))); // Khoảng cách giữa các tin nhắn
                }
                messagesContainer.revalidate(); messagesContainer.repaint();
                
                if (forceScroll) {
                    SwingUtilities.invokeLater(() -> {
                        JScrollBar vertical = scrollChatArea.getVerticalScrollBar();
                        vertical.setValue(vertical.getMaximum());
                    });
                }
            }
        }
    }

    private void createGroupDialog() {
        String name = JOptionPane.showInputDialog(this, "Nhập tên nhóm chat mới:");
        if (name == null || name.trim().isEmpty()) return;
        List<String> members = new ArrayList<>(); members.add(myId);
        for(Employee e : EmployeeManager.getInstance().getAllEmployees()) { if(!e.getId().equals(myId)) members.add(e.getId()); }
        EmployeeManager.getInstance().createGroup(name.trim(), myId, members); loadContacts(); JOptionPane.showMessageDialog(this, "Đã tạo nhóm thành công!");
    }

    // ==============================================================
    // THÀNH PHẦN MỚI QUAN TRỌNG: LÀM ĐẸP BONG BÓNG CHAT + CHỨC NĂNG
    // ==============================================================
    class MessageBubblePanel extends JPanel {
        private String[] logData;
        private JEditorPane htmlContentPane;

        public MessageBubblePanel(String[] logData) {
            this.logData = logData;
            boolean isMe = logData[0].equals(myId);
            String time = logData[2].substring(11, 16);
            String rawContent = logData[1];
            
            setLayout(new BorderLayout());
            setOpaque(false);
            
            // Setup màu sắc như yêu cầu (Mất màu xanh, xịn xò)
            // Khách (Dưới): Màu xám nhạt | Me (Trên): Màu xám đậm hơn chút
            Color bgColor = isMe ? new Color(209, 213, 219) : new Color(229, 231, 235);
            Color fgColor = isMe ? Color.BLACK : new Color(17, 24, 39);

            // Xử lý nội dung HTML
            htmlContentPane = new JEditorPane();
            htmlContentPane.setContentType("text/html");
            htmlContentPane.setEditable(false);
            htmlContentPane.setOpaque(false);
            htmlContentPane.setBackground(new Color(0,0,0,0)); // Trong suốt hoàn toàn

            String finalHtmlContent = "";
            if (rawContent.startsWith("[IMG]")) {
                String imgPath = rawContent.substring(5);
                String fileUrl = new File(imgPath).toURI().toString();
                finalHtmlContent = "<html><body><img src='" + fileUrl + "'></body></html>";
            } else {
                String safeText = rawContent.replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>");
                
                // HTML structure: Dùng bảng lồng bảng để fix lỗi ríu chữ 100%
                htmlContentPane.setText("<html><body style='font-family: Tahoma; font-size: 13px; margin: 0px;'>"+
                    "<table border='0' cellspacing='0' cellpadding='8' width='100%'><tr><td>"+
                        (isMe ? "" : "<b style='color:#6B7280; font-size:11px;'>"+logData[0]+"</b><br>")+
                        "<b style='color:"+(isMe ? "black" : "#111827")+";'>"+safeText+"</b><br>"+
                        "<div align='"+(isMe ? "right" : "left")+"'><span style='color:#6B7280; font-size:10px;'>"+time+"</span></div>"+
                    "</td></tr></table>"+
                    "</body></html>");
            }
            
            if (rawContent.startsWith("[IMG]")) { htmlContentPane.setText(finalHtmlContent); }

            // Thêm JEditorPane vào Panel Bong bóng chat
            JPanel bubbleWrapper = new JPanel(new BorderLayout());
            bubbleWrapper.setBackground(bgColor);
            bubbleWrapper.setBorder(new EmptyBorder(0, 0, 0, 0));
            bubbleWrapper.setOpaque(true);
            bubbleWrapper.add(htmlContentPane, BorderLayout.CENTER);
            
            // Bo góc cho Panel Bong bóng (Java Swing không bo được component thường, ta dùng mẹo lồng Panel)
            JPanel roundedWrapper = new JPanel(new BorderLayout()) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(bgColor);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15); // Bo góc 15px
                    g2.dispose();
                }
            };
            roundedWrapper.setOpaque(false);
            roundedWrapper.add(bubbleWrapper, BorderLayout.CENTER);
            
            // Alignment: Ép Max Width và Alignment của JPanel
            int maxWidth = 450; 
            if (rawContent.startsWith("[IMG]")) { roundedWrapper.setMaximumSize(new Dimension(220 + 20, 1000)); }
            else { roundedWrapper.setMaximumSize(new Dimension(maxWidth, 1000)); } // Giới hạn width max
            
            // Thêm alignment wrapper Panel
            JPanel alignPanel = new JPanel(new FlowLayout(isMe ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
            alignPanel.setOpaque(false);
            alignPanel.add(roundedWrapper);
            add(alignPanel, BorderLayout.CENTER);

            // ==========================================
            // THÊM CHỨC NĂNG: COPY / SỬA / XÓA TIN NHẮN
            // ==========================================
            JPopupMenu popupMenu = new JPopupMenu();
            
            // 1. Copy Chữ
            if (!rawContent.startsWith("[IMG]")) {
                JMenuItem copyItem = new JMenuItem("Copy tin nhắn");
                copyItem.addActionListener(e -> {
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(rawContent), null);
                    JOptionPane.showMessageDialog(this, "Đã Copy!");
                });
                popupMenu.add(copyItem);
            } else {
                // Copy Ảnh (Mẹo nhỏ)
                JMenuItem copyPathItem = new JMenuItem("Copy đường dẫn ảnh");
                copyPathItem.addActionListener(e -> {
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(rawContent.substring(5)), null);
                    JOptionPane.showMessageDialog(this, "Đã Copy đường dẫn!");
                });
                popupMenu.add(copyPathItem);
            }

            // 2. Chỉnh sửa & Xóa (Chỉ cho tin nhắn của MÌNH)
            if (isMe) {
                popupMenu.addSeparator();
                
                // Chỉ Edit được Chữ
                if (!rawContent.startsWith("[IMG]")) {
                    JMenuItem editItem = new JMenuItem("Sửa tin nhắn");
                    editItem.addActionListener(e -> editMessage());
                    popupMenu.add(editItem);
                }

                JMenuItem deleteItem = new JMenuItem("Xóa (Thu hồi)");
                deleteItem.addActionListener(e -> deleteMessage());
                popupMenu.add(deleteItem);
            }

            // Gắn sự kiện chuột vào cả Panel Bong bóng và HTML Content
            MouseAdapter popupAdapter = new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) { maybeShowPopup(e); }
                @Override public void mouseReleased(MouseEvent e) { maybeShowPopup(e); }
                private void maybeShowPopup(MouseEvent e) {
                    if (e.isPopupTrigger()) { popupMenu.show(e.getComponent(), e.getX(), e.getY()); }
                }
            };
            htmlContentPane.addMouseListener(popupAdapter);
            roundedWrapper.addMouseListener(popupAdapter);
        }

        private void editMessage() {
            String rawContent = logData[1];
            String newText = JOptionPane.showInputDialog(InternalChatPanel.this, "Sửa tin nhắn của bạn:", rawContent);
            if (newText != null && !newText.trim().isEmpty() && !newText.trim().equals(rawContent)) {
                // Note: EmployeeManager cần hàm này (Sẽ code tiếp ở dưới)
                EmployeeManager.getInstance().editMessageById(logData[0], logData[2], newText.trim()); 
                refreshChatHistory(true); // Redraw
                JOptionPane.showMessageDialog(this, "Đã sửa thành công!");
            }
        }

        private void deleteMessage() {
            if (JOptionPane.showConfirmDialog(InternalChatPanel.this, "Bạn có chắc muốn thu hồi tin nhắn này?", "Thu hồi tin nhắn", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                // Note: EmployeeManager cần hàm này (Sẽ code tiếp ở dưới)
                EmployeeManager.getInstance().deleteMessageById(logData[0], logData[2]); 
                refreshChatHistory(true); // Redraw
                JOptionPane.showMessageDialog(this, "Đã thu hồi tin nhắn!");
            }
        }
    }

    // ==========================================
    // CLASS VẼ ICON & RENDER DANH BẠ (DŨNG ICON TỰ VẼ)
    // ==========================================
    class CameraIcon implements Icon { public int getIconWidth() { return 24; } public int getIconHeight() { return 24; } public void paintIcon(Component c, Graphics g, int x, int y) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(new Color(107, 114, 128)); g2.fillRoundRect(x+2, y+6, 20, 14, 6, 6); g2.setColor(Color.WHITE); g2.fillOval(x+8, y+9, 8, 8); g2.setColor(new Color(107, 114, 128)); g2.fillOval(x+10, y+11, 4, 4); g2.fillRect(x+6, y+4, 6, 3); g2.fillRect(x+16, y+8, 4, 3); g2.dispose(); } }
    class AvatarIcon implements Icon { boolean isGroup; public AvatarIcon(boolean isGroup) { this.isGroup = isGroup; } public int getIconWidth() { return 36; } public int getIconHeight() { return 36; } public void paintIcon(Component c, Graphics g, int x, int y) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(new Color(156, 163, 175)); if(isGroup) { g2.fillOval(x+4, y+6, 12, 12); g2.fillArc(x, y+18, 20, 20, 0, 180); g2.fillOval(x+18, y+8, 10, 10); g2.fillArc(x+14, y+18, 18, 18, 0, 180); } else { g2.fillOval(x+10, y+4, 16, 16); g2.fillArc(x+4, y+20, 28, 24, 0, 180); } g2.dispose(); } }
    class ContactListRenderer extends DefaultListCellRenderer { @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) { JPanel panel = new JPanel(new BorderLayout(15, 0)); panel.setBorder(new EmptyBorder(12, 15, 12, 15)); panel.setOpaque(true); if (isSelected) { panel.setBackground(new Color(239, 246, 255)); } else { panel.setBackground(Color.WHITE); } String text = value.toString(); boolean isGroup = text.startsWith("[NHÓM]"); JLabel lblAvatar = new JLabel(new AvatarIcon(isGroup)); String displayName = isGroup ? text.split(" \\(ID")[0].replace("[NHÓM] ", "") : text.split(" - ")[1]; JLabel lblName = new JLabel(displayName); lblName.setFont(new Font("Tahoma", Font.BOLD, 14)); lblName.setForeground(isSelected ? new Color(29, 78, 216) : new Color(17, 24, 39)); panel.add(lblAvatar, BorderLayout.WEST); panel.add(lblName, BorderLayout.CENTER); return panel; } }
}