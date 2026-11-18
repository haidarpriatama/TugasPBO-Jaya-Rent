package rentalkendaraan.model;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import rentalkendaraan.exception.DataValidasiException;
import rentalkendaraan.exception.KendaraanTidakTersediaException;

/**
 * Kelas representasi transaksi penyewaan kendaraan.
 * Menghubungkan entitas Pelanggan dan Kendaraan secara polimorfik.
 */
public class TransaksiSewa implements Diskonable {
    private String idTransaksi;
    private Pelanggan pelanggan;
    private Kendaraan kendaraan;
    private String tanggalSewa;
    private int durasiHari;
    private boolean includeSupir;
    private boolean includeAsuransi;
    private double totalBiaya;
    private String statusTransaksi; // "AKTIF", "SELESAI", "DIBATALKAN"
    private String catatan;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public TransaksiSewa(String idTransaksi, Pelanggan pelanggan, Kendaraan kendaraan,
                         String tanggalSewa, int durasiHari, boolean includeSupir,
                         boolean includeAsuransi, String statusTransaksi, String catatan)
            throws DataValidasiException, KendaraanTidakTersediaException {
        setIdTransaksi(idTransaksi);
        setPelanggan(pelanggan);
        setKendaraan(kendaraan);
        setTanggalSewa(tanggalSewa);
        setDurasiHari(durasiHari);
        this.includeSupir = includeSupir;
        this.includeAsuransi = includeAsuransi;
        this.statusTransaksi = (statusTransaksi == null || statusTransaksi.isEmpty()) ? "AKTIF" : statusTransaksi;
        this.catatan = (catatan == null) ? "-" : catatan.trim();

        // Hitung total biaya secara polimorfis
        this.totalBiaya = hitungTotalBiaya();
    }

    /**
     * Menghitung total biaya transaksi dengan pemanggilan method polimorfik pada objek Kendaraan.
     */
    public double hitungTotalBiaya() {
        if (kendaraan == null || durasiHari <= 0) {
            return 0.0;
        }

        // 1. Biaya sewa dasar kendaraan (Polimorfisme dinamis)
        double biayaSewaKendaraan = kendaraan.hitungBiayaSewa(durasiHari);

        // 2. Layanan tambahan supir (jika kendaraan bukan Bus yang sudah wajib driver)
        double biayaSupir = 0.0;
        if (includeSupir && !(kendaraan instanceof Bus)) {
            biayaSupir = LayananTambahan.TARIF_SUPIR_PER_HARI * durasiHari;
        }

        // 3. Layanan tambahan asuransi komprehensif
        double biayaAsuransi = 0.0;
        if (includeAsuransi) {
            biayaAsuransi = (kendaraan instanceof Bus ? 50000.0 : LayananTambahan.TARIF_ASURANSI_PER_HARI) * durasiHari;
        }

        double subtotal = biayaSewaKendaraan + biayaSupir + biayaAsuransi;

        // 4. Diskon loyalitas transaksi jika durasi panjang
        double diskonTransaksi = hitungDiskon(durasiHari, subtotal);

        return Math.max(0, subtotal - diskonTransaksi);
    }

    @Override
    public double hitungDiskon(int durasiHari, double totalAwal) {
        // Diskon transaksi tambahan bagi sewa lebih dari 10 hari sebesar 3%
        if (durasiHari >= 10) {
            return totalAwal * 0.03;
        }
        return 0.0;
    }

    public void selesaikanPengembalian() {
        this.statusTransaksi = "SELESAI";
        if (this.kendaraan != null) {
            this.kendaraan.setStatusTersedia(true);
        }
    }

    // --- GETTER & SETTER DENGAN VALIDASI ENKAPSULASI ---
    public String getIdTransaksi() {
        return idTransaksi;
    }

    public void setIdTransaksi(String idTransaksi) throws DataValidasiException {
        if (idTransaksi == null || idTransaksi.trim().isEmpty()) {
            throw new DataValidasiException("ID Transaksi tidak boleh kosong.");
        }
        this.idTransaksi = idTransaksi.trim().toUpperCase();
    }

    public Pelanggan getPelanggan() {
        return pelanggan;
    }

    public void setPelanggan(Pelanggan pelanggan) throws DataValidasiException {
        if (pelanggan == null) {
            throw new DataValidasiException("Data Pelanggan tidak boleh kosong.");
        }
        this.pelanggan = pelanggan;
    }

    public Kendaraan getKendaraan() {
        return kendaraan;
    }

    public void setKendaraan(Kendaraan kendaraan) throws DataValidasiException, KendaraanTidakTersediaException {
        if (kendaraan == null) {
            throw new DataValidasiException("Data Kendaraan tidak boleh kosong.");
        }
        // Periksa ketersediaan jika ini transaksi baru
        if ("AKTIF".equalsIgnoreCase(this.statusTransaksi) && !kendaraan.isStatusTersedia()) {
            throw new KendaraanTidakTersediaException("Kendaraan [" + kendaraan.getNomorPlat() +
                    "] sedang disewa dan tidak tersedia untuk transaksi baru.");
        }
        this.kendaraan = kendaraan;
    }

    public String getTanggalSewa() {
        return tanggalSewa;
    }

    public void setTanggalSewa(String tanggalSewa) throws DataValidasiException {
        if (tanggalSewa == null || tanggalSewa.trim().isEmpty()) {
            tanggalSewa = LocalDate.now().format(DATE_FORMATTER);
        }
        try {
            LocalDate.parse(tanggalSewa.trim(), DATE_FORMATTER);
            this.tanggalSewa = tanggalSewa.trim();
        } catch (Exception e) {
            throw new DataValidasiException("Format tanggal harus YYYY-MM-DD (Contoh: 2026-11-20).");
        }
    }

