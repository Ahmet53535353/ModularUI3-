# GTNH ModularUI2 Backport Planı

GTNewHorizons/ModularUI2 (1.7.10) deposundaki hata düzeltmelerinin bu 1.12.2 fork'una
taşınması için hazırlanmış plan.

## Bağlam / lineage

- `CleanroomMC/ModularUI` — arşivlenmiş ana dal, Minecraft 1.12.2
- `SymmetricDevs/ModularUI` — ana dalın ~10 commit ilerisi
- `Ahmet53535353/ModularUI3-` — bu depo, SymmetricDevs fork'undan çatallanmış ve
  hâlâ 1.12.2 için güncelleniyor
- `GTNewHorizons/ModularUI2` — hâlâ güncellenen 1.7.10 çatalı; düzeltmelerin
  kaynağı olarak kullanılıyor

Taban commit: `7357953`
Hedef dal: `backport/gtnh-fixes`

## Taranan PR'lar ve durumları

GTNewHorizons/ModularUI2'deki commit'ler tarandı, her birinin bu depodaki
karşılığı yerel kaynak kod okunarak doğrulandı.

### Zaten backport edilmiş (tekrar iş yok)

| PR | Açıklama | Nerede |
| --- | --- | --- |
| #126 | Panel sürüklenirken tooltip görünmesin | `Widget.java:206`, `ItemSlot.java:117`, `TextFieldWidget.java:120` — `!context.hasDraggable()` mevcut |
| #129 | PanelSyncManager init'ten önce kilitlensin | `PanelSyncManager.java:64-65` — `locked = true` zaten `init` öncesinde |
| #133 | PanelSyncHandler/TextFieldHandler crash | `PanelSyncHandler.java:113-120` guard mevcut; `TextFieldHandler` cursor clamp mevcut |
| #152 | Boş FluidStack tam dolu çizilmesin | `AbstractFluidDisplayWidget.java:44` — `fluid.amount <= 0` guard mevcut |
| #118 | Ekran kapatma mantığı + text field focus | `PanelManager.java:242` `closePanelsAndScreen` mevcut |

### Backport edilmemiş ve bu depoda geçerli (bu dalın konusu)

| PR | Hata | Etki |
| --- | --- | --- |
| #163 | Debug modu deobf ortamda varsayılan açık | Widget konturları istenmeyen çiziliyor |
| #161 | HD pack / anizotropik padding'de fluid ikonu kırpılıyor | Sıvı görseli yanlış boyutta |
| #153 | Bounded number field'da `"50%"` max'a göre çarpılmıyor | Yüzde girdileri yanlış değere dönüşüyor |
| #147 | `setupDrawColor()` GL durumunu geri yüklemiyor | NEI eşyaları beyaz çiziliyor |

### Uygulanamaz / kapsam dışı

| PR | Neden |
| --- | --- |
| #136, #151 | evalex kütüphanesini patch'liyor. Bu repo evalex'i harici dependency olarak alıyor (`shadowDowngrade(deps.evalEx)`), kaynak dosyalara erişim yok |
| #121 | Generic sync handler API'sini kökten değiştiriyor (yüzlerce dosya, `SyncHandler<T, S>` generics eklemesi). Yüksek riskli, ayrı değerlendirme gerektirir |
| #135 | 1.7.10'a özgü `GuiContainer.mouseMovedOrUp` mixin'i ve `InventoryBasic` dummy inventory. 1.12.2'de karşılığı yok |
| #138 | 1.7.10 NEI `IContainerObjectHandler.objectUnderMouse` API'si. 1.12.2'de `objectUnderMouse` yok |
| #155 | Sadece exception mesajını zenginleştiriyor, davranış değişmiyor. İsteğe bağlı |
| #158 | Overlay deferred-render kuyruğu + lwjgl3ify `InputEvents`. 1.12.2'de karşılığı yok |
| #160 | Aynı slotla takas koruması. Düşük riskli, isteğe bağlı — ayrıca sorulacak |

## Uygulama adımları

### Aşama 0 — Dalı hazırla

1. `git checkout -b backport/gtnh-fixes`
2. **Commit 1** — `fix(textfield): correct marked-text deletion during insert`
   - `TextFieldHandler.java` — `insert()` içindeki `delete()` yanlış listeyi
     mutate ediyordu. Silme art araya gelen `List` üzerinde yapılıyor ve
     imleç konumu yerel `Point` ile taşınıyor
   - `src/test/java/com/cleanroommc/modularui/TextFieldHandlerTest.java` — yeni,
     iki test
   - Bu düzeltme GTNH'den değil, bu depodaki incelemeden çıktı; yol gösterici
     olduğu için dalda en başa konur
