package rentalkendaraan.model;

import rentalkendaraan.exception.DataValidasiException;

/**
 * Abstract class yang menjadi induk dari seluruh jenis kendaraan rental.
 * Menerapkan prinsip Abstraksi, Enkapsulasi, dan Polimorfisme.
 */
public abstract class Kendaraan {
    protected String idKendaraan;
    protected String nomorPlat;
    protected String merk;
    protected String model;
    protected int tahunProduksi;
    protected double tarifPerHari;
    protected boolean statusTersedia;

    /**
     * Konstruktor kelas abstrak Kendaraan.
     */
    public Kendaraan(String idKendaraan, String nomorPlat, String merk, String model,
                     int tahunProduksi, double tarifPerHari, boolean statusTersedia) throws DataValidasiException {
        setIdKendaraan(idKendaraan);
        setNomorPlat(nomorPlat);
        setMerk(merk);
        setModel(model);
        setTahunProduksi(tahunProduksi);
        setTarifPerHari(tarifPerHari);
        this.statusTersedia = statusTersedia;
    }

    // --- METHOD ABSTRAK (Prinsip Abstraksi) ---
    /**
     * Menghitung total biaya sewa dasar untuk durasi tertentu.
     * Diimplementasikan secara berbeda pada kelas turunan (Mobil, Motor, Bus).
     */
    public abstract double hitungBiayaSewa(int durasiHari);

    /**
     * Mengembalikan nama jenis kendaraan (mis. Mobil, Motor, Bus).
     */
    public abstract String getJenisKendaraan();

    /**
     * Mengembalikan rincian spesifikasi khusus unik dari masing-masing jenis kendaraan.
     */
    public abstract String getSpesifikasiKhusus();

    /**
     * Mengembalikan tarif denda per hari atas keterlambatan pengembalian unit armada.
     * Diimplementasikan secara polimorfik oleh kelas turunan (Mobil, Motor, Bus).
     * @return tarif denda per hari dalam Rupiah
     */
    public abstract double getTarifDendaPerHari();

    // --- METHOD OVERLOADING (Polimorfisme Overloading) ---
    /**
     * Overloading 1: Menghitung biaya sewa dengan opsi supir.
     */
    public double hitungBiayaSewa(int durasiHari, boolean includeSupir) {
        double biaya = hitungBiayaSewa(durasiHari);
        if (includeSupir) {
            biaya += (LayananTambahan.TARIF_SUPIR_PER_HARI * durasiHari);
        }
        return biaya;
    }

    /**
     * Overloading 2: Menghitung biaya sewa dengan opsi supir dan potongan diskon persentase langsung.
     */
    public double hitungBiayaSewa(int durasiHari, boolean includeSupir, double diskonPersen) {
        double totalAwal = hitungBiayaSewa(durasiHari, includeSupir);
        if (diskonPersen > 0) {
            double nominalDiskon = totalAwal * (diskonPersen / 100.0);
            return Math.max(0, totalAwal - nominalDiskon);
        }
        return totalAwal;
    }

    // --- GETTER DAN SETTER DENGAN VALIDASI (Enkapsulasi) ---
    public String getIdKendaraan() {
        return idKendaraan;
    }

    public void setIdKendaraan(String idKendaraan) throws DataValidasiException {
        if (idKendaraan == null || idKendaraan.trim().isEmpty()) {
            throw new DataValidasiException("ID Kendaraan tidak boleh kosong.");
        }
        this.idKendaraan = idKendaraan.trim().toUpperCase();
    }

    public String getNomorPlat() {
        return nomorPlat;
    }

    public void setNomorPlat(String nomorPlat) throws DataValidasiException {
        if (nomorPlat == null || nomorPlat.trim().isEmpty()) {
            throw new DataValidasiException("Nomor Plat polisi tidak boleh kosong.");
        }
        this.nomorPlat = nomorPlat.trim().toUpperCase();
    }

    public String getMerk() {
        return merk;
    }

    public void setMerk(String merk) throws DataValidasiException {
        if (merk == null || merk.trim().isEmpty()) {
            throw new DataValidasiException("Merk kendaraan tidak boleh kosong.");
        }
        this.merk = merk.trim();
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) throws DataValidasiException {
        if (model == null || model.trim().isEmpty()) {
            throw new DataValidasiException("Model/Tipe kendaraan tidak boleh kosong.");
        }
        this.model = model.trim();
    }

    public int getTahunProduksi() {
        return tahunProduksi;
    }

    public void setTahunProduksi(int tahunProduksi) throws DataValidasiException {
        if (tahunProduksi < 1990 || tahunProduksi > 2030) {
            throw new DataValidasiException("Tahun produksi tidak valid (rentang 1990 - 2030).");
        }
        this.tahunProduksi = tahunProduksi;
    }

    public double getTarifPerHari() {
        return tarifPerHari;
    }

    public void setTarifPerHari(double tarifPerHari) throws DataValidasiException {
        if (tarifPerHari <= 0) {
            throw new DataValidasiException("Tarif sewa per hari harus lebih besar dari 0.");
        }
        this.tarifPerHari = tarifPerHari;
    }

    public boolean isStatusTersedia() {
        return statusTersedia;
    }

    public void setStatusTersedia(boolean statusTersedia) {
        this.statusTersedia = statusTersedia;
    }

    @Override
    public String toString() {
        return "[" + getJenisKendaraan() + "] " + nomorPlat + " - " + merk + " " + model +
               " (Rp " + String.format("%,.0f", tarifPerHari) + "/hari) - " +
               (statusTersedia ? "TERSEDIA" : "DISEWA");
    }

    /**
     * Format representasi data untuk disimpan ke berkas CSV.
     */
    public abstract String toCsvRecord();
}
