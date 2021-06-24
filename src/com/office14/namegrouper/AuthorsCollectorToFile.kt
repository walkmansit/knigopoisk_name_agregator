package com.office14.namegrouper

import java.io.File

class AuthorsCollectorToFile(private val fileName:String) : AuthorsCollector<Cluster> {
    override fun collect(clusters: Iterable<Cluster>) {
        val outFileWriter = File(fileName).bufferedWriter()

        for (cluster in clusters){
            for (item in cluster.toList()) outFileWriter.appendLine(item.author)
            outFileWriter.appendLine()
        }
        outFileWriter.flush()
        outFileWriter.close()
    }
}