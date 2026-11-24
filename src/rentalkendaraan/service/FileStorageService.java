package rentalkendaraan.service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import rentalkendaraan.exception.DataValidasiException;
import rentalkendaraan.exception.RentalException;
import rentalkendaraan.model.*;

/**
 * Service untuk menangani persistensi data secara permanen ke media penyimpanan berkas (File I/O).
 * Menerapkan penanganan berkas teks berbasis CSV dengan blok try-catch, Exception Handling,
 * Atomic File Write (.tmp + move), transaksi berkas ganda dengan rollback disk,
 * serta strict parser whitelisting 10 kolom (lama) dan 13 kolom (baru) yang menjaga record tidak valid tetap aman.
 */
public class FileStorageService {
    private final String dataDir;
    private final String fileKendaraan;
    private final String filePelanggan;
    private final String fileTransaksi;

    // DateTimeFormatter presisi menggunakan pola uuuu-MM-dd dengan ResolverStyle.STRICT
    private static final DateTimeFormatter STRICT_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    // Menyimpan baris-baris record yang gagal diparse agar tidak terhapus saat penyimpanan ulang
    private final List<String> barisTransaksiTidakValid = new ArrayList<>();

    /**
     * Konstruktor default menggunakan direktori bawaan "data".
     */
    public FileStorageService() {
        this("data");
    }

