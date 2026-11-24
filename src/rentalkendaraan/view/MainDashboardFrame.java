package rentalkendaraan.view;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import rentalkendaraan.model.Kendaraan;
import rentalkendaraan.service.RentalManager;

/**
 * Frame Utama Aplikasi Rental Kendaraan (Main Dashboard).
 * Mengintegrasikan JMenuBar, JTabbedPane, Status Bar, serta sinkronisasi antarmuka panel
 * dengan tema Modern Green and White (UITheme).
 */
public class MainDashboardFrame extends JFrame {
    private final RentalManager manager;
    private final String activeUser;
    private final String activeRole;

    private JTabbedPane tabbedPane;
    private PanelKendaraan panelKendaraan;
    private PanelPelanggan panelPelanggan;
    private PanelTransaksi panelTransaksi;

    private JLabel lblStatusArmada;
    private JLabel lblStatusPelanggan;
    private JLabel lblStatusTransaksi;

    public MainDashboardFrame(RentalManager manager, String activeUser, String activeRole) {
        this.manager = manager;
        this.activeUser = activeUser;
        this.activeRole = activeRole;
        initComponents();
        updateStatusBar();
    }

    private void initComponents() {
        setTitle("Sistem Manajemen Rental Kendaraan Jaya Rent - Kelompok 7 (PBO 2026)");
        setSize(1120, 740);
        setMinimumSize(new Dimension(940, 620));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.WHITE);

        // --- 1. MENU BAR ---
        setJMenuBar(createMenuBar());

        // --- 2. HEADER BANNER (DARK GREEN & LIGHT GREEN ACCENTS) ---
        JPanel panelHeader = new JPanel(new BorderLayout(12, 10));
        panelHeader.setBackground(UITheme.DARK_GREEN);
        panelHeader.setBorder(new EmptyBorder(16, 24, 16, 24));

        // Left Header: Brand App Title & Subtitle
        JPanel panelTitle = new JPanel(new GridLayout(2, 1, 2, 3));
        panelTitle.setOpaque(false);

        JLabel lblAppName = new JLabel("RENTAL KENDARAAN JAYA RENT CAR");
        lblAppName.setFont(UITheme.FONT_TITLE_LARGE);
        lblAppName.setForeground(UITheme.WHITE);
        panelTitle.add(lblAppName);

        JLabel lblAppDesc = new JLabel("Sistem Informasi Manajemen Armada & Penyewaan Berbasis OOP (MVC)");
        lblAppDesc.setFont(UITheme.FONT_REGULAR);
        lblAppDesc.setForeground(UITheme.LIGHT_GREEN);
        panelTitle.add(lblAppDesc);

        panelHeader.add(panelTitle, BorderLayout.WEST);

        // Right Header: User Role Badge & Timestamp
        JPanel panelUserInfo = new JPanel(new GridLayout(2, 1, 2, 4));
        panelUserInfo.setOpaque(false);

        JPanel panelBadgeUser = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        panelBadgeUser.setOpaque(false);

        JLabel lblUserTitle = new JLabel("Pengguna Aktif:");
        lblUserTitle.setFont(UITheme.FONT_SMALL);
        lblUserTitle.setForeground(UITheme.LIGHT_GREEN);
        panelBadgeUser.add(lblUserTitle);

