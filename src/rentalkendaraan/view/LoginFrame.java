package rentalkendaraan.view;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import rentalkendaraan.service.RentalManager;

/**
 * Tampilan Frame Autentikasi Login Pengguna Aplikasi.
 * Menerapkan form input dengan JPasswordField, JCheckBox intip password, JComboBox peran,
 * dan event handling navigasi ke MainDashboardFrame dengan tema Modern Green and White.
 */
public class LoginFrame extends JFrame {
    private final RentalManager manager;

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JCheckBox chkLihatPassword;
    private JComboBox<String> cbRole;
    private JButton btnLogin;
    private JButton btnKeluar;

    public LoginFrame(RentalManager manager) {
        this.manager = manager;
        initComponents();
    }

    private void initComponents() {
        setTitle("Login - Sistem Rental Kendaraan Jaya Rent (Kelompok 7)");
        setSize(460, 470);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.SOFT_GREEN);

        // --- 1. HEADER PANEL BANNER (DARK GREEN) ---
        JPanel panelHeader = new JPanel(new GridLayout(2, 1, 2, 4));
        panelHeader.setBackground(UITheme.DARK_GREEN);
        panelHeader.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("RENTAL KENDARAAN JAYA", JLabel.CENTER);
        lblTitle.setFont(UITheme.FONT_TITLE_LARGE);
        lblTitle.setForeground(UITheme.WHITE);
        panelHeader.add(lblTitle);

        JLabel lblSubtitle = new JLabel("Sistem Manajemen Rental Armada - Kelompok 7 (PBO)", JLabel.CENTER);
        lblSubtitle.setFont(UITheme.FONT_REGULAR);
        lblSubtitle.setForeground(UITheme.LIGHT_GREEN);
        panelHeader.add(lblSubtitle);

        add(panelHeader, BorderLayout.NORTH);

        // --- 2. BODY FORM PANEL (MODERN WHITE CARD) ---
        JPanel panelWrapper = new JPanel(new BorderLayout());
        panelWrapper.setOpaque(false);
        panelWrapper.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel panelBody = new JPanel(new GridBagLayout());
        panelBody.setBackground(UITheme.WHITE);
        panelBody.setBorder(UITheme.createCardBorder());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Username
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.35;
        JLabel lblUser = new JLabel("Nama Pengguna:");
        lblUser.setFont(UITheme.FONT_BOLD);
        lblUser.setForeground(UITheme.TEXT_PRIMARY);
        panelBody.add(lblUser, gbc);

        gbc.gridx = 1; gbc.weightx = 0.65;
        txtUsername = new JTextField("admin");
        txtUsername.setFont(UITheme.FONT_REGULAR);
        txtUsername.setBorder(UITheme.createFieldBorder());
        txtUsername.setPreferredSize(new Dimension(220, 34));
        panelBody.add(txtUsername, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel lblPass = new JLabel("Kata Sandi:");
        lblPass.setFont(UITheme.FONT_BOLD);
        lblPass.setForeground(UITheme.TEXT_PRIMARY);
        panelBody.add(lblPass, gbc);

        gbc.gridx = 1;
        txtPassword = new JPasswordField("admin123");
        txtPassword.setFont(UITheme.FONT_REGULAR);
        txtPassword.setBorder(UITheme.createFieldBorder());
        txtPassword.setPreferredSize(new Dimension(220, 34));
        panelBody.add(txtPassword, gbc);

        // Lihat Password Checkbox
        gbc.gridx = 1; gbc.gridy = 2;
        gbc.insets = new Insets(2, 8, 6, 8);
        chkLihatPassword = new JCheckBox("Tampilkan Kata Sandi");
        chkLihatPassword.setBackground(UITheme.WHITE);
        chkLihatPassword.setForeground(UITheme.TEXT_SECONDARY);
        chkLihatPassword.setFont(UITheme.FONT_SMALL);
        chkLihatPassword.setFocusPainted(false);
        chkLihatPassword.addActionListener(e -> {
            if (chkLihatPassword.isSelected()) {
                txtPassword.setEchoChar((char) 0);
            } else {
                txtPassword.setEchoChar('•');
            }
        });
        panelBody.add(chkLihatPassword, gbc);

        // Hak Akses / Role (JComboBox)
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.insets = new Insets(6, 8, 6, 8);
        JLabel lblRole = new JLabel("Hak Akses / Peran:");
        lblRole.setFont(UITheme.FONT_BOLD);
        lblRole.setForeground(UITheme.TEXT_PRIMARY);
        panelBody.add(lblRole, gbc);

        gbc.gridx = 1;
        cbRole = new JComboBox<>(new String[]{"Administrator Sistem", "Operator Rental"});
        cbRole.setFont(UITheme.FONT_REGULAR);
        cbRole.setBackground(UITheme.WHITE);
        cbRole.setPreferredSize(new Dimension(220, 34));
        panelBody.add(cbRole, gbc);

        // Tombol Aksi (Masuk Sistem & Keluar)
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 8, 8, 8);
        JPanel panelBtn = new JPanel(new GridLayout(1, 2, 12, 0));
        panelBtn.setOpaque(false);

        btnLogin = new JButton("Masuk Sistem");
        UITheme.stylePrimaryButton(btnLogin);
        btnLogin.setPreferredSize(new Dimension(140, 38));
        btnLogin.addActionListener(e -> aksiLogin());

        btnKeluar = new JButton("Keluar");
        UITheme.styleSecondaryButton(btnKeluar);
        btnKeluar.setPreferredSize(new Dimension(140, 38));
        btnKeluar.addActionListener(e -> System.exit(0));

        panelBtn.add(btnLogin);
        panelBtn.add(btnKeluar);
        panelBody.add(panelBtn, gbc);

        // Hint login
        gbc.gridy = 5;
        gbc.insets = new Insets(8, 8, 4, 8);
        JLabel lblHint = new JLabel("Default login: admin / admin123  |  operator / 12345", JLabel.CENTER);
        lblHint.setFont(UITheme.FONT_SMALL);
        lblHint.setForeground(UITheme.TEXT_MUTED);
        panelBody.add(lblHint, gbc);

        panelWrapper.add(panelBody, BorderLayout.CENTER);
        add(panelWrapper, BorderLayout.CENTER);
    }

    private void aksiLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String role = (String) cbRole.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Nama pengguna dan kata sandi wajib diisi!",
                    "Peringatan Login", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validasi autentikasi sederhana
        if (("admin".equalsIgnoreCase(username) && "admin123".equals(password)) ||
            ("operator".equalsIgnoreCase(username) && "12345".equals(password))) {

            JOptionPane.showMessageDialog(this,
                    "Selamat datang, " + username + "!\nLogin berhasil sebagai: " + role,
                    "Autentikasi Berhasil", JOptionPane.INFORMATION_MESSAGE);

            dispose(); // Tutup LoginFrame
            SwingUtilities.invokeLater(() -> {
                MainDashboardFrame dashboard = new MainDashboardFrame(manager, username, role);
                dashboard.setVisible(true);
            });
        } else {
            JOptionPane.showMessageDialog(this,
                    "Nama pengguna atau kata sandi tidak cocok!\nSilakan periksa kembali.",
                    "Login Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }
}
