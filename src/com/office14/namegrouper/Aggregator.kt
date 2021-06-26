package com.office14.namegrouper

import java.io.File

class Aggregator (private val provider:AuthorsProvider<out AuthorRecord>, private val collector:AuthorsCollector<Cluster>, private val sanitizer: Sanitizer) {

    fun clusterByGroups(logFileName:String){
        //val outFile = File(outFileName)
        //val logFile = File(logFileName)

        //if (outFile.exists()) outFile.delete()
        //if (logFile.exists()) logFile.delete()

        val clusters = mutableListOf<Cluster>()

        val rows = sanitizer.sanitize( provider.provide())

        println("start grouping records")
        val start = System.currentTimeMillis()
        for ((count, author) in rows.withIndex()){

            if (count != 0 && count % 10000 == 0)
                println("first $count records has been grouped")

            var match = false

            for (cluster in clusters){
                val pair = cluster.canAdd(author)
                if (pair.first) {
                    if (pair.second){
                        cluster.add(cluster.presenter)
                        cluster.updatePresenter(author)
                    }
                    else
                        cluster.add(author)

                    match = true
                    break
                }
            }
            if (!match) clusters.add(Cluster(author))
        }

        println("grouping complete in ${(System.currentTimeMillis()-start)/1000} sec")

        /*val outFileWriter = outFile.bufferedWriter()
        val logFileWriter = logFile.bufferedWriter()

        for (cluster in clusters){
            for (name in cluster.toList()) outFileWriter.appendLine(name)
            outFileWriter.appendLine()
        }
        outFileWriter.flush()
        outFileWriter.close()

        logFileWriter.flush()
        logFileWriter.close()*/

        collector.collect(clusters)
    }

}