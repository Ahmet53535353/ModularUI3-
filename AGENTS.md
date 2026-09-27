# AGENTS.md

ModularUI — Minecraft 1.12.2 / Forge için bir GUI **kütüphane** modu. Uygulama değil;
`modId = modularui`, package `com.cleanroommc.modularui`.

## Cleanroom durumu (en çok yanlış anlaşılan konu)

- Package ve maven group (`com.cleanroommc.modularui`, `com.cleanroommc`) **miras**dır.
  Yeniden adlandırma, taşıma.
- `mcmod.info` içinde `dependencies: []` — **runtime Cleanroom bağımlılığı yok**.
  Mod saf Forge'da çalışır, Cleanroom gerekmez.
- Depodaki tek Cleanroom artifact'ları `build-logic/convention/src/main/kotlin/minecraft.gradle.kts`
  içinde `runtimeOnly` dev shim'leri (`osxNarratorBlocker`, `strip-latest-forge-requirements`).
  `isTransitive = false` ve `shadowImplementation` configuration'ı dışında oldukları
  için hiçbir zaman jar'a girmiyorlar — yalnızca geliştirme classpath'inde "kütüphane
  olarak" yükleniyorlar.
- Maven yayın `repo.cleanroommc.com` (`gradle.properties`).

Yeni bir şey eklerken Cleanroom'a runtime bağımlılığı **eklemeyin**.

## Komutlar

```bash
./gradlew build                                          # tam build
./gradlew test                                           # tüm testler
./gradlew test --tests "com.cleanroommc.modularui.utils.MathUtilsTest"   # tek test
./gradlew test --rerun-tasks                             # UP-TO-DATE'i atla, testleri gerçekten çalıştır
./gradlew tasks --all | grep '^run'                      # run görevlerini listele
```

- **`--offline` çalışmıyor.** RFG'nin fernflower indirme görevi offline modda
  `IllegalStateException` fırlatıp build'i düşürüyor. Kullanmayın.
- **`--rerun-tasks` olmadan test çıktısı boş görünür** — görev UP-TO-DATE olur.
- `clean` `build/libs`'taki tüm jar'ları siler; `./gradlew build` ile geri üretilir.
- Run görevleri: `runClient`, `runServer` (deobf) · `runObfClient`, `runObfServer`
  (Forge obfuscated) · `runVanillaClient`, `runVanillaServer` (modsuz, hata ayıklamak için)
  · `runClientModernJava`, `runServerModernJava` (Java 25 + lwjgl3ify).
- Oyun içi test GUI'leri: Numpad4. `enableTestGuis` gerekir (deobf ortamda varsayılan
  true) ve ayrıca Baubles modu yüklü olmalı.
- İsteğe bağlı dev modları açmak için `gradle.properties` içindeki `debug_bogo`
  ve `debug_nea` anahtarları `runtimeOnly` bağımlılık ekler.

## Toolchain tuzakları

