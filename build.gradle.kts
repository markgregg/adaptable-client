import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.7.3"
	id("io.spring.dependency-management") version "1.0.13.RELEASE"
	kotlin("jvm") version "1.6.21"
	kotlin("plugin.spring") version "1.6.21"
	`maven-publish`
	signing
	id("org.jetbrains.dokka") version "1.4.20"
}

group = "org.adaptable"
version = "1.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

val projectDescription = "Adaptable client"
val githubRepo = "markgregg/adaptable-client"
val licenseUrl = "https://opensource.org/licenses/Apache-2.0"
val licenseName = "Apache 2"

val kotestVersion = "5.4.2"
val mockitoKotlinVersion = "3.2.0"
val classgraphVersion="4.8.149"
val rxkotlinVersion="3.0.1"
val rxjavaVersion="3.1.5"
val adaptableExpression = "1.0.0-SNAPSHOT"
val adaptableCommon = "1.0.0-SNAPSHOT"
val adaptableCommonWeb = "1.0.1-SNAPSHOT"

val downLoadPackageUser="markgregg"
val downLoadPackageToken="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"

repositories {
	mavenCentral()
	maven {
		url = uri("https://maven.pkg.github.com/markgregg/adaptable-expression")
		credentials {
			username = downLoadPackageUser
			password = downLoadPackageToken
		}
	}
	maven {
		url = uri("https://maven.pkg.github.com/markgregg/adaptable-common")
		credentials {
			username = downLoadPackageUser
			password = downLoadPackageToken
		}
	}
	maven {
		url = uri("https://maven.pkg.github.com/markgregg/adaptable-common-web")
		credentials {
			username = downLoadPackageUser
			password = downLoadPackageToken
		}
	}
}

dependencies {
	implementation("org.adaptable:adaptable-expression:$adaptableExpression")
	implementation("org.adaptable:adaptable-common:$adaptableCommon")
	implementation("org.adaptable:adaptable-common-web:$adaptableCommonWeb")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("io.github.classgraph:classgraph:$classgraphVersion")
	implementation("io.reactivex.rxjava3:rxkotlin:$rxkotlinVersion")
	implementation("io.reactivex.rxjava3:rxjava:$rxjavaVersion")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
	testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
	testImplementation("io.kotest:kotest-property:$kotestVersion")
	testImplementation("org.mockito.kotlin:mockito-kotlin:$mockitoKotlinVersion")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
	enabled = false
}

tasks.getByName<Jar>("jar") {
	enabled = true
	archiveClassifier.set("")
}


publishing {
	publications {
		create<MavenPublication>("maven") {
			groupId = project.group.toString()
			artifactId = project.name
			version = project.version.toString()
			from(components["kotlin"])
			pom {
				name.set(project.name)
				description.set(projectDescription)
				url.set("https://github.com/$githubRepo")
				licenses {
					license {
						name.set(licenseName)
						url.set(licenseUrl)
					}
				}
				developers {
					developer {
						id.set("markgregg")
						name.set("Mark Gregg")
					}
				}
				scm {
					url.set(
						"https://github.com/$githubRepo.git"
					)
					connection.set(
						"scm:git:git://github.com/$githubRepo.git"
					)
					developerConnection.set(
						"scm:git:git://github.com/$githubRepo.git"
					)
				}
				issueManagement {
					url.set("https://github.com/$githubRepo/issues")
				}
			}
		}
	}

	repositories {
		maven {
			url = uri("https://maven.pkg.github.com/markgregg/adaptable-client")
			credentials {
				username = System.getenv("USERNAME")
				password = System.getenv("TOKEN")
			}
		}
	}
}
