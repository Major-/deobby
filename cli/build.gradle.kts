plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":transform"))
    implementation("com.xenomachina:kotlin-argparser:2.0.7")
    implementation("com.moandjiezana.toml:toml4j:0.7.2")
    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("ch.qos.logback:logback-classic:1.2.3")
}
