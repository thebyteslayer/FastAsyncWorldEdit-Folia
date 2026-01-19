plugins {
    id("buildlogic.libs")
}

tasks.register("build") {
    dependsOn("assemble")
}
