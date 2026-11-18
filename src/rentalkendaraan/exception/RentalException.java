package rentalkendaraan.exception;

/**
 * Base custom exception untuk seluruh modul aplikasi Rental Kendaraan.
 */
public class RentalException extends Exception {
    public RentalException(String message) {
        super(message);
    }

    public RentalException(String message, Throwable cause) {
        super(message, cause);
    }
}
