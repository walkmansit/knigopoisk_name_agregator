package com.office14.namegrouper

import java.io.File

class Aggregator (private val provider:AuthorsProvider<out AuthorRecord>, private val collector:AuthorsCollector<Cluster>) {

    companion object {
        private val validChars = "абвгдеёжзийклмнопрстуфхцчшщъыьэюяАБВГДЕЁЖЗИКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ -.‘".toSet()

        //A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z.
        private val engChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toSet()

        private val illegalChars = ",;()".toSet()
    }

    fun sanitize(inpFileName:String,outFileName:String,unvalidFileName:String){

        var  totalCount = 0
        var  validCount = 0
        var  invalidCount = 0

        val inpFileStream = File(inpFileName).inputStream()
        val outFile = File(outFileName)
        val invalidFile = File(unvalidFileName)

        val outFileWriter = outFile.bufferedWriter()
        val invalidFileWriter = invalidFile.bufferedWriter()


        if (outFile.exists()) outFile.delete()
        outFile.createNewFile()
        if (invalidFile.exists()) invalidFile.delete()
        invalidFile.createNewFile()

        inpFileStream.bufferedReader().forEachLine {
            totalCount++

            val (valid, error) = validateSanityName(it)
            if (valid) {
                validCount++
                outFileWriter.appendLine(it)
            }
            else {
                invalidCount++
                invalidFileWriter.appendLine("$it - $error")
            }
        }

        outFileWriter.flush()
        outFileWriter.close()

        invalidFileWriter.flush()
        invalidFileWriter.close()

        println("total:$totalCount valid:$validCount unvalid:$invalidCount")

    }

    fun clusterByGroups(logFileName:String){
        //val outFile = File(outFileName)
        //val logFile = File(logFileName)

        //if (outFile.exists()) outFile.delete()
        //if (logFile.exists()) logFile.delete()

        val clusters = mutableListOf<Cluster>()

        for ((count, author) in provider.provide().withIndex()){

            if (count > 10000)
                return

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

    private fun prepareLogFile(){

    }

    private fun validateSanityName(name:String): Pair<Boolean,String> {

        if (name.length < 3 ) return false to "name is too short"

        for (ch in name){
            if (engChars.contains(ch)) return false to "character $ch is eng char"
            if (illegalChars.contains(ch)) return false to "character $ch is illegal, name might contain several entries"
            if (!validChars.contains(ch)) return false to "character $ch is not valid"
        }

        return true to "is valid"
    }


}