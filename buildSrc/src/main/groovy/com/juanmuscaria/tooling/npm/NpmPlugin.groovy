package com.juanmuscaria.tooling.npm

import com.fasterxml.jackson.databind.ObjectMapper
import net.minecraftforge.artifactural.api.artifact.ArtifactIdentifier
import net.minecraftforge.artifactural.base.repository.ArtifactProviderBuilder
import net.minecraftforge.artifactural.base.repository.SimpleRepository
import net.minecraftforge.artifactural.gradle.GradleRepositoryAdapter
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy

import java.util.zip.ZipException
import java.util.zip.ZipFile

class NpmPlugin implements Plugin<Project> {
    static final ObjectMapper mapper = new ObjectMapper()

    void apply(Project project) {
        project.extensions.create("npm", NpmExtension, project)

        final artifactProvider = new NpmArtifactProvider(project)
        final GradleRepositoryAdapter adapter = GradleRepositoryAdapter.add(project.getRepositories(), "npm",
                new File(project.buildDir, "npm"),
                SimpleRepository.of(ArtifactProviderBuilder.begin(ArtifactIdentifier.class)
                        .provide(artifact -> artifactProvider.getArtifact(artifact)
                        ))
        )
        adapter.content { artifactProvider.configureContent(it) }


        // Add Sass support
        project.plugins.withId('io.miret.etienne.sass') {
            var unpackNpmSass = project.tasks.register("unpackNpmSass", Copy) {
                group = "npm"
                destinationDir = new File("$project.buildDir/generated/scss")
                include("**/*.scss")
                project.configurations.compileClasspath.resolvedConfiguration.resolvedArtifacts.collect {
                    def moduleInfo = it.moduleVersion.id
                    if (moduleInfo.group == NpmArtifactProvider.NPM_GROUP && it.classifier == null) {
                        var packageJsonEntry = getEntry(it.file, "package/package.json")
                        if (packageJsonEntry != null) {
                            try {
                                var packageJson = mapper.readValue(packageJsonEntry, PackageJson.class)
                                if (packageJson.sass != null) {
                                    from(project.zipTree(it.file)) {
                                        var originalPath = packageJson.sass
                                        var lastIndex = originalPath.lastIndexOf('/')
                                        var repackPath = "/package/"
                                        if (lastIndex != -1) {
                                            repackPath = "/package/" + originalPath.substring(0, lastIndex + 1)
                                        }
                                        filesMatching(repackPath + "**") {
                                            it.path = it.path.replace(repackPath, "/")
                                        }
                                        into(moduleInfo.name)
                                    }
                                }
                            } catch (Exception e) {
                                logger.warn("Unable to parse NPM package.json", e)
                            }
                        }
                    }
                }
            }
            project.tasks.named("compileSass") {
                it.dependsOn(unpackNpmSass.name)
                it.loadPath(unpackNpmSass.get().destinationDir)
            }
        }

        project.tasks.register("unpackNpmCss", Copy) {
            group = "npm"
            destinationDir = new File("$project.buildDir/generated/css")
            include("**/*.css")
            project.configurations.compileClasspath.resolvedConfiguration.resolvedArtifacts.collect {
                def moduleInfo = it.moduleVersion.id
                if (moduleInfo.group == NpmArtifactProvider.NPM_GROUP && it.classifier == null) {
                    var packageJsonEntry = getEntry(it.file, "package/package.json")
                    if (packageJsonEntry != null) {
                        try {
                            var packageJson = mapper.readValue(packageJsonEntry, PackageJson.class)
                            if (packageJson.style != null) {
                                from(project.zipTree(it.file)) {
                                    var originalPath = packageJson.style
                                    var lastIndex = originalPath.lastIndexOf('/')
                                    var repackPath = "/package/"
                                    if (lastIndex != -1) {
                                        repackPath = "/package/" + originalPath.substring(0, lastIndex + 1)
                                    }
                                    filesMatching(repackPath + "**") {
                                        it.path = it.path.replace(repackPath, "/")
                                    }
                                    into(moduleInfo.name)
                                }
                            }
                        } catch (Exception e) {
                            logger.warn("Unable to parse NPM package.json", e)
                        }
                    }
                }
            }
        }

        project.tasks.register("unpackNpmJs", Copy) {
            group = "npm"
            destinationDir = new File("$project.buildDir/generated/js")
            include("**/*.js")
            project.configurations.compileClasspath.resolvedConfiguration.resolvedArtifacts.collect {
                def moduleInfo = it.moduleVersion.id
                if (moduleInfo.group == NpmArtifactProvider.NPM_GROUP && it.classifier == null) {
                    var packageJsonEntry = getEntry(it.file, "package/package.json")
                    if (packageJsonEntry != null) {
                        try {
                            var packageJson = mapper.readValue(packageJsonEntry, PackageJson.class)
                            if (packageJson.main != null) {
                                from(project.zipTree(it.file)) {
                                    var originalPath = packageJson.main
                                    var lastIndex = originalPath.lastIndexOf('/')
                                    var repackPath = "/package/"
                                    if (lastIndex != -1) {
                                        repackPath = "/package/" + originalPath.substring(0, lastIndex + 1)
                                    }
                                    filesMatching(repackPath + "**") {
                                        it.path = it.path.replace(repackPath, "/")
                                    }
                                    into(moduleInfo.name)
                                }
                            }
                        } catch (Exception e) {
                            logger.warn("Unable to parse NPM package.json", e)
                        }
                    }
                }
            }
        }

        project.tasks.register("unpackNpmSvg", Copy) {
            group = "npm"
            destinationDir = new File("$project.buildDir/generated/svg")
            include("**/*.svg")
            project.configurations.compileClasspath.resolvedConfiguration.resolvedArtifacts.collect {
                def moduleInfo = it.moduleVersion.id
                if (moduleInfo.group == NpmArtifactProvider.NPM_GROUP && it.classifier == null) {
                    from(project.zipTree(it.file)) {
                        var repackPath = "/package/"
                        filesMatching(repackPath + "**") {
                            it.path = it.path.replace(repackPath, "/")
                        }
                        into(moduleInfo.name.replace('/', '@'))
                    }
                }
            }
        }
    }

    static InputStream getEntry(File zipFile, String entryName) {
        if (!zipFile.exists() || !zipFile.isFile()) {
            return null
        }

        try {
            ZipFile zip = new ZipFile(zipFile)
            def entry = zip.getEntry(entryName)

            if (entry) {
                return zip.getInputStream(entry)
            } else {
                return null
            }
        } catch (ZipException ignored) {
            return null
        }
    }
}
