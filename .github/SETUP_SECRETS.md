# 🔐 Setup GitHub Secrets

Panduan untuk mengkonfigurasi GitHub Actions agar bisa build dan kirim APK ke Telegram **dengan aman**.

## 📋 Secrets yang Diperlukan

Buka **Settings → Secrets and variables → Actions → New repository secret** di repository GitHub kamu.

### 1. Telegram Credentials

| Secret Name | Deskripsi | Contoh |
|-------------|-----------|--------|
| `TELEGRAM_BOT_TOKEN` | Token dari @BotFather | `1234567890:AAH...` |
| `TELEGRAM_CHAT_ID` | ID grup/channel Telegram | `-1001234567890` |

### 2. Keystore Credentials

| Secret Name | Deskripsi |
|-------------|-----------|
| `KEYSTORE_BASE64` | Keystore dalam format base64 |
| `KEYSTORE_PASSWORD` | Password keystore |
| `KEY_ALIAS` | Alias key dalam keystore |
| `KEY_PASSWORD` | Password untuk key alias |

---

## 🔧 Cara Mendapatkan KEYSTORE_BASE64

Jalankan perintah ini di terminal/cmd untuk mengkonversi keystore ke base64:

### Windows (PowerShell):
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("path\to\your\keystore.jks")) | Out-File keystore_base64.txt
```

### Windows (CMD):
```cmd
certutil -encode path\to\your\keystore.jks keystore_base64.txt
```

### Linux/Mac:
```bash
base64 -i path/to/your/keystore.jks > keystore_base64.txt
```

Lalu copy isi file `keystore_base64.txt` dan paste sebagai value secret `KEYSTORE_BASE64`.

**⚠️ PENTING:** Hapus file `keystore_base64.txt` setelah selesai!

---

## 📌 Mendapatkan Telegram Chat ID

1. Tambahkan bot ke grup Telegram
2. Kirim pesan di grup tersebut
3. Buka URL berikut di browser (ganti TOKEN dengan token bot kamu):
   ```
   https://api.telegram.org/botTOKEN/getUpdates
   ```
4. Cari `"chat":{"id":-XXXXXXXXXX}` - itulah Chat ID kamu

---

## ✅ Checklist

- [ ] `TELEGRAM_BOT_TOKEN` sudah ditambahkan
- [ ] `TELEGRAM_CHAT_ID` sudah ditambahkan  
- [ ] `KEYSTORE_BASE64` sudah ditambahkan
- [ ] `KEYSTORE_PASSWORD` sudah ditambahkan
- [ ] `KEY_ALIAS` sudah ditambahkan
- [ ] `KEY_PASSWORD` sudah ditambahkan

---

## 🚀 Test Workflow

Setelah semua secrets dikonfigurasi:

1. Push commit ke branch `main` atau `master`
2. Atau buka tab **Actions** → **Build & Release to Telegram** → **Run workflow**

Workflow akan:
1. Build APK release dengan signing
2. Upload APK ke Telegram grup
3. Kirim notifikasi status build

---

## 🔒 Keamanan

- ✅ Semua credentials disimpan sebagai GitHub Secrets (terenkripsi)
- ✅ Tidak ada API key/token yang muncul di log
- ✅ Keystore tidak pernah dicommit ke repository
- ✅ File sensitif sudah ditambahkan ke `.gitignore`

---

## ❓ Troubleshooting

### Build gagal: "Keystore not found"
- Pastikan `KEYSTORE_BASE64` sudah benar
- Coba encode ulang keystore dengan format yang tepat

### Telegram upload gagal
- Pastikan bot sudah dijadikan admin di grup
- Verifikasi `TELEGRAM_CHAT_ID` sudah benar (harus negatif untuk grup)

### Permission denied
- Pastikan semua secrets sudah dikonfigurasi
- Check typo pada nama secret
