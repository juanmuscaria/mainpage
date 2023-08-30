package com.juanmuscaria.tooling.npm

import net.minecraftforge.artifactural.api.artifact.Artifact
import net.minecraftforge.artifactural.api.artifact.ArtifactIdentifier
import net.minecraftforge.artifactural.api.artifact.ArtifactType
import net.minecraftforge.artifactural.api.repository.ArtifactProvider
import net.minecraftforge.artifactural.base.artifact.StreamableArtifact
import net.minecraftforge.fml.unsafe.UnsafeHacks
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.RepositoryContentDescriptor
import org.orienteer.jnpm.JNPMService
import org.orienteer.jnpm.JNPMSettings

import java.lang.reflect.Field
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream

class NpmArtifactProvider implements ArtifactProvider<ArtifactIdentifier> {
    static final String NPM_GROUP = "org.npmjs"
    private final Project project
    private final Path baseTarDir = Paths.get(project.buildDir.path, "tmp", "npm")
    private final Field downloadDirField

    NpmArtifactProvider(Project project) {
        this.project = project
        if (!JNPMService.configured) {
            JNPMService.configure(JNPMSettings.builder()
                    .homeDirectory(Paths.get(project.buildDir.path, ".jnpm"))
                    .downloadDirectory(baseTarDir)
                    .build())
        }
        this.downloadDirField = JNPMSettings.getDeclaredField("downloadDirectory");
    }

    @Override
    Artifact getArtifact(ArtifactIdentifier info) {
        try {
            if (info.group == NPM_GROUP && info.extension == "jar") {
                project.logger.warn("requested npm package {} as jar", info)
                var type = ArtifactType.BINARY
                if (info.getClassifier() != null && info.getClassifier().endsWith("sources"))
                    type = ArtifactType.SOURCE

                // Normalize classifiers
                var classifier = info.classifier == null ? "" : info.classifier
                if (type == ArtifactType.SOURCE) {
                    if (classifier.endsWith("-sources"))
                        classifier = classifier.substring(0, classifier.lastIndexOf("-sources"))
                    else
                        classifier = classifier.substring(0, classifier.lastIndexOf("sources"))
                }

                switch (classifier) {
                    case "":
                        var pkgName = info.name.contains('/') ? '@' + info.name : info.name
                        var npmInfo = JNPMService.instance().getVersionInfo(pkgName, info.version)
                        var localJar = Path.of(project.buildDir.path, "npm", info.toString() + ".jar")
                        var downloadDir = baseTarDir.resolve(info.name)

                        Files.createDirectories(downloadDir)
                        UnsafeHacks.setField(downloadDirField, JNPMService.instance().settings, downloadDir)

                        npmInfo.downloadTarball().blockingAwait()
                        Files.createDirectories(localJar.getParent())
                        project.logger.warn(npmInfo.getLocalTarball().path)

                        Files.write(localJar, convert(npmInfo.getLocalTarball()))
                        return StreamableArtifact.ofFile(info, type, localJar.toFile())
                    default:
                        throw new IllegalArgumentException("Classifier %s is nod a valid NPM preprocessing.")
                }
            }
        } catch (Throwable e) {
            project.logger.error("Unable to process npm request", e)
        }
        return Artifact.none()
    }


    static void configureContent(RepositoryContentDescriptor content) {
        content.includeGroup(NPM_GROUP)
    }


    static byte[] convert(File tarFile) throws IOException {
        ByteArrayOutputStream jarBytes = new ByteArrayOutputStream()

        try (var fileIn = new FileInputStream(tarFile)
             var gzipIn = new GzipCompressorInputStream(fileIn)
             var tarIn = new TarArchiveInputStream(gzipIn)
             var jarOut = new JarOutputStream(jarBytes)) {

            TarArchiveEntry entry
            while ((entry = tarIn.getNextTarEntry()) != null) {
                JarEntry jarEntry = new JarEntry(entry.getName())
                jarOut.putNextEntry(jarEntry)
                tarIn.transferTo(jarOut)
                jarOut.closeEntry()
            }
        }

        return jarBytes.toByteArray()
    }
}
