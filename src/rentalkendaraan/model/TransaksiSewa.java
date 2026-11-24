package rentalkendaraan.model;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;
import rentalkendaraan.exception.DataValidasiException;
import rentalkendaraan.exception.KendaraanTidakTersediaException;

/**
 * Kelas representasi transaksi penyewaan kendaraan.
 * Menghubungkan entitas Pelanggan dan Kendaraan secara polimorfik,
 * serta mengelola perhitungan biaya sewa awal, denda keterlambatan, dan pelunasan pengembalian.
 */
public class TransaksiSewa implements Diskonable {
    private String idTransaksi;
    private Pelanggan pelanggan;
    private Kendaraan kendaraan;
    private String tanggalSewa;
    private int durasiHari;
    private boolean includeSupir;
    private boolean includeAsuransi;
    private double biayaSewaAwal;
    private String statusTransaksi; // "AKTIF", "SELESAI", "DIBATALKAN"
    private String catatan;

    // Atribut tambahan untuk pengembalian & denda keterlambatan
    private String tanggalKembaliAktual; // "uuuu-MM-dd" atau "-" jika belum dikembalikan
    private int hariTerlambat;           // Jumlah hari keterlambatan (0 jika tepat waktu)
    private double biayaDenda;           // Nominal denda (0.0 jika tidak ada)

    // DateTimeFormatter presisi menggunakan pola uuuu-MM-dd dengan ResolverStyle.STRICT
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    /**
     * Konstruktor 1 (9 Parameter) - Transaksi baru dibuat di aplikasi.
     * Biaya sewa awal dihitung otomatis dari tarif kendaraan dan layanan tambahan.
     */
    public TransaksiSewa(String idTransaksi, Pelanggan pelanggan, Kendaraan kendaraan,
                         String tanggalSewa, int durasiHari, boolean includeSupir,
                         boolean includeAsuransi, String statusTransaksi, String catatan)
            throws DataValidasiException, KendaraanTidakTersediaException {
        setIdTransaksi(idTransaksi);
        this.statusTransaksi = (statusTransaksi == null || statusTransaksi.trim().isEmpty()) ? "AKTIF" : statusTransaksi.trim().toUpperCase();
        setPelanggan(pelanggan);
        setKendaraan(kendaraan);
        setTanggalSewa(tanggalSewa);
        setDurasiHari(durasiHari);
        this.includeSupir = includeSupir;
        this.includeAsuransi = includeAsuransi;
        setCatatan(catatan);
        this.biayaSewaAwal = hitungBiayaSewaAwal();
        this.tanggalKembaliAktual = "-";
        this.hariTerlambat = 0;
        this.biayaDenda = 0.0;
    }

    /**
     * Konstruktor 2 (13 Parameter) - Mendukung pemuatan data persisten dari berkas (storage).
     * Nilai biayaSewaAwal yang dibaca dari berkas dipertahankan apa adanya (termasuk 0.0),
     * tanpa dihitung ulang secara otomatis.
     */
    public TransaksiSewa(String idTransaksi, Pelanggan pelanggan, Kendaraan kendaraan,
                         String tanggalSewa, int durasiHari, boolean includeSupir,
                         boolean includeAsuransi, double biayaSewaAwal, String statusTransaksi,
                         String catatan, String tanggalKembaliAktual, int hariTerlambat,
                         double biayaDenda)
            throws DataValidasiException, KendaraanTidakTersediaException {
        setIdTransaksi(idTransaksi);
        this.statusTransaksi = (statusTransaksi == null || statusTransaksi.trim().isEmpty()) ? "AKTIF" : statusTransaksi.trim().toUpperCase();
        setPelanggan(pelanggan);
        setKendaraan(kendaraan);
        setTanggalSewa(tanggalSewa);
        setDurasiHari(durasiHari);
        this.includeSupir = includeSupir;
        this.includeAsuransi = includeAsuransi;
        setCatatan(catatan);
        setBiayaSewaAwal(biayaSewaAwal);
        setTanggalKembaliAktual(tanggalKembaliAktual);
        setHariTerlambat(hariTerlambat);
        setBiayaDenda(biayaDenda);
    }

