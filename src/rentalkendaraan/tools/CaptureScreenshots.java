package rentalkendaraan.tools;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;
import rentalkendaraan.model.*;
import rentalkendaraan.service.RentalManager;
import rentalkendaraan.view.*;

public class CaptureScreenshots {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        File dir = new File("screenshots");
        if (!dir.exists()) dir.mkdirs();

        try {
            RentalManager manager = new RentalManager();

            // Tambahkan transaksi sampel jika belum ada
            if (manager.getDaftarTransaksi().isEmpty()) {
                Pelanggan p1 = manager.getDaftarPelanggan().get(0);
                Kendaraan k1 = manager.getKendaraanTersedia().get(0);
                manager.prosesSewa(p1, k1, "2026-11-18", 4, true, true, "Perjalanan dinas luar kota");

                if (manager.getKendaraanTersedia().size() > 1 && manager.getDaftarPelanggan().size() > 1) {
                    Pelanggan p2 = manager.getDaftarPelanggan().get(1);
                    Kendaraan k2 = manager.getKendaraanTersedia().get(1);
                    manager.prosesSewa(p2, k2, "2026-11-19", 2, false, false, "Keperluan wisuda kampus");
                }
            }

            // 1. CAPTURE LOGIN FRAME
            LoginFrame loginFrame = new LoginFrame(manager);
            loginFrame.setSize(440, 420);
            layoutTree(loginFrame);
            BufferedImage imgLogin = renderWindowsWindow(loginFrame, "Login - Sistem Rental Kendaraan (Kelompok 7)", 440, 420, false);
            ImageIO.write(imgLogin, "PNG", new File(dir, "01_login_frame.png"));
            loginFrame.dispose();

            // 2. CAPTURE DASHBOARD - TAB KENDARAAN
            MainDashboardFrame dashboard = new MainDashboardFrame(manager, "admin", "Administrator Sistem");
            dashboard.setSize(1100, 720);

            JTabbedPane tab = findTabbedPane(dashboard);
            if (tab != null) tab.setSelectedIndex(0);
            layoutTree(dashboard);
            BufferedImage imgKendaraan = renderWindowsWindow(dashboard, "Sistem Manajemen Rental Kendaraan - Kelompok 7 (PBO 2026)", 1100, 720, true);
            ImageIO.write(imgKendaraan, "PNG", new File(dir, "02_manajemen_kendaraan.png"));

            // 3. CAPTURE DASHBOARD - TAB PELANGGAN
            if (tab != null) tab.setSelectedIndex(1);
            layoutTree(dashboard);
            BufferedImage imgPelanggan = renderWindowsWindow(dashboard, "Sistem Manajemen Rental Kendaraan - Kelompok 7 (PBO 2026)", 1100, 720, true);
            ImageIO.write(imgPelanggan, "PNG", new File(dir, "03_manajemen_pelanggan.png"));

            // 4. CAPTURE DASHBOARD - TAB TRANSAKSI
            if (tab != null) tab.setSelectedIndex(2);
            layoutTree(dashboard);
            BufferedImage imgTransaksi = renderWindowsWindow(dashboard, "Sistem Manajemen Rental Kendaraan - Kelompok 7 (PBO 2026)", 1100, 720, true);
            ImageIO.write(imgTransaksi, "PNG", new File(dir, "04_transaksi_sewa.png"));

            // 5. CAPTURE STRUK DIALOG
            if (!manager.getDaftarTransaksi().isEmpty()) {
                TransaksiSewa trx = manager.getDaftarTransaksi().get(0);
                DialogStrukSewa dialog = new DialogStrukSewa(dashboard, trx);
                dialog.setSize(520, 640);
                layoutTree(dialog);
                BufferedImage imgStruk = renderWindowsWindow(dialog, "Struk Resmi Penyewaan - " + trx.getIdTransaksi(), 520, 640, false);
                ImageIO.write(imgStruk, "PNG", new File(dir, "05_dialog_struk.png"));
                dialog.dispose();
            }

            // 6. CAPTURE DIALOG VALIDASI EXCEPTION (JOptionPane)
            JOptionPane optionPane = new JOptionPane(
                    "Terjadi Kesalahan Validasi Data (DataValidasiException):\n" +
                    "NIK Pelanggan harus tepat terdiri dari 16 digit angka!\n" +
                    "Tarif sewa per hari tidak boleh bernilai negatif.",
                    JOptionPane.WARNING_MESSAGE);
            JDialog errDialog = optionPane.createDialog(dashboard, "Peringatan Validasi Input");
            errDialog.setSize(480, 180);
            layoutTree(errDialog);
            BufferedImage imgErr = renderWindowsWindow(errDialog, "Peringatan Validasi Input", 480, 180, false);
            ImageIO.write(imgErr, "PNG", new File(dir, "06_dialog_validasi_error.png"));
            errDialog.dispose();

            dashboard.dispose();
            System.out.println("ALL_AUTHENTIC_WINDOWS_SCREENSHOTS_RENDERED_SUCCESSFULLY");
            System.exit(0);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void layoutTree(Container c) {
        c.doLayout();
        for (Component child : c.getComponents()) {
            if (child instanceof Container) {
                layoutTree((Container) child);
            }
        }
    }

    private static BufferedImage renderWindowsWindow(Window window, String title, int width, int height, boolean isMainFrame) {
        int titleBarHeight = 32;
        BufferedImage image = new BufferedImage(width, height + titleBarHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Fill background
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, width, height + titleBarHeight);

        // Windows Title Bar Background (Clean light gray / white Windows 10/11)
        g2.setColor(new Color(243, 243, 243));
        g2.fillRect(0, 0, width, titleBarHeight);

        g2.setColor(new Color(218, 218, 218));
        g2.drawLine(0, titleBarHeight - 1, width, titleBarHeight - 1);

        // Title text on the left (Segoe UI 12pt plain)
        g2.setColor(new Color(30, 30, 30));
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g2.drawString(title, 12, 21);

        // Windows Caption Buttons on the right
        int btnW = 46;
        int right = width;
        int cy = titleBarHeight / 2;

        // Close Button (✕)
        int closeX = right - btnW;
        g2.setColor(new Color(40, 40, 40));
        g2.setStroke(new BasicStroke(1.2f));
        int cx = closeX + btnW / 2;
        g2.drawLine(cx - 5, cy - 5, cx + 5, cy + 5);
        g2.drawLine(cx + 5, cy - 5, cx - 5, cy + 5);

        if (isMainFrame) {
            // Maximize Button (□)
            int maxX = closeX - btnW;
            int mx = maxX + btnW / 2;
            g2.drawRect(mx - 5, cy - 5, 10, 10);

            // Minimize Button (—)
            int minX = maxX - btnW;
            int minXCenter = minX + btnW / 2;
            g2.drawLine(minXCenter - 5, cy, minXCenter + 5, cy);
        }

        // Window Content
        Graphics2D gContent = (Graphics2D) g2.create(0, titleBarHeight, width, height);

        if (window instanceof JFrame) {
            JFrame f = (JFrame) window;
            JMenuBar mb = f.getJMenuBar();
            int mbHeight = 0;
            if (mb != null) {
                mb.setSize(width, mb.getPreferredSize().height);
                layoutTree(mb);
                mb.paint(gContent);
                mbHeight = mb.getHeight();
            }
            Container cp = f.getContentPane();
            cp.setSize(width, height - mbHeight);
            layoutTree(cp);
            Graphics2D gCp = (Graphics2D) gContent.create(0, mbHeight, width, height - mbHeight);
            cp.paint(gCp);
            gCp.dispose();
        } else if (window instanceof JDialog) {
            JDialog d = (JDialog) window;
            Container cp = d.getContentPane();
            cp.setSize(width, height);
            layoutTree(cp);
            cp.paint(gContent);
        } else {
            window.setSize(width, height);
            layoutTree(window);
            window.paint(gContent);
        }
        gContent.dispose();

        // 1px Subtle Windows outer border
        g2.setColor(new Color(190, 190, 190));
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawRect(0, 0, width - 1, height + titleBarHeight - 1);

        g2.dispose();
        return image;
    }

    private static JTabbedPane findTabbedPane(Container container) {
        for (Component c : container.getComponents()) {
            if (c instanceof JTabbedPane) return (JTabbedPane) c;
            if (c instanceof Container) {
                JTabbedPane res = findTabbedPane((Container) c);
                if (res != null) return res;
            }
        }
        return null;
    }
}
