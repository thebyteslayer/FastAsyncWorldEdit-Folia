tasks.register("build") {
    dependsOn(subprojects.map { project ->
        when {
            project.tasks.findByName("build") != null -> project.tasks.named("build")
            project.tasks.findByName("assemble") != null -> project.tasks.named("assemble")
            project.tasks.findByName("jar") != null -> project.tasks.named("jar")
            else -> {
                // Skip projects without standard build tasks
                project.tasks.register("_noop") { }
                project.tasks.named("_noop")
            }
        }
    })
}
