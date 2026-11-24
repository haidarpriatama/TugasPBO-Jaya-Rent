package rentalkendaraan.view;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import rentalkendaraan.exception.RentalException;
import rentalkendaraan.model.Pelanggan;
import rentalkendaraan.service.RentalManager;

/**
 * Panel Manajemen Data Pelanggan (CRUD Pelanggan).
 * Menggunakan komponen input modern berdesain Green & White (UITheme).
 */
public class PanelPelanggan extends JPanel {
    private final RentalManager manager;
    private final Runnable onDataChangedCallback;

    // Form Components
    private JTextField txtId;
    private JTextField txtNama;
    private JTextField txtNik;
    private JTextField txtTelepon;
    private JRadioButton rbLaki;
    private JRadioButton rbPerempuan;
    private JTextArea txtAlamat;

    // Buttons
    private JButton btnTambah;
    private JButton btnUpdate;
    private JButton btnHapus;
    private JButton btnReset;

    // Table & Search
    private JTextField txtCari;
    private JTable tabelPelanggan;
    private DefaultTableModel tableModel;

    public PanelPelanggan(RentalManager manager, Runnable onDataChangedCallback) {
        this.manager = manager;
        this.onDataChangedCallback = onDataChangedCallback;
        setBackground(UITheme.WHITE);
        setLayout(new BorderLayout(14, 14));
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        initComponents();
        muatDataTabel(manager.getDaftarPelanggan());
        resetForm();
    }

