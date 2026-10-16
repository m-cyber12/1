package com.mcyber12.aiflowbypasser.downloads
import java.util.concurrent.CopyOnWriteArrayList

class DownloadInfo(
    val name: String,
    val size: Long = 0L,
    val downloaded: Boolean = false
)

object DownloadManager {
    private val downloads = CopyOnWriteArrayList<DownloadInfo>()

    fun startDownload(name: String) {
        val download = DownloadInfo(name)
        synchronized(downloads) {
            downloads.addFirst(download)
        }
    }

    fun listDownloads(limit: Int = 20): List<DownloadInfo> {
        return downloads.take(limit)
    }

    fun markFinished(name: String, size: Long) {
        val download = DownloadInfo(
            name = name,
            size = size,
            downloaded = true
        )
        synchronized(downloads) {
            downloads.removeIf { it.name == name }
            downloads.addFirst(download)
        }
    }

    fun clear() {
        synchronized(downloads) {
            downloads.clear()
        }
    }
}
