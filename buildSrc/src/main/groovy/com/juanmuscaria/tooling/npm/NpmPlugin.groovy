package com.juanmuscaria.tooling.npm

import net.minecraftforge.artifactural.api.artifact.ArtifactIdentifier
import net.minecraftforge.artifactural.base.repository.ArtifactProviderBuilder
import net.minecraftforge.artifactural.base.repository.SimpleRepository
import net.minecraftforge.artifactural.gradle.GradleRepositoryAdapter
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy

class NpmPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.extensions.create("npm", NpmExtension, project)

        final artifactProvider = new NpmArtifactProvider(project)
        final GradleRepositoryAdapter adapter = GradleRepositoryAdapter.add(project.getRepositories(), "npm", new File(project.buildDir, "npm"),
                SimpleRepository.of(ArtifactProviderBuilder.begin(ArtifactIdentifier.class)
                        .provide(artifact -> artifactProvider.getArtifact(artifact)
                        ))
        )
        adapter.content { artifactProvider.configureContent(it) }


        // Add Sass support
        project.plugins.withId('io.miret.etienne.sass') {
            var unpackNpmScss = project.tasks.register("unpackNpmSass", Copy) {
                group = "npm"
                destinationDir = new File("$project.buildDir/npm/sass")
                include("**/*.scss")
                project.configurations.compileClasspath.resolvedConfiguration.resolvedArtifacts.collect {
                    def moduleInfo = it.moduleVersion.id
                    if (moduleInfo.group == NpmArtifactProvider.NPM_GROUP && it.classifier == null) {
                        from(project.zipTree(it.file)) {
                            filesMatching("/package/scss/**") {
                                it.path = it.path.replace("/package/scss/", "/")
                            }
                            into(moduleInfo.name)
                        }
                    }
                }
            }
            project.tasks.named("compileSass") {
                it.dependsOn(unpackNpmScss.name)
                it.loadPath(unpackNpmScss.get().destinationDir)
            }
        }
    }
}
