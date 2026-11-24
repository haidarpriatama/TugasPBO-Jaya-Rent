package rentalkendaraan.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import rentalkendaraan.exception.DataValidasiException;
import rentalkendaraan.exception.KendaraanTidakTersediaException;
import rentalkendaraan.exception.RentalException;
import rentalkendaraan.model.*;

/**
 * Controller / Business Logic Manager untuk sistem Rental Kendaraan.
 * Mengelola Collection Generics (List dan Map), operasi CRUD, aturan bisnis,
 * serta persistensi data via FileStorageService.
 */
public class RentalManager {
    private final List<Kendaraan> daftarKendaraan;
    private final List<Pelanggan> daftarPelanggan;
    private final List<TransaksiSewa> daftarTransaksi;

    // Koleksi Generics Map untuk pencarian O(1) berdasarkan ID (selalu dinormalisasi uppercase)
    private final Map<String, Kendaraan> kendaraanMap;
    private final Map<String, Pelanggan> pelangganMap;
    private final Map<String, TransaksiSewa> transaksiMap;

    private final FileStorageService storageService;

    public RentalManager() {
        this(new FileStorageService());
    }

    public RentalManager(FileStorageService storageService) {
        this.daftarKendaraan = new ArrayList<>();
        this.daftarPelanggan = new ArrayList<>();
        this.daftarTransaksi = new ArrayList<>();

        this.kendaraanMap = new HashMap<>();
        this.pelangganMap = new HashMap<>();
        this.transaksiMap = new HashMap<>();

        this.storageService = (storageService != null) ? storageService : new FileStorageService();
        muatSemuaData();
    }

    private void muatSemuaData() {
        try {
            List<Kendaraan> kList = storageService.muatDataKendaraan();
            for (Kendaraan k : kList) {
                daftarKendaraan.add(k);
                kendaraanMap.put(k.getIdKendaraan().trim().toUpperCase(), k);
            }

            List<Pelanggan> pList = storageService.muatDataPelanggan();
            for (Pelanggan p : pList) {
                daftarPelanggan.add(p);
                pelangganMap.put(p.getIdPelanggan().trim().toUpperCase(), p);
            }

            List<TransaksiSewa> tList = storageService.muatDataTransaksi(pelangganMap, kendaraanMap);
            for (TransaksiSewa t : tList) {
                daftarTransaksi.add(t);
                transaksiMap.put(t.getIdTransaksi().trim().toUpperCase(), t);
                // Sinkronisasi status ketersediaan kendaraan jika masih ada transaksi AKTIF
                if ("AKTIF".equalsIgnoreCase(t.getStatusTransaksi()) && t.getKendaraan() != null) {
                    t.getKendaraan().setStatusTersedia(false);
                }
            }
        } catch (RentalException e) {
            System.err.println("Peringatan saat memuat data: " + e.getMessage());
        }
    }

    // =========================================================================
    // CRUD KENDARAAN
    // =========================================================================
    public List<Kendaraan> getDaftarKendaraan() {
        return Collections.unmodifiableList(daftarKendaraan);
    }

    public Kendaraan cariKendaraanById(String id) {
        if (id == null) return null;
        return kendaraanMap.get(id.trim().toUpperCase());
    }

