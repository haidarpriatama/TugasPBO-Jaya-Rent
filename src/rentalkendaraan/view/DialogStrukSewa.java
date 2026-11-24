package rentalkendaraan.view;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.*;
import rentalkendaraan.model.TransaksiSewa;

/**
 * Dialog Modal untuk menampilkan dan mencetak bukti nota/struk transaksi sewa kendaraan
 * dengan desain modern Green & White (UITheme).
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
        setSize(540, 650);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(UITheme.WHITE);

        // Header Panel (Dark Green)
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBackground(UITheme.DARK_GREEN);
        panelHeader.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JLabel lblTitle = new JLabel("BUKTI PEMBAYARAN & STRUK SEWA");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.WHITE);
        panelHeader.add(lblTitle, BorderLayout.WEST);

        JLabel lblSubtitle = new JLabel(transaksi.getIdTransaksi());
        lblSubtitle.setFont(UITheme.FONT_SUBTITLE);
        lblSubtitle.setForeground(UITheme.LIGHT_GREEN);
        panelHeader.add(lblSubtitle, BorderLayout.EAST);

        add(panelHeader, BorderLayout.NORTH);

        // Center: Struk Text Area
        txtStruk = new JTextArea();
        txtStruk.setFont(new Font("Courier New", Font.PLAIN, 12));
        txtStruk.setForeground(UITheme.TEXT_PRIMARY);
        txtStruk.setEditable(false);
        txtStruk.setText(transaksi.generateStruk());
        txtStruk.setBackground(UITheme.WHITE);
        txtStruk.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        txtStruk.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(txtStruk);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 14, 0, 14),
                BorderFactory.createLineBorder(UITheme.BORDER, 1)));
        scrollPane.getViewport().setBackground(UITheme.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom: Button Actions (Soft Green)
        JPanel panelButton = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        panelButton.setBackground(UITheme.SOFT_GREEN);
        panelButton.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.GREEN_BORDER));

        JButton btnTutup = new JButton("Tutup");
        btnTutup.setFont(UITheme.FONT_SMALL);
        btnTutup.setBackground(UITheme.WHITE);
        btnTutup.setForeground(UITheme.TEXT_PRIMARY);
        btnTutup.setFocusPainted(false);
        btnTutup.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)));
        btnTutup.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTutup.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnTutup.setBackground(UITheme.WHITE);
                btnTutup.setForeground(UITheme.DARK_GREEN);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btnTutup.setBackground(UITheme.WHITE);
                btnTutup.setForeground(UITheme.TEXT_PRIMARY);
            }
        });
        btnTutup.addActionListener(e -> dispose());

        JButton btnSimpanFile = new JButton("Simpan ke File (.txt)");
        btnSimpanFile.setFont(UITheme.FONT_SMALL_BOLD);
        btnSimpanFile.setBackground(UITheme.PRIMARY_GREEN);
        btnSimpanFile.setForeground(UITheme.WHITE);
        btnSimpanFile.setFocusPainted(false);
        btnSimpanFile.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btnSimpanFile.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSimpanFile.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnSimpanFile.setBackground(UITheme.SECONDARY_GREEN);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btnSimpanFile.setBackground(UITheme.PRIMARY_GREEN);
            }
        });
        btnSimpanFile.addActionListener(e -> simpanKeFile());

        panelButton.add(btnTutup);
        panelButton.add(btnSimpanFile);
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
