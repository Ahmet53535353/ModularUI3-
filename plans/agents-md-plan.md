# AGENTS.md Oluşturma Planı

Bu depo için `AGENTS.md` yazma planı. Dil: Türkçe (komut satırları İngilizce).
Kapsam: mekanik (build/test/kurallar) + upstream lineage.

## Doğrulama ilkesi

AGENTS.md bir doküman olduğu için klasik TDD yerine **iddia doğrulama** uygulanır:
dosyaya yazılan her komut ve her gerçek, yazıldıktan sonra fiilen çalıştırılarak
test edilir. Tahmin yazılmaz — doğrulanamayan iddia atlanır.

## Sekyonlar ve gireceği doğrulanmış bilgiler

### 1. Proje
- ModularUI, MC 1.12.2 Forge için GUI kütüphane modu. Uygulama değil.
- `modId = modularui`, package `com.cleanroommc.modularui`
- Kaynak: `gradle.properties:2,9`, `mcmod.info`

### 2. Cleanroom durumu (kullanıcı notu)
- Package/group `com.cleanroommc.modularui` ve maven group `com.cleanroommc` miras;
  yeniden adlandırma
- `mcmod.info` `dependencies: []` — runtime Cleanroom bağımlılığı yok, mod saf
  Forge'da çalışır
- Tek Cleanroom artifact'ları `minecraft.gradle.kts:132-133` `runtimeOnly` dev
  shim'leri (`osxNarratorBlocker`, `strip-latest-forge-requirements`),
  `isTransitive = false`, `shadowImplementation` dışında → jar'a girmiyor
- Maven yayın `repo.cleanroommc.com` (`gradle.properties:138`)

### 3. Komutlar
- `./gradlew build`, `./gradlew test`
- Tek test: `./gradlew test --tests "com.cleanroommc.modularui.utils.MathUtilsTest"`
- `--rerun-tasks` gerekli; yoksa UP-TO-DATE olup test çıktısı görünmüyor
- **`--offline` çalışmıyor** — RFG fernflower görevi `IllegalStateException` veriyor
- `clean` `build/libs`'ı siliyor, `./gradlew build` geri üretiyor
- Dev mod anahtarları: `gradle.properties` `debug_bogo`, `debug_nea`
- Test GUI: Numpad4, `enableTestGuis` + Baubles gerekir

### 4. Toolchain tuzakları
- **İki version catalog**: `libs` (gradle/libs.versions.toml) ve `deps`
  (gradle/deps.versions.toml)
- Derleme toolchain Azul 25; test/injected-interface Azul 8
- **`options.release` yok** → Java 25 sözdizimi yazılabilir, jvmdowngrader Java 8'e
  indirir
- `jvmdowngrader-java-api` modern JDK API'lerini geri taşır; testler gerçek Java 8
  JVM'inde (`azul_systems__inc_-8`) `List.of` kullanabiliyor
- `build-logic` included build; convention plugin'ler
  `build-logic/convention/src/main/kotlin/*.gradle.kts`
- RFG `deobf()` helper: `build.gradle.kts:16-18`
- **mixinbooter bilerek 10.x** (`build.gradle.kts:20-22`)

### 5. Test kuralları
- Testler Java 8 JVM'inde çalışır, sınıflar `downgradeTest` çıktısından
- **Font renderer'a dokunan test `NoClassDefFoundError: org/lwjgl/LWJGLException`**
  (`TextRenderer.getFontRenderer()`)
- Çözüm: `handler.setRenderer(null)` + `handler.setScrollArea(null)`
- `src/test/.../Bootstrap.java` hiçbir test tarafından çağrılmıyor
- Test classpath'ine patched MC + launcher ekleniyor (`test.gradle.kts:9`)

### 6. Repo kuralları
- `build/` gitignore'lu (`.gitignore:24`)
- `.editorconfig` `crlf` diyor ama dosyalar LF, `.gitattributes` yok → dosyalara güven
- `enableSpotless = false`; lint/typecheck görevi yok, doğrulama `build`/`check`
- Java satır uzunluğu 140

### 7. Mimari (görünmeyen kısım)
- Mixinbooter early mixin'ler + `LateMixins` geç mixin'leri (jei/evalex, mod
  yüklüyse) + coremod `core.ModularUICore` + `ClassTransformer` (ASM)
- Refmap: `build/tmp/mixins/mixins.modularui.refmap.json`
- `generateTags` BuildConfig → `Tags` sınıfı
- `mcmod.info` / `pack.mcmeta` / mixin json Gradle şablonları, `${mod_version}`
  token'ları genişletiliyor (`minecraft.gradle.kts:86-112`)

### 8. Upstream lineage
- `CleanroomMC/ModularUI` arşiv; `SymmetricDevs/ModularUI` ~10 commit ileride;
  bu repo ondan çatallanmış
- `GTNewHorizons/ModularUI2` = 1.7.10 çatalı, hâlâ bakımda, düzeltme kaynağı
- Alınanlar `plans/gtnh-backport-plan.md`'de kayıtlı
- Port tuzakları: evalex harici dependency; 1.12.2 `IIcon` değil
  `TextureAtlasSprite`; `int mode` değil `ClickType`
- Yayınlanan dosya `reobfJar` çıktısı (girdisi `shadeDowngradedApi`);
  `build_and_publish.yaml` manuel `workflow_dispatch`

## Yazmadan önce doğrulanacak iki belirsizlik

1. Run görevlerinin adları. RFG jar'ından `runVanillaClient`/`runVanillaServer`
   göründü, `runClient`/`runServer` doğrulanamadı. `./gradlew tasks` ile teyit
   edilecek; doğrulanamayan ad yazılmayacak.
2. `reobfJar` çıktısının tam dosya adı. `build/libs` `clean` ile silinmişti;
   `./gradlew build` sonrası teyit edilecek.

## Uygulama sonrası test

AGENTS.md'ye yazılan her komut fiilen çalıştırılacak:
- `./gradlew test --tests ...` tekil test
- `--rerun-tasks` ile tam test
- `./gradlew build`
- belgelenen run görevleri (`./gradlew tasks` ile listelenip)
