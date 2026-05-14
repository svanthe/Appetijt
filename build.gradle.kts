// Top-level build file for the ${project.name} root project.

plugins {
    // We gebruiken hier de namen zoals gedefinieerd in de TOML file hierboven
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.google.devtools.ksp) apply false
 }


