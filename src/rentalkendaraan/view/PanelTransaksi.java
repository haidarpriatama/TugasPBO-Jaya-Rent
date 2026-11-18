package rentalkendaraan.view;

import java.awt.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import rentalkendaraan.exception.RentalException;
import rentalkendaraan.model.*;
import rentalkendaraan.service.RentalManager;

/**
 * Panel Manajemen Transaksi Penyewaan dan Pengembalian Armada Kendaraan.
 * Menghubungkan entitas Pelanggan dan Kendaraan secara dinamis dan polimorfik,
 * serta menghitung biaya dan diskon secara real-time.
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
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        initComponents();
        segarkanFormPilihan();
        muatDataTabel(manager.getDaftarTransaksi());
        updateKalkulasiEstimasi();
    }

    private void initComponents() {
        // --- BAGIAN KIRI: FORMULIR TRANSAKSI BARU ---
        JPanel panelKiri = new JPanel(new BorderLayout(8, 8));
        panelKiri.setPreferredSize(new Dimension(390, 600));

        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(148, 163, 184)),
                "Formulir Penyewaan Armada",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13), new Color(30, 41, 59)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // ID Transaksi
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        panelForm.add(new JLabel("No. Transaksi:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        txtIdTrx = new JTextField();
        txtIdTrx.setEditable(false);
        txtIdTrx.setBackground(new Color(241, 245, 249));
        panelForm.add(txtIdTrx, gbc);

        // Pelanggan (JComboBox)
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panelForm.add(new JLabel("Pelanggan:"), gbc);
        gbc.gridx = 1;
        cbPelanggan = new JComboBox<>();
        panelForm.add(cbPelanggan, gbc);

        // Kendaraan Tersedia (JComboBox)
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panelForm.add(new JLabel("Armada Unit:"), gbc);
        gbc.gridx = 1;
        cbKendaraan = new JComboBox<>();
        cbKendaraan.addActionListener(e -> updateKalkulasiEstimasi());
        panelForm.add(cbKendaraan, gbc);

        // Tanggal Sewa
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panelForm.add(new JLabel("Tgl Sewa (Y-M-D):"), gbc);
        gbc.gridx = 1;
        txtTglSewa = new JTextField(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        panelForm.add(txtTglSewa, gbc);

        // Durasi Sewa (JSpinner)
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panelForm.add(new JLabel("Durasi Sewa:"), gbc);
        gbc.gridx = 1;
        spnDurasi = new JSpinner(new SpinnerNumberModel(1, 1, 90, 1));
        spnDurasi.addChangeListener(e -> updateKalkulasiEstimasi());
        panelForm.add(spnDurasi, gbc);

        // Opsi Supir (JCheckBox)
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        chkSupir = new JCheckBox("Layanan Jasa Supir (+Rp 150.000/hari)");
        chkSupir.addActionListener(e -> updateKalkulasiEstimasi());
        panelForm.add(chkSupir, gbc);

        // Opsi Asuransi (JCheckBox)
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        chkAsuransi = new JCheckBox("Asuransi Perlindungan Tambahan (+Rp 35.000/hari)");
        chkAsuransi.addActionListener(e -> updateKalkulasiEstimasi());
        panelForm.add(chkAsuransi, gbc);

        // Catatan Tambahan
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        panelForm.add(new JLabel("Catatan Sewa:"), gbc);
        gbc.gridx = 1;
        txtCatatan = new JTextField("-");
        panelForm.add(txtCatatan, gbc);

        // Panel Box Estimasi Total
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JPanel panelEstimasi = new JPanel(new BorderLayout(4, 4));
        panelEstimasi.setBackground(new Color(240, 253, 244));
        panelEstimasi.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(187, 247, 208)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        lblRincianKalkulasi = new JLabel("Rincian kalkulasi...");
        lblRincianKalkulasi.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblRincianKalkulasi.setForeground(new Color(21, 128, 61));
        panelEstimasi.add(lblRincianKalkulasi, BorderLayout.NORTH);

        lblEstimasiTotal = new JLabel("TOTAL: Rp 0");
        lblEstimasiTotal.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblEstimasiTotal.setForeground(new Color(22, 101, 52));
        panelEstimasi.add(lblEstimasiTotal, BorderLayout.CENTER);

        panelForm.add(panelEstimasi, gbc);

        // Tombol Proses Sewa
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        btnProsesSewa = new JButton("Proses Penyewaan Kendaraan");
        btnProsesSewa.setBackground(new Color(22, 163, 74));
        btnProsesSewa.setForeground(Color.WHITE);
        btnProsesSewa.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnProsesSewa.setFocusPainted(false);
        btnProsesSewa.setPreferredSize(new Dimension(200, 38));
        btnProsesSewa.addActionListener(e -> aksiProsesSewa());
        panelForm.add(btnProsesSewa, gbc);

        panelKiri.add(new JScrollPane(panelForm), BorderLayout.CENTER);
        add(panelKiri, BorderLayout.WEST);

        // --- BAGIAN KANAN: TABEL TRANSAKSI & AKSI PENGEMBALIAN ---
        JPanel panelKanan = new JPanel(new BorderLayout(8, 8));

        // Filter Bar
        JPanel panelFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        panelFilter.setBackground(new Color(248, 250, 252));
        panelFilter.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        panelFilter.add(new JLabel("Status Transaksi:"));
        cbFilterStatus = new JComboBox<>(new String[]{"SEMUA", "AKTIF", "SELESAI", "DIBATALKAN"});
        cbFilterStatus.addActionListener(e -> aksiFilter());
        panelFilter.add(cbFilterStatus);

        JButton btnSegarkan = new JButton("Segarkan Data");
        btnSegarkan.addActionListener(e -> segarkanData());
        panelFilter.add(btnSegarkan);

        panelKanan.add(panelFilter, BorderLayout.NORTH);

        // Table JTable
        String[] columns = {"ID Transaksi", "Pelanggan", "Armada", "No. Plat", "Tgl Sewa", "Durasi", "Total Biaya", "Status", "Catatan"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelTransaksi = new JTable(tableModel);
        tabelTransaksi.setRowHeight(26);
        tabelTransaksi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabelTransaksi.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabelTransaksi.getTableHeader().setBackground(new Color(241, 245, 249));
        tabelTransaksi.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Right renderer untuk Total Biaya
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        tabelTransaksi.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);

        tabelTransaksi.getSelectionModel().addListSelectionListener(e -> {
            boolean selected = tabelTransaksi.getSelectedRow() >= 0;
            btnSelesaiPengembalian.setEnabled(selected);
            btnLihatStruk.setEnabled(selected);
            btnBatalTransaksi.setEnabled(selected);
        });

        JScrollPane scrollTable = new JScrollPane(tabelTransaksi);
        panelKanan.add(scrollTable, BorderLayout.CENTER);

        // Tombol Aksi di Bawah Tabel
        JPanel panelBottomActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        panelBottomActions.setBackground(new Color(248, 250, 252));
        panelBottomActions.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        btnSelesaiPengembalian = new JButton("Selesaikan Pengembalian Unit");
        btnSelesaiPengembalian.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSelesaiPengembalian.setBackground(new Color(37, 99, 235));
        btnSelesaiPengembalian.setForeground(Color.WHITE);
        btnSelesaiPengembalian.setEnabled(false);
        btnSelesaiPengembalian.addActionListener(e -> aksiSelesaikanPengembalian());

        btnLihatStruk = new JButton("Lihat / Cetak Struk Sewa");
        btnLihatStruk.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLihatStruk.setBackground(new Color(79, 70, 229));
        btnLihatStruk.setForeground(Color.WHITE);
        btnLihatStruk.setEnabled(false);
        btnLihatStruk.addActionListener(e -> aksiBukaStruk());

        btnBatalTransaksi = new JButton("Batalkan Transaksi");
        btnBatalTransaksi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnBatalTransaksi.setBackground(new Color(220, 38, 38));
        btnBatalTransaksi.setForeground(Color.WHITE);
        btnBatalTransaksi.setEnabled(false);
        btnBatalTransaksi.addActionListener(e -> aksiBatalkanTransaksi());

        panelBottomActions.add(btnSelesaiPengembalian);
        panelBottomActions.add(btnLihatStruk);
        panelBottomActions.add(btnBatalTransaksi);

        panelKanan.add(panelBottomActions, BorderLayout.SOUTH);

        add(panelKanan, BorderLayout.CENTER);
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
                    "Rp " + rupiahFormat.format(t.getTotalBiaya()),
                    t.getStatusTransaksi(),
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
        int konfirmasi = JOptionPane.showConfirmDialog(this,
                "Apakah pelanggan telah mengembalikan unit kendaraan untuk transaksi " + idTrx + "?\n" +
                        "Status akan diubah menjadi SELESAI dan armada akan kembali TERSEDIA.",
                "Konfirmasi Pengembalian Unit",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (konfirmasi == JOptionPane.YES_OPTION) {
            try {
                manager.prosesPengembalian(idTrx);
                JOptionPane.showMessageDialog(this,
                        "Unit armada berhasil dikembalikan!\nStatus transaksi " + idTrx + " kini SELESAI.",
                        "Pengembalian Berhasil", JOptionPane.INFORMATION_MESSAGE);
                segarkanData();
                if (onDataChangedCallback != null) onDataChangedCallback.run();
            } catch (RentalException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Gagal Pengembalian", JOptionPane.ERROR_MESSAGE);
            }
        }
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
}
