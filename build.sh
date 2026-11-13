#!/bin/sh
# wxpoc 构建: aapt2(二制清单) + javac17 + d8(dex) + zip + apksigner(v1+v2)
# 工具链: /opt/data/cache/scratch/android-tools/android-16 (Google build-tools r36)
set -e
cd "$(dirname "$0")"

T=/opt/data/cache/scratch/android-tools/android-16
STUBS=/opt/data/cache/scratch/android-tools/android-34/android.jar
J=/opt/data/cache/scratch/jdk/jdk-17.0.9+9/bin
export PATH="$J:$PATH"
JAVA="$J/java"; JAVAC="$J/javac"; KEYTOOL="$J/keytool"
OUT=build
rm -rf "$OUT" && mkdir -p "$OUT"

# 1) 二制清单 + 空壳 apk(无 res, 对 API34 框架链接)
"$T/aapt2" link -I "$STUBS" --manifest AndroidManifest.xml -o "$OUT/base.apk"
cd "$OUT"

# 2) 编译(桩 + 模块, 对 API34 桩 jar; 出错即停, 不吞残留)
"$JAVAC" -source 8 -target 8 -cp "$STUBS" \
    $(find ../src -name '*.java') -d classes || { echo "javac 失败"; exit 1; }
NF=$(find classes -name '*.class' | wc -l)
[ "$NF" -ge 10 ] || { echo "class 数不足: $NF"; exit 1; }

# 3) dex
mkdir -p dexdir
"$T/d8" --release --lib "$STUBS" $(find classes -name '*.class') --output dexdir
mv dexdir/classes.dex .

# 4) 打包(host 无 zip, 用 JDK jar 工具; 当前在 $OUT)
"$J/jar" uf base.apk classes.dex
cd ..
"$J/jar" uf build/base.apk assets

# 5) 签名
[ -f "$T/poc.keystore" ] || "$KEYTOOL" -genkeypair -keystore "$T/poc.keystore" \
    -storepass wxpoc99 -keypass wxpoc99 -alias poc -keyalg RSA -keysize 2048 \
    -validity 36500 -dname "CN=wxpoc, OU=chuxin, O=chuxin"
"$T/apksigner" sign --ks "$T/poc.keystore" --ks-pass pass:wxpoc99 --key-pass pass:wxpoc99 \
    --v1-signing-enabled true --v2-signing-enabled true \
    --out wxpoc.apk "$OUT/base.apk"
"$T/apksigner" verify --print-certs wxpoc.apk | head -3
echo "== SIGNED: $(pwd)/wxpoc.apk ($(stat -c%s wxpoc.apk) bytes)"
