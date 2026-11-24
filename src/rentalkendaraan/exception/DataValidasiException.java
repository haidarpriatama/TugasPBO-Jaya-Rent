package rentalkendaraan.exception;

/**
 * Exception yang dilemparkan ketika terjadi kegagalan validasi data input.
 */
public class DataValidasiException extends RentalException {
    public DataValidasiException(String message) {
        super(message);
    }
}
