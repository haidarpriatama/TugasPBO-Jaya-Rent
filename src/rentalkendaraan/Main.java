package rentalkendaraan;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import rentalkendaraan.service.RentalManager;
import rentalkendaraan.view.LoginFrame;

/**
 * Titik masuk utama (Main Entry Point) aplikasi Rental Kendaraan.
 * Menginisialisasi Look and Feel modern, controller data, dan membuka jendela Login.
 */
public class Main {
    public static void main(String[] args) {
        // Mengatur Look and Feel ke System native Look and Feel agar tampilan serasi dengan OS
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Jika gagal, gunakan default Swing Look and Feel
        }

        SwingUtilities.invokeLater(() -> {
            RentalManager manager = new RentalManager();
            LoginFrame loginFrame = new LoginFrame(manager);
            loginFrame.setVisible(true);
        });
    }
}