    // --- METODE VALIDASI INTERNAL ---
    private static void validasiBiaya(double nilai, String namaField) throws DataValidasiException {
        if (Double.isNaN(nilai) || Double.isInfinite(nilai)) {
            throw new DataValidasiException(namaField + " tidak boleh NaN atau Infinite.");
        }
        if (nilai < 0.0) {
            throw new DataValidasiException(namaField + " tidak boleh bernilai negatif: " + nilai);
        }
    }

    private static void validasiHariTerlambat(int hari) throws DataValidasiException {
        if (hari < 0) {
            throw new DataValidasiException("Hari keterlambatan tidak boleh bernilai negatif: " + hari);
        }
    }

    /**
     * Menghitung biaya sewa awal (sebelum denda) dengan pemanggilan method polimorfik pada objek Kendaraan.
     */
    public double hitungBiayaSewaAwal() {
        if (kendaraan == null || durasiHari <= 0) {
            return 0.0;
        }

        // 1. Biaya sewa dasar kendaraan (Polimorfisme dinamis)
        double biayaSewaKendaraan = kendaraan.hitungBiayaSewa(durasiHari);

        // 2. Layanan tambahan supir (jika kendaraan bukan Bus yang sudah wajib kru supir)
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

        // 4. Diskon loyalitas transaksi jika durasi panjang (>= 10 hari sebesar 3%)
        double diskonTransaksi = hitungDiskon(durasiHari, subtotal);

        return Math.max(0, subtotal - diskonTransaksi);
    }

    /**
     * Alias method untuk kompatibilitas ke kode yang memanggil hitungTotalBiaya().
     */
    public double hitungTotalBiaya() {
        return hitungBiayaSewaAwal();
    }

    @Override
    public double hitungDiskon(int durasiHari, double totalAwal) {
        // Diskon transaksi tambahan bagi sewa lebih dari 10 hari sebesar 3%
        if (durasiHari >= 10) {
            return totalAwal * 0.03;
        }
        return 0.0;
    }

    /**
     * Menghitung tanggal jatuh tempo pengembalian sewa.
     */
    public String getTanggalJatuhTempo() {
        try {
            LocalDate tglSewa = LocalDate.parse(this.tanggalSewa.trim(), DATE_FORMATTER);
            return tglSewa.plusDays(this.durasiHari).format(DATE_FORMATTER);
        } catch (Exception e) {
            return "-";
        }
    }

    /**
     * Menghitung jumlah hari keterlambatan dari tanggal pengembalian aktual terhadap jatuh tempo.
     */
    public int hitungHariKeterlambatan(String tanggalKembali) throws DataValidasiException {
        if (tanggalKembali == null || tanggalKembali.trim().isEmpty() || "-".equals(tanggalKembali.trim())) {
            return 0;
        }
        try {
            LocalDate tglKembali = LocalDate.parse(tanggalKembali.trim(), DATE_FORMATTER);
            LocalDate tglSewa = LocalDate.parse(this.tanggalSewa.trim(), DATE_FORMATTER);
            if (tglKembali.isBefore(tglSewa)) {
                throw new DataValidasiException("Tanggal pengembalian (" + tanggalKembali +
                        ") tidak boleh lebih awal dari tanggal sewa (" + this.tanggalSewa + ").");
            }
            LocalDate tglJatuhTempo = tglSewa.plusDays(this.durasiHari);
            long selisihHari = ChronoUnit.DAYS.between(tglJatuhTempo, tglKembali);
            return (int) Math.max(0, selisihHari);
        } catch (DataValidasiException e) {
            throw e;
        } catch (Exception e) {
            throw new DataValidasiException("Format tanggal pengembalian tidak valid (Gunakan pola uuuu-MM-dd): " + e.getMessage());
        }
    }

