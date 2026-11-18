package rentalkendaraan.model;

/**
 * Interface untuk kelas yang mendukung perhitungan biaya layanan tambahan
 * seperti pengemudi (driver) dan asuransi proteksi kendaraan.
 */
public interface LayananTambahan {
    double TARIF_SUPIR_PER_HARI = 150000.0;
    double TARIF_ASURANSI_PER_HARI = 35000.0;

    /**
     * Menghitung total biaya tambahan berdasarkan opsi yang dipilih.
     * @param includeSupir apakah menggunakan jasa supir
     * @param includeAsuransi apakah menggunakan asuransi tambahan
     * @param durasiHari jumlah hari sewa
     * @return total biaya layanan tambahan
     */
    double hitungBiayaLayananTambahan(boolean includeSupir, boolean includeAsuransi, int durasiHari);
}
