package rentalkendaraan.view;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import rentalkendaraan.model.Kendaraan;
import rentalkendaraan.service.RentalManager;

/**
 * Frame Utama Aplikasi Rental Kendaraan (Main Dashboard).
 * Mengintegrasikan JMenuBar, JTabbedPane, Status Bar, serta sinkronisasi antarmuka panel.
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
        setTitle("Sistem Manajemen Rental Kendaraan - Kelompok 7 (PBO 2026)");
        setSize(1100, 720);
        setMinimumSize(new Dimension(900, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- MENU BAR ---
        setJMenuBar(createMenuBar());

        // --- HEADER BANNER ---
        JPanel panelHeader = new JPanel(new BorderLayout(10, 10));
        panelHeader.setBackground(new Color(15, 23, 42)); // Slate 900
        panelHeader.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JPanel panelTitle = new JPanel(new GridLayout(2, 1, 2, 2));
        panelTitle.setOpaque(false);

        JLabel lblAppName = new JLabel("RENTAL KENDARAAN JAYA RENT CAR");
        lblAppName.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblAppName.setForeground(new Color(248, 250, 252));
        panelTitle.add(lblAppName);

        JLabel lblAppDesc = new JLabel("Sistem Informasi Manajemen Armada & Penyewaan Berbasis OOP (MVC)");
        lblAppDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblAppDesc.setForeground(new Color(148, 163, 184));
        panelTitle.add(lblAppDesc);

        panelHeader.add(panelTitle, BorderLayout.WEST);

        // Right Info: User & Timestamp
        JPanel panelUserInfo = new JPanel(new GridLayout(2, 1, 2, 2));
        panelUserInfo.setOpaque(false);

        JLabel lblUser = new JLabel("Pengguna: " + activeUser + " (" + activeRole + ")", JLabel.RIGHT);
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUser.setForeground(new Color(56, 189, 248)); // Sky 400
        panelUserInfo.add(lblUser);

        String tglSekarang = LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy"));
        JLabel lblDate = new JLabel(tglSekarang, JLabel.RIGHT);
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDate.setForeground(new Color(203, 213, 225));
        panelUserInfo.add(lblDate);

        panelHeader.add(panelUserInfo, BorderLayout.EAST);
        add(panelHeader, BorderLayout.NORTH);

        // --- CENTER: TABBED PANE DENGAN 3 PANEL UTAMA ---
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

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

        // --- BOTTOM: STATUS BAR ---
        JPanel panelStatusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 6));
        panelStatusBar.setBackground(new Color(241, 245, 249));
        panelStatusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(203, 213, 225)));

        lblStatusArmada = new JLabel("Armada: 0 Unit");
        lblStatusArmada.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panelStatusBar.add(lblStatusArmada);

        panelStatusBar.add(new JSeparator(SwingConstants.VERTICAL));

        lblStatusPelanggan = new JLabel("Pelanggan: 0 Orang");
        lblStatusPelanggan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panelStatusBar.add(lblStatusPelanggan);

        panelStatusBar.add(new JSeparator(SwingConstants.VERTICAL));

        lblStatusTransaksi = new JLabel("Transaksi: 0");
        lblStatusTransaksi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panelStatusBar.add(lblStatusTransaksi);

        panelStatusBar.add(new JSeparator(SwingConstants.VERTICAL));

        JLabel lblStorage = new JLabel("Media Penyimpanan: Berkas Teks CSV Permanen (data/*.txt)");
        lblStorage.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblStorage.setForeground(new Color(71, 85, 105));
        panelStatusBar.add(lblStorage);

        add(panelStatusBar, BorderLayout.SOUTH);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Menu Berkas
        JMenu menuBerkas = new JMenu("Berkas");
        JMenuItem itemLogout = new JMenuItem("Logout Pengguna");
        itemLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Apakah Anda yakin ingin logout?", "Konfirmasi Logout",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                SwingUtilities.invokeLater(() -> new LoginFrame(manager).setVisible(true));
            }
        });

        JMenuItem itemKeluar = new JMenuItem("Keluar Aplikasi");
        itemKeluar.addActionListener(e -> System.exit(0));

        menuBerkas.add(itemLogout);
        menuBerkas.addSeparator();
        menuBerkas.add(itemKeluar);
        menuBar.add(menuBerkas);

        // Menu Master Data
        JMenu menuMaster = new JMenu("Data Master");
        JMenuItem itemKendaraan = new JMenuItem("Manajemen Armada Kendaraan");
        itemKendaraan.addActionListener(e -> tabbedPane.setSelectedIndex(0));

        JMenuItem itemPelanggan = new JMenuItem("Manajemen Data Pelanggan");
        itemPelanggan.addActionListener(e -> tabbedPane.setSelectedIndex(1));

        menuMaster.add(itemKendaraan);
        menuMaster.add(itemPelanggan);
        menuBar.add(menuMaster);

        // Menu Transaksi
        JMenu menuTransaksi = new JMenu("Transaksi");
        JMenuItem itemSewa = new JMenuItem("Penyewaan & Pengembalian Armada");
        itemSewa.addActionListener(e -> tabbedPane.setSelectedIndex(2));
        menuTransaksi.add(itemSewa);
        menuBar.add(menuTransaksi);

        // Menu Bantuan
        JMenu menuBantuan = new JMenu("Bantuan");
        JMenuItem itemAbout = new JMenuItem("Tentang Aplikasi & Kelompok");
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
                Tema: Rental Kendaraan
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
