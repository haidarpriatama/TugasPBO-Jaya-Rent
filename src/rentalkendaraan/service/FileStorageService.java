package rentalkendaraan.service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import rentalkendaraan.exception.DataValidasiException;
import rentalkendaraan.exception.RentalException;
import rentalkendaraan.model.*;

/**
 * Service untuk menangani persistensi data secara permanen ke media penyimpanan berkas (File I/O).
 * Menerapkan penanganan berkas teks berbasis CSV dengan blok try-catch dan Exception Handling.
 */
public class FileStorageService {
    private static final String DATA_DIR = "data";
    private static final String FILE_KENDARAAN = DATA_DIR + File.separator + "kendaraan.txt";
    private static final String FILE_PELANGGAN = DATA_DIR + File.separator + "pelanggan.txt";
    private static final String FILE_TRANSAKSI = DATA_DIR + File.separator + "transaksi.txt";

    public FileStorageService() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Memuat daftar kendaraan dari berkas penyimpanan.
     * Jika berkas belum ada, maka data sampel awal akan otomatis digenerate.
     */
    public List<Kendaraan> muatDataKendaraan() throws RentalException {
        List<Kendaraan> list = new ArrayList<>();
        File file = new File(FILE_KENDARAAN);

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

                String[] parts = line.split(";");
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
     * Menyimpan seluruh list kendaraan ke dalam file teks secara permanen.
     */
    public void simpanDataKendaraan(List<Kendaraan> list) throws RentalException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(FILE_KENDARAAN), StandardCharsets.UTF_8))) {
            for (Kendaraan k : list) {
                writer.write(k.toCsvRecord());
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RentalException("Gagal menyimpan berkas data kendaraan: " + e.getMessage(), e);
        }
    }

    /**
     * Memuat daftar pelanggan dari berkas penyimpanan.
     */
    public List<Pelanggan> muatDataPelanggan() throws RentalException {
        List<Pelanggan> list = new ArrayList<>();
        File file = new File(FILE_PELANGGAN);

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

                String[] parts = line.split(";");
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
     * Menyimpan daftar pelanggan ke berkas penyimpanan.
     */
    public void simpanDataPelanggan(List<Pelanggan> list) throws RentalException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(FILE_PELANGGAN), StandardCharsets.UTF_8))) {
            for (Pelanggan p : list) {
                writer.write(p.toCsvRecord());
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RentalException("Gagal menyimpan berkas data pelanggan: " + e.getMessage(), e);
        }
    }

    /**
     * Memuat daftar transaksi sewa dari berkas penyimpanan.
     */
    public List<TransaksiSewa> muatDataTransaksi(Map<String, Pelanggan> pelangganMap,
                                                 Map<String, Kendaraan> kendaraanMap) throws RentalException {
        List<TransaksiSewa> list = new ArrayList<>();
        File file = new File(FILE_TRANSAKSI);

        if (!file.exists()) {
            return list;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split(";");
                if (parts.length >= 9) {
                    String idTrx = parts[0];
                    String idPel = parts[1];
                    String idKen = parts[2];
                    String tgl = parts[3];
                    int durasi = Integer.parseInt(parts[4]);
                    boolean supir = Boolean.parseBoolean(parts[5]);
                    boolean asuransi = Boolean.parseBoolean(parts[6]);
                    // double totalSaved = Double.parseDouble(parts[7]);
                    String status = parts[8];
                    String catatan = (parts.length >= 10) ? parts[9] : "-";

                    Pelanggan pel = pelangganMap.get(idPel);
                    Kendaraan ken = kendaraanMap.get(idKen);

                    if (pel != null && ken != null) {
                        TransaksiSewa trx = new TransaksiSewa(idTrx, pel, ken, tgl, durasi, supir, asuransi, status, catatan);
                        list.add(trx);
                    }
                }
            }
        } catch (Exception e) {
            throw new RentalException("Gagal membaca berkas data transaksi: " + e.getMessage(), e);
        }

        return list;
    }

    /**
     * Menyimpan daftar transaksi sewa ke berkas penyimpanan.
     */
    public void simpanDataTransaksi(List<TransaksiSewa> list) throws RentalException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(FILE_TRANSAKSI), StandardCharsets.UTF_8))) {
            for (TransaksiSewa t : list) {
                writer.write(t.toCsvRecord());
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RentalException("Gagal menyimpan berkas data transaksi: " + e.getMessage(), e);
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