- **İki version catalog var, karıştırma:**
  - `libs` → `gradle/libs.versions.toml` (plugin'ler, build araçları, JUnit)
  - `deps` → `gradle/deps.versions.toml` (mod bağımlılıkları)
  Yanlışı kullanmak derleme hatası verir.
- Derleme toolchain **Azul 25**. Testler, `compileInjectedInterfacesJava` ve
  downgrade **Azul 8** toolchain'inde çalışır.
- **`options.release` hiçbir yerde ayarlı değil.** Bu yüzden Java 25 sözdizimi
  yazabilirsiniz, derleme geçer; sonra jvmdowngrader bytecode'u Java 8'e indirir.
  Yani hedef hâlâ Java 8 uyumluluğudur — derleyici sizi uyarmaz.
- `jvmdowngrader-java-api` modern JDK API'lerini geri taşır. Testler gerçek Java 8
  JVM'inde (`azul_systems__inc_-8`) çalıştığı halde `List.of` gibi Java 9+ API'leri
  çalışır. Karşılanmayan bir modern API seçerseniz hata **derlemede değil, çalışma
  zamanında** çıkar.
- `build-logic` ayrı bir included build. Convention plugin'ler
  `build-logic/convention/src/main/kotlin/*.gradle.kts` altında.
- Mod bağımlılıklarını RFG ile deobfuscate etmek için `build.gradle.kts` içindeki
  `deobf()` helper'ını kullanın (doğrudan `rfg.deobf` yazmayın).
- **mixinbooter bilerek 10.x'te tutuluyor** (`build.gradle.kts` yorumu): 11.x
  type-parametreli mixin'lerde ve `FMLDeobfuscatingRemapper`'da runtime'ı bozuyor.
  Yükseltmeyin.

## Test kuralları

- Testler **Java 8 JVM'inde** çalışır ve sınıfları `downgradeTest` çıktısından gelir
  (jvmdg, `build-logic/convention/src/main/kotlin/jvmdg.gradle.kts`).
- **Font renderer'a dokunan her test patlar:** `TextRenderer.getFontRenderer()`
  `Minecraft.getMinecraft().fontRenderer` çağırır ve test JVM'inde
  `NoClassDefFoundError: org/lwjgl/LWJGLException` verir. Mevcut tüm testler bu
  yüzden saf mantık testidir (Format, Camera, Sizer, WidgetTree).
- Render bağımsız handler testleri için renderer'ı devre dışı bırakın:
  ```java
  // renderer/scrollArea canlı GL font + gui context istiyor, handler mantığı için gereksiz
  this.handler.setRenderer(null);
  this.handler.setScrollArea(null);
  ```
  Örnek: `src/test/java/com/cleanroommc/modularui/TextFieldHandlerTest.java`
- `src/test/java/com/cleanroommc/modularui/Bootstrap.java` **hiçbir test tarafından
  çağrılmıyor.** Minecraft'ın bootstrap edildiğini varsaymayın.
- Test classpath'ine patched Minecraft ve MC launcher eklenir
  (`build-logic/convention/src/main/kotlin/test.gradle.kts`).

## Repo kuralları

- `build/` gitignore'lu (`.gitignore`). Asla commit etmeyin.
- `.editorconfig` `end_of_line = crlf` diyor ama **tüm dosyalar LF** ve
  `.gitattributes` yok. Editör ayarına değil, dosyalara güven — formatlayıp
  kaydederseniz tüm dosya değişmiş gibi diff çıkar.
- `enableSpotless = false`: format kapısı yok. Ayrı lint veya typecheck görevi yok;
  doğrulama `build` / `check` (check = test).
- Java satır uzunluğu 140 (`.editorconfig`), 4 boşluk girintileme.

## Mimari — görünmeyen kısım

- Üç katmanlı transform birlikte çalışır:
  1. **Mixinbooter early mixin'ler** — `core/mixins/early`, `mixin.modularui.json`
  2. **Geç (late) mixin'ler** — `core/LateMixins`, `jei` ve `evalex` json'ları,
     yalnızca ilgili mod yüklüyse uygulanır
  3. **Coremod** — `core.ModularUICore` (RetroFuturaBootstrap) ve `core/ClassTransformer`
     (ASM ile elle transform)
- Refmap üretiliyor: `build/tmp/mixins/mixins.modularui.refmap.json`
- `generateTags` BuildConfig, `modGroup` altında `Tags` sınıfı üretir
  (`MOD_ID`, `MOD_VERSION`, ...). Elle sabit yazmayın.
- `mcmod.info`, `pack.mcmeta` ve mixin json'ları **Gradle şablonlarıdır**;
  `${mod_version}` gibi token'larla genişletilir
  (`minecraft.gradle.kts`, `tasks.processResources`). Sürümü dosyaya elle yazmayın.

## Upstream lineage ve backport

Sıra: `CleanroomMC/ModularUI` (arşivlenmiş ana dal) → `SymmetricDevs/ModularUI`
(~10 commit ileride) → bu depo. `GTNewHorizons/ModularUI2` ise **hâlâ bakımda olan
1.7.10 çatalı** ve düzeltmelerin kaynağı olarak kullanılır.

- **Yeniden backport etmeden önce `plans/gtnh-backport-plan.md` dosyasına bakın.**
  Hangi PR'ların alındığı, hangisinin neden alınamadığı orada kayıtlı.
- GTNH PR'larını okurken: PR numarası commit mesajında geçmiyorsa
  `https://github.com/GTNewHorizons/ModularUI2` commit geçmişinde arayın.

Port tuzakları (1.7.10 → 1.12.2):

- Upstream evalex'in **kaynak dosyalarını** patch'liyor; bu repo evalex'i harici
  dependency olarak aldığı için o patch'ler alınamaz.
- 1.12.2'de `IIcon` yok, `TextureAtlasSprite` + `getTextureMapBlocks().getAtlasSprite()`
  var. Fluid/strip draw diff'leri birebir uygulanmaz, mantık aynı çevrilir.
- Slot tıklaması `int mode` değil `ClickType` kullanıyor.
- 1.12.2'de `ButtonWidget` gibi sınıflar farklı; isim eşleştirmesine güvenmeyin.

## Yayınlama

- `build/libs` içindeki dosyaların çoğu ara üründür. `build/libs` içinde
  classifier'ı **olmayan** `ModularUI-<version>.jar` yayınlanan dosyadır:
  RFG'nin `reobfJar` görevi `shadeDowngradedApi` girdisini alıp
  `build/libs/ModularUI-<version>.jar` yoluna yazar, yani düz `jar` çıktısının
  **üzerine yazar**. `-shaded`, `-downgraded`, `-downgraded-shaded` dosyaları bu
  zincirin girdileridir; `-sources` ve `-dev` yan dosyalardır.
- Modrinth ve CurseForge yüklemeleri `reobfJar` çıktısını kullanır
  (`build-logic/convention/src/main/kotlin/publish.gradle.kts`).
- `.github/workflows/build_and_publish.yaml` yalnızca manuel
  (`workflow_dispatch`) çalışır ve master'a sürüm bump'ı pushlar. Otomatik
  yayın yok.