    private void initComponents() {
        // --- BAGIAN KIRI: CARD FORM INPUT PELANGGAN ---
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

        JLabel lblJudulForm = new JLabel("Form Data Pelanggan");
        lblJudulForm.setFont(UITheme.FONT_TITLE);
        lblJudulForm.setForeground(UITheme.DARK_GREEN);
        panelHeaderForm.add(lblJudulForm);

        JLabel lblSubJudul = new JLabel("Kelola identitas dan kontak pelanggan rental");
        lblSubJudul.setFont(UITheme.FONT_SMALL);
        lblSubJudul.setForeground(UITheme.TEXT_SECONDARY);
        panelHeaderForm.add(lblSubJudul);

        panelCardForm.add(panelHeaderForm, BorderLayout.NORTH);

        // Body Form (GridBagLayout)
        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setBackground(UITheme.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 4, 5, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // ID Pelanggan
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        panelForm.add(createFormLabel("ID Pelanggan:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        txtId = new JTextField();
        txtId.setEditable(false);
        txtId.setFont(UITheme.FONT_REGULAR);
        txtId.setBackground(UITheme.SOFT_GREEN);
        txtId.setForeground(UITheme.DARK_GREEN);
        txtId.setBorder(UITheme.createGreenFieldBorder());
        txtId.setPreferredSize(new Dimension(100, 30));
        panelForm.add(txtId, gbc);

        // Nama Lengkap
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panelForm.add(createFormLabel("Nama Lengkap:"), gbc);
        gbc.gridx = 1;
        txtNama = new JTextField();
        txtNama.setFont(UITheme.FONT_REGULAR);
        txtNama.setBorder(UITheme.createFieldBorder());
        txtNama.setPreferredSize(new Dimension(100, 30));
        panelForm.add(txtNama, gbc);

        // NIK KTP (16 Digit)
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panelForm.add(createFormLabel("Nomor KTP/NIK:"), gbc);
        gbc.gridx = 1;
        txtNik = new JTextField();
        txtNik.setFont(UITheme.FONT_REGULAR);
        txtNik.setBorder(UITheme.createFieldBorder());
        txtNik.setPreferredSize(new Dimension(100, 30));
        panelForm.add(txtNik, gbc);

        // Nomor Telepon / WA
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panelForm.add(createFormLabel("No. Telepon/WA:"), gbc);
        gbc.gridx = 1;
        txtTelepon = new JTextField();
        txtTelepon.setFont(UITheme.FONT_REGULAR);
        txtTelepon.setBorder(UITheme.createFieldBorder());
        txtTelepon.setPreferredSize(new Dimension(100, 30));
        panelForm.add(txtTelepon, gbc);

        // Jenis Kelamin (Radio Buttons)
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panelForm.add(createFormLabel("Jenis Kelamin:"), gbc);
        gbc.gridx = 1;
        JPanel panelGender = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panelGender.setOpaque(false);

        rbLaki = new JRadioButton("Laki-laki", true);
        rbLaki.setFont(UITheme.FONT_REGULAR);
        rbLaki.setForeground(UITheme.TEXT_PRIMARY);
        rbLaki.setBackground(UITheme.WHITE);
        rbLaki.setFocusPainted(false);

        rbPerempuan = new JRadioButton("Perempuan");
        rbPerempuan.setFont(UITheme.FONT_REGULAR);
        rbPerempuan.setForeground(UITheme.TEXT_PRIMARY);
        rbPerempuan.setBackground(UITheme.WHITE);
        rbPerempuan.setFocusPainted(false);

        ButtonGroup bgGender = new ButtonGroup();
        bgGender.add(rbLaki);
        bgGender.add(rbPerempuan);
        panelGender.add(rbLaki);
        panelGender.add(rbPerempuan);
        panelForm.add(panelGender, gbc);

        // Alamat Domisili (JTextArea dengan JScrollPane rapi)
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panelForm.add(createFormLabel("Alamat Domisili:"), gbc);
        gbc.gridx = 1;
        txtAlamat = new JTextArea(4, 18);
        txtAlamat.setFont(UITheme.FONT_REGULAR);
        txtAlamat.setForeground(UITheme.TEXT_PRIMARY);
        txtAlamat.setLineWrap(true);
        txtAlamat.setWrapStyleWord(true);
        txtAlamat.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        JScrollPane scrollAlamat = new JScrollPane(txtAlamat);
        scrollAlamat.setBorder(UITheme.createFieldBorder());
        scrollAlamat.getViewport().setBackground(UITheme.WHITE);
        scrollAlamat.setPreferredSize(new Dimension(100, 80));
        panelForm.add(scrollAlamat, gbc);

        // Tombol Operasi CRUD (2x2 Grid)
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel panelBtn = new JPanel(new GridLayout(2, 2, 8, 8));
        panelBtn.setOpaque(false);
        panelBtn.setPreferredSize(new Dimension(340, 80));
        panelBtn.setBorder(BorderFactory.createEmptyBorder(12, 0, 4, 0));

        // 1. Tambah Pelanggan (Primary Green Solid)
        btnTambah = new JButton("Tambah Pelanggan");
        UITheme.stylePrimaryButton(btnTambah);
        btnTambah.addActionListener(e -> aksiTambah());

        // 2. Simpan Perubahan (Outline Green)
        btnUpdate = new JButton("Simpan Perubahan");
        UITheme.styleOutlineSuccessButton(btnUpdate);
        btnUpdate.setEnabled(false);
        btnUpdate.addActionListener(e -> aksiUpdate());

        // 3. Hapus Pelanggan (Danger Red)
        btnHapus = new JButton("Hapus Pelanggan");
        UITheme.styleDangerButton(btnHapus);
        btnHapus.setEnabled(false);
        btnHapus.addActionListener(e -> aksiHapus());

        // 4. Bersihkan Form (Secondary Netral)
        btnReset = new JButton("Bersihkan Form");
        UITheme.styleNeutralButton(btnReset);
        btnReset.addActionListener(e -> resetForm());

        panelBtn.add(btnTambah);
        panelBtn.add(btnUpdate);
        panelBtn.add(btnHapus);
        panelBtn.add(btnReset);
        panelForm.add(panelBtn, gbc);

        panelCardForm.add(panelForm, BorderLayout.CENTER);

        JScrollPane scrollForm = new JScrollPane(panelCardForm);
        scrollForm.setBorder(null);
        scrollForm.getViewport().setBackground(UITheme.WHITE);
        scrollForm.getVerticalScrollBar().setUnitIncrement(14);
        panelKiri.add(scrollForm, BorderLayout.CENTER);

        add(panelKiri, BorderLayout.WEST);

        // --- BAGIAN KANAN: TABEL DATA PELANGGAN ---
        JPanel panelKanan = new JPanel(new BorderLayout(10, 10));
        panelKanan.setOpaque(false);

        // Filter / Search Bar
        JPanel panelSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        panelSearch.setBackground(UITheme.WHITE);
        panelSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));

        JLabel lblCari = new JLabel("Pencarian Pelanggan:");
        lblCari.setFont(UITheme.FONT_BOLD);
        lblCari.setForeground(UITheme.TEXT_PRIMARY);
        panelSearch.add(lblCari);

        txtCari = new JTextField(16);
        txtCari.setFont(UITheme.FONT_REGULAR);
        txtCari.setBorder(UITheme.createFieldBorder());
        txtCari.setPreferredSize(new Dimension(160, 30));
        // Pencarian real-time via DocumentListener dan Enter key
        txtCari.addActionListener(e -> aksiCari());
        txtCari.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { aksiCari(); }
            @Override
            public void removeUpdate(DocumentEvent e) { aksiCari(); }
            @Override
            public void changedUpdate(DocumentEvent e) { aksiCari(); }
        });
        panelSearch.add(txtCari);

