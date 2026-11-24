package rentalkendaraan.model;

import rentalkendaraan.exception.DataValidasiException;

/**
 * Kelas representasi kendaraan jenis Bus Pariwisata / Minibus.
 * Menerapkan Pewarisan dari Kendaraan serta mengimplementasikan Diskonable dan LayananTambahan.
 */
public class Bus extends Kendaraan implements Diskonable, LayananTambahan {
    private int kapasitasKursi;       // Jumlah kursi penumpang (mis. 25, 35, 50)
    private boolean fasilitasToilet;  // Fasilitas toilet dalam armada
    private boolean wajibSupir;       // Sesuai regulasi bus komersial, sewa bus wajib didampingi driver kru

    public Bus(String idKendaraan, String nomorPlat, String merk, String model,
               int tahunProduksi, double tarifPerHari, boolean statusTersedia,
               int kapasitasKursi, boolean fasilitasToilet, boolean wajibSupir) throws DataValidasiException {
        super(idKendaraan, nomorPlat, merk, model, tahunProduksi, tarifPerHari, statusTersedia);
        setKapasitasKursi(kapasitasKursi);
        this.fasilitasToilet = fasilitasToilet;
        this.wajibSupir = wajibSupir;
    }

    // --- OVERRIDING METHOD ABSTRAK KELAS INDUK (Polimorfisme) ---
    @Override
    public double hitungBiayaSewa(int durasiHari) {
        if (durasiHari <= 0) return 0;
        double biayaArmada = tarifPerHari * durasiHari;
        // Biaya operasional supir dan kru profesional bus (Rp 300.000 / hari)
        double biayaKruSupir = wajibSupir ? (300000.0 * durasiHari) : 0;
        double subtotal = biayaArmada + biayaKruSupir;
        double diskon = hitungDiskon(durasiHari, subtotal);
        return subtotal - diskon;
    }

    @Override
    public String getJenisKendaraan() {
        return "Bus";
    }

    @Override
    public String getSpesifikasiKhusus() {
        return "Kapasitas: " + kapasitasKursi + " Kursi | Toilet: " +
               (fasilitasToilet ? "Ada" : "Tidak") + " | Driver Kru: " + (wajibSupir ? "Wajib" : "Opsional");
    }

    @Override
    public double getTarifDendaPerHari() {
        return 150000.0; // Denda keterlambatan bus: Rp 150.000/hari
    }

    // --- IMPLEMENTASI INTERFACE Diskonable ---
    @Override
    public double hitungDiskon(int durasiHari, double totalAwal) {
        // Diskon rombongan untuk pemakaian jangka panjang (>= 3 hari dapat 7.5%)
        if (durasiHari >= 3) {
            return totalAwal * 0.075;
        }
        return 0.0;
    }

    // --- IMPLEMENTASI INTERFACE LayananTambahan ---
    @Override
    public double hitungBiayaLayananTambahan(boolean includeSupir, boolean includeAsuransi, int durasiHari) {
        double biaya = 0.0;
        // Bus sudah include kru supir dalam tarif utama jika wajibSupir = true,
        // asuransi penumpang grup dihitung Rp 50.000 per hari
        if (includeAsuransi) {
            biaya += 50000.0 * durasiHari;
        }
        return biaya;
    }

    // --- GETTER & SETTER DENGAN VALIDASI ---
    public int getKapasitasKursi() {
        return kapasitasKursi;
    }

    public void setKapasitasKursi(int kapasitasKursi) throws DataValidasiException {
        if (kapasitasKursi < 10 || kapasitasKursi > 65) {
            throw new DataValidasiException("Kapasitas kursi bus harus berkisar antara 10 hingga 65 kursi.");
        }
        this.kapasitasKursi = kapasitasKursi;
    }

    public boolean isFasilitasToilet() {
        return fasilitasToilet;
    }

    public void setFasilitasToilet(boolean fasilitasToilet) {
        this.fasilitasToilet = fasilitasToilet;
    }

    public boolean isWajibSupir() {
        return wajibSupir;
    }

    public void setWajibSupir(boolean wajibSupir) {
        this.wajibSupir = wajibSupir;
    }

    @Override
    public String toCsvRecord() {
        return String.join(";",
                "BUS",
                idKendaraan,
                nomorPlat,
                merk,
                model,
                String.valueOf(tahunProduksi),
                String.valueOf(tarifPerHari),
                String.valueOf(statusTersedia),
                String.valueOf(kapasitasKursi),
                String.valueOf(fasilitasToilet),
                String.valueOf(wajibSupir)
        );
    }
}
