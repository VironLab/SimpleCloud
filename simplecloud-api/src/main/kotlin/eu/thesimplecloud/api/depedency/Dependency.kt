package eu.thesimplecloud.api.depedency

import eu.thesimplecloud.api.utils.Downloader
import eu.thesimplecloud.api.utils.WebContentLoader
import java.io.File
import java.io.IOException

data class Dependency(val groupId: String, val artifactId: String, val version: String) {

    companion object {

        val DEPENDENCIES_DIR = File("dependencies/")

        val POM_DIR = File("dependencies/poms/")
    }

    fun getName() = "$artifactId-$version"

    fun getDownloadURL(repoUrl: String): String {
        return getUrlWithoutExtension(repoUrl) + ".jar"
    }

    fun getDownloadedFile(): File {
        return File(DEPENDENCIES_DIR, "${getName()}.jar")
    }

    fun getDownloadedPomFile(): File {
        return File(POM_DIR, "$groupId-$artifactId.pom")
    }

    fun getDownloadedLastVersionFile(): File {
        return File(POM_DIR, "$groupId-$artifactId.lastVersion")
    }


    @Throws(IOException::class)
    fun download(repoUrl: String) {
        return download(repoUrl, getDownloadedFile())
    }
    @Throws(IOException::class)
    fun download(repoUrl: String, downloadFile: File) {
        Downloader().userAgentDownload(this.getDownloadURL(repoUrl), downloadFile)
    }

    fun getPomContent(repoUrl: String): String? {
        return WebContentLoader().loadContent(getUrlWithoutExtension(repoUrl) + ".pom")
    }

    fun getMetaDataContent(repoUrl: String): String? {
        return WebContentLoader().loadContent(getMainURL(repoUrl) + "maven-metadata.xml")
    }

    fun getMainURL(repoUrl: String): String {
        return repoUrl + groupId.replace("\\.".toRegex(), "/") + "/" + artifactId + "/"
    }

    fun getUrlWithoutExtension(repoUrl: String): String {
        return getMainURL(repoUrl) + version + "/" + artifactId + "-" + version
    }

}