        JButton btnCari = new JButton("Cari");
        UITheme.stylePrimaryButton(btnCari);
        btnCari.setPreferredSize(new Dimension(80, 30));
        btnCari.addActionListener(e -> aksiCari());
        panelSearch.add(btnCari);

        JButton btnResetCari = new JButton("Reset");
        UITheme.styleNeutralButton(btnResetCari);
        btnResetCari.setPreferredSize(new Dimension(80, 30));
        btnResetCari.addActionListener(e -> {
            if (txtCari.getText().isEmpty()) {
                muatDataTabel(manager.getDaftarPelanggan());
            } else {
                txtCari.setText("");
            }
        });
        panelSearch.add(btnResetCari);

        panelKanan.add(panelSearch, BorderLayout.NORTH);

        // JTable Pelanggan dengan 6 Kolom
        String[] cols = {"ID Pelanggan", "Nama Lengkap", "NIK KTP", "No. Telepon", "Jenis Kelamin", "Alamat Domisili"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelPelanggan = new JTable(tableModel);
        tabelPelanggan.setRowHeight(32);
        tabelPelanggan.setFont(UITheme.FONT_REGULAR);
        tabelPelanggan.setGridColor(UITheme.BORDER);
        tabelPelanggan.setShowGrid(true);
        tabelPelanggan.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelPelanggan.setSelectionBackground(UITheme.LIGHT_GREEN);
        tabelPelanggan.setSelectionForeground(UITheme.DARK_GREEN);

        // Header Custom Styling Dark Green
        JTableHeader tableHeader = tabelPelanggan.getTableHeader();
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

        // Cell Renderer Khusus: Zebra Striping & Alignment
        tabelPelanggan.setDefaultRenderer(Object.class, new PelangganCellRenderer());

        // Lebar Kolom Proporsional
        tabelPelanggan.getColumnModel().getColumn(0).setPreferredWidth(90);   // ID Pelanggan
        tabelPelanggan.getColumnModel().getColumn(1).setPreferredWidth(150);  // Nama Lengkap
        tabelPelanggan.getColumnModel().getColumn(2).setPreferredWidth(130);  // NIK KTP
        tabelPelanggan.getColumnModel().getColumn(3).setPreferredWidth(110);  // No. Telepon
        tabelPelanggan.getColumnModel().getColumn(4).setPreferredWidth(95);   // Jenis Kelamin
        tabelPelanggan.getColumnModel().getColumn(5).setPreferredWidth(220);  // Alamat Domisili

        tabelPelanggan.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                pilihBarisTabel();
            }
        });

        JScrollPane scrollTable = new JScrollPane(tabelPelanggan);
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

    private void muatDataTabel(List<Pelanggan> list) {
        tableModel.setRowCount(0);
        for (Pelanggan p : list) {
            tableModel.addRow(new Object[]{
                    p.getIdPelanggan(),
                    p.getNama(),
                    p.getNik(),
                    p.getNomorTelepon(),
                    p.getJenisKelamin(),
                    p.getAlamat()
            });
        }
    }

    private void pilihBarisTabel() {
        int row = tabelPelanggan.getSelectedRow();
        if (row < 0) return;

        String id = (String) tableModel.getValueAt(row, 0);
        Pelanggan p = manager.cariPelangganById(id);
        if (p == null) return;

        txtId.setText(p.getIdPelanggan());
        txtNama.setText(p.getNama());
        txtNik.setText(p.getNik());
        txtTelepon.setText(p.getNomorTelepon());
        if ("Perempuan".equalsIgnoreCase(p.getJenisKelamin())) {
            rbPerempuan.setSelected(true);
        } else {
            rbLaki.setSelected(true);
        }
        txtAlamat.setText(p.getAlamat());

        btnTambah.setEnabled(false);
        btnUpdate.setEnabled(true);
        btnHapus.setEnabled(true);
    }

    private void resetForm() {
        txtId.setText(manager.generateNextIdPelanggan());
        txtNama.setText("");
        txtNik.setText("");
        txtTelepon.setText("");
        rbLaki.setSelected(true);
        txtAlamat.setText("");

        tabelPelanggan.clearSelection();
        btnTambah.setEnabled(true);
        btnUpdate.setEnabled(false);
        btnHapus.setEnabled(false);
    }

    private Pelanggan buatObjekDariForm(String id) throws RentalException {
        String nama = txtNama.getText().trim();
        String nik = txtNik.getText().trim();
        String telp = txtTelepon.getText().trim();
        String gender = rbLaki.isSelected() ? "Laki-laki" : "Perempuan";
        String alamat = txtAlamat.getText().trim();

        return new Pelanggan(id, nama, nik, telp, alamat, gender);
    }

    private void aksiTambah() {
        try {
            Pelanggan p = buatObjekDariForm(manager.generateNextIdPelanggan());
            manager.tambahPelanggan(p);
            JOptionPane.showMessageDialog(this,
                    "Pelanggan [" + p.getNama() + "] berhasil didaftarkan!",
                    "Sukses", JOptionPane.INFORMATION_MESSAGE);
            muatDataTabel(manager.getDaftarPelanggan());
            resetForm();
            if (onDataChangedCallback != null) onDataChangedCallback.run();
        } catch (RentalException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void aksiUpdate() {
        try {
            String id = txtId.getText();
            Pelanggan p = buatObjekDariForm(id);
            manager.updatePelanggan(p);
            JOptionPane.showMessageDialog(this,
                    "Data pelanggan [" + p.getNama() + "] berhasil diperbarui!",
                    "Sukses", JOptionPane.INFORMATION_MESSAGE);
            muatDataTabel(manager.getDaftarPelanggan());
            resetForm();
            if (onDataChangedCallback != null) onDataChangedCallback.run();
        } catch (RentalException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void aksiHapus() {
        String id = txtId.getText();
        Pelanggan p = manager.cariPelangganById(id);
        if (p == null) return;

        int konfirmasi = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin menghapus data pelanggan:\n" +
                        "[" + p.getIdPelanggan() + "] " + p.getNama() + " (" + p.getNik() + ")?",
                "Konfirmasi Hapus Data",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (konfirmasi == JOptionPane.YES_OPTION) {
            try {
                manager.hapusPelanggan(id);
                JOptionPane.showMessageDialog(this, "Pelanggan berhasil dihapus.", "Informasi", JOptionPane.INFORMATION_MESSAGE);
                muatDataTabel(manager.getDaftarPelanggan());
                resetForm();
                if (onDataChangedCallback != null) onDataChangedCallback.run();
            } catch (RentalException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Gagal Menghapus", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void aksiCari() {
        String kw = txtCari.getText();
        List<Pelanggan> hasil = manager.cariPelanggan(kw);
        muatDataTabel(hasil);
    }

    public void segarkanData() {
        muatDataTabel(manager.getDaftarPelanggan());
    }

    /**
     * Renderer kustom sel tabel pelanggan:
     * - Zebra striping: Genap putih, ganjil soft green (#F0FDF4).
     * - Perataan teks sesuai konteks data (tengah untuk ID, NIK, Telp, Gender; kiri untuk Nama & Alamat).
     * - Seleksi bernuansa hijau kontras (LIGHT_GREEN & DARK_GREEN).
     */
    private static class PelangganCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            c.setFont(UITheme.FONT_REGULAR);

            // Perataan kolom: ID, NIK, Telepon, Gender rata tengah. Nama & Alamat rata kiri.
            if (column == 0 || column == 2 || column == 3 || column == 4) {
                c.setHorizontalAlignment(JLabel.CENTER);
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
