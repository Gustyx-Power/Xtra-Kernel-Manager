#!/bin/bash

# --- Membersihkan Proyek (Langkah Awal Anda) ---
echo "Membersihkan cache build..."
rm -rf app/build/
rm -rf build/
rm -rf .gradle/

echo "Membersihkan cache Gradle..."
./gradlew clean
echo "Pembersihan selesai."
echo ""

# --- Set Keystore Path ---
KEYSTORE_PATH="/home/gustyxpower/Documents/Project/XMS/Keystore/Keystore-XKM/xkm-release-key.jks"

# Validasi sederhana untuk memeriksa apakah file ada
if [ ! -f "$KEYSTORE_PATH" ]; then
    echo "Error: File keystore tidak ditemukan di '$KEYSTORE_PATH'"
    exit 1
fi

# --- Meminta Informasi Keystore Secara Interaktif ---
echo "Silakan masukkan detail keystore untuk menandatangani aplikasi:"

# 1. Menampilkan path keystore yang digunakan
echo "Menggunakan keystore di: $KEYSTORE_PATH"

# 2. Meminta alias key
read -p "Masukkan alias key Anda: " KEY_ALIAS

# 3. Meminta password keystore (input disembunyikan)
read -s -p "Masukkan password keystore Anda: " KEYSTORE_PASSWORD
echo "" # Pindah ke baris baru setelah input password

# 4. Meminta password key alias (input disembunyikan)
read -s -p "Masukkan password untuk alias '$KEY_ALIAS': " KEY_PASSWORD
echo "" # Pindah ke baris baru setelah input password
echo ""

echo "Masukkan changelog untuk rilis ini (tekan Ctrl+D setelah selesai):"
CHANGELOG=$(cat)
echo ""

# --- Menjalankan Build Gradle dengan Properti ---
echo "Memulai build release dengan informasi keystore dan Changelog yang diberikan..."

# Menjalankan gradlew dengan meneruskan variabel sebagai properti (-P)
./gradlew buildAndPublish \
    -PmyKeystorePath="$KEYSTORE_PATH" \
    -PmyKeystorePassword="$KEYSTORE_PASSWORD" \
    -PmyKeyAlias="$KEY_ALIAS" \
    -PmyKeyPassword="$KEY_PASSWORD" \
    -PmyChangelog="$CHANGELOG"

# Cek status build
if [ $? -eq 0 ]; then
    echo "✅ Build release selesai!"
    echo "Anda bisa menemukan APK di folder: app/build/outputs/apk/release/"
else
    echo "❌ Build gagal. Silakan periksa log di atas."
    exit 1
fi