3. **Commit 2** — `build: add missing gradle wrapper files`
   - `gradlew` + `gradle/wrapper/gradle-wrapper.jar`
   - `.gitignore:13` wrapper.jar'ı ignore etmemeyi zaten söylüyor ama dosya
     hiç commit edilmemiş. Yani master'ı temiz klonlayan kimse build edemez

### Aşama 1 — PR #163: debug modu

`ModularUIConfig.java:29`

```java
-public static boolean guiDebugMode = FMLLaunchHandler.isDeobfuscatedEnvironment();
+public static boolean guiDebugMode = false;
```

`FMLLaunchHandler` import'u `:42`'de `enableTestGuis` için kullanılmaya devam
ettiğinden silinmez.

Commit: `fix(config): disable gui debug mode by default (GTNH #163)`

### Aşama 2 — PR #161: fluid tile boyutu

`GuiDraw.java`

- `private static final int FLUID_TILE_SIZE = 16;` sabiti eklenir
- `drawFluidTexture` içinde `sprite.getIconWidth(), sprite.getIconHeight()`
  yerine `FLUID_TILE_SIZE, FLUID_TILE_SIZE` kullanılır

Sebep: HD resource pack veya anizotropik filtreleme atlas'a padding eklediğinde
sprite 16'dan büyük olabiliyor. Bu durumda sprite boyutu bir karo'nun ne kadar
çizileceğini tarif etmiyor ve ikon kırpılıyor.

1.12.2 farkı: upstream `IIcon fluidStill = fluid.getIcon(content)` kullanıyor,
bu depo `TextureAtlasSprite` + `Minecraft.getTextureMapBlocks().getAtlasSprite(...)`
kullanıyor. Diff birebir uygulanamıyor ama düzeltilen mantık aynı.

Commit: `fix(draw): tile fluid icons at 16px regardless of sprite resolution (GTNH #161)`

### Aşama 3 — PR #153: bounded field yüzde

TDD ile: önce kırmızı test, sonra implementasyon.

1. `MathUtils.java` — mevcut `percentOrSelf(double, long)` korunur, yanına
   `percentOrSelf(String expression, double value, long maxValue)` overload'u
   eklenir. İfade `%` içeriyorsa `Math.round(value * maxValue)`, yoksa mevcut
   yola devreder
2. `TextFieldWidget.java` — `@FunctionalInterface NumberValidator { double apply(String input, double value); }`
   ve `numbersDouble(NumberValidator)` overload'u. Mevcut
   `numbersDouble(DAM.UnaryDoubleOperator)` tek satırlık delegasyona çevrilir
   (1 argümanlı lambda çağrıları çözümleme belirsizliği yaratmaz).
   `numbersLong(validator, min, max)` `numbersDouble((val, d) -> ...)` kullanır ve
   `MathUtils.percentOrSelf(val, d, maxValue)` çağırır
3. `src/test/java/com/cleanroommc/modularui/utils/MathUtilsTest.java` — yeni

Doğrulama değerleri (mevcut `percentOrSelf` semantiğiyle elle kontrol edildi):

| İfade | Değer | Max | Beklenen |
| --- | --- | --- | --- |
| `"1"` | 1 | 256 | 1 |
| `"0.5"` | 0.5 | 256 | 128 |
| `"50%"` | 0.5 | 256 | 128 |
| `"100%"` | 1 | 256 | 256 |
| `"200%"` | 2 | 256 | 512 |

Commit: `fix(textfield): preserve explicit percentages in bounded number fields (GTNH #153)`

## Doğrulama

Her commit sonrası `./gradlew test`, sonda tam `./gradlew build`.

Not: `./gradlew --offline` çalışmıyor — RFG'nin fernflower indirme görevi
offline modda `IllegalStateException` fırlatıyor. `--offline` kullanılmadan
çalıştırılacak.

## Test edilebilirlik notu

- Aşama 3 (PR #153) gerçek bir birim teste sahip — TDD uygulanıyor
- Aşama 1 (PR #163) config varsayılanı. Test JVM'inde
  `isDeobfuscatedEnvironment()` zaten `false` döndüğü için yazılacak test
  düzeltmeden önce de geçerdi, yani gerçek bir regresyon yakalayamazdı.
  Derleme ile doğrulanır, çalışma zamanı davranışı manuel kontrol gerektirir
- Aşama 2 (PR #161) saf render kodu, GL gerektiriyor. Derleme ile doğrulanır,
  görsel olarak HD resource pack ile manuel kontrol gerekir
- Aşama 0 commit 1 (TextFieldHandler) iki testle doğrulandı
