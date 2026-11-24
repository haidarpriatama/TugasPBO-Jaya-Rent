package rentalkendaraan.model;

import rentalkendaraan.exception.DataValidasiException;

/**
 * Kelas representasi kendaraan jenis Mobil.
 * Menerapkan Pewarisan dari Kendaraan serta mengimplementasikan Diskonable dan LayananTambahan.
 */
public class Mobil extends Kendaraan implements Diskonable, LayananTambahan {
    private String tipeMobil;          // Contoh: MPV, SUV, Sedan, Hatchback
    private String transmisi;          // Manual / Otomatis
    private int kapasitasPenumpang;    // Jumlah kursi penumpang

    public Mobil(String idKendaraan, String nomorPlat, String merk, String model,
                 int tahunProduksi, double tarifPerHari, boolean statusTersedia,
                 String tipeMobil, String transmisi, int kapasitasPenumpang) throws DataValidasiException {
        super(idKendaraan, nomorPlat, merk, model, tahunProduksi, tarifPerHari, statusTersedia);
        setTipeMobil(tipeMobil);
        setTransmisi(transmisi);
        setKapasitasPenumpang(kapasitasPenumpang);
    }

    // --- OVERRIDING METHOD ABSTRAK KELAS INDUK (Polimorfisme) ---
    @Override
    public double hitungBiayaSewa(int durasiHari) {
        if (durasiHari <= 0) return 0;
        double biayaDasar = tarifPerHari * durasiHari;
        // Asuransi standar perlindungan kendaraan mobil (Rp 25.000/hari)
        double asuransiStandar = 25000.0 * durasiHari;
        double subtotal = biayaDasar + asuransiStandar;
        double diskon = hitungDiskon(durasiHari, subtotal);
        return subtotal - diskon;
    }

    @Override
    public String getJenisKendaraan() {
        return "Mobil";
    }

    @Override
    public String getSpesifikasiKhusus() {
        return "Tipe: " + tipeMobil + " | Transmisi: " + transmisi + " | " + kapasitasPenumpang + " Kursi";
    }

    @Override
    public double getTarifDendaPerHari() {
        return 50000.0; // Denda keterlambatan mobil: Rp 50.000/hari
    }

    // --- IMPLEMENTASI INTERFACE Diskonable ---
    @Override
    public double hitungDiskon(int durasiHari, double totalAwal) {
        // Diskon progresif: >= 7 hari dapat 10%, >= 3 hari dapat 5%
        if (durasiHari >= 7) {
            return totalAwal * 0.10;
        } else if (durasiHari >= 3) {
            return totalAwal * 0.05;
        }
        return 0.0;
    }

    // --- IMPLEMENTASI INTERFACE LayananTambahan ---
    @Override
    public double hitungBiayaLayananTambahan(boolean includeSupir, boolean includeAsuransi, int durasiHari) {
        double biaya = 0.0;
        if (includeSupir) {
            biaya += TARIF_SUPIR_PER_HARI * durasiHari;
        }
        if (includeAsuransi) {
            biaya += TARIF_ASURANSI_PER_HARI * durasiHari;
        }
        return biaya;
    }

    // --- GETTER & SETTER DENGAN VALIDASI ---
    public String getTipeMobil() {
        return tipeMobil;
    }

    public void setTipeMobil(String tipeMobil) throws DataValidasiException {
        if (tipeMobil == null || tipeMobil.trim().isEmpty()) {
            throw new DataValidasiException("Tipe mobil tidak boleh kosong.");
        }
        this.tipeMobil = tipeMobil.trim();
    }

    public String getTransmisi() {
        return transmisi;
    }

    public void setTransmisi(String transmisi) throws DataValidasiException {
        if (transmisi == null || (!transmisi.equalsIgnoreCase("Manual") && !transmisi.equalsIgnoreCase("Otomatis"))) {
            throw new DataValidasiException("Transmisi harus bernilai 'Manual' atau 'Otomatis'.");
        }
        this.transmisi = transmisi.trim();
    }

    public int getKapasitasPenumpang() {
        return kapasitasPenumpang;
    }

    public void setKapasitasPenumpang(int kapasitasPenumpang) throws DataValidasiException {
        if (kapasitasPenumpang < 1 || kapasitasPenumpang > 12) {
            throw new DataValidasiException("Kapasitas penumpang mobil harus antara 1 sampai 12 orang.");
        }
        this.kapasitasPenumpang = kapasitasPenumpang;
    }

    @Override
    public String toCsvRecord() {
        return String.join(";",
                "MOBIL",
                idKendaraan,
                nomorPlat,
                merk,
                model,
                String.valueOf(tahunProduksi),
                String.valueOf(tarifPerHari),
                String.valueOf(statusTersedia),
                tipeMobil,
                transmisi,
                String.valueOf(kapasitasPenumpang)
        );
    }
}
