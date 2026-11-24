package rentalkendaraan.view;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import rentalkendaraan.exception.DataValidasiException;
import rentalkendaraan.exception.RentalException;
import rentalkendaraan.model.TransaksiSewa;
import rentalkendaraan.service.RentalManager;

/**
 * Dialog Modal untuk pemrosesan pengembalian kendaraan secara interaktif.
 * Menyediakan live preview perhitungan keterlambatan dan denda berbasis polimorfisme,
 * validasi kalender presisi (uuuu-MM-dd), serta konfirmasi pengembalian unit ke RentalManager
 * dengan standar tema Modern Green & White (UITheme).
 */
public class DialogPengembalian extends JDialog {
    private final RentalManager manager;
    private final TransaksiSewa transaksi;
    private final Runnable onReturnSuccess;

    private JTextField txtTanggalKembali;
    private JLabel lblHariTerlambat;
    private JLabel lblEstimasiDenda;
    private JLabel lblTotalBayar;
    private JLabel lblStatusValidasi;
    private JButton btnKonfirmasi;
    private JButton btnBatal;

    private final DecimalFormat rupiahFormat = new DecimalFormat("#,###");
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    public DialogPengembalian(Frame owner, RentalManager manager, TransaksiSewa transaksi, Runnable onReturnSuccess) {
        super(owner, "Pengembalian Kendaraan - " + transaksi.getIdTransaksi(), true);
        this.manager = manager;
        this.transaksi = transaksi;
        this.onReturnSuccess = onReturnSuccess;

        initComponents();
        updateLiveKalkulasi();
    }

    private void initComponents() {
        setSize(560, 640);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(10, 10));
        setResizable(false);
        getContentPane().setBackground(UITheme.WHITE);

