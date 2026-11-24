package rentalkendaraan.view;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import rentalkendaraan.exception.RentalException;
import rentalkendaraan.model.*;
import rentalkendaraan.service.RentalManager;

/**
 * Panel Manajemen Transaksi Penyewaan dan Pengembalian Armada Kendaraan.
 * Mengintegrasikan form penyewaan baru, kalkulasi biaya polimorfik real-time,
 * tabel riwayat transaksi, dan dialog pengembalian unit dengan tema Modern Green & White (UITheme).
 */
public class PanelTransaksi extends JPanel {
    private final RentalManager manager;
    private final Frame parentFrame;
    private final Runnable onDataChangedCallback;

    // Form Components
    private JTextField txtIdTrx;
    private JComboBox<Pelanggan> cbPelanggan;
    private JComboBox<Kendaraan> cbKendaraan;
    private JTextField txtTglSewa;
    private JSpinner spnDurasi;
    private JCheckBox chkSupir;
    private JCheckBox chkAsuransi;
    private JTextField txtCatatan;

    // Live Calculation Display
    private JLabel lblEstimasiTotal;
    private JLabel lblRincianKalkulasi;

    // Table & Actions
    private JComboBox<String> cbFilterStatus;
    private JTable tabelTransaksi;
    private DefaultTableModel tableModel;

    private JButton btnProsesSewa;
    private JButton btnSelesaiPengembalian;
    private JButton btnLihatStruk;
    private JButton btnBatalTransaksi;

    private final DecimalFormat rupiahFormat = new DecimalFormat("#,###");

    public PanelTransaksi(RentalManager manager, Frame parentFrame, Runnable onDataChangedCallback) {
        this.manager = manager;
        this.parentFrame = parentFrame;
        this.onDataChangedCallback = onDataChangedCallback;
        setBackground(UITheme.WHITE);
        setLayout(new BorderLayout(14, 14));
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        initComponents();
        segarkanFormPilihan();
        muatDataTabel(manager.getDaftarTransaksi());
        updateKalkulasiEstimasi();
    }

