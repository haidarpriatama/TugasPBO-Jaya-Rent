package rentalkendaraan.view;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import rentalkendaraan.exception.RentalException;
import rentalkendaraan.model.Pelanggan;
import rentalkendaraan.service.RentalManager;

/**
 * Panel Manajemen Data Pelanggan (CRUD Pelanggan).
 * Menggunakan komponen JTextField, JRadioButton, JTextArea, JTable, dan validasi data.
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
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        initComponents();
        muatDataTabel(manager.getDaftarPelanggan());
        resetForm();
    }

    private void initComponents() {
        // --- BAGIAN KIRI: FORMULIR INPUT ---
        JPanel panelKiri = new JPanel(new BorderLayout(8, 8));
        panelKiri.setPreferredSize(new Dimension(360, 600));

        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(148, 163, 184)),
                "Formulir Data Pelanggan",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13), new Color(30, 41, 59)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // ID Pelanggan
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        panelForm.add(new JLabel("ID Pelanggan:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtId = new JTextField();
        txtId.setEditable(false);
        txtId.setBackground(new Color(241, 245, 249));
        panelForm.add(txtId, gbc);

        // Nama Lengkap
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panelForm.add(new JLabel("Nama Lengkap:"), gbc);
        gbc.gridx = 1;
        txtNama = new JTextField();
        panelForm.add(txtNama, gbc);

        // NIK KTP (16 Digit)
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panelForm.add(new JLabel("Nomor KTP/NIK:"), gbc);
        gbc.gridx = 1;
        txtNik = new JTextField();
        panelForm.add(txtNik, gbc);

        // Nomor Telepon / WA
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panelForm.add(new JLabel("No. Telepon/WA:"), gbc);
        gbc.gridx = 1;
        txtTelepon = new JTextField();
        panelForm.add(txtTelepon, gbc);

        // Jenis Kelamin (Radio Buttons)
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panelForm.add(new JLabel("Jenis Kelamin:"), gbc);
        gbc.gridx = 1;
        JPanel panelGender = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        rbLaki = new JRadioButton("Laki-laki", true);
        rbPerempuan = new JRadioButton("Perempuan");
        ButtonGroup bgGender = new ButtonGroup();
        bgGender.add(rbLaki);
        bgGender.add(rbPerempuan);
        panelGender.add(rbLaki);
        panelGender.add(rbPerempuan);
        panelForm.add(panelGender, gbc);

        // Alamat Domisili (JTextArea)
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panelForm.add(new JLabel("Alamat Lengkap:"), gbc);
        gbc.gridx = 1;
        txtAlamat = new JTextArea(4, 20);
        txtAlamat.setLineWrap(true);
        txtAlamat.setWrapStyleWord(true);
        JScrollPane scrollAlamat = new JScrollPane(txtAlamat);
        panelForm.add(scrollAlamat, gbc);

        // Tombol Aksi
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel panelBtn = new JPanel(new GridLayout(2, 2, 6, 6));
        panelBtn.setBorder(BorderFactory.createEmptyBorder(12, 0, 4, 0));

        btnTambah = new JButton("Tambah Pelanggan");
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

        btnHapus = new JButton("Hapus Pelanggan");
        btnHapus.setBackground(new Color(220, 38, 38));
        btnHapus.setForeground(Color.WHITE);
        btnHapus.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnHapus.setEnabled(false);
        btnHapus.addActionListener(e -> aksiHapus());

        btnReset = new JButton("Bersihkan Form");
        btnReset.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnReset.addActionListener(e -> resetForm());

        panelBtn.add(btnTambah);
        panelBtn.add(btnUpdate);
        panelBtn.add(btnHapus);
        panelBtn.add(btnReset);
        panelForm.add(panelBtn, gbc);

        panelKiri.add(panelForm, BorderLayout.NORTH);
        add(panelKiri, BorderLayout.WEST);

        // --- BAGIAN KANAN: TABEL DATA PELANGGAN ---
        JPanel panelKanan = new JPanel(new BorderLayout(8, 8));

        JPanel panelSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        panelSearch.setBackground(new Color(248, 250, 252));
        panelSearch.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        panelSearch.add(new JLabel("Pencarian Pelanggan:"));
        txtCari = new JTextField(16);
        panelSearch.add(txtCari);

        JButton btnCari = new JButton("Cari");
        btnCari.addActionListener(e -> aksiCari());
        panelSearch.add(btnCari);

        JButton btnResetCari = new JButton("Reset");
        btnResetCari.addActionListener(e -> {
            txtCari.setText("");
            muatDataTabel(manager.getDaftarPelanggan());
        });
        panelSearch.add(btnResetCari);

        panelKanan.add(panelSearch, BorderLayout.NORTH);

        String[] cols = {"ID Pelanggan", "Nama Lengkap", "NIK KTP", "No. Telepon", "Jenis Kelamin", "Alamat Domisili"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelPelanggan = new JTable(tableModel);
        tabelPelanggan.setRowHeight(26);
        tabelPelanggan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabelPelanggan.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabelPelanggan.getTableHeader().setBackground(new Color(241, 245, 249));
        tabelPelanggan.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tabelPelanggan.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                pilihBarisTabel();
            }
        });

        JScrollPane scrollTable = new JScrollPane(tabelPelanggan);
        panelKanan.add(scrollTable, BorderLayout.CENTER);

        add(panelKanan, BorderLayout.CENTER);
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
}
