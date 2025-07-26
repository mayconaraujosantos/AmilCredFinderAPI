import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // Referencie os plugins do catálogo de versões
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.application)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.mappie.plugin)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
}

group = "com.amilapi"
version = "1.0.0" // Use semantic versioning. 1.0-SNAPSHOT is typically for development.

repositories {
    mavenCentral()
    // Se você encontrar problemas para resolver bibliotecas kotlinx ou outros artefatos JetBrains,
    // pode ser necessário descomentar o seguinte repositório:
     maven("https://maven.pkg.jetbrains.space/public/p/kotlin/dev")
}

kotlin {
    // Stick to JVM 17 as it's a good LTS version for Kotlin projects.
    jvmToolchain(17)
    // Configure Kotlin compiler options for better performance and warnings.
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
        languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
        freeCompilerArgs.addAll(
            "-Xjvm-default=all", // Habilita métodos padrão em interfaces
            "-Xopt-in=kotlin.RequiresOptIn", // Para anotações @OptIn
            "-Xskip-prerelease-check", // Útil para versões mais recentes do Kotlin
            "-Xemit-jvm-type-annotations", // Emite anotações de tipo no bytecode
            "-Xno-param-assertions", // Desabilita asserções de tempo de execução para parâmetros (pode melhorar o desempenho)
            "-Xno-call-assertions", // Desabilita asserções de tempo de execução para chamadas
            "-Xexplicit-api=strict" // Impõe o modo de API explícito para a superfície da API pública
        )
    }
}

dependencies {
    // Referencie as bibliotecas do catálogo de versões
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)

    // Logging
    implementation(libs.logback.classic)
    implementation(libs.kotlin.logging.jvm)

    // Configuração
    implementation(libs.typesafe.config)

    // Ktor Client
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.jackson)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.cookies)
    // Considerar adicionar `ktor-client-auth` se tiver necessidades de autenticação.
    // implementation(libs.ktor.client.auth)

    // Web Framework
    implementation(libs.javalin)
    implementation(libs.jackson.module.kotlin)

    // Mapeamento
    implementation(libs.mappie.api)

    // Testes
    testImplementation(libs.kotlin.test)
    testImplementation(libs.mockk)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.assertj.core)
}

tasks.test {
    useJUnitPlatform()
    // Mostra os resultados dos testes no console
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
    // Maximiza o paralelismo para testes
    maxParallelForks = Runtime.getRuntime().availableProcessors()
    forkEvery = 1
}

application {
    mainClass.set("com.amilapi.MainKt")
}

// Configuração do Detekt
detekt {
    toolVersion = libs.versions.detekt.get() // Pega a versão do catálogo
    // Arquivos de entrada (diretórios do código fonte)
    source = files("src/main/kotlin", "src/test/kotlin")
    // Arquivos de configuração para regras do Detekt (opcional, você pode gerar um padrão)
    // config = files("config/detekt/detekt.yml")
    buildUponDefaultConfig = true // Usa as regras padrão do Detekt como base
    allRules = false // Habilita apenas regras específicas, não todas (pode ser barulhento inicialmente)
    parallel = true // Executa as tarefas do Detekt em paralelo
    // Opções de relatório
    reports {
        xml {
            required.set(true)
            outputLocation.set(file("build/reports/detekt/detekt.xml"))
        }
        html {
            required.set(true)
            outputLocation.set(file("build/reports/detekt/detekt.html"))
        }
    }
}

// Configuração do Ktlint
ktlint {
    version.set(libs.versions.ktlint.get()) // Pega a versão do catálogo
    // Verifica todos os conjuntos de fontes Kotlin por padrão
    kotlinScriptAdditionalPaths {
        include(fileTree("src/main/kotlin"))
        include(fileTree("src/test/kotlin"))
    }
    // Habilita a autocorreção na construção, bom para CI/CD
    // Também pode executar `./gradlew ktlintFormat`
    debug.set(false)
    verbose.set(true)
    android.set(false) // Defina como true se for um projeto Android
    // Desabilita algumas regras se necessário
    // disabledRules.set(setOf("filename"))
}

// Tarefas para garantir que as verificações de qualidade do código sejam executadas antes da construção.
// Considere descomentar estas em um ambiente CI/CD ou antes de fazer merge.
// tasks.check {
//     dependsOn(tasks.detekt)
//     dependsOn(tasks.ktlintCheck)
// }
//
// tasks.preBuild {
//     dependsOn(tasks.ktlintFormat) // Formata o código antes da construção (opcional, pode ser incômodo durante o desenvolvimento)
// }