    /**
     * Menghitung total denda keterlambatan menggunakan tarif polimorfis kendaraan.
     */
    public double hitungBiayaDenda(int keterlambatanHari) {
        if (kendaraan == null || keterlambatanHari <= 0) {
            return 0.0;
        }
        return keterlambatanHari * kendaraan.getTarifDendaPerHari();
    }

    /**
     * Selesaikan pengembalian unit default (menggunakan tanggal sistem hari ini).
     */
    public void selesaikanPengembalian() throws DataValidasiException {
        selesaikanPengembalian(LocalDate.now().format(DATE_FORMATTER));
    }

    /**
     * Selesaikan pengembalian unit dengan tanggal kembali aktual dan kalkulasi denda.
     * Validasi dilakukan terlebih dahulu sebelum status objek dimutasi.
     */
    public void selesaikanPengembalian(String tanggalKembali) throws DataValidasiException {
        if ("SELESAI".equalsIgnoreCase(this.statusTransaksi)) {
            throw new DataValidasiException("Transaksi [" + idTransaksi + "] sudah berstatus SELESAI dan tidak dapat diselesaikan kembali.");
        }
        if ("DIBATALKAN".equalsIgnoreCase(this.statusTransaksi)) {
            throw new DataValidasiException("Transaksi [" + idTransaksi + "] sudah berstatus DIBATALKAN dan tidak dapat diproses pengembaliannya.");
        }
        if (tanggalKembali == null || tanggalKembali.trim().isEmpty()) {
            tanggalKembali = LocalDate.now().format(DATE_FORMATTER);
        }

        // Validasi tanggal dan hitung keterlambatan TERLEBIH DAHULU sebelum mutasi state
        int terlambat = hitungHariKeterlambatan(tanggalKembali);
        double denda = hitungBiayaDenda(terlambat);

        // Hanya jika seluruh validasi berhasil tanpa exception, ubah state objek
        this.tanggalKembaliAktual = tanggalKembali.trim();
        this.hariTerlambat = terlambat;
        this.biayaDenda = denda;
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
            throw new DataValidasiException("Format tanggal harus uuuu-MM-dd dan merupakan tanggal kalender yang valid (Contoh: 2026-11-20): " + e.getMessage());
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
        this.biayaSewaAwal = hitungBiayaSewaAwal();
    }

    public boolean isIncludeAsuransi() {
        return includeAsuransi;
    }

    public void setIncludeAsuransi(boolean includeAsuransi) {
        this.includeAsuransi = includeAsuransi;
        this.biayaSewaAwal = hitungBiayaSewaAwal();
    }

    public double getBiayaSewaAwal() {
        return biayaSewaAwal;
    }

    public void setBiayaSewaAwal(double biayaSewaAwal) throws DataValidasiException {
        validasiBiaya(biayaSewaAwal, "Biaya sewa awal");
        this.biayaSewaAwal = biayaSewaAwal;
    }

    public double getBiayaDenda() {
        return biayaDenda;
    }

    public void setBiayaDenda(double biayaDenda) throws DataValidasiException {
        validasiBiaya(biayaDenda, "Biaya denda");
        this.biayaDenda = biayaDenda;
    }

    /**
     * Mengembalikan total pembayaran keseluruhan (biaya sewa awal + biaya denda).
     */
    public double getTotalPembayaran() {
        return biayaSewaAwal + biayaDenda;
    }

    /**
     * Kompatibilitas dengan pemanggil getTotalBiaya().
     */
    public double getTotalBiaya() {
        return getTotalPembayaran();
    }

    /**
     * Kompatibilitas dengan pemanggil setTotalBiaya().
     */
    public void setTotalBiaya(double totalBiaya) throws DataValidasiException {
        validasiBiaya(totalBiaya, "Total biaya");
        this.biayaSewaAwal = totalBiaya;
    }

    public String getTanggalKembaliAktual() {
        return tanggalKembaliAktual;
    }

