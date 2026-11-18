package rentalkendaraan.view;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import rentalkendaraan.exception.RentalException;
import rentalkendaraan.model.*;
import rentalkendaraan.service.RentalManager;

/**
 * Panel Manajemen Armada Kendaraan (CRUD Mobil, Motor, Bus).
 * Menerapkan variasi komponen input GUI dan layout manager yang responsif.
 */
public class PanelKendaraan extends JPanel {
    private final RentalManager manager;
    private final Runnable onDataChangedCallback;

    // Komponen Form Input Utama
    private JTextField txtId;
    private JComboBox<String> cbJenis;
    private JTextField txtPlat;
    private JTextField txtMerk;
    private JTextField txtModel;
    private JSpinner spnTahun;
    private JTextField txtTarif;
    private JCheckBox chkTersedia;

    // Komponen Spesifik Mobil
    private JPanel panelMobil;
    private JComboBox<String> cbTipeMobil;
    private JRadioButton rbManual;
    private JRadioButton rbOtomatis;
    private JSpinner spnKapasitasMobil;

    // Komponen Spesifik Motor
    private JPanel panelMotor;
    private JComboBox<String> cbTipeMotor;
    private JSpinner spnCcMotor;
    private JCheckBox chkHelm;

    // Komponen Spesifik Bus
    private JPanel panelBus;
    private JSpinner spnKursiBus;
    private JCheckBox chkToilet;
    private JCheckBox chkWajibSupir;

    private JPanel panelCardKhusus;
    private CardLayout cardLayoutKhusus;

    // Tombol Aksi
    private JButton btnTambah;
    private JButton btnUpdate;
    private JButton btnHapus;
    private JButton btnReset;

    // Komponen Filter & Tabel
    private JTextField txtCari;
    private JComboBox<String> cbFilterJenis;
    private JComboBox<String> cbFilterStatus;
    private JTable tabelKendaraan;
    private DefaultTableModel tableModel;

    private final DecimalFormat rupiahFormat = new DecimalFormat("#,###");

    public PanelKendaraan(RentalManager manager, Runnable onDataChangedCallback) {
        this.manager = manager;
        this.onDataChangedCallback = onDataChangedCallback;
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        initComponents();
        muatDataTabel(manager.getDaftarKendaraan());
        resetForm();
    }

