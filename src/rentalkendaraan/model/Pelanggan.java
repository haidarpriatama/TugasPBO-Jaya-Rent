package rentalkendaraan.model;

import rentalkendaraan.exception.DataValidasiException;

/**
 * Kelas entitas data Pelanggan rental kendaraan.
 * Menerapkan prinsip Enkapsulasi penuh dengan validasi data pada setiap setter.
 */
public class Pelanggan {
    private String idPelanggan;
    private String nama;
    private String nik;
    private String nomorTelepon;
    private String alamat;
    private String jenisKelamin; // "Laki-laki" atau "Perempuan"

    public Pelanggan(String idPelanggan, String nama, String nik,
                     String nomorTelepon, String alamat, String jenisKelamin) throws DataValidasiException {
        setIdPelanggan(idPelanggan);
        setNama(nama);
        setNik(nik);
        setNomorTelepon(nomorTelepon);
        setAlamat(alamat);
        setJenisKelamin(jenisKelamin);
    }

    // --- GETTER & SETTER DENGAN VALIDASI ENKAPSULASI ---
    public String getIdPelanggan() {
        return idPelanggan;
    }

    public void setIdPelanggan(String idPelanggan) throws DataValidasiException {
        if (idPelanggan == null || idPelanggan.trim().isEmpty()) {
            throw new DataValidasiException("ID Pelanggan tidak boleh kosong.");
        }
        this.idPelanggan = idPelanggan.trim().toUpperCase();
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) throws DataValidasiException {
        if (nama == null || nama.trim().isEmpty()) {
            throw new DataValidasiException("Nama pelanggan tidak boleh kosong.");
        }
        if (nama.trim().length() < 2) {
            throw new DataValidasiException("Nama pelanggan minimal harus 2 karakter.");
        }
        this.nama = nama.trim();
    }

    public String getNik() {
        return nik;
    }

    public void setNik(String nik) throws DataValidasiException {
        if (nik == null || nik.trim().isEmpty()) {
            throw new DataValidasiException("NIK pelanggan tidak boleh kosong.");
        }
        String cleanNik = nik.trim();
        if (!cleanNik.matches("\\d{16}")) {
            throw new DataValidasiException("NIK harus tepat terdiri dari 16 digit angka.");
        }
        this.nik = cleanNik;
    }

    public String getNomorTelepon() {
        return nomorTelepon;
    }

    public void setNomorTelepon(String nomorTelepon) throws DataValidasiException {
        if (nomorTelepon == null || nomorTelepon.trim().isEmpty()) {
            throw new DataValidasiException("Nomor telepon tidak boleh kosong.");
        }
        String cleanPhone = nomorTelepon.trim().replaceAll("[^0-9+]", "");
        if (cleanPhone.length() < 9 || cleanPhone.length() > 15) {
            throw new DataValidasiException("Nomor telepon tidak valid (panjang 9-15 digit).");
        }
        this.nomorTelepon = cleanPhone;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) throws DataValidasiException {
        if (alamat == null || alamat.trim().isEmpty()) {
            throw new DataValidasiException("Alamat tempat tinggal tidak boleh kosong.");
        }
        this.alamat = alamat.trim();
    }

    public String getJenisKelamin() {
        return jenisKelamin;
    }

    public void setJenisKelamin(String jenisKelamin) throws DataValidasiException {
        if (jenisKelamin == null || (!jenisKelamin.equalsIgnoreCase("Laki-laki") && !jenisKelamin.equalsIgnoreCase("Perempuan"))) {
            throw new DataValidasiException("Jenis kelamin harus 'Laki-laki' atau 'Perempuan'.");
        }
        this.jenisKelamin = jenisKelamin.trim();
    }

    @Override
    public String toString() {
        return "[" + idPelanggan + "] " + nama + " (" + nomorTelepon + ")";
    }

    public String toCsvRecord() {
        return String.join(";",
                idPelanggan,
                nama,
                nik,
                nomorTelepon,
                alamat.replace(";", ",").replace("\n", " "),
                jenisKelamin
        );
    }
}