    public void setTanggalKembaliAktual(String tanggalKembaliAktual) throws DataValidasiException {
        if (tanggalKembaliAktual == null || tanggalKembaliAktual.trim().isEmpty() || "-".equals(tanggalKembaliAktual.trim())) {
            this.tanggalKembaliAktual = "-";
        } else {
            try {
                LocalDate.parse(tanggalKembaliAktual.trim(), DATE_FORMATTER);
                this.tanggalKembaliAktual = tanggalKembaliAktual.trim();
            } catch (Exception e) {
                throw new DataValidasiException("Format tanggal kembali aktual tidak valid (Gunakan pola uuuu-MM-dd): " + e.getMessage());
            }
        }
    }

    public int getHariTerlambat() {
        return hariTerlambat;
    }

    public void setHariTerlambat(int hariTerlambat) throws DataValidasiException {
        validasiHariTerlambat(hariTerlambat);
        this.hariTerlambat = hariTerlambat;
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
        if (catatan == null || catatan.trim().isEmpty()) {
            this.catatan = "-";
        } else {
            this.catatan = catatan.trim().replace(";", ",");
        }
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
        sb.append(String.format("Jatuh Tempo    : %s\n", getTanggalJatuhTempo()));
        sb.append(String.format("Tgl Kembali    : %s\n", (tanggalKembaliAktual != null && !tanggalKembaliAktual.isEmpty()) ? tanggalKembaliAktual : "-"));
        if ("SELESAI".equalsIgnoreCase(statusTransaksi)) {
            if (hariTerlambat > 0) {
                sb.append(String.format("Keterlambatan  : %d Hari (Terlambat)\n", hariTerlambat));
            } else {
                sb.append("Keterlambatan  : 0 Hari (Tepat Waktu)\n");
            }
        } else {
            sb.append("Keterlambatan  : 0 Hari\n");
        }
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
        sb.append(String.format("Tarif Denda/Hr : Rp %s\n", rupiahFormat.format(kendaraan.getTarifDendaPerHari())));
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
        sb.append(String.format("Biaya Sewa Awal: Rp %s\n", rupiahFormat.format(biayaSewaAwal)));
        if ("SELESAI".equalsIgnoreCase(statusTransaksi) && hariTerlambat > 0) {
            sb.append(String.format("Total Denda    : Rp %s (%d hari @Rp %s)\n",
                    rupiahFormat.format(biayaDenda),
                    hariTerlambat,
                    rupiahFormat.format(kendaraan.getTarifDendaPerHari())));
        } else {
            sb.append("Total Denda    : Rp 0\n");
        }
        sb.append(String.format("Catatan Tambahan: %s\n", catatan));
        sb.append("====================================================\n");
        sb.append(String.format("TOTAL PEMBAYARAN: Rp %s\n", rupiahFormat.format(getTotalPembayaran())));
        sb.append("====================================================\n");
        sb.append("          Terima Kasih Atas Kepercayaan Anda!       \n");
        sb.append("      Simpan struk ini sebagai bukti resmi sewa     \n");
        sb.append("====================================================\n");
        return sb.toString();
    }

    /**
     * Format representasi data record CSV dengan 13 kolom.
     */
    public String toCsvRecord() {
        return String.join(";",
                idTransaksi,
                pelanggan.getIdPelanggan(),
                kendaraan.getIdKendaraan(),
                tanggalSewa,
                String.valueOf(durasiHari),
                String.valueOf(includeSupir),
                String.valueOf(includeAsuransi),
                String.valueOf(biayaSewaAwal),
                statusTransaksi,
                (catatan == null || catatan.trim().isEmpty() ? "-" : catatan.trim().replace(";", ",")),
                (tanggalKembaliAktual == null || tanggalKembaliAktual.trim().isEmpty() ? "-" : tanggalKembaliAktual.trim()),
                String.valueOf(hariTerlambat),
                String.valueOf(biayaDenda)
        );
    }
}
