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

import org.apache.maven.repository.internal.MavenRepositorySystemUtils
import org.eclipse.aether.DefaultRepositorySystemSession
import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.RepositorySystemSession
import org.eclipse.aether.artifact.Artifact
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.collection.CollectRequest
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.graph.DependencyFilter
import org.eclipse.aether.impl.DefaultServiceLocator
import org.eclipse.aether.repository.LocalRepository
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.resolution.DependencyRequest
import org.eclipse.aether.resolution.DependencyResult
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory
import org.eclipse.aether.spi.connector.transport.TransporterFactory
import org.eclipse.aether.transport.file.FileTransporterFactory
import org.eclipse.aether.transport.http.HttpTransporterFactory
import org.eclipse.aether.util.artifact.JavaScopes
import org.eclipse.aether.util.filter.DependencyFilterUtils
import java.io.File


/**
 * Created by IntelliJ IDEA.
 * Date: 13.07.2020
 * Time: 08:31
 * @author Frederick Baier
 */
fun _main() {
    println("---ssssaaaaaaaaaaa-")
    val locator: DefaultServiceLocator = MavenRepositorySystemUtils.newServiceLocator()
    val system: RepositorySystem = newRepositorySystem(locator)
    val session: RepositorySystemSession = newSession(system)
    val central: RemoteRepository = RemoteRepository.Builder("central", "default", "https://repo1.maven.org/maven2/").build()
    val cloud: RemoteRepository = RemoteRepository.Builder("cloud", "default", "https://repo.thesimplecloud.eu/artifactory/list/gradle-dev-local/").build()
    val cloud2: RemoteRepository = RemoteRepository.Builder("simplecloud", "default", "https://repo.thesimplecloud.eu/artifactory/list/gradle-release-local/").build()
    val artifact: Artifact = DefaultArtifact("eu.thesimplecloud.simplecloud:simplecloud-launcher:1.3.3-SNAPSHOT")
    val collectRequest = CollectRequest(Dependency(artifact, JavaScopes.COMPILE), listOf(cloud, central, cloud2))
    val filter: DependencyFilter = DependencyFilterUtils.classpathFilter(JavaScopes.COMPILE, JavaScopes.RUNTIME)
    val request = DependencyRequest(collectRequest, filter)
    val result: DependencyResult = system.resolveDependencies(session, request)
    for (artifactResult in result.artifactResults) {
        println(artifactResult.artifact.file)
    }

    val artifact2: Artifact = DefaultArtifact("eu.thesimplecloud.clientserverapi:clientserverapi:3.0.9-SNAPSHOT")
    val collectRequest2 = CollectRequest(Dependency(artifact2, JavaScopes.COMPILE), listOf(cloud2, central, cloud))
    val request2 = DependencyRequest(collectRequest2, filter)
    val result2: DependencyResult = system.resolveDependencies(session, request2)
    for (artifactResult in result2.artifactResults) {
        println(artifactResult.artifact.file)
    }
}

private fun newRepositorySystem(locator: DefaultServiceLocator): RepositorySystem {
    locator.addService(RepositoryConnectorFactory::class.java, BasicRepositoryConnectorFactory::class.java)
    locator.addService(TransporterFactory::class.java, FileTransporterFactory::class.java)
    locator.addService(TransporterFactory::class.java, HttpTransporterFactory::class.java)
    return locator.getService(RepositorySystem::class.java)
}

private fun newSession(system: RepositorySystem): RepositorySystemSession {
    val session: DefaultRepositorySystemSession = MavenRepositorySystemUtils.newSession()
    val localRepo = LocalRepository(eu.thesimplecloud.api.depedency.Dependency.LOCAL_REPO)
    session.localRepositoryManager = system.newLocalRepositoryManager(session, localRepo)
    return session
}


fun main() {
    val file = File("C:\\Users\\frede\\IdeaProjects\\simplecloud-v2\\target\\local-repo\\com\\google\\guava\\guava")
    println(file.listFiles().map { it.name }.sorted())
}