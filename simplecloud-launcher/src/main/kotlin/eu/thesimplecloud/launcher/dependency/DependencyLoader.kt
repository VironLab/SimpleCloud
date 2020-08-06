/*
 * MIT License
 *
 * Copyright (C) 2020 The SimpleCloud authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package eu.thesimplecloud.launcher.dependency

import eu.thesimplecloud.api.depedency.Dependency
import eu.thesimplecloud.api.external.ResourceFinder
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.resolution.ArtifactResult
import java.io.File

/**
 * Created by IntelliJ IDEA.
 * Date: 16.07.2020
 * Time: 15:20
 * @author Frederick Baier
 */

class DependencyLoader {

    private val central: RemoteRepository = RemoteRepository.Builder("central", "default", "https://repo1.maven.org/maven2/").build()
    private val cloudDevelopmentRepository: RemoteRepository = RemoteRepository.Builder("cloud", "default", "https://repo.thesimplecloud.eu/artifactory/list/gradle-dev-local/").build()
    private val cloudReleaseRepository: RemoteRepository = RemoteRepository.Builder("simplecloud", "default", "https://repo.thesimplecloud.eu/artifactory/list/gradle-release-local/").build()

    private val loadedDependencies = mutableListOf<Dependency>()

    /**
     * Loads the specified dependencies
     * @return a list containing all loaded artifacts
     */
    fun loadDependencies(repositories: List<String>, dependencies: List<DefaultArtifact>): List<ArtifactResult> {
        println("load dependencies called")
        val ms = System.currentTimeMillis()
        val session = DependencyResolver(repositories, dependencies)
        val results = session.resolveDependencies()
        println("took ${System.currentTimeMillis() - ms}ms")
        val resultToJarToInstall = results.map { it to getJarToInstall(it) }
                .filter { it.second != null }.map { it as Pair<ArtifactResult, File> }.toMap()

        val notInstalledDependencies = resultToJarToInstall.filter { !isFileAlreadyLoaded(it.value) }
        notInstalledDependencies.map { it.value }.forEach { installJar(it) }
        this.loadedDependencies.addAll(notInstalledDependencies.map { Dependency.fromAetherArtifact(it.key.artifact) })
        return results
    }

    private fun isFileAlreadyLoaded(file: File): Boolean {
        return this.loadedDependencies.map { it.getDownloadedFile() }.contains(file)
    }

    private fun installJar(file: File) {
        ResourceFinder.addToClassLoader(file)
    }

    private fun getJarToInstall(dependency: ArtifactResult): File? {
        val file = dependency.artifact.file
        val directory = file.parentFile.parentFile
        val newestVersion = directory.listFiles().map { it.name }.max()!!
        val newestDependencyDir = File(directory, "$newestVersion/")
        return findFirstJarFileInDir(newestDependencyDir)
    }

    private fun findFirstJarFileInDir(dir: File): File? {
        if (!dir.isDirectory) {
            println("skipping ${dir.absolutePath}")
            return null
        }
        return dir.listFiles().firstOrNull { it.name.endsWith(".jar") }
    }

    fun getLoadedDependencies(): List<Dependency> = this.loadedDependencies


    companion object {
        val instance = DependencyLoader()
    }


}