        // --- 1. HEADER PANEL (DARK GREEN) ---
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBackground(UITheme.DARK_GREEN);
        panelHeader.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JLabel lblTitle = new JLabel("PENGEMBALIAN UNIT KENDARAAN");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.WHITE);
        panelHeader.add(lblTitle, BorderLayout.WEST);

        JLabel lblSubtitle = new JLabel(transaksi.getIdTransaksi());
        lblSubtitle.setFont(UITheme.FONT_SUBTITLE);
        lblSubtitle.setForeground(UITheme.LIGHT_GREEN);
        panelHeader.add(lblSubtitle, BorderLayout.EAST);

        add(panelHeader, BorderLayout.NORTH);

        // --- 2. CENTER PANEL: INFORMASI TRANSAKSI & INPUT PENGEMBALIAN ---
        JPanel panelCenter = new JPanel();
        panelCenter.setLayout(new BoxLayout(panelCenter, BoxLayout.Y_AXIS));
        panelCenter.setBackground(UITheme.WHITE);
        panelCenter.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));

        // Group 1: Informasi Transaksi
        JPanel panelInfo = new JPanel(new GridBagLayout());
        panelInfo.setBackground(UITheme.WHITE);
        panelInfo.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1),
                "Informasi Transaksi Sewa",
                TitledBorder.LEFT, TitledBorder.TOP,
                UITheme.FONT_SMALL_BOLD, UITheme.DARK_GREEN));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int r = 0;
        addInfoRow(panelInfo, gbc, r++, "No. Transaksi", transaksi.getIdTransaksi());
        addInfoRow(panelInfo, gbc, r++, "Pelanggan", transaksi.getPelanggan().getIdPelanggan() + " - " + transaksi.getPelanggan().getNama());
        addInfoRow(panelInfo, gbc, r++, "Unit Armada", transaksi.getKendaraan().getMerk() + " " + transaksi.getKendaraan().getModel() + " [" + transaksi.getKendaraan().getNomorPlat() + "]");
        addInfoRow(panelInfo, gbc, r++, "Jenis Kendaraan", transaksi.getKendaraan().getJenisKendaraan());
        addInfoRow(panelInfo, gbc, r++, "Tanggal Sewa", transaksi.getTanggalSewa() + " (" + transaksi.getDurasiHari() + " Hari)");
        addInfoRow(panelInfo, gbc, r++, "Jatuh Tempo", transaksi.getTanggalJatuhTempo());
        // Menggunakan polimorfisme Kendaraan.getTarifDendaPerHari() tanpa instanceof
        addInfoRow(panelInfo, gbc, r++, "Tarif Denda/Hari", "Rp " + rupiahFormat.format(transaksi.getKendaraan().getTarifDendaPerHari()));

        panelCenter.add(panelInfo);
        panelCenter.add(Box.createRigidArea(new Dimension(0, 10)));

        // Group 2: Input Pengembalian & Live Calculation
        JPanel panelInput = new JPanel(new GridBagLayout());
        panelInput.setBackground(UITheme.WHITE);
        panelInput.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UITheme.GREEN_BORDER, 1),
                "Proses & Live Preview Pengembalian",
                TitledBorder.LEFT, TitledBorder.TOP,
                UITheme.FONT_SMALL_BOLD, UITheme.DARK_GREEN));

        GridBagConstraints gbcIn = new GridBagConstraints();
        gbcIn.insets = new Insets(5, 8, 5, 8);
        gbcIn.fill = GridBagConstraints.HORIZONTAL;
        gbcIn.anchor = GridBagConstraints.WEST;

        int rowIn = 0;

        // Input Tanggal Pengembalian
        gbcIn.gridx = 0; gbcIn.gridy = rowIn; gbcIn.weightx = 0.38;
        JLabel lblTglTitle = new JLabel("Tgl Pengembalian:");
        lblTglTitle.setFont(UITheme.FONT_BOLD);
        lblTglTitle.setForeground(UITheme.TEXT_PRIMARY);
        panelInput.add(lblTglTitle, gbcIn);

        gbcIn.gridx = 1; gbcIn.weightx = 0.62;
        JPanel panelTgl = new JPanel(new BorderLayout(6, 0));
        panelTgl.setOpaque(false);

        txtTanggalKembali = new JTextField(LocalDate.now().format(DATE_FORMATTER));
        txtTanggalKembali.setFont(UITheme.FONT_REGULAR);
        txtTanggalKembali.setBorder(UITheme.createFieldBorder());
        txtTanggalKembali.setPreferredSize(new Dimension(100, 30));
        panelTgl.add(txtTanggalKembali, BorderLayout.CENTER);

        JButton btnToday = new JButton("Hari Ini");
        btnToday.setFont(UITheme.FONT_SMALL);
        btnToday.setBackground(UITheme.WHITE);
        btnToday.setForeground(UITheme.TEXT_PRIMARY);
        btnToday.setFocusPainted(false);
        btnToday.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        btnToday.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnToday.addActionListener(e -> txtTanggalKembali.setText(LocalDate.now().format(DATE_FORMATTER)));
        panelTgl.add(btnToday, BorderLayout.EAST);

        panelInput.add(panelTgl, gbcIn);

        // Keterangan format
        rowIn++;
        gbcIn.gridx = 1; gbcIn.gridy = rowIn;
        JLabel lblHint = new JLabel("Pola tanggal: uuuu-MM-dd (Contoh: " + LocalDate.now().format(DATE_FORMATTER) + ")");
        lblHint.setFont(UITheme.FONT_SMALL);
        lblHint.setForeground(UITheme.TEXT_MUTED);
        panelInput.add(lblHint, gbcIn);

        // Keterlambatan
        rowIn++;
        gbcIn.gridx = 0; gbcIn.gridy = rowIn;
        JLabel lblTerlambatTitle = new JLabel("Keterlambatan:");
        lblTerlambatTitle.setFont(UITheme.FONT_BOLD);
        lblTerlambatTitle.setForeground(UITheme.TEXT_PRIMARY);
        panelInput.add(lblTerlambatTitle, gbcIn);

        gbcIn.gridx = 1;
        lblHariTerlambat = new JLabel("0 Hari (Tepat Waktu)");
        lblHariTerlambat.setFont(UITheme.FONT_BOLD);
        lblHariTerlambat.setForeground(UITheme.DARK_GREEN);
        panelInput.add(lblHariTerlambat, gbcIn);

        // Biaya Sewa Awal
        rowIn++;
        gbcIn.gridx = 0; gbcIn.gridy = rowIn;
        JLabel lblSewaTitle = new JLabel("Biaya Sewa Awal:");
        lblSewaTitle.setFont(UITheme.FONT_BOLD);
        lblSewaTitle.setForeground(UITheme.TEXT_PRIMARY);
        panelInput.add(lblSewaTitle, gbcIn);

        gbcIn.gridx = 1;
        JLabel lblBiayaSewa = new JLabel("Rp " + rupiahFormat.format(transaksi.getBiayaSewaAwal()));
        lblBiayaSewa.setFont(UITheme.FONT_REGULAR);
        lblBiayaSewa.setForeground(UITheme.TEXT_PRIMARY);
        panelInput.add(lblBiayaSewa, gbcIn);

        // Estimasi Denda
        rowIn++;
        gbcIn.gridx = 0; gbcIn.gridy = rowIn;
        JLabel lblDendaTitle = new JLabel("Estimasi Denda:");
        lblDendaTitle.setFont(UITheme.FONT_BOLD);
        lblDendaTitle.setForeground(UITheme.TEXT_PRIMARY);
        panelInput.add(lblDendaTitle, gbcIn);

        gbcIn.gridx = 1;
        lblEstimasiDenda = new JLabel("Rp 0");
        lblEstimasiDenda.setFont(UITheme.FONT_BOLD);
        lblEstimasiDenda.setForeground(UITheme.DARK_GREEN);
        panelInput.add(lblEstimasiDenda, gbcIn);

        // Total Pembayaran Akhir
        rowIn++;
        gbcIn.gridx = 0; gbcIn.gridy = rowIn;
        JLabel lblTotTitle = new JLabel("Total Pembayaran:");
        lblTotTitle.setFont(UITheme.FONT_TITLE);
        lblTotTitle.setForeground(UITheme.TEXT_PRIMARY);
        panelInput.add(lblTotTitle, gbcIn);

        gbcIn.gridx = 1;
        lblTotalBayar = new JLabel("Rp " + rupiahFormat.format(transaksi.getTotalPembayaran()));
        lblTotalBayar.setFont(UITheme.FONT_TITLE_LARGE);
        lblTotalBayar.setForeground(UITheme.DARK_GREEN);
        panelInput.add(lblTotalBayar, gbcIn);

        // Status Validasi Pesan
        rowIn++;
        gbcIn.gridx = 0; gbcIn.gridy = rowIn; gbcIn.gridwidth = 2;
        lblStatusValidasi = new JLabel(" ");
        lblStatusValidasi.setFont(UITheme.FONT_SMALL_BOLD);
        panelInput.add(lblStatusValidasi, gbcIn);

        panelCenter.add(panelInput);
        add(panelCenter, BorderLayout.CENTER);

        // --- 3. BOTTOM PANEL: TOMBOL AKSI (SOFT GREEN) ---
        JPanel panelBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        panelBottom.setBackground(UITheme.SOFT_GREEN);
        panelBottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.GREEN_BORDER));

        btnBatal = new JButton("Batal");
        btnBatal.setFont(UITheme.FONT_SMALL);
        btnBatal.setBackground(UITheme.WHITE);
        btnBatal.setForeground(UITheme.TEXT_PRIMARY);
        btnBatal.setFocusPainted(false);
        btnBatal.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)));
        btnBatal.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBatal.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnBatal.setBackground(UITheme.WHITE);
                btnBatal.setForeground(UITheme.DARK_GREEN);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btnBatal.setBackground(UITheme.WHITE);
                btnBatal.setForeground(UITheme.TEXT_PRIMARY);
            }
        });
        btnBatal.addActionListener(e -> dispose());
        panelBottom.add(btnBatal);

        btnKonfirmasi = new JButton("Konfirmasi Pengembalian");
        btnKonfirmasi.setFont(UITheme.FONT_SMALL_BOLD);
        btnKonfirmasi.setBackground(UITheme.PRIMARY_GREEN);
        btnKonfirmasi.setForeground(UITheme.WHITE);
        btnKonfirmasi.setFocusPainted(false);
        btnKonfirmasi.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btnKonfirmasi.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnKonfirmasi.setPreferredSize(new Dimension(200, 36));
        btnKonfirmasi.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btnKonfirmasi.isEnabled()) btnKonfirmasi.setBackground(UITheme.SECONDARY_GREEN);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (btnKonfirmasi.isEnabled()) btnKonfirmasi.setBackground(UITheme.PRIMARY_GREEN);
            }
        });
        btnKonfirmasi.addActionListener(e -> aksiKonfirmasiPengembalian());
        panelBottom.add(btnKonfirmasi);

        add(panelBottom, BorderLayout.SOUTH);

        // Live DocumentListener pada input tanggal
        txtTanggalKembali.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updateLiveKalkulasi(); }
            @Override
            public void removeUpdate(DocumentEvent e) { updateLiveKalkulasi(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updateLiveKalkulasi(); }
        });
    }

    private void addInfoRow(JPanel panel, GridBagConstraints gbc, int row, String label, String value) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.38;
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(UITheme.FONT_REGULAR);
        lbl.setForeground(UITheme.TEXT_SECONDARY);
        panel.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 0.62;
        JLabel val = new JLabel(value);
        val.setFont(UITheme.FONT_BOLD);
        val.setForeground(UITheme.TEXT_PRIMARY);
        panel.add(val, gbc);
    }

    /**
     * Menghitung keterlambatan dan estimasi denda secara real-time saat tanggal diubah.
     * Menggunakan polimorfisme objek Kendaraan tanpa instanceof.
     */
    private void updateLiveKalkulasi() {
        String input = txtTanggalKembali.getText().trim();
        if (input.isEmpty()) {
            lblStatusValidasi.setText("Silakan masukkan tanggal pengembalian.");
            lblStatusValidasi.setForeground(UITheme.DANGER);
            btnKonfirmasi.setEnabled(false);
            lblHariTerlambat.setText("-");
            lblEstimasiDenda.setText("Rp 0");
            lblTotalBayar.setText("Rp " + rupiahFormat.format(transaksi.getBiayaSewaAwal()));
            return;
        }

        try {
            // Validasi format dan kalkulasi keterlambatan via model TransaksiSewa
            int terlambat = transaksi.hitungHariKeterlambatan(input);

            // Delegasikan perhitungan nominal denda ke method model TransaksiSewa (enkapsulasi bisnis)
            double denda = transaksi.hitungBiayaDenda(terlambat);
            double total = transaksi.getBiayaSewaAwal() + denda;

            if (terlambat > 0) {
                lblHariTerlambat.setText(terlambat + " Hari (Terlambat)");
                lblHariTerlambat.setForeground(UITheme.DANGER);

                lblEstimasiDenda.setText("Rp " + rupiahFormat.format(denda));
                lblEstimasiDenda.setForeground(UITheme.DANGER);

                lblStatusValidasi.setText("Terlambat " + terlambat + " hari dari jatuh tempo (" + transaksi.getTanggalJatuhTempo() + "). Dikenakan denda.");
                lblStatusValidasi.setForeground(UITheme.WARNING);
            } else {
                lblHariTerlambat.setText("0 Hari (Tepat Waktu)");
                lblHariTerlambat.setForeground(UITheme.DARK_GREEN);

                lblEstimasiDenda.setText("Rp 0");
                lblEstimasiDenda.setForeground(UITheme.DARK_GREEN);

                lblStatusValidasi.setText("Pengembalian tepat waktu (Bebas denda keterlambatan).");
                lblStatusValidasi.setForeground(UITheme.DARK_GREEN);
            }

            lblTotalBayar.setText("Rp " + rupiahFormat.format(total));
            btnKonfirmasi.setEnabled(true);

        } catch (DataValidasiException ex) {
            lblStatusValidasi.setText(ex.getMessage());
            lblStatusValidasi.setForeground(UITheme.DANGER);
            lblHariTerlambat.setText("Tidak Valid");
            lblHariTerlambat.setForeground(UITheme.DANGER);
            lblEstimasiDenda.setText("Rp 0");
            lblTotalBayar.setText("Rp " + rupiahFormat.format(transaksi.getBiayaSewaAwal()));
            btnKonfirmasi.setEnabled(false);
        }
    }

    /**
     * Memproses pengembalian kendaraan secara definitif melalui RentalManager.
     */
    private void aksiKonfirmasiPengembalian() {
        String tglKembali = txtTanggalKembali.getText().trim();

        // Validasi transaksi tidak boleh SELESAI atau DIBATALKAN
        if (!"AKTIF".equalsIgnoreCase(transaksi.getStatusTransaksi())) {
            JOptionPane.showMessageDialog(this,
                    "Transaksi [" + transaksi.getIdTransaksi() + "] berstatus " +
                    transaksi.getStatusTransaksi() + " dan tidak dapat diproses pengembaliannya.",
                    "Peringatan", JOptionPane.WARNING_MESSAGE);
            dispose();
            return;
        }

        try {
            int terlambat = transaksi.hitungHariKeterlambatan(tglKembali);
            double denda = transaksi.hitungBiayaDenda(terlambat);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Konfirmasi pengembalian unit:\n" +
                    "- No. Transaksi  : " + transaksi.getIdTransaksi() + "\n" +
                    "- Armada         : " + transaksi.getKendaraan().getNomorPlat() + " (" + transaksi.getKendaraan().getMerk() + " " + transaksi.getKendaraan().getModel() + ")\n" +
                    "- Tgl Pengembalian: " + tglKembali + "\n" +
                    "- Keterlambatan  : " + terlambat + " Hari\n" +
                    "- Nominal Denda  : Rp " + rupiahFormat.format(denda) + "\n" +
                    "- Total Tagihan  : Rp " + rupiahFormat.format(transaksi.getBiayaSewaAwal() + denda) + "\n\n" +
                    "Lanjutkan proses pengembalian unit?",
                    "Konfirmasi Pengembalian Unit",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            // Panggil Controller / Service
            TransaksiSewa hasil = manager.prosesPengembalian(transaksi.getIdTransaksi(), tglKembali);

            JOptionPane.showMessageDialog(this,
                    "Pengembalian armada berhasil diproses!\n" +
                    "Armada [" + hasil.getKendaraan().getNomorPlat() + "] telah kembali TERSEDIA.\n" +
                    "Status transaksi " + hasil.getIdTransaksi() + " kini SELESAI.\n" +
                    "Total Pembayaran: Rp " + rupiahFormat.format(hasil.getTotalPembayaran()),
                    "Pengembalian Sukses",
                    JOptionPane.INFORMATION_MESSAGE);

            if (onReturnSuccess != null) {
                onReturnSuccess.run();
            }
            dispose();

        } catch (RentalException ex) {
            JOptionPane.showMessageDialog(this,
                    "Gagal memproses pengembalian:\n" + ex.getMessage(),
                    "Kesalahan Pengembalian",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