    /**
     * Konstruktor dengan parameter direktori untuk mendukung pengujian terisolasi
     * tanpa memodifikasi data riil aplikasi.
     */
    public FileStorageService(String dataDir) {
        this.dataDir = dataDir;
        this.fileKendaraan = dataDir + File.separator + "kendaraan.txt";
        this.filePelanggan = dataDir + File.separator + "pelanggan.txt";
        this.fileTransaksi = dataDir + File.separator + "transaksi.txt";

        File dir = new File(dataDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public List<String> getBarisTransaksiTidakValid() {
        return Collections.unmodifiableList(barisTransaksiTidakValid);
    }

    public boolean hasBarisTransaksiTidakValid() {
        return !barisTransaksiTidakValid.isEmpty();
    }

    // --- HELPER PARSER & VALIDASI STRICT ---
    private static boolean parseStrictBoolean(String value, String fieldName) throws DataValidasiException {
        if ("true".equalsIgnoreCase(value)) {
            return true;
        } else if ("false".equalsIgnoreCase(value)) {
            return false;
        } else {
            throw new DataValidasiException("Nilai boolean untuk " + fieldName + " tidak valid: '" + value + "' (harus 'true' atau 'false')");
        }
    }

    private static int parseStrictPositiveInt(String value, String fieldName) throws DataValidasiException {
        try {
            int val = Integer.parseInt(value);
            if (val <= 0) {
                throw new DataValidasiException(fieldName + " harus lebih besar dari 0: " + value);
            }
            return val;
        } catch (NumberFormatException e) {
            throw new DataValidasiException(fieldName + " bukan bilangan bulat valid: '" + value + "'");
        }
    }

    private static int parseStrictNonNegativeInt(String value, String fieldName) throws DataValidasiException {
        try {
            int val = Integer.parseInt(value);
            if (val < 0) {
                throw new DataValidasiException(fieldName + " tidak boleh negatif: " + value);
            }
            return val;
        } catch (NumberFormatException e) {
            throw new DataValidasiException(fieldName + " bukan bilangan bulat valid: '" + value + "'");
        }
    }

    private static double parseStrictNonNegativeDouble(String value, String fieldName) throws DataValidasiException {
        try {
            double d = Double.parseDouble(value);
            if (Double.isNaN(d) || Double.isInfinite(d)) {
                throw new DataValidasiException(fieldName + " tidak boleh NaN atau Infinite: " + value);
            }
            if (d < 0.0) {
                throw new DataValidasiException(fieldName + " tidak boleh negatif: " + value);
            }
            return d;
        } catch (NumberFormatException e) {
            throw new DataValidasiException(fieldName + " bukan angka desimal valid: '" + value + "'");
        }
    }

    private static void validateStatusTransaksi(String status) throws DataValidasiException {
        if (!"AKTIF".equalsIgnoreCase(status) &&
            !"SELESAI".equalsIgnoreCase(status) &&
            !"DIBATALKAN".equalsIgnoreCase(status)) {
            throw new DataValidasiException("Status transaksi tidak valid: '" + status + "' (harus AKTIF, SELESAI, atau DIBATALKAN)");
        }
    }

    /**
     * Mengganti target file dengan file temporary secara atomik.
     */
    private void atomicReplace(File tempFile, File targetFile) throws IOException {
        try {
            Files.move(tempFile.toPath(), targetFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tempFile.toPath(), targetFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Memuat daftar kendaraan dari berkas penyimpanan.
     * Jika berkas belum ada, maka data sampel awal akan otomatis digenerate.
     */
    public List<Kendaraan> muatDataKendaraan() throws RentalException {
        List<Kendaraan> list = new ArrayList<>();
        File file = new File(fileKendaraan);

        if (!file.exists()) {
            list = buatDataSampelKendaraan();
            simpanDataKendaraan(list);
            return list;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split(";", -1);
                if (parts.length < 8) continue;

                String jenis = parts[0].toUpperCase();
                String id = parts[1];
                String plat = parts[2];
                String merk = parts[3];
                String model = parts[4];
                int tahun = Integer.parseInt(parts[5]);
                double tarif = Double.parseDouble(parts[6]);
                boolean status = Boolean.parseBoolean(parts[7]);

                Kendaraan k = null;
                if ("MOBIL".equals(jenis) && parts.length >= 11) {
                    String tipe = parts[8];
                    String transmisi = parts[9];
                    int kapasitas = Integer.parseInt(parts[10]);
                    k = new Mobil(id, plat, merk, model, tahun, tarif, status, tipe, transmisi, kapasitas);
                } else if ("MOTOR".equals(jenis) && parts.length >= 11) {
                    String tipe = parts[8];
                    int cc = Integer.parseInt(parts[9]);
                    boolean helm = Boolean.parseBoolean(parts[10]);
                    k = new Motor(id, plat, merk, model, tahun, tarif, status, tipe, cc, helm);
                } else if ("BUS".equals(jenis) && parts.length >= 11) {
                    int kursi = Integer.parseInt(parts[8]);
                    boolean toilet = Boolean.parseBoolean(parts[9]);
                    boolean supir = Boolean.parseBoolean(parts[10]);
                    k = new Bus(id, plat, merk, model, tahun, tarif, status, kursi, toilet, supir);
                }

                if (k != null) {
                    list.add(k);
                }
            }
        } catch (IOException | NumberFormatException | DataValidasiException e) {
            throw new RentalException("Gagal membaca berkas data kendaraan: " + e.getMessage(), e);
        }

        return list;
    }

    /**
     * Menyimpan seluruh list kendaraan ke dalam file teks secara permanen dengan atomic write.
     */
    public void simpanDataKendaraan(List<Kendaraan> list) throws RentalException {
        File targetFile = new File(fileKendaraan);
        File tempFile = new File(targetFile.getParentFile(), targetFile.getName() + ".tmp");

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(tempFile), StandardCharsets.UTF_8))) {
            for (Kendaraan k : list) {
                writer.write(k.toCsvRecord());
                writer.newLine();
            }
        } catch (IOException e) {
            if (tempFile.exists()) tempFile.delete();
            throw new RentalException("Gagal menulis berkas sementara kendaraan: " + e.getMessage(), e);
        }

        try {
            atomicReplace(tempFile, targetFile);
        } catch (IOException e) {
            if (tempFile.exists()) tempFile.delete();
            throw new RentalException("Gagal mengganti berkas kendaraan secara atomik: " + e.getMessage(), e);
        }
    }

    /**
     * Memuat daftar pelanggan dari berkas penyimpanan.
     */
    public List<Pelanggan> muatDataPelanggan() throws RentalException {
        List<Pelanggan> list = new ArrayList<>();
        File file = new File(filePelanggan);

        if (!file.exists()) {
            list = buatDataSampelPelanggan();
            simpanDataPelanggan(list);
            return list;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split(";", -1);
                if (parts.length >= 6) {
                    Pelanggan p = new Pelanggan(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]);
                    list.add(p);
                }
            }
        } catch (IOException | DataValidasiException e) {
            throw new RentalException("Gagal membaca berkas data pelanggan: " + e.getMessage(), e);
        }

