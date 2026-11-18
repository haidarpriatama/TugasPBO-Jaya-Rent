package rentalkendaraan.exception;

/**
 * Exception yang dilemparkan ketika mencoba menyewa kendaraan yang sedang tidak tersedia/disewa.
 */
public class KendaraanTidakTersediaException extends RentalException {
    public KendaraanTidakTersediaException(String message) {
        super(message);
    }
}
