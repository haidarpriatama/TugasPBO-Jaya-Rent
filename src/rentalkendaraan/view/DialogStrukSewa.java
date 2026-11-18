package rentalkendaraan.view;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.*;
import rentalkendaraan.model.TransaksiSewa;

/**
 * Dialog Modal untuk menampilkan dan mencetak bukti nota/struk transaksi sewa kendaraan.
 */
public class DialogStrukSewa extends JDialog {
    private final TransaksiSewa transaksi;
    private JTextArea txtStruk;

    public DialogStrukSewa(Frame owner, TransaksiSewa transaksi) {
        super(owner, "Struk Resmi Penyewaan - " + transaksi.getIdTransaksi(), true);
        this.transaksi = transaksi;
        initComponents();
    }

    private void initComponents() {
        setSize(520, 640);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(10, 10));

        // Header Panel
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBackground(new Color(30, 41, 59));
        panelHeader.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel lblTitle = new JLabel("BUKTI PEMBAYARAN & STRUK SEWA");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(Color.WHITE);
        panelHeader.add(lblTitle, BorderLayout.WEST);

        JLabel lblSubtitle = new JLabel(transaksi.getIdTransaksi());
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitle.setForeground(new Color(226, 232, 240));
        panelHeader.add(lblSubtitle, BorderLayout.EAST);

        add(panelHeader, BorderLayout.NORTH);

        // Center: Struk Text Area
        txtStruk = new JTextArea();
        txtStruk.setFont(new Font("Courier New", Font.PLAIN, 12));
        txtStruk.setEditable(false);
        txtStruk.setText(transaksi.generateStruk());
        txtStruk.setBackground(new Color(248, 250, 252));
        txtStruk.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        txtStruk.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(txtStruk);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
        add(scrollPane, BorderLayout.CENTER);

        // Bottom: Button Actions
        JPanel panelButton = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        panelButton.setBackground(new Color(241, 245, 249));

        JButton btnSimpanFile = new JButton("Simpan ke File (.txt)");
        btnSimpanFile.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSimpanFile.setBackground(new Color(37, 99, 235));
        btnSimpanFile.setForeground(Color.WHITE);
        btnSimpanFile.setFocusPainted(false);
        btnSimpanFile.addActionListener(e -> simpanKeFile());

        JButton btnTutup = new JButton("Tutup");
        btnTutup.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnTutup.addActionListener(e -> dispose());

        panelButton.add(btnSimpanFile);
        panelButton.add(btnTutup);
        add(panelButton, BorderLayout.SOUTH);
    }

    private void simpanKeFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("Struk_" + transaksi.getIdTransaksi() + ".txt"));
        int ret = chooser.showSaveDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File target = chooser.getSelectedFile();
            try (FileWriter fw = new FileWriter(target)) {
                fw.write(txtStruk.getText());
                JOptionPane.showMessageDialog(this,
                        "Struk berhasil disimpan di:\n" + target.getAbsolutePath(),
                        "Sukses", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Gagal menyimpan berkas: " + ex.getMessage(),
                        "Kesalahan I/O", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