        JLabel lblBadge = new JLabel(" " + activeUser + " (" + activeRole + ") ");
        lblBadge.setFont(UITheme.FONT_SMALL_BOLD);
        lblBadge.setForeground(UITheme.DARK_GREEN);
        lblBadge.setOpaque(true);
        lblBadge.setBackground(UITheme.LIGHT_GREEN);
        lblBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.GREEN_BORDER, 1),
                BorderFactory.createEmptyBorder(2, 6, 2, 6)));
        panelBadgeUser.add(lblBadge);
        panelUserInfo.add(panelBadgeUser);

        String tglSekarang = LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy"));
        JLabel lblDate = new JLabel(tglSekarang, JLabel.RIGHT);
        lblDate.setFont(UITheme.FONT_SMALL);
        lblDate.setForeground(new Color(0xE2, 0xF9, 0xE7)); // Soft light green-white
        panelUserInfo.add(lblDate);

        panelHeader.add(panelUserInfo, BorderLayout.EAST);
        add(panelHeader, BorderLayout.NORTH);

        // --- 3. CENTER: TABBED PANE DENGAN STYLING HIJAU-PUTIH ---
        UIManager.put("TabbedPane.selected", UITheme.SOFT_GREEN);
        UIManager.put("TabbedPane.tabInsets", new Insets(8, 20, 8, 20));

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_SUBTITLE);
        tabbedPane.setBackground(UITheme.WHITE);
        tabbedPane.setForeground(UITheme.DARK_GREEN);
        tabbedPane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER));

        // Callback ketika ada mutasi data untuk menyinkronkan seluruh panel
        Runnable syncCallback = () -> {
            panelKendaraan.segarkanData();
            panelPelanggan.segarkanData();
            panelTransaksi.segarkanData();
            updateStatusBar();
        };

        panelKendaraan = new PanelKendaraan(manager, syncCallback);
        panelPelanggan = new PanelPelanggan(manager, syncCallback);
        panelTransaksi = new PanelTransaksi(manager, this, syncCallback);

        tabbedPane.addTab("  1. Kelola Armada Kendaraan  ", panelKendaraan);
        tabbedPane.addTab("  2. Kelola Data Pelanggan  ", panelPelanggan);
        tabbedPane.addTab("  3. Transaksi Penyewaan & Kembali  ", panelTransaksi);

        add(tabbedPane, BorderLayout.CENTER);

        // --- 4. BOTTOM: STATUS BAR (SOFT GREEN & GREEN BORDER) ---
        JPanel panelStatusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 8));
        panelStatusBar.setBackground(UITheme.SOFT_GREEN);
        panelStatusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.GREEN_BORDER));

        lblStatusArmada = new JLabel("Armada: 0 Unit");
        lblStatusArmada.setFont(UITheme.FONT_BOLD);
        lblStatusArmada.setForeground(UITheme.DARK_GREEN);
        panelStatusBar.add(lblStatusArmada);

        JSeparator sep1 = new JSeparator(SwingConstants.VERTICAL);
        sep1.setPreferredSize(new Dimension(2, 16));
        sep1.setForeground(UITheme.GREEN_BORDER);
        panelStatusBar.add(sep1);

        lblStatusPelanggan = new JLabel("Pelanggan: 0 Orang");
        lblStatusPelanggan.setFont(UITheme.FONT_BOLD);
        lblStatusPelanggan.setForeground(UITheme.DARK_GREEN);
        panelStatusBar.add(lblStatusPelanggan);

        JSeparator sep2 = new JSeparator(SwingConstants.VERTICAL);
        sep2.setPreferredSize(new Dimension(2, 16));
        sep2.setForeground(UITheme.GREEN_BORDER);
        panelStatusBar.add(sep2);

        lblStatusTransaksi = new JLabel("Transaksi: 0");
        lblStatusTransaksi.setFont(UITheme.FONT_BOLD);
        lblStatusTransaksi.setForeground(UITheme.DARK_GREEN);
        panelStatusBar.add(lblStatusTransaksi);

        JSeparator sep3 = new JSeparator(SwingConstants.VERTICAL);
        sep3.setPreferredSize(new Dimension(2, 16));
        sep3.setForeground(UITheme.GREEN_BORDER);
        panelStatusBar.add(sep3);

        JLabel lblStorage = new JLabel("Penyimpanan: Berkas Teks CSV Permanen (data/*.txt)");
        lblStorage.setFont(UITheme.FONT_SMALL);
        lblStorage.setForeground(UITheme.TEXT_SECONDARY);
        panelStatusBar.add(lblStorage);

        add(panelStatusBar, BorderLayout.SOUTH);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setFont(UITheme.FONT_REGULAR);
        menuBar.setBackground(UITheme.WHITE);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER));

        // Menu Berkas
        JMenu menuBerkas = new JMenu("Berkas");
        menuBerkas.setFont(UITheme.FONT_REGULAR);
        menuBerkas.setForeground(UITheme.TEXT_PRIMARY);

        JMenuItem itemLogout = new JMenuItem("Logout Pengguna");
        itemLogout.setFont(UITheme.FONT_REGULAR);
        itemLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Apakah Anda yakin ingin logout dari sistem?", "Konfirmasi Logout",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                SwingUtilities.invokeLater(() -> new LoginFrame(manager).setVisible(true));
            }
        });

        JMenuItem itemKeluar = new JMenuItem("Keluar Aplikasi");
        itemKeluar.setFont(UITheme.FONT_REGULAR);
        itemKeluar.addActionListener(e -> System.exit(0));

        menuBerkas.add(itemLogout);
        menuBerkas.addSeparator();
        menuBerkas.add(itemKeluar);
        menuBar.add(menuBerkas);

        // Menu Master Data
        JMenu menuMaster = new JMenu("Data Master");
        menuMaster.setFont(UITheme.FONT_REGULAR);
        menuMaster.setForeground(UITheme.TEXT_PRIMARY);

        JMenuItem itemKendaraan = new JMenuItem("Manajemen Armada Kendaraan");
        itemKendaraan.setFont(UITheme.FONT_REGULAR);
        itemKendaraan.addActionListener(e -> tabbedPane.setSelectedIndex(0));

        JMenuItem itemPelanggan = new JMenuItem("Manajemen Data Pelanggan");
        itemPelanggan.setFont(UITheme.FONT_REGULAR);
        itemPelanggan.addActionListener(e -> tabbedPane.setSelectedIndex(1));

        menuMaster.add(itemKendaraan);
        menuMaster.add(itemPelanggan);
        menuBar.add(menuMaster);

        // Menu Transaksi
        JMenu menuTransaksi = new JMenu("Transaksi");
        menuTransaksi.setFont(UITheme.FONT_REGULAR);
        menuTransaksi.setForeground(UITheme.TEXT_PRIMARY);

        JMenuItem itemSewa = new JMenuItem("Penyewaan & Pengembalian Armada");
        itemSewa.setFont(UITheme.FONT_REGULAR);
        itemSewa.addActionListener(e -> tabbedPane.setSelectedIndex(2));
        menuTransaksi.add(itemSewa);
        menuBar.add(menuTransaksi);

        // Menu Bantuan
        JMenu menuBantuan = new JMenu("Bantuan");
        menuBantuan.setFont(UITheme.FONT_REGULAR);
        menuBantuan.setForeground(UITheme.TEXT_PRIMARY);

        JMenuItem itemAbout = new JMenuItem("Tentang Aplikasi & Pengembang");
        itemAbout.setFont(UITheme.FONT_REGULAR);
        itemAbout.addActionListener(e -> tampilkanTentangAplikasi());
        menuBantuan.add(itemAbout);
        menuBar.add(menuBantuan);

        return menuBar;
    }

    private void updateStatusBar() {
        int totalKendaraan = manager.getDaftarKendaraan().size();
        long tersedia = manager.getDaftarKendaraan().stream().filter(Kendaraan::isStatusTersedia).count();
        long disewa = totalKendaraan - tersedia;

        lblStatusArmada.setText(String.format("Armada: %d Unit (%d Tersedia, %d Disewa)", totalKendaraan, tersedia, disewa));
        lblStatusPelanggan.setText(String.format("Pelanggan: %d Orang", manager.getDaftarPelanggan().size()));
        lblStatusTransaksi.setText(String.format("Transaksi: %d Riwayat", manager.getDaftarTransaksi().size()));
    }

    private void tampilkanTentangAplikasi() {
        String pesan = """
                =======================================================
                    SISTEM APLIKASI RENTAL KENDARAAN (JAYA RENT)
                =======================================================
                Tugas Proyek Pemrograman Berorientasi Objek (PBO) GUI 2026
                Tema: Rental Kendaraan Modern
                Kelompok: 7
                
                Anggota Tim:
                1. Kevin Nibras Kusuma (NIM: 21120124140131)
                2. Hanif Arifandi Zulfikar (NIM: 21120124140154)
                3. Haidar Rafi Apta Priatama (NIM: 21120124140107)
                
                Departemen Teknik Komputer - Universitas Diponegoro
                
                Penerapan Fitur OOP:
                - Enkapsulasi: Validasi setter/getter ketat
                - Pewarisan: Kendaraan -> Mobil, Motor, Bus
                - Abstraksi: Abstract Class Kendaraan & Interface Diskonable, LayananTambahan
                - Polimorfisme: Overriding hitungBiayaSewa() & Overloading
                - Exception Handling: RentalException, DataValidasiException, File I/O
                - Collection & Generics: List<T>, Map<K,V>
                =======================================================
                """;
        JOptionPane.showMessageDialog(this, pesan, "Tentang Pengembang", JOptionPane.INFORMATION_MESSAGE);
    }
}
