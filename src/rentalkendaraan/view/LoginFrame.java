package rentalkendaraan.view;

import java.awt.*;
import javax.swing.*;
import rentalkendaraan.service.RentalManager;

/**
 * Tampilan Frame Autentikasi Login Pengguna Aplikasi.
 * Menerapkan form input dengan JPasswordField, JCheckBox intip password, JComboBox peran,
 * dan event handling navigasi ke MainDashboardFrame.
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
        setTitle("Login - Sistem Rental Kendaraan (Kelompok 7)");
        setSize(440, 420);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        // Header Panel Banner
        JPanel panelHeader = new JPanel(new GridLayout(2, 1, 2, 4));
        panelHeader.setBackground(new Color(15, 23, 42)); // Slate 900
        panelHeader.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("RENTAL KENDARAAN JAYA", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(248, 250, 252));
        panelHeader.add(lblTitle);

        JLabel lblSubtitle = new JLabel("Tugas Proyek PBO Kelompok 7", JLabel.CENTER);
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(148, 163, 184));
        panelHeader.add(lblSubtitle);

        add(panelHeader, BorderLayout.NORTH);

        // Body Form Panel
        JPanel panelBody = new JPanel(new GridBagLayout());
        panelBody.setBackground(Color.WHITE);
        panelBody.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Username
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        JLabel lblUser = new JLabel("Nama Pengguna:");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panelBody.add(lblUser, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        txtUsername = new JTextField("admin");
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panelBody.add(txtUsername, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel lblPass = new JLabel("Kata Sandi:");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panelBody.add(lblPass, gbc);

        gbc.gridx = 1;
        txtPassword = new JPasswordField("admin123");
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panelBody.add(txtPassword, gbc);

        // Lihat Password Checkbox
        gbc.gridx = 1; gbc.gridy = 2;
        chkLihatPassword = new JCheckBox("Tampilkan Kata Sandi");
        chkLihatPassword.setBackground(Color.WHITE);
        chkLihatPassword.setFont(new Font("Segoe UI", Font.PLAIN, 11));
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
        JLabel lblRole = new JLabel("Hak Akses / Peran:");
        lblRole.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panelBody.add(lblRole, gbc);

        gbc.gridx = 1;
        cbRole = new JComboBox<>(new String[]{"Administrator Sistem", "Operator Rental"});
        cbRole.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panelBody.add(cbRole, gbc);

        // Tombol Aksi
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 6, 6, 6);
        JPanel panelBtn = new JPanel(new GridLayout(1, 2, 12, 0));
        panelBtn.setBackground(Color.WHITE);

        btnLogin = new JButton("Masuk Sistem");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogin.setBackground(new Color(37, 99, 235));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.addActionListener(e -> aksiLogin());

        btnKeluar = new JButton("Keluar");
        btnKeluar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnKeluar.setBackground(new Color(241, 245, 249));
        btnKeluar.addActionListener(e -> System.exit(0));

        panelBtn.add(btnLogin);
        panelBtn.add(btnKeluar);
        panelBody.add(panelBtn, gbc);

        // Hint login
        gbc.gridy = 5;
        JLabel lblHint = new JLabel("Default login: admin / admin123", JLabel.CENTER);
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblHint.setForeground(new Color(100, 116, 139));
        panelBody.add(lblHint, gbc);

        add(panelBody, BorderLayout.CENTER);
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