    public String generateNextIdKendaraan() {
        int max = 0;
        for (Kendaraan k : daftarKendaraan) {
            String id = k.getIdKendaraan();
            if (id.startsWith("KND-")) {
                try {
                    int num = Integer.parseInt(id.substring(4));
                    if (num > max) max = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("KND-%03d", max + 1);
    }

    public void tambahKendaraan(Kendaraan k) throws RentalException {
        if (k == null) throw new DataValidasiException("Data kendaraan tidak boleh null.");
        // Cek duplikasi nomor plat & ID
        for (Kendaraan existing : daftarKendaraan) {
            if (existing.getNomorPlat().equalsIgnoreCase(k.getNomorPlat())) {
                throw new DataValidasiException("Nomor plat [" + k.getNomorPlat() + "] sudah terdaftar!");
            }
            if (existing.getIdKendaraan().equalsIgnoreCase(k.getIdKendaraan())) {
                throw new DataValidasiException("ID Kendaraan [" + k.getIdKendaraan() + "] sudah ada!");
            }
        }
        daftarKendaraan.add(k);
        kendaraanMap.put(k.getIdKendaraan().trim().toUpperCase(), k);
        storageService.simpanDataKendaraan(daftarKendaraan);
    }

    public void updateKendaraan(Kendaraan kBaru) throws RentalException {
        if (kBaru == null) throw new DataValidasiException("Data kendaraan tidak boleh null.");
        Kendaraan target = kendaraanMap.get(kBaru.getIdKendaraan().trim().toUpperCase());
        if (target == null) {
            throw new DataValidasiException("Kendaraan dengan ID " + kBaru.getIdKendaraan() + " tidak ditemukan.");
        }

        // Cek apakah nomor plat bentrok dengan armada lain
        for (Kendaraan other : daftarKendaraan) {
            if (!other.getIdKendaraan().equalsIgnoreCase(kBaru.getIdKendaraan())
                    && other.getNomorPlat().equalsIgnoreCase(kBaru.getNomorPlat())) {
                throw new DataValidasiException("Nomor plat [" + kBaru.getNomorPlat() + "] digunakan armada lain!");
            }
        }

        int index = daftarKendaraan.indexOf(target);
        daftarKendaraan.set(index, kBaru);
        kendaraanMap.put(kBaru.getIdKendaraan().trim().toUpperCase(), kBaru);
        storageService.simpanDataKendaraan(daftarKendaraan);
    }

    public void hapusKendaraan(String idKendaraan) throws RentalException {
        if (idKendaraan == null || idKendaraan.trim().isEmpty()) {
            throw new DataValidasiException("ID Kendaraan tidak boleh kosong.");
        }
        Kendaraan k = kendaraanMap.get(idKendaraan.trim().toUpperCase());
        if (k == null) {
            throw new DataValidasiException("Kendaraan tidak ditemukan.");
        }
        // Cek apakah sedang digunakan dalam transaksi aktif
        for (TransaksiSewa t : daftarTransaksi) {
            if (t.getKendaraan().getIdKendaraan().equalsIgnoreCase(idKendaraan)
                    && "AKTIF".equalsIgnoreCase(t.getStatusTransaksi())) {
                throw new DataValidasiException("Kendaraan sedang disewa dalam transaksi aktif ("
                        + t.getIdTransaksi() + ") dan tidak dapat dihapus!");
            }
        }
        daftarKendaraan.remove(k);
        kendaraanMap.remove(idKendaraan.trim().toUpperCase());
        storageService.simpanDataKendaraan(daftarKendaraan);
    }

    public List<Kendaraan> cariKendaraan(String kataKunci) {
        if (kataKunci == null || kataKunci.trim().isEmpty()) {
            return getDaftarKendaraan();
        }
        String kw = kataKunci.trim().toLowerCase();
        return daftarKendaraan.stream()
                .filter(k -> k.getIdKendaraan().toLowerCase().contains(kw)
                        || k.getNomorPlat().toLowerCase().contains(kw)
                        || k.getMerk().toLowerCase().contains(kw)
                        || k.getModel().toLowerCase().contains(kw)
                        || k.getJenisKendaraan().toLowerCase().contains(kw))
                .collect(Collectors.toList());
    }

    public List<Kendaraan> filterKendaraan(String jenis, Boolean hanyaTersedia) {
        return daftarKendaraan.stream()
                .filter(k -> jenis == null || jenis.equalsIgnoreCase("SEMUA") || k.getJenisKendaraan().equalsIgnoreCase(jenis))
                .filter(k -> hanyaTersedia == null || (k.isStatusTersedia() == hanyaTersedia))
                .collect(Collectors.toList());
    }

    // =========================================================================
    // CRUD PELANGGAN
    // =========================================================================
    public List<Pelanggan> getDaftarPelanggan() {
        return Collections.unmodifiableList(daftarPelanggan);
    }

    public Pelanggan cariPelangganById(String id) {
        if (id == null) return null;
        return pelangganMap.get(id.trim().toUpperCase());
    }

    public String generateNextIdPelanggan() {
        int max = 0;
        for (Pelanggan p : daftarPelanggan) {
            String id = p.getIdPelanggan();
            if (id.startsWith("PLG-")) {
                try {
                    int num = Integer.parseInt(id.substring(4));
                    if (num > max) max = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("PLG-%03d", max + 1);
    }

    public void tambahPelanggan(Pelanggan p) throws RentalException {
        if (p == null) throw new DataValidasiException("Data pelanggan tidak boleh null.");
        for (Pelanggan existing : daftarPelanggan) {
            if (existing.getIdPelanggan().equalsIgnoreCase(p.getIdPelanggan())) {
                throw new DataValidasiException("ID Pelanggan [" + p.getIdPelanggan() + "] sudah ada.");
            }
            if (existing.getNik().equalsIgnoreCase(p.getNik())) {
                throw new DataValidasiException("NIK [" + p.getNik() + "] sudah terdaftar atas nama: " + existing.getNama());
            }
        }
        daftarPelanggan.add(p);
        pelangganMap.put(p.getIdPelanggan().trim().toUpperCase(), p);
        storageService.simpanDataPelanggan(daftarPelanggan);
    }

    public void updatePelanggan(Pelanggan pBaru) throws RentalException {
        if (pBaru == null) throw new DataValidasiException("Data pelanggan tidak boleh null.");
        Pelanggan target = pelangganMap.get(pBaru.getIdPelanggan().trim().toUpperCase());
        if (target == null) {
            throw new DataValidasiException("Pelanggan tidak ditemukan.");
        }
        for (Pelanggan other : daftarPelanggan) {
            if (!other.getIdPelanggan().equalsIgnoreCase(pBaru.getIdPelanggan())
                    && other.getNik().equalsIgnoreCase(pBaru.getNik())) {
                throw new DataValidasiException("NIK [" + pBaru.getNik() + "] telah digunakan pelanggan lain!");
            }
        }
        int idx = daftarPelanggan.indexOf(target);
        daftarPelanggan.set(idx, pBaru);
        pelangganMap.put(pBaru.getIdPelanggan().trim().toUpperCase(), pBaru);
        storageService.simpanDataPelanggan(daftarPelanggan);
    }

    public void hapusPelanggan(String idPelanggan) throws RentalException {
        if (idPelanggan == null || idPelanggan.trim().isEmpty()) {
            throw new DataValidasiException("ID Pelanggan tidak boleh kosong.");
        }
        Pelanggan p = pelangganMap.get(idPelanggan.trim().toUpperCase());
        if (p == null) {
            throw new DataValidasiException("Pelanggan tidak ditemukan.");
        }
        // Cek apakah ada riwayat transaksi aktif
        for (TransaksiSewa t : daftarTransaksi) {
            if (t.getPelanggan().getIdPelanggan().equalsIgnoreCase(idPelanggan)
                    && "AKTIF".equalsIgnoreCase(t.getStatusTransaksi())) {
                throw new DataValidasiException("Pelanggan sedang memiliki transaksi aktif ("
                        + t.getIdTransaksi() + ") dan tidak dapat dihapus!");
            }
        }
        daftarPelanggan.remove(p);
        pelangganMap.remove(idPelanggan.trim().toUpperCase());
        storageService.simpanDataPelanggan(daftarPelanggan);
    }

    public List<Pelanggan> cariPelanggan(String kataKunci) {
        if (kataKunci == null || kataKunci.trim().isEmpty()) {
            return getDaftarPelanggan();
        }
        String kw = kataKunci.trim().toLowerCase();
        return daftarPelanggan.stream()
                .filter(p -> p.getIdPelanggan().toLowerCase().contains(kw)
                        || p.getNama().toLowerCase().contains(kw)
                        || p.getNik().toLowerCase().contains(kw)
                        || p.getNomorTelepon().toLowerCase().contains(kw)
                        || p.getAlamat().toLowerCase().contains(kw))
                .collect(Collectors.toList());
    }

    // =========================================================================
    // TRANSAKSI PENYEWAAN & PENGEMBALIAN
    // =========================================================================
    public List<TransaksiSewa> getDaftarTransaksi() {
        return Collections.unmodifiableList(daftarTransaksi);
    }

    public TransaksiSewa cariTransaksiById(String id) {
        if (id == null) return null;
        return transaksiMap.get(id.trim().toUpperCase());
    }

    public String generateNextIdTransaksi() {
        int max = 1000;
        for (TransaksiSewa t : daftarTransaksi) {
            String id = t.getIdTransaksi();
            if (id.startsWith("TRX-")) {
                try {
                    int num = Integer.parseInt(id.substring(4));
                    if (num > max) max = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("TRX-%04d", max + 1);
    }

    public TransaksiSewa prosesSewa(Pelanggan p, Kendaraan k, String tglSewa, int durasi,
                                    boolean supir, boolean asuransi, String catatan)
            throws RentalException {
        if (p == null) throw new DataValidasiException("Pelanggan harus dipilih.");
        if (k == null) throw new DataValidasiException("Kendaraan harus dipilih.");
        if (!k.isStatusTersedia()) {
            throw new KendaraanTidakTersediaException("Kendaraan [" + k.getNomorPlat() + "] sedang tidak tersedia!");
        }

        String nextId = generateNextIdTransaksi();
        TransaksiSewa trx = new TransaksiSewa(nextId, p, k, tglSewa, durasi, supir, asuransi, "AKTIF", catatan);

        // Ubah status ketersediaan armada
        k.setStatusTersedia(false);

        daftarTransaksi.add(0, trx); // letakkan di paling atas
        transaksiMap.put(trx.getIdTransaksi().trim().toUpperCase(), trx);

        // Persist perubahan secara konsisten dengan rollback in-memory jika gagal
        try {
            storageService.simpanTransaksiDanKendaraan(daftarKendaraan, daftarTransaksi);
        } catch (RentalException e) {
            k.setStatusTersedia(true);
            daftarTransaksi.remove(trx);
            transaksiMap.remove(trx.getIdTransaksi().trim().toUpperCase());
            throw e;
        }

        return trx;
    }

    public TransaksiSewa prosesPengembalian(String idTransaksi, String tanggalKembaliAktual) throws RentalException {
        if (idTransaksi == null || idTransaksi.trim().isEmpty()) {
            throw new DataValidasiException("ID Transaksi tidak boleh kosong.");
        }
        TransaksiSewa trx = transaksiMap.get(idTransaksi.trim().toUpperCase());
        if (trx == null) {
            throw new DataValidasiException("Transaksi dengan ID [" + idTransaksi + "] tidak ditemukan.");
        }
        if ("SELESAI".equalsIgnoreCase(trx.getStatusTransaksi())) {
            throw new DataValidasiException("Transaksi [" + idTransaksi + "] sudah berstatus SELESAI dan tidak dapat diselesaikan kembali.");
        }
        if ("DIBATALKAN".equalsIgnoreCase(trx.getStatusTransaksi())) {
            throw new DataValidasiException("Transaksi [" + idTransaksi + "] sudah berstatus DIBATALKAN dan tidak dapat diproses pengembaliannya.");
        }

        // Snapshot in-memory state sebelum mutasi
        String statusLama = trx.getStatusTransaksi();
        String tglKembaliLama = trx.getTanggalKembaliAktual();
        int hariTerlambatLama = trx.getHariTerlambat();
        double dendaLama = trx.getBiayaDenda();
        boolean statusArmadaLama = (trx.getKendaraan() != null) && trx.getKendaraan().isStatusTersedia();

        // Panggil pengembalian model untuk menghitung denda dan keterlambatan secara polimorfis
        trx.selesaikanPengembalian(tanggalKembaliAktual);

        // Pastikan armada berstatus tersedia
        if (trx.getKendaraan() != null) {
            trx.getKendaraan().setStatusTersedia(true);
        }

        // Simpan kedua berkas secara transactional aman dengan rollback
        try {
            storageService.simpanTransaksiDanKendaraan(daftarKendaraan, daftarTransaksi);
        } catch (RentalException e) {
            // Rollback objek in-memory ke kondisi semula jika penyimpanan gagal
            trx.setStatusTransaksi(statusLama);
            try {
                trx.setTanggalKembaliAktual(tglKembaliLama);
                trx.setHariTerlambat(hariTerlambatLama);
                trx.setBiayaDenda(dendaLama);
            } catch (DataValidasiException ignored) {}
            if (trx.getKendaraan() != null) {
                trx.getKendaraan().setStatusTersedia(statusArmadaLama);
            }
            throw e;
        }

        return trx;
    }

    public TransaksiSewa prosesPengembalian(String idTransaksi) throws RentalException {
        return prosesPengembalian(idTransaksi, LocalDate.now().format(DateTimeFormatter.ofPattern("uuuu-MM-dd")));
    }

    public void batalkanTransaksi(String idTransaksi) throws RentalException {
        if (idTransaksi == null || idTransaksi.trim().isEmpty()) {
            throw new DataValidasiException("ID Transaksi tidak boleh kosong.");
        }
        TransaksiSewa trx = transaksiMap.get(idTransaksi.trim().toUpperCase());
        if (trx == null) {
            throw new DataValidasiException("Transaksi tidak ditemukan.");
        }
        if ("SELESAI".equalsIgnoreCase(trx.getStatusTransaksi())) {
            throw new DataValidasiException("Transaksi yang telah selesai tidak dapat dibatalkan.");
        }
        if ("DIBATALKAN".equalsIgnoreCase(trx.getStatusTransaksi())) {
            throw new DataValidasiException("Transaksi sudah dibatalkan sebelumnya.");
        }

        String statusLama = trx.getStatusTransaksi();
        boolean statusArmadaLama = (trx.getKendaraan() != null) && trx.getKendaraan().isStatusTersedia();

        trx.setStatusTransaksi("DIBATALKAN");
        if (trx.getKendaraan() != null) {
            trx.getKendaraan().setStatusTersedia(true);
        }

        try {
            storageService.simpanTransaksiDanKendaraan(daftarKendaraan, daftarTransaksi);
        } catch (RentalException e) {
            trx.setStatusTransaksi(statusLama);
            if (trx.getKendaraan() != null) {
                trx.getKendaraan().setStatusTersedia(statusArmadaLama);
            }
            throw e;
        }
    }

    public List<Kendaraan> getKendaraanTersedia() {
        return filterKendaraan("SEMUA", true);
    }
}