        return list;
    }

    /**
     * Menyimpan daftar pelanggan ke berkas penyimpanan dengan atomic write.
     */
    public void simpanDataPelanggan(List<Pelanggan> list) throws RentalException {
        File targetFile = new File(filePelanggan);
        File tempFile = new File(targetFile.getParentFile(), targetFile.getName() + ".tmp");

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(tempFile), StandardCharsets.UTF_8))) {
            for (Pelanggan p : list) {
                writer.write(p.toCsvRecord());
                writer.newLine();
            }
        } catch (IOException e) {
            if (tempFile.exists()) tempFile.delete();
            throw new RentalException("Gagal menulis berkas sementara pelanggan: " + e.getMessage(), e);
        }

        try {
            atomicReplace(tempFile, targetFile);
        } catch (IOException e) {
            if (tempFile.exists()) tempFile.delete();
            throw new RentalException("Gagal mengganti berkas pelanggan secara atomik: " + e.getMessage(), e);
        }
    }

    /**
     * Memuat daftar transaksi sewa dari berkas penyimpanan.
     * Menerima strictly tepat 10 kolom untuk format lama dan 13 kolom untuk format baru.
     * Record dengan jumlah kolom di luar 10/13 atau data numerik rusak diabaikan secara aman,
     * dicatat ke konsol, dan disimpan di barisTransaksiTidakValid agar tidak terhapus saat penyimpanan ulang.
     */
    public List<TransaksiSewa> muatDataTransaksi(Map<String, Pelanggan> pelangganMap,
                                                 Map<String, Kendaraan> kendaraanMap) throws RentalException {
        List<TransaksiSewa> list = new ArrayList<>();
        File file = new File(fileTransaksi);

        barisTransaksiTidakValid.clear();

        if (!file.exists()) {
            return list;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                // Gunakan split(";", -1) agar kolom kosong di akhir tidak terpotong
                String[] parts = line.split(";", -1);

                // Validasi ketat: Hanya terima format lama tepat 10 kolom atau format baru tepat 13 kolom
                if (parts.length != 10 && parts.length != 13) {
                    System.err.println("[Peringatan Integritas Parser] Record transaksi ditolak karena jumlah kolom tidak valid ("
                            + parts.length + " kolom, diharapkan tepat 10 atau 13): " + line);
                    barisTransaksiTidakValid.add(line);
                    continue;
                }

                try {
                    String idTrx = parts[0].trim();
                    String idPel = parts[1].trim();
                    String idKen = parts[2].trim();
                    String tgl = parts[3].trim();

                    if (idTrx.isEmpty() || idPel.isEmpty() || idKen.isEmpty()) {
                        throw new DataValidasiException("ID Transaksi, Pelanggan, atau Kendaraan tidak boleh kosong.");
                    }

                    // Validasi tanggal sewa kalender secara presisi
                    try {
                        LocalDate.parse(tgl, STRICT_DATE_FORMATTER);
                    } catch (Exception e) {
                        throw new DataValidasiException("Format tanggal sewa tidak valid ('" + tgl + "'): " + e.getMessage());
                    }

                    int durasi = parseStrictPositiveInt(parts[4].trim(), "Durasi sewa");
                    boolean supir = parseStrictBoolean(parts[5].trim(), "Include supir");
                    boolean asuransi = parseStrictBoolean(parts[6].trim(), "Include asuransi");
                    double biayaSewaAwal = parseStrictNonNegativeDouble(parts[7].trim(), "Biaya sewa awal");

                    String status = parts[8].trim().isEmpty() ? "AKTIF" : parts[8].trim().toUpperCase();
                    validateStatusTransaksi(status);

                    String catatan = parts[9].trim().isEmpty() ? "-" : parts[9].trim();

                    // Default untuk transaksi lama (10 kolom)
                    String tanggalKembaliAktual = "-";
                    int hariTerlambat = 0;
                    double biayaDenda = 0.0;

                    // Jika format baru (13 kolom), baca data denda riil
                    if (parts.length == 13) {
                        String tglKembaliRaw = parts[10].trim();
                        if (!tglKembaliRaw.isEmpty() && !"-".equals(tglKembaliRaw)) {
                            try {
                                LocalDate.parse(tglKembaliRaw, STRICT_DATE_FORMATTER);
                                tanggalKembaliAktual = tglKembaliRaw;
                            } catch (Exception e) {
                                throw new DataValidasiException("Tanggal kembali aktual tidak valid ('" + tglKembaliRaw + "'): " + e.getMessage());
                            }
                        }

                        hariTerlambat = parseStrictNonNegativeInt(parts[11].trim(), "Hari keterlambatan");
                        biayaDenda = parseStrictNonNegativeDouble(parts[12].trim(), "Biaya denda");
                    }

                    Pelanggan pel = pelangganMap.get(idPel.toUpperCase());
                    if (pel == null) pel = pelangganMap.get(idPel);

                    Kendaraan ken = kendaraanMap.get(idKen.toUpperCase());
                    if (ken == null) ken = kendaraanMap.get(idKen);

                    // Validasi referensi integritas foreign key
                    if (pel == null || ken == null) {
                        System.err.println("[Peringatan Integritas Relasi] Record transaksi ditolak karena ID referensi tidak ditemukan (Pelanggan: "
                                + idPel + ", Kendaraan: " + idKen + "): " + line);
                        barisTransaksiTidakValid.add(line);
                        continue;
                    }

                    TransaksiSewa trx = new TransaksiSewa(
                            idTrx, pel, ken, tgl, durasi, supir, asuransi,
                            biayaSewaAwal, status, catatan,
                            tanggalKembaliAktual, hariTerlambat, biayaDenda
                    );
                    list.add(trx);

                } catch (Exception e) {
                    System.err.println("[Peringatan Integritas Data] Record transaksi ditolak karena kesalahan validasi ("
                            + e.getMessage() + "): " + line);
                    barisTransaksiTidakValid.add(line);
                }
            }
        } catch (Exception e) {
            throw new RentalException("Gagal membaca berkas data transaksi: " + e.getMessage(), e);
        }

        return list;
    }

    /**
     * Menyimpan daftar transaksi sewa ke berkas penyimpanan dengan atomic write (.tmp + atomic move).
     * Seluruh baris transaksi tidak valid juga dipertahankan agar tidak hilang dari disk.
     */
    public void simpanDataTransaksi(List<TransaksiSewa> list) throws RentalException {
        File targetFile = new File(fileTransaksi);
        File tempFile = new File(targetFile.getParentFile(), targetFile.getName() + ".tmp");

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(tempFile), StandardCharsets.UTF_8))) {
            for (TransaksiSewa t : list) {
                writer.write(t.toCsvRecord());
                writer.newLine();
            }
            // Tuliskan kembali baris transaksi tidak valid agar record yang rusak tidak hilang dari disk
            for (String barisRusak : barisTransaksiTidakValid) {
                writer.write(barisRusak);
                writer.newLine();
            }
        } catch (IOException e) {
            if (tempFile.exists()) tempFile.delete();
            throw new RentalException("Gagal menulis berkas sementara transaksi: " + e.getMessage(), e);
        }

        try {
            atomicReplace(tempFile, targetFile);
        } catch (IOException e) {
            if (tempFile.exists()) tempFile.delete();
            throw new RentalException("Gagal mengganti berkas transaksi secara atomik: " + e.getMessage(), e);
        }
    }

    /**
     * Menyimpan data transaksi dan kendaraan secara transactional konsisten.
     * Jika salah satu penyimpanan gagal, berkas yang sudah tertulis akan di-rollback ke snapshot sebelumnya.
     * Jika berkas awalnya belum ada, berkas baru yang sempat dibuat akan dihapus saat rollback.
     */
    public void simpanTransaksiDanKendaraan(List<Kendaraan> kList, List<TransaksiSewa> tList) throws RentalException {
        File fKen = new File(fileKendaraan);
        File fTrx = new File(fileTransaksi);
        boolean fKenExisted = fKen.exists();
        boolean fTrxExisted = fTrx.exists();
        byte[] backupKen = null;
        byte[] backupTrx = null;

        try {
            if (fKenExisted) backupKen = Files.readAllBytes(fKen.toPath());
            if (fTrxExisted) backupTrx = Files.readAllBytes(fTrx.toPath());
        } catch (IOException e) {
            throw new RentalException("Gagal mencadangkan berkas sebelum penyimpanan: " + e.getMessage(), e);
        }

        boolean kenSaved = false;
        try {
            simpanDataKendaraan(kList);
            kenSaved = true;
        } catch (RentalException eKen) {
            throw new RentalException("Gagal menyimpan berkas kendaraan: " + eKen.getMessage(), eKen);
        }

        try {
            simpanDataTransaksi(tList);
        } catch (RentalException eTrx) {
            // Rollback berkas kendaraan ke snapshot sebelumnya jika penulisan transaksi gagal
            String rollbackError = null;
            if (kenSaved) {
                try {
                    if (fKenExisted && backupKen != null) {
                        Files.write(fKen.toPath(), backupKen, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    } else if (!fKenExisted && fKen.exists()) {
                        Files.delete(fKen.toPath());
                    }
                } catch (IOException eRollback) {
                    rollbackError = eRollback.getMessage();
                }
            }
            if (rollbackError != null) {
                throw new RentalException("KRITIS: Gagal menyimpan data transaksi (" + eTrx.getMessage()
                        + ") dan pemulihan berkas kendaraan juga GAGAL (" + rollbackError
                        + "). Integritas berkas perlu diperiksa!", eTrx);
            } else {
                throw new RentalException("Gagal menyimpan data transaksi: " + eTrx.getMessage()
                        + ". Berkas kendaraan telah berhasil dipulihkan ke kondisi semula.", eTrx);
            }
        }
    }

    // --- DATA SAMPEL AWAL UNTUK INISIALISASI ---
    private List<Kendaraan> buatDataSampelKendaraan() {
        List<Kendaraan> list = new ArrayList<>();
        try {
            list.add(new Mobil("KND-001", "B 1234 ABC", "Toyota", "Avanza Veloz", 2023, 400000, true, "MPV", "Otomatis", 7));
            list.add(new Mobil("KND-002", "H 8888 XY", "Honda", "HR-V RS Turbo", 2024, 650000, true, "SUV", "Otomatis", 5));
            list.add(new Mobil("KND-003", "B 5678 JKL", "Mitsubishi", "Pajero Sport", 2023, 850000, true, "SUV", "Otomatis", 7));
            list.add(new Motor("KND-004", "H 4321 ZA", "Honda", "Vario 160", 2024, 90000, true, "Matic", 160, true));
            list.add(new Motor("KND-005", "B 3141 PI", "Yamaha", "NMAX 155 Connected", 2023, 120000, true, "Matic", 155, true));
            list.add(new Bus("KND-006", "H 7777 BUS", "Isuzu", "Elf Microbus Long", 2023, 1200000, true, 20, false, true));
            list.add(new Bus("KND-007", "AD 9999 PO", "Mercedes-Benz", "OH 1626 Jetbus 5", 2024, 2500000, true, 48, true, true));
        } catch (DataValidasiException e) {
            System.err.println("Gagal membuat sampel kendaraan: " + e.getMessage());
        }
        return list;
    }

    private List<Pelanggan> buatDataSampelPelanggan() {
        List<Pelanggan> list = new ArrayList<>();
        try {
            list.add(new Pelanggan("PLG-001", "Haidar Rafi Apta Priatama", "3374011201010001", "081234567890", "Jl. Tembalang Baru No. 10, Semarang", "Laki-laki"));
            list.add(new Pelanggan("PLG-002", "Muhammad Arya Putra", "3374011201010002", "085678901234", "Jl. Banjarsari Selatan No. 5, Semarang", "Laki-laki"));
            list.add(new Pelanggan("PLG-003", "Chico Diar Ramadhan", "3374011201010003", "087812345678", "Jl. Prof. Soedarto No. 12, Semarang", "Laki-laki"));
            list.add(new Pelanggan("PLG-004", "Siti Nurhaliza", "3374011201010004", "081399887766", "Jl. Pandanaran No. 45, Semarang", "Perempuan"));
        } catch (DataValidasiException e) {
            System.err.println("Gagal membuat sampel pelanggan: " + e.getMessage());
        }
        return list;
    }
}