    private void initComponents() {
        // --- BAGIAN KIRI: FORM INPUT KENDARAAN ---
        JPanel panelKiri = new JPanel(new BorderLayout(8, 8));
        panelKiri.setPreferredSize(new Dimension(360, 600));

        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(148, 163, 184)),
                "Formulir Data Armada Kendaraan",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13), new Color(30, 41, 59)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // ID Kendaraan
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        panelForm.add(new JLabel("ID Kendaraan:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtId = new JTextField();
        txtId.setEditable(false);
        txtId.setBackground(new Color(241, 245, 249));
        panelForm.add(txtId, gbc);

        // Jenis Kendaraan
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panelForm.add(new JLabel("Jenis Armada:"), gbc);
        gbc.gridx = 1;
        cbJenis = new JComboBox<>(new String[]{"Mobil", "Motor", "Bus"});
        cbJenis.addActionListener(e -> gantiKartuSpesifikasi((String) cbJenis.getSelectedItem()));
        panelForm.add(cbJenis, gbc);

        // Nomor Plat
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panelForm.add(new JLabel("Nomor Plat:"), gbc);
        gbc.gridx = 1;
        txtPlat = new JTextField();
        panelForm.add(txtPlat, gbc);

        // Merk
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panelForm.add(new JLabel("Merk Armada:"), gbc);
        gbc.gridx = 1;
        txtMerk = new JTextField();
        panelForm.add(txtMerk, gbc);

        // Model
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panelForm.add(new JLabel("Model / Tipe:"), gbc);
        gbc.gridx = 1;
        txtModel = new JTextField();
        panelForm.add(txtModel, gbc);

        // Tahun Produksi (JSpinner)
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panelForm.add(new JLabel("Tahun Pembuatan:"), gbc);
        gbc.gridx = 1;
        spnTahun = new JSpinner(new SpinnerNumberModel(2023, 1990, 2030, 1));
        panelForm.add(spnTahun, gbc);

        // Tarif Sewa Per Hari
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panelForm.add(new JLabel("Tarif / Hari (Rp):"), gbc);
        gbc.gridx = 1;
        txtTarif = new JTextField();
        panelForm.add(txtTarif, gbc);

        // Status Ketersediaan (JCheckBox)
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panelForm.add(new JLabel("Status Unit:"), gbc);
        gbc.gridx = 1;
        chkTersedia = new JCheckBox("Tersedia untuk Disewa", true);
        panelForm.add(chkTersedia, gbc);

        // --- PANEL KARTU SPESIFIKASI KHUSUS SESUAI POLIMORFISME ---
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        cardLayoutKhusus = new CardLayout();
        panelCardKhusus = new JPanel(cardLayoutKhusus);
        panelCardKhusus.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                "Parameter Khusus Jenis",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), new Color(71, 85, 105)));

        initSubPanelMobil();
        initSubPanelMotor();
        initSubPanelBus();

        panelCardKhusus.add(panelMobil, "Mobil");
        panelCardKhusus.add(panelMotor, "Motor");
        panelCardKhusus.add(panelBus, "Bus");
        panelForm.add(panelCardKhusus, gbc);

        // Tombol Operasi CRUD
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JPanel panelBtnAction = new JPanel(new GridLayout(2, 2, 6, 6));
        panelBtnAction.setBorder(BorderFactory.createEmptyBorder(8, 0, 4, 0));

        btnTambah = new JButton("Tambah Unit");
        btnTambah.setBackground(new Color(22, 163, 74));
        btnTambah.setForeground(Color.WHITE);
        btnTambah.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnTambah.addActionListener(e -> aksiTambah());

        btnUpdate = new JButton("Simpan Perubahan");
        btnUpdate.setBackground(new Color(37, 99, 235));
        btnUpdate.setForeground(Color.WHITE);
        btnUpdate.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnUpdate.setEnabled(false);
        btnUpdate.addActionListener(e -> aksiUpdate());

        btnHapus = new JButton("Hapus Unit");
        btnHapus.setBackground(new Color(220, 38, 38));
        btnHapus.setForeground(Color.WHITE);
        btnHapus.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnHapus.setEnabled(false);
        btnHapus.addActionListener(e -> aksiHapus());

        btnReset = new JButton("Bersihkan Form");
        btnReset.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnReset.addActionListener(e -> resetForm());

        panelBtnAction.add(btnTambah);
        panelBtnAction.add(btnUpdate);
        panelBtnAction.add(btnHapus);
        panelBtnAction.add(btnReset);
        panelForm.add(panelBtnAction, gbc);

        panelKiri.add(new JScrollPane(panelForm), BorderLayout.CENTER);
        add(panelKiri, BorderLayout.WEST);

        // --- BAGIAN KANAN: TABEL DAN PENCARIAN / FILTER ---
        JPanel panelKanan = new JPanel(new BorderLayout(8, 8));

        // Filter & Search Bar
        JPanel panelFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        panelFilter.setBackground(new Color(248, 250, 252));
        panelFilter.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        panelFilter.add(new JLabel("Cari Unit:"));
        txtCari = new JTextField(12);
        panelFilter.add(txtCari);

        JButton btnCari = new JButton("Cari");
        btnCari.addActionListener(e -> aksiCari());
        panelFilter.add(btnCari);

        panelFilter.add(Box.createHorizontalStrut(10));
        panelFilter.add(new JLabel("Filter Jenis:"));
        cbFilterJenis = new JComboBox<>(new String[]{"SEMUA", "Mobil", "Motor", "Bus"});
        cbFilterJenis.addActionListener(e -> aksiFilter());
        panelFilter.add(cbFilterJenis);

        panelFilter.add(new JLabel("Status:"));
        cbFilterStatus = new JComboBox<>(new String[]{"SEMUA", "Tersedia", "Disewa"});
        cbFilterStatus.addActionListener(e -> aksiFilter());
        panelFilter.add(cbFilterStatus);

        JButton btnResetFilter = new JButton("Reset");
        btnResetFilter.addActionListener(e -> {
            txtCari.setText("");
            cbFilterJenis.setSelectedIndex(0);
            cbFilterStatus.setSelectedIndex(0);
            muatDataTabel(manager.getDaftarKendaraan());
        });
        panelFilter.add(btnResetFilter);

        panelKanan.add(panelFilter, BorderLayout.NORTH);

        // Tabel JTable
        String[] columns = {"ID", "Jenis", "No. Plat", "Merk", "Model", "Tahun", "Tarif/Hari", "Status", "Spesifikasi Khusus"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelKendaraan = new JTable(tableModel);
        tabelKendaraan.setRowHeight(26);
        tabelKendaraan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabelKendaraan.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabelKendaraan.getTableHeader().setBackground(new Color(241, 245, 249));
        tabelKendaraan.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Rata kanan untuk kolom tarif
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        tabelKendaraan.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);

        tabelKendaraan.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                pilihBarisTabel();
            }
        });

        JScrollPane scrollTable = new JScrollPane(tabelKendaraan);
        panelKanan.add(scrollTable, BorderLayout.CENTER);

        add(panelKanan, BorderLayout.CENTER);
    }

    private void initSubPanelMobil() {
        panelMobil = new JPanel(new GridLayout(3, 2, 4, 4));
        panelMobil.add(new JLabel("Tipe Bodi:"));
        cbTipeMobil = new JComboBox<>(new String[]{"MPV", "SUV", "Sedan", "Hatchback", "City Car"});
        panelMobil.add(cbTipeMobil);

        panelMobil.add(new JLabel("Transmisi:"));
        JPanel pTrans = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        rbManual = new JRadioButton("Manual");
        rbOtomatis = new JRadioButton("Otomatis", true);
        ButtonGroup bgTrans = new ButtonGroup();
        bgTrans.add(rbManual);
        bgTrans.add(rbOtomatis);
        pTrans.add(rbManual);
        pTrans.add(rbOtomatis);
        panelMobil.add(pTrans);

        panelMobil.add(new JLabel("Kapasitas:"));
        spnKapasitasMobil = new JSpinner(new SpinnerNumberModel(7, 1, 12, 1));
        panelMobil.add(spnKapasitasMobil);
    }

    private void initSubPanelMotor() {
        panelMotor = new JPanel(new GridLayout(3, 2, 4, 4));
        panelMotor.add(new JLabel("Tipe Motor:"));
        cbTipeMotor = new JComboBox<>(new String[]{"Matic", "Manual/Bebek", "Sport"});
        panelMotor.add(cbTipeMotor);

        panelMotor.add(new JLabel("Kapasitas CC:"));
        spnCcMotor = new JSpinner(new SpinnerNumberModel(150, 50, 1000, 25));
        panelMotor.add(spnCcMotor);

        panelMotor.add(new JLabel("Fasilitas:"));
        chkHelm = new JCheckBox("2 Helm & Jas Hujan", true);
        panelMotor.add(chkHelm);
    }

    private void initSubPanelBus() {
        panelBus = new JPanel(new GridLayout(3, 2, 4, 4));
        panelBus.add(new JLabel("Kapasitas Kursi:"));
        spnKursiBus = new JSpinner(new SpinnerNumberModel(30, 10, 65, 5));
        panelBus.add(spnKursiBus);

        panelBus.add(new JLabel("Toilet Dalam:"));
        chkToilet = new JCheckBox("Tersedia Toilet", false);
        panelBus.add(chkToilet);

        panelBus.add(new JLabel("Regulasi Driver:"));
        chkWajibSupir = new JCheckBox("Wajib Kru Driver", true);
        chkWajibSupir.setEnabled(false); // Standar operasional wajib
        panelBus.add(chkWajibSupir);
    }

    private void gantiKartuSpesifikasi(String jenis) {
        if (cardLayoutKhusus != null && panelCardKhusus != null) {
            cardLayoutKhusus.show(panelCardKhusus, jenis);
        }
    }

    private void muatDataTabel(List<Kendaraan> list) {
        tableModel.setRowCount(0);
        for (Kendaraan k : list) {
            tableModel.addRow(new Object[]{
                    k.getIdKendaraan(),
                    k.getJenisKendaraan(),
                    k.getNomorPlat(),
                    k.getMerk(),
                    k.getModel(),
                    k.getTahunProduksi(),
                    "Rp " + rupiahFormat.format(k.getTarifPerHari()),
                    k.isStatusTersedia() ? "TERSEDIA" : "DISEWA",
                    k.getSpesifikasiKhusus()
            });
        }
    }

    private void pilihBarisTabel() {
        int row = tabelKendaraan.getSelectedRow();
        if (row < 0) return;

        String id = (String) tableModel.getValueAt(row, 0);
        Kendaraan k = manager.cariKendaraanById(id);
        if (k == null) return;

        txtId.setText(k.getIdKendaraan());
        txtPlat.setText(k.getNomorPlat());
        txtMerk.setText(k.getMerk());
        txtModel.setText(k.getModel());
        spnTahun.setValue(k.getTahunProduksi());
        txtTarif.setText(String.valueOf((long) k.getTarifPerHari()));
        chkTersedia.setSelected(k.isStatusTersedia());

        if (k instanceof Mobil) {
            Mobil m = (Mobil) k;
            cbJenis.setSelectedItem("Mobil");
            cbTipeMobil.setSelectedItem(m.getTipeMobil());
            if ("Manual".equalsIgnoreCase(m.getTransmisi())) {
                rbManual.setSelected(true);
            } else {
                rbOtomatis.setSelected(true);
            }
            spnKapasitasMobil.setValue(m.getKapasitasPenumpang());
        } else if (k instanceof Motor) {
            Motor m = (Motor) k;
            cbJenis.setSelectedItem("Motor");
            cbTipeMotor.setSelectedItem(m.getTipeMotor());
            spnCcMotor.setValue(m.getKapasitasMesinCc());
            chkHelm.setSelected(m.isIncludeHelmDua());
        } else if (k instanceof Bus) {
            Bus b = (Bus) k;
            cbJenis.setSelectedItem("Bus");
            spnKursiBus.setValue(b.getKapasitasKursi());
            chkToilet.setSelected(b.isFasilitasToilet());
            chkWajibSupir.setSelected(b.isWajibSupir());
        }

        btnTambah.setEnabled(false);
        btnUpdate.setEnabled(true);
        btnHapus.setEnabled(true);
    }

    private void resetForm() {
        txtId.setText(manager.generateNextIdKendaraan());
        cbJenis.setSelectedIndex(0);
        txtPlat.setText("");
        txtMerk.setText("");
        txtModel.setText("");
        spnTahun.setValue(2023);
        txtTarif.setText("");
        chkTersedia.setSelected(true);

        cbTipeMobil.setSelectedIndex(0);
        rbOtomatis.setSelected(true);
        spnKapasitasMobil.setValue(7);

        cbTipeMotor.setSelectedIndex(0);
        spnCcMotor.setValue(150);
        chkHelm.setSelected(true);

        spnKursiBus.setValue(30);
        chkToilet.setSelected(false);
        chkWajibSupir.setSelected(true);

        tabelKendaraan.clearSelection();
        btnTambah.setEnabled(true);
        btnUpdate.setEnabled(false);
        btnHapus.setEnabled(false);
    }

    private Kendaraan buatObjekDariForm(String id) throws RentalException {
        String plat = txtPlat.getText().trim();
        String merk = txtMerk.getText().trim();
        String model = txtModel.getText().trim();
        int tahun = (Integer) spnTahun.getValue();

        double tarif;
        try {
            tarif = Double.parseDouble(txtTarif.getText().trim().replaceAll("[^0-9.]", ""));
        } catch (NumberFormatException e) {
            throw new rentalkendaraan.exception.DataValidasiException("Tarif harus berupa angka nominal yang valid.");
        }

        boolean tersedia = chkTersedia.isSelected();
        String jenis = (String) cbJenis.getSelectedItem();

        if ("Mobil".equalsIgnoreCase(jenis)) {
            String tipe = (String) cbTipeMobil.getSelectedItem();
            String transmisi = rbManual.isSelected() ? "Manual" : "Otomatis";
            int kapasitas = (Integer) spnKapasitasMobil.getValue();
            return new Mobil(id, plat, merk, model, tahun, tarif, tersedia, tipe, transmisi, kapasitas);
        } else if ("Motor".equalsIgnoreCase(jenis)) {
            String tipe = (String) cbTipeMotor.getSelectedItem();
            int cc = (Integer) spnCcMotor.getValue();
            boolean helm = chkHelm.isSelected();
            return new Motor(id, plat, merk, model, tahun, tarif, tersedia, tipe, cc, helm);
        } else if ("Bus".equalsIgnoreCase(jenis)) {
            int kursi = (Integer) spnKursiBus.getValue();
            boolean toilet = chkToilet.isSelected();
            boolean supir = chkWajibSupir.isSelected();
            return new Bus(id, plat, merk, model, tahun, tarif, tersedia, kursi, toilet, supir);
        }

        throw new rentalkendaraan.exception.DataValidasiException("Jenis armada tidak dikenal.");
    }

    private void aksiTambah() {
        try {
            Kendaraan k = buatObjekDariForm(manager.generateNextIdKendaraan());
            manager.tambahKendaraan(k);
            JOptionPane.showMessageDialog(this,
                    "Armada baru [" + k.getNomorPlat() + "] berhasil ditambahkan ke sistem!",
                    "Sukses", JOptionPane.INFORMATION_MESSAGE);
            muatDataTabel(manager.getDaftarKendaraan());
            resetForm();
            if (onDataChangedCallback != null) onDataChangedCallback.run();
        } catch (RentalException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void aksiUpdate() {
        try {
            String id = txtId.getText();
            Kendaraan k = buatObjekDariForm(id);
            manager.updateKendaraan(k);
            JOptionPane.showMessageDialog(this,
                    "Data armada [" + k.getNomorPlat() + "] berhasil diperbarui!",
                    "Sukses", JOptionPane.INFORMATION_MESSAGE);
            muatDataTabel(manager.getDaftarKendaraan());
            resetForm();
            if (onDataChangedCallback != null) onDataChangedCallback.run();
        } catch (RentalException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void aksiHapus() {
        String id = txtId.getText();
        Kendaraan k = manager.cariKendaraanById(id);
        if (k == null) return;

        int konfirmasi = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin menghapus data armada:\n" +
                        "[" + k.getIdKendaraan() + "] " + k.getNomorPlat() + " - " + k.getMerk() + " " + k.getModel() + "?",
                "Konfirmasi Hapus Data",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (konfirmasi == JOptionPane.YES_OPTION) {
            try {
                manager.hapusKendaraan(id);
                JOptionPane.showMessageDialog(this, "Armada berhasil dihapus.", "Informasi", JOptionPane.INFORMATION_MESSAGE);
                muatDataTabel(manager.getDaftarKendaraan());
                resetForm();
                if (onDataChangedCallback != null) onDataChangedCallback.run();
            } catch (RentalException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Gagal Menghapus", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void aksiCari() {
        String kw = txtCari.getText();
        List<Kendaraan> hasil = manager.cariKendaraan(kw);
        muatDataTabel(hasil);
    }

    private void aksiFilter() {
        String jenis = (String) cbFilterJenis.getSelectedItem();
        String statusStr = (String) cbFilterStatus.getSelectedItem();

        Boolean hanyaTersedia = null;
        if ("Tersedia".equalsIgnoreCase(statusStr)) hanyaTersedia = true;
        else if ("Disewa".equalsIgnoreCase(statusStr)) hanyaTersedia = false;

        List<Kendaraan> hasil = manager.filterKendaraan(jenis, hanyaTersedia);
        muatDataTabel(hasil);
    }

    public void segarkanData() {
        muatDataTabel(manager.getDaftarKendaraan());
    }
}