    private void initComponents() {
        // --- BAGIAN KIRI: CARD FORM PENYEWAAN BARU ---
        JPanel panelKiri = new JPanel(new BorderLayout(8, 8));
        panelKiri.setPreferredSize(new Dimension(390, 600));
        panelKiri.setOpaque(false);

        // Card Container untuk Form
        JPanel panelCardForm = new JPanel(new BorderLayout(0, 10));
        panelCardForm.setBackground(UITheme.WHITE);
        panelCardForm.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));

        // Header Card Form
        JPanel panelHeaderForm = new JPanel(new GridLayout(2, 1, 2, 2));
        panelHeaderForm.setOpaque(false);
        panelHeaderForm.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
                BorderFactory.createEmptyBorder(0, 0, 8, 0)));

        JLabel lblJudulForm = new JLabel("Formulir Penyewaan Armada");
        lblJudulForm.setFont(UITheme.FONT_TITLE);
        lblJudulForm.setForeground(UITheme.DARK_GREEN);
        panelHeaderForm.add(lblJudulForm);

        JLabel lblSubJudul = new JLabel("Registrasi transaksi sewa & kalkulasi biaya real-time");
        lblSubJudul.setFont(UITheme.FONT_SMALL);
        lblSubJudul.setForeground(UITheme.TEXT_SECONDARY);
        panelHeaderForm.add(lblSubJudul);

        panelCardForm.add(panelHeaderForm, BorderLayout.NORTH);

        // Body Form (GridBagLayout)
        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setBackground(UITheme.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // ID Transaksi
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        panelForm.add(createFormLabel("No. Transaksi:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        txtIdTrx = new JTextField();
        txtIdTrx.setEditable(false);
        txtIdTrx.setFont(UITheme.FONT_REGULAR);
        txtIdTrx.setBackground(UITheme.SOFT_GREEN);
        txtIdTrx.setForeground(UITheme.DARK_GREEN);
        txtIdTrx.setBorder(UITheme.createGreenFieldBorder());
        txtIdTrx.setPreferredSize(new Dimension(100, 30));
        panelForm.add(txtIdTrx, gbc);

        // Pelanggan (JComboBox)
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panelForm.add(createFormLabel("Pelanggan:"), gbc);
        gbc.gridx = 1;
        cbPelanggan = new JComboBox<>();
        cbPelanggan.setFont(UITheme.FONT_REGULAR);
        cbPelanggan.setBackground(UITheme.WHITE);
        cbPelanggan.setPreferredSize(new Dimension(100, 30));
        panelForm.add(cbPelanggan, gbc);

        // Kendaraan Tersedia (JComboBox)
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panelForm.add(createFormLabel("Armada Unit:"), gbc);
        gbc.gridx = 1;
        cbKendaraan = new JComboBox<>();
        cbKendaraan.setFont(UITheme.FONT_REGULAR);
        cbKendaraan.setBackground(UITheme.WHITE);
        cbKendaraan.setPreferredSize(new Dimension(100, 30));
        cbKendaraan.addActionListener(e -> updateKalkulasiEstimasi());
        panelForm.add(cbKendaraan, gbc);

        // Tanggal Sewa
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panelForm.add(createFormLabel("Tgl Sewa (Y-M-D):"), gbc);
        gbc.gridx = 1;
        txtTglSewa = new JTextField(LocalDate.now().format(DateTimeFormatter.ofPattern("uuuu-MM-dd")));
        txtTglSewa.setFont(UITheme.FONT_REGULAR);
        txtTglSewa.setBorder(UITheme.createFieldBorder());
        txtTglSewa.setPreferredSize(new Dimension(100, 30));
        panelForm.add(txtTglSewa, gbc);

        // Durasi Sewa (JSpinner)
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panelForm.add(createFormLabel("Durasi Sewa:"), gbc);
        gbc.gridx = 1;
        spnDurasi = new JSpinner(new SpinnerNumberModel(1, 1, 90, 1));
        spnDurasi.setFont(UITheme.FONT_REGULAR);
        spnDurasi.setPreferredSize(new Dimension(100, 30));
        spnDurasi.setBorder(UITheme.createFieldBorder());
        spnDurasi.addChangeListener(e -> updateKalkulasiEstimasi());
        panelForm.add(spnDurasi, gbc);

        // Opsi Supir (JCheckBox)
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        chkSupir = new JCheckBox("Layanan Jasa Supir (+Rp 150.000/hari)");
        chkSupir.setFont(UITheme.FONT_REGULAR);
        chkSupir.setForeground(UITheme.TEXT_PRIMARY);
        chkSupir.setBackground(UITheme.WHITE);
        chkSupir.setFocusPainted(false);
        chkSupir.addActionListener(e -> updateKalkulasiEstimasi());
        panelForm.add(chkSupir, gbc);

        // Opsi Asuransi (JCheckBox)
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        chkAsuransi = new JCheckBox("Asuransi Perlindungan Tambahan (+Rp 35.000/hari)");
        chkAsuransi.setFont(UITheme.FONT_REGULAR);
        chkAsuransi.setForeground(UITheme.TEXT_PRIMARY);
        chkAsuransi.setBackground(UITheme.WHITE);
        chkAsuransi.setFocusPainted(false);
        chkAsuransi.addActionListener(e -> updateKalkulasiEstimasi());
        panelForm.add(chkAsuransi, gbc);

        // Catatan Tambahan
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        panelForm.add(createFormLabel("Catatan Sewa:"), gbc);
        gbc.gridx = 1;
        txtCatatan = new JTextField("-");
        txtCatatan.setFont(UITheme.FONT_REGULAR);
        txtCatatan.setBorder(UITheme.createFieldBorder());
        txtCatatan.setPreferredSize(new Dimension(100, 30));
        panelForm.add(txtCatatan, gbc);

        // Panel Box Estimasi Total (Card Soft Green)
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JPanel panelEstimasi = new JPanel(new BorderLayout(4, 4));
        panelEstimasi.setBackground(UITheme.SOFT_GREEN);
        panelEstimasi.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.GREEN_BORDER, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        lblRincianKalkulasi = new JLabel("Rincian kalkulasi...");
        lblRincianKalkulasi.setFont(UITheme.FONT_SMALL);
        lblRincianKalkulasi.setForeground(UITheme.SECONDARY_GREEN);
        panelEstimasi.add(lblRincianKalkulasi, BorderLayout.NORTH);

        lblEstimasiTotal = new JLabel("TOTAL: Rp 0");
        lblEstimasiTotal.setFont(UITheme.FONT_TITLE_LARGE);
        lblEstimasiTotal.setForeground(UITheme.DARK_GREEN);
        panelEstimasi.add(lblEstimasiTotal, BorderLayout.CENTER);

        panelForm.add(panelEstimasi, gbc);

        // Tombol Proses Sewa (Primary Green)
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        btnProsesSewa = new JButton("Proses Penyewaan Kendaraan");
        UITheme.stylePrimaryButton(btnProsesSewa);
        btnProsesSewa.setFont(UITheme.FONT_BOLD);
        btnProsesSewa.setPreferredSize(new Dimension(240, 36));
        btnProsesSewa.addActionListener(e -> aksiProsesSewa());
        panelForm.add(btnProsesSewa, gbc);

        panelCardForm.add(panelForm, BorderLayout.CENTER);

        JScrollPane scrollForm = new JScrollPane(panelCardForm);
        scrollForm.setBorder(null);
        scrollForm.getViewport().setBackground(UITheme.WHITE);
        scrollForm.getVerticalScrollBar().setUnitIncrement(14);
        panelKiri.add(scrollForm, BorderLayout.CENTER);

        add(panelKiri, BorderLayout.WEST);

        // --- BAGIAN KANAN: TABEL TRANSAKSI & AKSI PENGEMBALIAN ---
        JPanel panelKanan = new JPanel(new BorderLayout(10, 10));
        panelKanan.setOpaque(false);

        // Filter Bar
        JPanel panelFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        panelFilter.setBackground(UITheme.WHITE);
        panelFilter.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));

        JLabel lblFilter = new JLabel("Status Transaksi:");
        lblFilter.setFont(UITheme.FONT_BOLD);
        lblFilter.setForeground(UITheme.TEXT_PRIMARY);
        panelFilter.add(lblFilter);

        cbFilterStatus = new JComboBox<>(new String[]{"SEMUA", "AKTIF", "SELESAI", "DIBATALKAN"});
        cbFilterStatus.setFont(UITheme.FONT_REGULAR);
        cbFilterStatus.setBackground(UITheme.WHITE);
        cbFilterStatus.setPreferredSize(new Dimension(130, 30));
        cbFilterStatus.addActionListener(e -> aksiFilter());
        panelFilter.add(cbFilterStatus);

        JButton btnSegarkan = new JButton("Segarkan Data");
        UITheme.styleNeutralButton(btnSegarkan);
        btnSegarkan.setPreferredSize(new Dimension(120, 30));
        btnSegarkan.addActionListener(e -> segarkanData());
        panelFilter.add(btnSegarkan);

        panelKanan.add(panelFilter, BorderLayout.NORTH);

        // Tabel JTable dengan 11 Kolom Standar
        String[] columns = {
                "ID Transaksi", "Pelanggan", "Armada", "No. Plat",
                "Tgl Sewa", "Durasi", "Status",
                "Biaya Sewa", "Denda", "Total Pembayaran", "Catatan"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelTransaksi = new JTable(tableModel);
        tabelTransaksi.setRowHeight(32);
        tabelTransaksi.setFont(UITheme.FONT_REGULAR);
        tabelTransaksi.setGridColor(UITheme.BORDER);
        tabelTransaksi.setShowGrid(true);
        tabelTransaksi.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelTransaksi.setSelectionBackground(UITheme.LIGHT_GREEN);
        tabelTransaksi.setSelectionForeground(UITheme.DARK_GREEN);

        // Header Custom Styling Dark Green
        JTableHeader tableHeader = tabelTransaksi.getTableHeader();
        tableHeader.setPreferredSize(new Dimension(tableHeader.getWidth(), 34));
        tableHeader.setReorderingAllowed(false);
        tableHeader.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setFont(UITheme.FONT_BOLD);
                lbl.setBackground(UITheme.DARK_GREEN);
                lbl.setForeground(UITheme.WHITE);
                lbl.setHorizontalAlignment(JLabel.CENTER);
                lbl.setOpaque(true);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 1, UITheme.SECONDARY_GREEN),
                        BorderFactory.createEmptyBorder(6, 6, 6, 6)));
                return lbl;
            }
        });

        // Cell Renderer Khusus: Zebra Striping, Alignment Sesuai Tipe, dan Badge Status Transaksi
        tabelTransaksi.setDefaultRenderer(Object.class, new TransaksiCellRenderer());

        // Lebar Kolom Proporsional
        tabelTransaksi.getColumnModel().getColumn(0).setPreferredWidth(85);   // ID Transaksi
        tabelTransaksi.getColumnModel().getColumn(1).setPreferredWidth(125);  // Pelanggan
        tabelTransaksi.getColumnModel().getColumn(2).setPreferredWidth(110);  // Armada
        tabelTransaksi.getColumnModel().getColumn(3).setPreferredWidth(85);   // No. Plat
        tabelTransaksi.getColumnModel().getColumn(4).setPreferredWidth(85);   // Tgl Sewa
        tabelTransaksi.getColumnModel().getColumn(5).setPreferredWidth(65);   // Durasi
        tabelTransaksi.getColumnModel().getColumn(6).setPreferredWidth(95);   // Status
        tabelTransaksi.getColumnModel().getColumn(7).setPreferredWidth(105);  // Biaya Sewa
        tabelTransaksi.getColumnModel().getColumn(8).setPreferredWidth(90);   // Denda
        tabelTransaksi.getColumnModel().getColumn(9).setPreferredWidth(115);  // Total Pembayaran
        tabelTransaksi.getColumnModel().getColumn(10).setPreferredWidth(90);  // Catatan

        // Listener seleksi baris: Tombol aksi hanya aktif sesuai status
        tabelTransaksi.getSelectionModel().addListSelectionListener(e -> {
            int selRow = tabelTransaksi.getSelectedRow();
            if (selRow >= 0) {
                String idTrx = (String) tableModel.getValueAt(selRow, 0);
                TransaksiSewa trx = manager.cariTransaksiById(idTrx);
                boolean isAktif = (trx != null && "AKTIF".equalsIgnoreCase(trx.getStatusTransaksi()));
                btnSelesaiPengembalian.setEnabled(isAktif);
                btnBatalTransaksi.setEnabled(isAktif);
                btnLihatStruk.setEnabled(true);
            } else {
                btnSelesaiPengembalian.setEnabled(false);
                btnBatalTransaksi.setEnabled(false);
                btnLihatStruk.setEnabled(false);
            }
        });

        JScrollPane scrollTable = new JScrollPane(tabelTransaksi);
        scrollTable.getViewport().setBackground(UITheme.WHITE);
        scrollTable.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));
        panelKanan.add(scrollTable, BorderLayout.CENTER);

        // Tombol Aksi di Bawah Tabel
        JPanel panelBottomActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        panelBottomActions.setBackground(UITheme.WHITE);
        panelBottomActions.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));

        // 1. Kembalikan Kendaraan (Primary Green Solid)
        btnSelesaiPengembalian = new JButton("Kembalikan Kendaraan");
        UITheme.stylePrimaryButton(btnSelesaiPengembalian);
        btnSelesaiPengembalian.setPreferredSize(new Dimension(180, 36));
        btnSelesaiPengembalian.setEnabled(false);
        btnSelesaiPengembalian.addActionListener(e -> aksiSelesaikanPengembalian());

        // 2. Lihat / Cetak Struk Sewa (Outline Green)
        btnLihatStruk = new JButton("Lihat / Cetak Struk Sewa");
        UITheme.styleOutlineSuccessButton(btnLihatStruk);
        btnLihatStruk.setPreferredSize(new Dimension(185, 36));
        btnLihatStruk.setEnabled(false);
        btnLihatStruk.addActionListener(e -> aksiBukaStruk());

        // 3. Batalkan Transaksi (Danger Red)
        btnBatalTransaksi = new JButton("Batalkan Transaksi");
        UITheme.styleDangerButton(btnBatalTransaksi);
        btnBatalTransaksi.setFont(UITheme.FONT_SMALL);
        btnBatalTransaksi.setPreferredSize(new Dimension(160, 36));
        btnBatalTransaksi.setEnabled(false);
        btnBatalTransaksi.addActionListener(e -> aksiBatalkanTransaksi());

        panelBottomActions.add(btnSelesaiPengembalian);
        panelBottomActions.add(btnLihatStruk);
        panelBottomActions.add(btnBatalTransaksi);

        panelKanan.add(panelBottomActions, BorderLayout.SOUTH);

        add(panelKanan, BorderLayout.CENTER);
    }

    private JLabel createFormLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_PRIMARY);
        return lbl;
    }

    public void segarkanFormPilihan() {
        txtIdTrx.setText(manager.generateNextIdTransaksi());

        // Update Combo Pelanggan
        cbPelanggan.removeAllItems();
        for (Pelanggan p : manager.getDaftarPelanggan()) {
            cbPelanggan.addItem(p);
        }

        // Update Combo Kendaraan Tersedia
        cbKendaraan.removeAllItems();
        List<Kendaraan> tersedia = manager.getKendaraanTersedia();
        for (Kendaraan k : tersedia) {
            cbKendaraan.addItem(k);
        }

        updateKalkulasiEstimasi();
    }

    private void updateKalkulasiEstimasi() {
        Kendaraan k = (Kendaraan) cbKendaraan.getSelectedItem();
        if (k == null) {
            lblRincianKalkulasi.setText("Silakan pilih unit armada yang tersedia.");
            lblEstimasiTotal.setText("TOTAL: Rp 0");
            btnProsesSewa.setEnabled(false);
            return;
        }

        btnProsesSewa.setEnabled(true);
        int durasi = (Integer) spnDurasi.getValue();
        boolean supir = chkSupir.isSelected();
        boolean asuransi = chkAsuransi.isSelected();

        // Hitung biaya kendaraan secara polimorfis
        double biayaDasar = k.hitungBiayaSewa(durasi);

        double biayaSupir = 0;
        if (supir && !(k instanceof Bus)) {
            biayaSupir = LayananTambahan.TARIF_SUPIR_PER_HARI * durasi;
        }

        double biayaAsuransi = 0;
        if (asuransi) {
            biayaAsuransi = (k instanceof Bus ? 50000.0 : LayananTambahan.TARIF_ASURANSI_PER_HARI) * durasi;
        }

        double subtotal = biayaDasar + biayaSupir + biayaAsuransi;
        double diskonTrx = (durasi >= 10) ? subtotal * 0.03 : 0;
        double grandTotal = Math.max(0, subtotal - diskonTrx);

        String info = String.format("Sewa %s (%d hr): Rp %s | Layanan: Rp %s",
                k.getJenisKendaraan(), durasi, rupiahFormat.format(biayaDasar),
                rupiahFormat.format(biayaSupir + biayaAsuransi));
        lblRincianKalkulasi.setText(info);
        lblEstimasiTotal.setText("TOTAL: Rp " + rupiahFormat.format(grandTotal));
    }

    private void muatDataTabel(List<TransaksiSewa> list) {
        tableModel.setRowCount(0);
        for (TransaksiSewa t : list) {
            tableModel.addRow(new Object[]{
                    t.getIdTransaksi(),
                    t.getPelanggan().getNama(),
                    t.getKendaraan().getMerk() + " " + t.getKendaraan().getModel(),
                    t.getKendaraan().getNomorPlat(),
                    t.getTanggalSewa(),
                    t.getDurasiHari() + " Hari",
                    t.getStatusTransaksi(),
                    "Rp " + rupiahFormat.format(t.getBiayaSewaAwal()),
                    "Rp " + rupiahFormat.format(t.getBiayaDenda()),
                    "Rp " + rupiahFormat.format(t.getTotalPembayaran()),
                    t.getCatatan()
            });
        }
    }

    private void aksiProsesSewa() {
        Pelanggan p = (Pelanggan) cbPelanggan.getSelectedItem();
        Kendaraan k = (Kendaraan) cbKendaraan.getSelectedItem();
        String tgl = txtTglSewa.getText().trim();
        int durasi = (Integer) spnDurasi.getValue();
        boolean supir = chkSupir.isSelected();
        boolean asuransi = chkAsuransi.isSelected();
        String catatan = txtCatatan.getText().trim();

        if (p == null) {
            JOptionPane.showMessageDialog(this, "Silakan pilih data pelanggan!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (k == null) {
            JOptionPane.showMessageDialog(this, "Tidak ada armada yang dipilih / tersedia!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            TransaksiSewa trx = manager.prosesSewa(p, k, tgl, durasi, supir, asuransi, catatan);
            JOptionPane.showMessageDialog(this,
                    "Transaksi " + trx.getIdTransaksi() + " berhasil dibuat!\nArmada [" +
                            k.getNomorPlat() + "] telah tercatat dalam status DISEWA.",
                    "Penyewaan Sukses", JOptionPane.INFORMATION_MESSAGE);

            segarkanData();
            if (onDataChangedCallback != null) onDataChangedCallback.run();

            // Tampilkan struk otomatis
            DialogStrukSewa dialog = new DialogStrukSewa(parentFrame, trx);
            dialog.setVisible(true);

        } catch (RentalException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Gagal Transaksi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void aksiSelesaikanPengembalian() {
        int row = tabelTransaksi.getSelectedRow();
        if (row < 0) return;

        String idTrx = (String) tableModel.getValueAt(row, 0);
        TransaksiSewa trx = manager.cariTransaksiById(idTrx);
        if (trx == null) {
            JOptionPane.showMessageDialog(this, "Data transaksi tidak ditemukan!", "Kesalahan", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!"AKTIF".equalsIgnoreCase(trx.getStatusTransaksi())) {
            JOptionPane.showMessageDialog(this,
                    "Transaksi [" + idTrx + "] berstatus " + trx.getStatusTransaksi() +
                    " dan tidak dapat diproses pengembaliannya.",
                    "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Buka Dialog Pengembalian Kendaraan dengan live preview & UITheme
        DialogPengembalian dialog = new DialogPengembalian(parentFrame, manager, trx, () -> {
            segarkanData();
            if (onDataChangedCallback != null) onDataChangedCallback.run();
        });
        dialog.setVisible(true);
    }

    private void aksiBukaStruk() {
        int row = tabelTransaksi.getSelectedRow();
        if (row < 0) return;

        String idTrx = (String) tableModel.getValueAt(row, 0);
        for (TransaksiSewa t : manager.getDaftarTransaksi()) {
            if (t.getIdTransaksi().equalsIgnoreCase(idTrx)) {
                DialogStrukSewa dialog = new DialogStrukSewa(parentFrame, t);
                dialog.setVisible(true);
                return;
            }
        }
    }

    private void aksiBatalkanTransaksi() {
        int row = tabelTransaksi.getSelectedRow();
        if (row < 0) return;

        String idTrx = (String) tableModel.getValueAt(row, 0);
        int konfirmasi = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin membatalkan transaksi " + idTrx + "?",
                "Konfirmasi Pembatalan",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (konfirmasi == JOptionPane.YES_OPTION) {
            try {
                manager.batalkanTransaksi(idTrx);
                JOptionPane.showMessageDialog(this, "Transaksi " + idTrx + " berhasil dibatalkan.", "Informasi", JOptionPane.INFORMATION_MESSAGE);
                segarkanData();
                if (onDataChangedCallback != null) onDataChangedCallback.run();
            } catch (RentalException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Gagal Pembatalan", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void aksiFilter() {
        String status = (String) cbFilterStatus.getSelectedItem();
        if ("SEMUA".equalsIgnoreCase(status)) {
            muatDataTabel(manager.getDaftarTransaksi());
        } else {
            List<TransaksiSewa> filtered = manager.getDaftarTransaksi().stream()
                    .filter(t -> t.getStatusTransaksi().equalsIgnoreCase(status))
                    .toList();
            muatDataTabel(filtered);
        }
    }

    public void segarkanData() {
        segarkanFormPilihan();
        aksiFilter();
    }

    /**
     * Renderer kustom sel tabel transaksi:
     * - Zebra striping: Genap putih, ganjil soft green (#F0FDF4).
     * - Perataan teks sesuai konteks data (tengah untuk ID, Tgl, Durasi; kanan untuk nominal; kiri untuk lainnya).
     * - Kolom status (indeks 6) berwujud pill/badge dengan palet kontras yang mudah dibedakan.
     */
    private static class TransaksiCellRenderer extends DefaultTableCellRenderer {
        private final JPanel panelBadge = new JPanel(new GridBagLayout());
        private final JLabel lblBadge = new JLabel();

        public TransaksiCellRenderer() {
            panelBadge.setOpaque(true);
            lblBadge.setOpaque(true);
            lblBadge.setFont(UITheme.FONT_SMALL_BOLD);
            panelBadge.add(lblBadge);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            // Kolom Status (Column 6): Custom Badge
            if (column == 6 && value != null) {
                String status = value.toString().toUpperCase();
                lblBadge.setText(" " + status + " ");

                if ("AKTIF".equals(status)) {
                    lblBadge.setBackground(new Color(0xE0, 0xF2, 0xFE)); // Soft Sky Blue
                    lblBadge.setForeground(new Color(0x03, 0x69, 0xA1)); // Dark Sky Blue
                    lblBadge.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(0xBA, 0xE6, 0xFD), 1),
                            BorderFactory.createEmptyBorder(2, 8, 2, 8)));
                } else if ("SELESAI".equals(status)) {
                    lblBadge.setBackground(UITheme.LIGHT_GREEN);
                    lblBadge.setForeground(UITheme.DARK_GREEN);
                    lblBadge.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(UITheme.GREEN_BORDER, 1),
                            BorderFactory.createEmptyBorder(2, 8, 2, 8)));
                } else { // DIBATALKAN
                    lblBadge.setBackground(new Color(0xFE, 0xE2, 0xE2)); // Soft Red
                    lblBadge.setForeground(UITheme.DANGER_HOVER); // Dark Red
                    lblBadge.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(0xFE, 0xCA, 0xCA), 1),
                            BorderFactory.createEmptyBorder(2, 8, 2, 8)));
                }

                if (isSelected) {
                    panelBadge.setBackground(UITheme.LIGHT_GREEN);
                } else {
                    panelBadge.setBackground(row % 2 == 0 ? UITheme.WHITE : UITheme.SOFT_GREEN);
                }
                return panelBadge;
            }

            JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            c.setFont(UITheme.FONT_REGULAR);

            // Perataan kolom
            if (column == 0 || column == 4 || column == 5) {
                c.setHorizontalAlignment(JLabel.CENTER);
            } else if (column == 7 || column == 8 || column == 9) {
                c.setHorizontalAlignment(JLabel.RIGHT);
            } else {
                c.setHorizontalAlignment(JLabel.LEFT);
            }

            // Zebra Striping & Selection Color
            if (isSelected) {
                c.setBackground(UITheme.LIGHT_GREEN);
                c.setForeground(UITheme.DARK_GREEN);
            } else {
                c.setBackground(row % 2 == 0 ? UITheme.WHITE : UITheme.SOFT_GREEN);
                c.setForeground(UITheme.TEXT_PRIMARY);
            }

            c.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            return c;
        }
    }
}
