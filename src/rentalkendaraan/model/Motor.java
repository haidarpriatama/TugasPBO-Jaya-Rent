package rentalkendaraan.model;

import rentalkendaraan.exception.DataValidasiException;

/**
 * Kelas representasi kendaraan jenis Sepeda Motor.
 * Menerapkan Pewarisan dari Kendaraan.
 */
public class Motor extends Kendaraan {
    private String tipeMotor;        // Matic, Manual/Bebek, Sport
    private int kapasitasMesinCc;    // Kapasitas mesin dalam CC (mis. 125, 150)
    private boolean includeHelmDua;  // Fasilitas 2 helm SNI & jas hujan

    public Motor(String idKendaraan, String nomorPlat, String merk, String model,
                 int tahunProduksi, double tarifPerHari, boolean statusTersedia,
                 String tipeMotor, int kapasitasMesinCc, boolean includeHelmDua) throws DataValidasiException {
        super(idKendaraan, nomorPlat, merk, model, tahunProduksi, tarifPerHari, statusTersedia);
        setTipeMotor(tipeMotor);
        setKapasitasMesinCc(kapasitasMesinCc);
        this.includeHelmDua = includeHelmDua;
    }

    // --- OVERRIDING METHOD ABSTRAK KELAS INDUK (Polimorfisme) ---
    @Override
    public double hitungBiayaSewa(int durasiHari) {
        if (durasiHari <= 0) return 0;
        // Promo paket motor: sewa >= 5 hari mendapatkan bonus bebas biaya sewa 1 hari
        int hariDihitung = durasiHari;
        if (durasiHari >= 5) {
            hariDihitung = durasiHari - 1;
        }
        return tarifPerHari * hariDihitung;
    }

    @Override
    public String getJenisKendaraan() {
        return "Motor";
    }

    @Override
    public String getSpesifikasiKhusus() {
        return "Tipe: " + tipeMotor + " | " + kapasitasMesinCc + " cc | 2 Helm & Jas Hujan: " +
               (includeHelmDua ? "Ya" : "Tidak");
    }

    @Override
    public double getTarifDendaPerHari() {
        return 25000.0; // Denda keterlambatan motor: Rp 25.000/hari
    }

    // --- GETTER & SETTER DENGAN VALIDASI ---
    public String getTipeMotor() {
        return tipeMotor;
    }

    public void setTipeMotor(String tipeMotor) throws DataValidasiException {
        if (tipeMotor == null || tipeMotor.trim().isEmpty()) {
            throw new DataValidasiException("Tipe motor tidak boleh kosong.");
        }
        this.tipeMotor = tipeMotor.trim();
    }

    public int getKapasitasMesinCc() {
        return kapasitasMesinCc;
    }

    public void setKapasitasMesinCc(int kapasitasMesinCc) throws DataValidasiException {
        if (kapasitasMesinCc < 50 || kapasitasMesinCc > 1500) {
            throw new DataValidasiException("Kapasitas mesin motor tidak realistis (50 - 1500 cc).");
        }
        this.kapasitasMesinCc = kapasitasMesinCc;
    }

    public boolean isIncludeHelmDua() {
        return includeHelmDua;
    }

    public void setIncludeHelmDua(boolean includeHelmDua) {
        this.includeHelmDua = includeHelmDua;
    }

    @Override
    public String toCsvRecord() {
        return String.join(";",
                "MOTOR",
                idKendaraan,
                nomorPlat,
                merk,
                model,
                String.valueOf(tahunProduksi),
                String.valueOf(tarifPerHari),
                String.valueOf(statusTersedia),
                tipeMotor,
                String.valueOf(kapasitasMesinCc),
                String.valueOf(includeHelmDua)
        );
    }
}
