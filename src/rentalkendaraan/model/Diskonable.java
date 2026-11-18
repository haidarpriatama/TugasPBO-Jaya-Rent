package rentalkendaraan.model;

/**
 * Interface untuk objek yang dapat dihitung potongan harga / diskon sewanya.
 */
public interface Diskonable {
    /**
     * Menghitung besaran nominal diskon berdasarkan durasi sewa dan total biaya awal.
     * @param durasiHari durasi penyewaan dalam hari
     * @param totalAwal total biaya sewa sebelum diskon
     * @return nominal potongan harga (rupiah)
     */
    double hitungDiskon(int durasiHari, double totalAwal);
}
