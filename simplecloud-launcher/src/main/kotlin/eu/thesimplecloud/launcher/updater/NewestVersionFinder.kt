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

package eu.thesimplecloud.launcher.updater

import eu.thesimplecloud.api.depedency.Dependency
import org.apache.maven.repository.internal.MavenRepositorySystemUtils
import org.eclipse.aether.DefaultRepositorySystemSession
import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.RepositorySystemSession
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory
import org.eclipse.aether.impl.DefaultServiceLocator
import org.eclipse.aether.repository.LocalRepository
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.resolution.VersionRangeRequest
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory
import org.eclipse.aether.spi.connector.transport.TransporterFactory
import org.eclipse.aether.transport.file.FileTransporterFactory
import org.eclipse.aether.transport.http.HttpTransporterFactory
import java.util.*


/**
 * Created by IntelliJ IDEA.
 * Date: 17.07.2020
 * Time: 10:22
 * @author Frederick Baier
 */
class NewestVersionFinder {

    fun findNewestVersion(groupId: String, artifactId: String, repository: String): String {
        val central: RemoteRepository = RemoteRepository.Builder(UUID.randomUUID().toString(), "default", repository).build()
        val locator: DefaultServiceLocator = MavenRepositorySystemUtils.newServiceLocator()
        val system: RepositorySystem = newRepositorySystem(locator)
        val session: RepositorySystemSession = newSession(system)
        val artifact = DefaultArtifact("${groupId}:${artifactId}:[0,)")

        val rangeRequest = VersionRangeRequest()
        rangeRequest.artifact = artifact
        rangeRequest.repositories = listOf(central)

        val rangeResult = system.resolveVersionRange(session, rangeRequest)

        val newestVersion = rangeResult.highestVersion
        return newestVersion.toString()
    }

    private fun newRepositorySystem(locator: DefaultServiceLocator): RepositorySystem {
        locator.addService(RepositoryConnectorFactory::class.java, BasicRepositoryConnectorFactory::class.java)
        locator.addService(TransporterFactory::class.java, FileTransporterFactory::class.java)
        locator.addService(TransporterFactory::class.java, HttpTransporterFactory::class.java)
        return locator.getService(RepositorySystem::class.java)
    }

    private fun newSession(system: RepositorySystem): RepositorySystemSession {
        val session: DefaultRepositorySystemSession = MavenRepositorySystemUtils.newSession()
        val localRepo = LocalRepository(Dependency.LOCAL_REPO)
        session.localRepositoryManager = system.newLocalRepositoryManager(session, localRepo)
        return session
    }

}