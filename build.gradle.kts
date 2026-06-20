allprojects {
    group = "jp.andpad"
    version = "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        maven {
            name = "intra-mart"
            url = uri("https://repo.intra-mart.co.jp/maven")
            content {
                includeGroup("jp.co.intra_mart")
            }
        }
    }
}

subprojects {
    tasks.withType<Test> {
        useJUnitPlatform()
        maxParallelForks = 1
    }
}
