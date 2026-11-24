package rentalkendaraan.view;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.List;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import rentalkendaraan.exception.RentalException;
import rentalkendaraan.model.*;
import rentalkendaraan.service.RentalManager;

/**
 * Panel Manajemen Armada Kendaraan (CRUD Mobil, Motor, Bus).
 * Menerapkan variasi komponen input GUI dan layout manager yang responsif
 * dengan desain modern bertema Green & White (UITheme).
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
        setBackground(UITheme.WHITE);
        setLayout(new BorderLayout(14, 14));
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        initComponents();
        muatDataTabel(manager.getDaftarKendaraan());
        resetForm();
    }

    private void initComponents() {
        // --- BAGIAN KIRI: CARD FORM INPUT KENDARAAN ---
        JPanel panelKiri = new JPanel(new BorderLayout(8, 8));
        panelKiri.setPreferredSize(new Dimension(380, 600));
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

        JLabel lblJudulForm = new JLabel("Form Data Armada Kendaraan");
        lblJudulForm.setFont(UITheme.FONT_TITLE);
        lblJudulForm.setForeground(UITheme.DARK_GREEN);
        panelHeaderForm.add(lblJudulForm);

        JLabel lblSubJudul = new JLabel("Kelola data armada Mobil, Motor, dan Bus");
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

        // ID Kendaraan
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        panelForm.add(createFormLabel("ID Kendaraan:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        txtId = new JTextField();
        txtId.setEditable(false);
        txtId.setFont(UITheme.FONT_REGULAR);
        txtId.setBackground(UITheme.SOFT_GREEN);
        txtId.setForeground(UITheme.DARK_GREEN);
        txtId.setBorder(UITheme.createGreenFieldBorder());
        txtId.setPreferredSize(new Dimension(100, 30));
        panelForm.add(txtId, gbc);

        // Jenis Kendaraan
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panelForm.add(createFormLabel("Jenis Armada:"), gbc);
        gbc.gridx = 1;
        cbJenis = new JComboBox<>(new String[]{"Mobil", "Motor", "Bus"});
        cbJenis.setFont(UITheme.FONT_REGULAR);
        cbJenis.setBackground(UITheme.WHITE);
        cbJenis.setPreferredSize(new Dimension(100, 30));
        cbJenis.addActionListener(e -> gantiKartuSpesifikasi((String) cbJenis.getSelectedItem()));
        panelForm.add(cbJenis, gbc);

        // Nomor Plat
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panelForm.add(createFormLabel("Nomor Plat:"), gbc);
        gbc.gridx = 1;
        txtPlat = new JTextField();
        txtPlat.setFont(UITheme.FONT_REGULAR);
        txtPlat.setBorder(UITheme.createFieldBorder());
        txtPlat.setPreferredSize(new Dimension(100, 30));
        panelForm.add(txtPlat, gbc);

        // Merk
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panelForm.add(createFormLabel("Merk Armada:"), gbc);
        gbc.gridx = 1;
        txtMerk = new JTextField();
        txtMerk.setFont(UITheme.FONT_REGULAR);
        txtMerk.setBorder(UITheme.createFieldBorder());
        txtMerk.setPreferredSize(new Dimension(100, 30));
        panelForm.add(txtMerk, gbc);

        // Model
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panelForm.add(createFormLabel("Model / Seri:"), gbc);
        gbc.gridx = 1;
        txtModel = new JTextField();
        txtModel.setFont(UITheme.FONT_REGULAR);
        txtModel.setBorder(UITheme.createFieldBorder());
        txtModel.setPreferredSize(new Dimension(100, 30));
        panelForm.add(txtModel, gbc);

        // Tahun Produksi (JSpinner)
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panelForm.add(createFormLabel("Tahun Buat:"), gbc);
        gbc.gridx = 1;
        spnTahun = new JSpinner(new SpinnerNumberModel(2023, 1990, 2030, 1));
        spnTahun.setFont(UITheme.FONT_REGULAR);
        spnTahun.setPreferredSize(new Dimension(100, 30));
        spnTahun.setBorder(UITheme.createFieldBorder());
        panelForm.add(spnTahun, gbc);

        // Tarif Sewa Per Hari
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panelForm.add(createFormLabel("Tarif / Hari (Rp):"), gbc);
        gbc.gridx = 1;
        txtTarif = new JTextField();
        txtTarif.setFont(UITheme.FONT_REGULAR);
        txtTarif.setBorder(UITheme.createFieldBorder());
        txtTarif.setPreferredSize(new Dimension(100, 30));
        panelForm.add(txtTarif, gbc);

        // Status Ketersediaan (JCheckBox)
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panelForm.add(createFormLabel("Status Unit:"), gbc);
        gbc.gridx = 1;
        chkTersedia = new JCheckBox("Tersedia untuk Disewa", true);
        chkTersedia.setFont(UITheme.FONT_REGULAR);
        chkTersedia.setForeground(UITheme.DARK_GREEN);
        chkTersedia.setBackground(UITheme.WHITE);
        chkTersedia.setFocusPainted(false);
        panelForm.add(chkTersedia, gbc);

        // --- PANEL KARTU SPESIFIKASI KHUSUS SESUAI POLIMORFISME ---
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        cardLayoutKhusus = new CardLayout();
        panelCardKhusus = new JPanel(cardLayoutKhusus);
        panelCardKhusus.setBackground(UITheme.WHITE);
        panelCardKhusus.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UITheme.GREEN_BORDER, 1),
                "Parameter Khusus Jenis",
                TitledBorder.LEFT, TitledBorder.TOP,
                UITheme.FONT_SMALL_BOLD, UITheme.DARK_GREEN));

        initSubPanelMobil();
        initSubPanelMotor();
        initSubPanelBus();

        panelCardKhusus.add(panelMobil, "Mobil");
        panelCardKhusus.add(panelMotor, "Motor");
        panelCardKhusus.add(panelBus, "Bus");
        panelForm.add(panelCardKhusus, gbc);

        // Tombol Operasi CRUD (2x2 Grid)
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JPanel panelBtnAction = new JPanel(new GridLayout(2, 2, 8, 8));
        panelBtnAction.setOpaque(false);
        panelBtnAction.setPreferredSize(new Dimension(340, 80));
        panelBtnAction.setBorder(BorderFactory.createEmptyBorder(10, 0, 4, 0));

        // 1. Tambah Unit (Primary Green Solid)
        btnTambah = new JButton("Tambah Unit");
        UITheme.stylePrimaryButton(btnTambah);
        btnTambah.addActionListener(e -> aksiTambah());

        // 2. Simpan Perubahan (Outline Green)
        btnUpdate = new JButton("Simpan Perubahan");
        UITheme.styleOutlineSuccessButton(btnUpdate);
        btnUpdate.setEnabled(false);
        btnUpdate.addActionListener(e -> aksiUpdate());

        // 3. Hapus Unit (Danger Red)
        btnHapus = new JButton("Hapus Unit");
        UITheme.styleDangerButton(btnHapus);
        btnHapus.setEnabled(false);
        btnHapus.addActionListener(e -> aksiHapus());

        // 4. Bersihkan Form (Secondary Netral)
        btnReset = new JButton("Bersihkan Form");
        UITheme.styleNeutralButton(btnReset);
        btnReset.addActionListener(e -> resetForm());

        panelBtnAction.add(btnTambah);
        panelBtnAction.add(btnUpdate);
        panelBtnAction.add(btnHapus);
        panelBtnAction.add(btnReset);
        panelForm.add(panelBtnAction, gbc);

        panelCardForm.add(panelForm, BorderLayout.CENTER);

        JScrollPane scrollForm = new JScrollPane(panelCardForm);
        scrollForm.setBorder(null);
        scrollForm.getViewport().setBackground(UITheme.WHITE);
        scrollForm.getVerticalScrollBar().setUnitIncrement(14);
        panelKiri.add(scrollForm, BorderLayout.CENTER);

        add(panelKiri, BorderLayout.WEST);

        // --- BAGIAN KANAN: TABEL DAN PENCARIAN / FILTER ---
        JPanel panelKanan = new JPanel(new BorderLayout(10, 10));
        panelKanan.setOpaque(false);

        // Filter & Search Bar
        JPanel panelFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        panelFilter.setBackground(UITheme.WHITE);
        panelFilter.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));

        JLabel lblCari = new JLabel("Cari Unit:");
        lblCari.setFont(UITheme.FONT_BOLD);
        lblCari.setForeground(UITheme.TEXT_PRIMARY);
        panelFilter.add(lblCari);

        txtCari = new JTextField(12);
        txtCari.setFont(UITheme.FONT_REGULAR);
        txtCari.setBorder(UITheme.createFieldBorder());
        txtCari.setPreferredSize(new Dimension(130, 30));
        // Dukungan pencarian real-time saat mengetik dan tombol enter
        txtCari.addActionListener(e -> aksiCari());
        txtCari.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { aksiCari(); }
            @Override
            public void removeUpdate(DocumentEvent e) { aksiCari(); }
            @Override
            public void changedUpdate(DocumentEvent e) { aksiCari(); }
        });
        panelFilter.add(txtCari);

        JButton btnCari = new JButton("Cari");
        btnCari.setFont(UITheme.FONT_SMALL_BOLD);
        btnCari.setBackground(UITheme.PRIMARY_GREEN);
        btnCari.setForeground(UITheme.WHITE);
        btnCari.setFocusPainted(false);
        btnCari.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        btnCari.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCari.addActionListener(e -> aksiCari());
        panelFilter.add(btnCari);

        panelFilter.add(Box.createHorizontalStrut(10));

        JLabel lblFilterJenis = new JLabel("Filter Jenis:");
        lblFilterJenis.setFont(UITheme.FONT_BOLD);
        lblFilterJenis.setForeground(UITheme.TEXT_PRIMARY);
        panelFilter.add(lblFilterJenis);

        cbFilterJenis = new JComboBox<>(new String[]{"SEMUA", "Mobil", "Motor", "Bus"});
        cbFilterJenis.setFont(UITheme.FONT_REGULAR);
        cbFilterJenis.setBackground(UITheme.WHITE);
        cbFilterJenis.setPreferredSize(new Dimension(95, 30));
        cbFilterJenis.addActionListener(e -> aksiFilter());
        panelFilter.add(cbFilterJenis);

        JLabel lblFilterStatus = new JLabel("Status:");
        lblFilterStatus.setFont(UITheme.FONT_BOLD);
        lblFilterStatus.setForeground(UITheme.TEXT_PRIMARY);
        panelFilter.add(lblFilterStatus);

        cbFilterStatus = new JComboBox<>(new String[]{"SEMUA", "Tersedia", "Disewa"});
        cbFilterStatus.setFont(UITheme.FONT_REGULAR);
        cbFilterStatus.setBackground(UITheme.WHITE);
        cbFilterStatus.setPreferredSize(new Dimension(95, 30));
        cbFilterStatus.addActionListener(e -> aksiFilter());
        panelFilter.add(cbFilterStatus);

        JButton btnResetFilter = new JButton("Reset");
        UITheme.styleNeutralButton(btnResetFilter);
        btnResetFilter.setPreferredSize(new Dimension(80, 30));
        btnResetFilter.addActionListener(e -> {
            txtCari.setText("");
            cbFilterJenis.setSelectedIndex(0);
            cbFilterStatus.setSelectedIndex(0);
            muatDataTabel(manager.getDaftarKendaraan());
        });
        panelFilter.add(btnResetFilter);

        panelKanan.add(panelFilter, BorderLayout.NORTH);

        // Tabel JTable dengan 9 Kolom Standar
        String[] columns = {"ID", "Jenis", "No. Plat", "Merk", "Model", "Tahun", "Tarif/Hari", "Status", "Spesifikasi Khusus"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelKendaraan = new JTable(tableModel);
        tabelKendaraan.setRowHeight(32);
        tabelKendaraan.setFont(UITheme.FONT_REGULAR);
        tabelKendaraan.setGridColor(UITheme.BORDER);
        tabelKendaraan.setShowGrid(true);
        tabelKendaraan.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelKendaraan.setSelectionBackground(UITheme.LIGHT_GREEN);
        tabelKendaraan.setSelectionForeground(UITheme.DARK_GREEN);

        // Header Tabel Custom Styling Dark Green
        JTableHeader tableHeader = tabelKendaraan.getTableHeader();
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

        // Cell Renderer Khusus: Zebra Striping, Alignment Sesuai Tipe, dan Badge Status
        tabelKendaraan.setDefaultRenderer(Object.class, new KendaraanCellRenderer());

        // Lebar Kolom Proporsional
        tabelKendaraan.getColumnModel().getColumn(0).setPreferredWidth(65);   // ID
        tabelKendaraan.getColumnModel().getColumn(1).setPreferredWidth(75);   // Jenis
        tabelKendaraan.getColumnModel().getColumn(2).setPreferredWidth(85);   // No Plat
        tabelKendaraan.getColumnModel().getColumn(3).setPreferredWidth(95);   // Merk
        tabelKendaraan.getColumnModel().getColumn(4).setPreferredWidth(100);  // Model
        tabelKendaraan.getColumnModel().getColumn(5).setPreferredWidth(55);   // Tahun
        tabelKendaraan.getColumnModel().getColumn(6).setPreferredWidth(105);  // Tarif/Hari
        tabelKendaraan.getColumnModel().getColumn(7).setPreferredWidth(95);   // Status
        tabelKendaraan.getColumnModel().getColumn(8).setPreferredWidth(200);  // Spesifikasi

        tabelKendaraan.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                pilihBarisTabel();
            }
        });

        JScrollPane scrollTable = new JScrollPane(tabelKendaraan);
        scrollTable.getViewport().setBackground(UITheme.WHITE);
        scrollTable.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));
        panelKanan.add(scrollTable, BorderLayout.CENTER);

        add(panelKanan, BorderLayout.CENTER);
    }

    private JLabel createFormLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_PRIMARY);
        return lbl;
    }

    private void initSubPanelMobil() {
        panelMobil = new JPanel(new GridLayout(3, 2, 6, 6));
        panelMobil.setBackground(UITheme.WHITE);
        panelMobil.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        panelMobil.add(createFormLabel("Tipe Bodi:"));
        cbTipeMobil = new JComboBox<>(new String[]{"MPV", "SUV", "Sedan", "Hatchback", "City Car"});
        cbTipeMobil.setFont(UITheme.FONT_REGULAR);
        cbTipeMobil.setBackground(UITheme.WHITE);
        panelMobil.add(cbTipeMobil);

        panelMobil.add(createFormLabel("Transmisi:"));
        JPanel pTrans = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pTrans.setOpaque(false);
        rbManual = new JRadioButton("Manual");
        rbManual.setFont(UITheme.FONT_REGULAR);
        rbManual.setBackground(UITheme.WHITE);
        rbManual.setFocusPainted(false);

        rbOtomatis = new JRadioButton("Otomatis", true);
        rbOtomatis.setFont(UITheme.FONT_REGULAR);
        rbOtomatis.setBackground(UITheme.WHITE);
        rbOtomatis.setFocusPainted(false);

        ButtonGroup bgTrans = new ButtonGroup();
        bgTrans.add(rbManual);
        bgTrans.add(rbOtomatis);
        pTrans.add(rbManual);
        pTrans.add(rbOtomatis);
        panelMobil.add(pTrans);

        panelMobil.add(createFormLabel("Kapasitas:"));
        spnKapasitasMobil = new JSpinner(new SpinnerNumberModel(7, 1, 12, 1));
        spnKapasitasMobil.setFont(UITheme.FONT_REGULAR);
        panelMobil.add(spnKapasitasMobil);
    }

    private void initSubPanelMotor() {
        panelMotor = new JPanel(new GridLayout(3, 2, 6, 6));
        panelMotor.setBackground(UITheme.WHITE);
        panelMotor.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        panelMotor.add(createFormLabel("Tipe Motor:"));
        cbTipeMotor = new JComboBox<>(new String[]{"Matic", "Manual/Bebek", "Sport"});
        cbTipeMotor.setFont(UITheme.FONT_REGULAR);
        cbTipeMotor.setBackground(UITheme.WHITE);
        panelMotor.add(cbTipeMotor);

        panelMotor.add(createFormLabel("Kapasitas CC:"));
        spnCcMotor = new JSpinner(new SpinnerNumberModel(150, 50, 1000, 25));
        spnCcMotor.setFont(UITheme.FONT_REGULAR);
        panelMotor.add(spnCcMotor);

        panelMotor.add(createFormLabel("Fasilitas:"));
        chkHelm = new JCheckBox("2 Helm & Jas Hujan", true);
        chkHelm.setFont(UITheme.FONT_REGULAR);
        chkHelm.setBackground(UITheme.WHITE);
        chkHelm.setFocusPainted(false);
        panelMotor.add(chkHelm);
    }

    private void initSubPanelBus() {
        panelBus = new JPanel(new GridLayout(3, 2, 6, 6));
        panelBus.setBackground(UITheme.WHITE);
        panelBus.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        panelBus.add(createFormLabel("Kapasitas Kursi:"));
        spnKursiBus = new JSpinner(new SpinnerNumberModel(30, 10, 65, 5));
        spnKursiBus.setFont(UITheme.FONT_REGULAR);
        panelBus.add(spnKursiBus);

        panelBus.add(createFormLabel("Toilet Dalam:"));
        chkToilet = new JCheckBox("Tersedia Toilet", false);
        chkToilet.setFont(UITheme.FONT_REGULAR);
        chkToilet.setBackground(UITheme.WHITE);
        chkToilet.setFocusPainted(false);
        panelBus.add(chkToilet);

        panelBus.add(createFormLabel("Regulasi Driver:"));
        chkWajibSupir = new JCheckBox("Wajib Kru Driver", true);
        chkWajibSupir.setFont(UITheme.FONT_REGULAR);
        chkWajibSupir.setBackground(UITheme.WHITE);
        chkWajibSupir.setFocusPainted(false);
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

    /**
     * Renderer kustom sel tabel armada kendaraan:
     * - Zebra striping: Genap putih, ganjil soft green (#F0FDF4).
     * - Perataan teks sesuai konteks data (tengah, kanan, kiri).
     * - Kolom status (indeks 7) berwujud pill/badge dengan palet UITheme.
     */
    private static class KendaraanCellRenderer extends DefaultTableCellRenderer {
        private final JPanel panelBadge = new JPanel(new GridBagLayout());
        private final JLabel lblBadge = new JLabel();

        public KendaraanCellRenderer() {
            panelBadge.setOpaque(true);
            lblBadge.setOpaque(true);
            lblBadge.setFont(UITheme.FONT_SMALL_BOLD);
            panelBadge.add(lblBadge);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            // Kolom Status (Column 7): Custom Badge
            if (column == 7 && value != null) {
                String status = value.toString();
                lblBadge.setText(" " + status + " ");

                if ("TERSEDIA".equalsIgnoreCase(status)) {
                    lblBadge.setBackground(UITheme.LIGHT_GREEN);
                    lblBadge.setForeground(UITheme.DARK_GREEN);
                    lblBadge.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(UITheme.GREEN_BORDER, 1),
                            BorderFactory.createEmptyBorder(2, 8, 2, 8)));
                } else { // DISEWA
                    lblBadge.setBackground(new Color(0xFE, 0xF3, 0xC7)); // Soft Amber
                    lblBadge.setForeground(new Color(0x92, 0x40, 0x0E)); // Dark Amber
                    lblBadge.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(0xFC, 0xD3, 0x4D), 1),
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
            if (column == 0 || column == 1 || column == 2 || column == 5) {
                c.setHorizontalAlignment(JLabel.CENTER);
            } else if (column == 6) {
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
