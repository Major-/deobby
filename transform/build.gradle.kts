plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":asm"))
    implementation("org.ow2.asm:asm:7.2")
    implementation("org.ow2.asm:asm-commons:7.2")
    implementation("org.ow2.asm:asm-util:7.2")
}
