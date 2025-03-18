description = "Provides plugins that are used by Gradle subprojects"

plugins { `lifecycle-base` }

tasks.check {
    dependsOn(subprojects.map { "${it.name}:check" })
}
