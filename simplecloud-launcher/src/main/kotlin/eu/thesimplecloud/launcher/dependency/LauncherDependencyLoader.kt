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
import org.eclipse.aether.resolution.ArtifactResult


/**
 * Created by IntelliJ IDEA.
 * Date: 16.07.2020
 * Time: 21:13
 * @author Frederick Baier
 */
class LauncherDependencyLoader {


    fun loadDependenciesToStart() {
        println("Loading default dependencies...")
        loadDefaultDependencies()
        println("Loading cloud dependencies...")
        loadCloudDependencies()
    }

    private fun loadDefaultDependencies() {
        val content = LauncherDependencyLoader::class.java.getResource("/dependencies.txt").readText()
        val dependenciesAsString = content.split("\n")

        val dependencies = dependenciesAsString.map { Dependency.fromSingleString(it) }
        dependencies.forEach {
            it.downloadIfNecessary(MAVEN_CENTRAL)
        }
        dependencies.forEach {
            ResourceFinder.addToClassLoader(it.getDownloadedFile())
            println("Loaded ${it.getName()} ${it.getDownloadedFile().absolutePath}")
        }
    }

    private fun loadCloudDependencies() {
        val dependencies = loadLauncherDependencies()
        val clientServerAPIDependency = dependencies
                .firstOrNull { it.artifact.groupId == "eu.thesimplecloud.clientserverapi" }
                ?: throw IllegalStateException("Cannot find clientserverapi")
        loadClientServerAPIDependencies(clientServerAPIDependency.artifact.version)
    }

    private fun loadLauncherDependencies(): List<ArtifactResult> {
        val currentVersion = System.getProperty("simplecloud.version") ?: LauncherDependencyLoader::class.java.`package`.implementationVersion
        val artifact = DefaultArtifact("eu.thesimplecloud.simplecloud:simplecloud-launcher:$currentVersion")
        val cloudRepos = if (currentVersion.endsWith("SNAPSHOT")) listOf(DEV_REPO, RELEASE_REPO) else listOf(RELEASE_REPO)
        return DependencyLoader.instance.loadDependencies(cloudRepos.union(listOf("https://repo.maven.apache.org/maven2/")).toList(), listOf(artifact))
    }

    private fun loadClientServerAPIDependencies(version: String): List<ArtifactResult> {
        val artifact = DefaultArtifact("eu.thesimplecloud.clientserverapi:clientserverapi:$version")
        val cloudRepo = RELEASE_REPO
        return DependencyLoader.instance.loadDependencies(listOf(cloudRepo, "https://repo.maven.apache.org/maven2/"), listOf(artifact))
    }

    companion object {
        const val DEV_REPO = "https://repo.thesimplecloud.eu/artifactory/list/gradle-dev-local/"
        const val RELEASE_REPO = "https://repo.thesimplecloud.eu/artifactory/list/gradle-release-local/"
        const val MAVEN_CENTRAL = "https://repo.maven.apache.org/maven2/"
    }

}