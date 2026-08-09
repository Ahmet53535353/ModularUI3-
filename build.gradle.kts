@file:Suppress("AvoidDuplicateDependencies")

plugins {
    alias(conventions.plugins.repositories)
    alias(conventions.plugins.minecraft)
    alias(conventions.plugins.publish)
    alias(conventions.plugins.shadow)
    alias(conventions.plugins.jvmdg)
    alias(conventions.plugins.idea)
    alias(conventions.plugins.test)
    alias(conventions.plugins.jvm)
}

dependencies {
    // Helper to resolve a version catalog dependency through RFG's deobfuscation.
    fun Provider<MinimalExternalModuleDependency>.deobf() = get().let {
        rfg.deobf("${it.module.group}:${it.module.name}:${it.versionConstraint.requiredVersion}")
    }

    // Mixinbooter 11.x breaks runtime (mixins with type-parameters, FMLDeobfuscatingRemapper)
    // So we use Mixinbooter 10.x here, which contains the mixin annotation processor.
    annotationProcessor(libs.mixinbooter)

    shadowImplementation(deps.joml) { isTransitive = false }
    shadowDowngrade(deps.evalEx) { isTransitive = false }
    shadowDowngrade(deps.dataFixerUpper) { isTransitive = false }

    // lombok for evalex
    compileOnly(deps.lombok)
    annotationProcessor(deps.lombok)
    testCompileOnly(deps.lombok)
    testAnnotationProcessor(deps.lombok)

    // Dev-only compile/runtime mods
    compileOnlyApi(deps.jspecify)
    compileOnlyApi(deps.annotations)

    compileOnlyApi(deps.bogoSorter.deobf())
    if (providers.gradleProperty("debug_bogo").map { it.toBoolean() }.getOrElse(false)) {
        runtimeOnly(deps.bogoSorter.deobf())
        runtimeOnly(deps.key.binding.patch.deobf())
    }

    compileOnlyApi(deps.baubles.deobf())
    compileOnlyApi(deps.nea.deobf())
    testImplementation(deps.nea.deobf())
    if (providers.gradleProperty("debug_nea").map { it.toBoolean() }.getOrElse(false)) {
        runtimeOnly(deps.nea.deobf())
    }

    implementation(deps.hei) // HEI

    runtimeOnly(deps.theOneProbe) // TOP
}

configurations {
    compileOnly {
        // exclude GNU trove, FastUtil is superior and still updated
        exclude(group = "net.sf.trove4j", module = "trove4j")
        // exclude javax.annotation from findbugs, JetBrains annotations are superior
        exclude(group = "com.google.code.findbugs", module = "jsr305")
        // exclude scala as we don't use it for anything and causes import confusion
        exclude(group = "org.scala-lang")
        exclude(group = "org.scala-lang.modules")
        exclude(group = "org.scala-lang.plugins")
    }
}