    public int getDurasiHari() {
        return durasiHari;
    }

    public void setDurasiHari(int durasiHari) throws DataValidasiException {
        if (durasiHari <= 0) {
            throw new DataValidasiException("Durasi sewa minimal adalah 1 hari.");
        }
        this.durasiHari = durasiHari;
    }

    public boolean isIncludeSupir() {
        return includeSupir;
    }

    public void setIncludeSupir(boolean includeSupir) {
        this.includeSupir = includeSupir;
        this.totalBiaya = hitungTotalBiaya();
    }

    public boolean isIncludeAsuransi() {
        return includeAsuransi;
    }

    public void setIncludeAsuransi(boolean includeAsuransi) {
        this.includeAsuransi = includeAsuransi;
        this.totalBiaya = hitungTotalBiaya();
    }

    public double getTotalBiaya() {
        return totalBiaya;
    }

    public void setTotalBiaya(double totalBiaya) {
        this.totalBiaya = totalBiaya;
    }

    public String getStatusTransaksi() {
        return statusTransaksi;
    }

    public void setStatusTransaksi(String statusTransaksi) {
        this.statusTransaksi = statusTransaksi;
    }

    public String getCatatan() {
        return catatan;
    }

    public void setCatatan(String catatan) {
        this.catatan = catatan;
    }

    /**
     * Menghasilkan teks struk / nota penyewaan yang rapi dan terformat.
     */
    public String generateStruk() {
        DecimalFormat rupiahFormat = new DecimalFormat("#,###");
        StringBuilder sb = new StringBuilder();
        sb.append("====================================================\n");
        sb.append("            NOTA RESMI RENTAL KENDARAAN             \n");
        sb.append("             KELOMPOK 7 - JAYA RENT CAR             \n");
        sb.append("====================================================\n");
        sb.append(String.format("No. Transaksi  : %s\n", idTransaksi));
        sb.append(String.format("Tanggal Sewa   : %s\n", tanggalSewa));
        sb.append(String.format("Durasi Sewa    : %d Hari\n", durasiHari));
        sb.append(String.format("Status         : %s\n", statusTransaksi));
        sb.append("----------------------------------------------------\n");
        sb.append("DATA PELANGGAN:\n");
        sb.append(String.format("ID / Nama      : %s / %s\n", pelanggan.getIdPelanggan(), pelanggan.getNama()));
        sb.append(String.format("NIK            : %s\n", pelanggan.getNik()));
        sb.append(String.format("No. Telepon    : %s\n", pelanggan.getNomorTelepon()));
        sb.append(String.format("Alamat         : %s\n", pelanggan.getAlamat()));
        sb.append("----------------------------------------------------\n");
        sb.append("DATA KENDARAAN:\n");
        sb.append(String.format("Jenis Armada   : %s\n", kendaraan.getJenisKendaraan()));
        sb.append(String.format("No. Polisi     : %s\n", kendaraan.getNomorPlat()));
        sb.append(String.format("Unit           : %s %s (%d)\n", kendaraan.getMerk(), kendaraan.getModel(), kendaraan.getTahunProduksi()));
        sb.append(String.format("Spesifikasi    : %s\n", kendaraan.getSpesifikasiKhusus()));
        sb.append(String.format("Tarif / Hari   : Rp %s\n", rupiahFormat.format(kendaraan.getTarifPerHari())));
        sb.append("----------------------------------------------------\n");
        sb.append("RINCIAN BIAYA & LAYANAN:\n");
        sb.append(String.format("- Sewa Dasar   : Rp %s\n", rupiahFormat.format(kendaraan.hitungBiayaSewa(durasiHari))));
        if (includeSupir) {
            double bSupir = (kendaraan instanceof Bus) ? 0 : (LayananTambahan.TARIF_SUPIR_PER_HARI * durasiHari);
            sb.append(String.format("- Jasa Supir   : Rp %s %s\n", rupiahFormat.format(bSupir),
                    (kendaraan instanceof Bus ? "(Included in bus fee)" : "")));
        }
        if (includeAsuransi) {
            double bAsuransi = (kendaraan instanceof Bus ? 50000.0 : LayananTambahan.TARIF_ASURANSI_PER_HARI) * durasiHari;
            sb.append(String.format("- Asuransi     : Rp %s\n", rupiahFormat.format(bAsuransi)));
        }
        sb.append(String.format("Catatan Tambahan: %s\n", catatan));
        sb.append("====================================================\n");
        sb.append(String.format("TOTAL PEMBAYARAN: Rp %s\n", rupiahFormat.format(totalBiaya)));
        sb.append("====================================================\n");
        sb.append("          Terima Kasih Atas Kepercayaan Anda!       \n");
        sb.append("      Simpan struk ini sebagai bukti resmi sewa     \n");
        sb.append("====================================================\n");
        return sb.toString();
    }

    public String toCsvRecord() {
        return String.join(";",
                idTransaksi,
                pelanggan.getIdPelanggan(),
                kendaraan.getIdKendaraan(),
                tanggalSewa,
                String.valueOf(durasiHari),
                String.valueOf(includeSupir),
                String.valueOf(includeAsuransi),
                String.valueOf(totalBiaya),
                statusTransaksi,
                catatan.replace(";", ",")
        );
    }
}
