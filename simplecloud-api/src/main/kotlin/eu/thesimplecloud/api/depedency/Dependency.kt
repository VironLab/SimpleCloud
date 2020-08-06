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

package eu.thesimplecloud.api.depedency

import eu.thesimplecloud.api.utils.Downloader
import org.eclipse.aether.artifact.Artifact
import org.eclipse.aether.artifact.DefaultArtifact
import java.io.File
import java.io.IOException

data class Dependency(val groupId: String, val artifactId: String, val version: String) {

    fun getName() = "$artifactId-$version"

    fun downloadIfNecessary(repoUrl: String) {
        val fileInRepo = getDownloadedFile()
        if (!fileInRepo.exists())
            downloadFile(repoUrl)
    }

    private fun downloadFile(repoUrl: String) {
        Downloader().userAgentDownload(getDownloadURL(repoUrl), getDownloadedFile())
    }

    fun getDownloadedFile(): File {
        val replacedGroupId = groupId.replace(".", "/")
        val replacedArtifactId= artifactId.replace(".", "/")
        val path = "$replacedGroupId/$replacedArtifactId/$version/$artifactId-$version.jar"
        return File(LOCAL_REPO, path)
    }

    private fun getDownloadURL(repoUrl: String): String {
        return getUrlWithoutExtension(repoUrl) + ".jar"
    }

    private fun getMainURL(repoUrl: String): String {
        return repoUrl + groupId.replace("\\.".toRegex(), "/") + "/" + artifactId + "/"
    }

    private fun getUrlWithoutExtension(repoUrl: String): String {
        return getMainURL(repoUrl) + version + "/" + artifactId + "-" + version
    }

    @Throws(IOException::class)
    fun download(repoUrl: String, downloadFile: File) {
        Downloader().userAgentDownload(this.getDownloadURL(repoUrl), downloadFile)
    }

    fun toAetherArtifact(): DefaultArtifact {
        return DefaultArtifact("$groupId:$artifactId:$version")
    }

    companion object {
        fun fromAetherArtifact(artifact: Artifact): Dependency {
            return Dependency(artifact.groupId, artifact.artifactId, artifact.version)
        }

        fun fromSingleString(string: String): Dependency {
            val array = string.split(":")
            return Dependency(array[0], array[1], array[2])
        }

        const val LOCAL_REPO = "local-repo/"
    }

}