#!/bin/bash

echo "Mendeteksi perangkat Android yang terhubung..."
DEVICES=($(adb devices | awk 'NR>1 && $2=="device" {print $1}'))
COUNT=${#DEVICES[@]}

if [ $COUNT -eq 0 ]; then
    echo "Tidak ada perangkat yang terhubung via USB. Build akan tetap dijalankan tanpa instalasi ke perangkat."
    SKIP_INSTALL=1
else
    # Ambil info model dan versi Android untuk setiap device
    declare -a DEVICE_INFOS
    for SERIAL in "${DEVICES[@]}"; do
        MODEL=$(adb -s "$SERIAL" shell getprop ro.product.model | tr -d '\r')
        VERSION=$(adb -s "$SERIAL" shell getprop ro.build.version.release | tr -d '\r')
        DEVICE_INFOS+=("$SERIAL | Model: $MODEL | Android: $VERSION")
    done

    if [ $COUNT -eq 1 ]; then
        SELECTED_DEVICE=${DEVICES[0]}
        echo "Perangkat terdeteksi: ${DEVICE_INFOS[0]}"
    else
        echo "Beberapa perangkat terdeteksi:"
        for i in "${!DEVICE_INFOS[@]}"; do
            echo "$((i+1)). ${DEVICE_INFOS[$i]}"
        done
        read -p "Pilih perangkat (1-$COUNT): " IDX
        IDX=$((IDX-1))
        if [ $IDX -ge 0 ] && [ $IDX -lt $COUNT ]; then
            SELECTED_DEVICE=${DEVICES[$IDX]}
            echo "Perangkat dipilih: ${DEVICE_INFOS[$IDX]}"
        else
            echo "Pilihan tidak valid. Build akan tetap dijalankan tanpa instalasi ke perangkat."
            SKIP_INSTALL=1
        fi
    fi
fi

if [ -z "$SKIP_INSTALL" ]; then
    export ANDROID_SERIAL=$SELECTED_DEVICE
fi

echo "Membersihkan cache build..."
rm -rf app/build/
rm -rf build/
rm -rf .gradle/

echo "Membersihkan cache Gradle..."
./gradlew clean

echo "Melakukan build debug yang bersih..."
./gradlew assembleDebug --parallel --max-workers=$(nproc)

APK_PATH=$(find app/build/outputs/apk/debug -name '*.apk' | head -n 1)
if [ -f "$APK_PATH" ]; then
    if [ -z "$SKIP_INSTALL" ]; then
        echo "Menginstall APK ke perangkat $SELECTED_DEVICE..."
        adb -s $SELECTED_DEVICE install -r "$APK_PATH"
        echo "APK berhasil diinstall."
    else
        echo "Build selesai. Tidak ada perangkat yang terhubung, APK tidak diinstall."
    fi
else
    echo "APK tidak ditemukan. Build mungkin gagal."
fi

echo "Build debug selesai!"