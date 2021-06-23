package com.office14.namegrouper

import java.io.File
import java.lang.Exception
import java.lang.StringBuilder

class Agregator {

    companion object {
        private val validChars = "абвгдеёжзийклмнопрстуфхцчшщъыьэюяАБВГДЕЁЖЗИКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ -.‘".toSet()

        //A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z.
        private val engChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz,;()".toSet()
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

    fun clusterByGroups(inpFileName:String, outFileName:String){

        val outFile = File(outFileName)
        if (outFile.exists()) outFile.delete()

        val clusters = mutableListOf<Cluster>()

        var count = 0
        File(inpFileName).bufferedReader().forEachLine {

            if (++count > 10000)
                return@forEachLine


            var match = false

            for (cluster in clusters){
                val pair = cluster.canAdd(it)
                if (pair.first) {
                    cluster.add(it)
                    if (pair.second) cluster.updatePresenter(it)
                    match = true
                    break
                }
            }
            if (!match) clusters.add(Cluster(it))


        }

        val outFileWriter = outFile.bufferedWriter()

        for (cluster in clusters){
            for (name in cluster.toList()) outFileWriter.appendLine(name)
            outFileWriter.appendLine()
        }
        outFileWriter.flush()
        outFileWriter.close()
    }


    private fun validateSanityName(name:String): Pair<Boolean,String> {

        if (name.length < 3 ) return false to "name is too short"

        for (ch in name){
            if (engChars.contains(ch)) return false to "character $ch is eng char"
            if (!validChars.contains(ch)) return false to "character $ch is not valid"
        }

        return true to "is valid"
    }

    private class Cluster(var presenter:String){

        private var presenterTrimmedBase = trimValue(presenter)

        fun updatePresenter(value:String){
            presenter = value
            presenterTrimmedBase = trimValue(presenter)
        }

        fun canAdd(candidate:String) : Pair< Boolean, Boolean > {

            var reverted = false
            var candidateTrimmed = trimValue(candidate)
            var presenterTrimmed = trimValue(presenter)

            if (candidateTrimmed.length > presenterTrimmed.length) {
                val v = candidateTrimmed
                candidateTrimmed = presenterTrimmed
                presenterTrimmed = v
                reverted = !reverted

            }

                    //var presenterPartsPrep = if (presenterTrimmed.contains(',')||presenterTrimmed.contains(';')) presenterTrimmed.split(',',';')[0] else  presenterTrimmed
                    //var candidatePartsPrep = if (candidateTrimmed.contains(',')||candidateTrimmed.contains(';')) candidateTrimmed.split(',',';')[0] else  candidateTrimmed

            var presenterParts = candidateTrimmed.split(' ').filter { it != "" }
            var candidateParts = presenterTrimmed.split(' ').filter { it != "" }
            if (candidateParts.size > presenterParts.size) {
                val v = candidateParts
                candidateParts = presenterParts
                presenterParts = v
                reverted = !reverted
            }

            if (presenterParts.size == candidateParts.size){
                when (presenterParts.size){
                    //1 -> return matchParts(presenterParts,candidateParts, mapOf(0 to 0)) to reverted
                    1 -> return matchPartsForMap(presenterParts,candidateParts, map_1_1 ) to reverted
                    //2 -> return (matchParts(presenterParts,candidateParts, mapOf(0 to 0,1 to 1)) ||
                    //        matchParts(presenterParts,candidateParts, mapOf(0 to 1,1 to 0))) to reverted
                    2 -> return matchPartsForMap(presenterParts,candidateParts, map_2_2 ) to reverted
                    //3 -> return (matchParts(presenterParts,candidateParts, mapOf(0 to 0, 1 to 1, 2 to 2)) ||
                    //        matchParts(presenterParts,candidateParts, mapOf(0 to 2, 1 to 0, 2 to 1)) ||
                    //        matchParts(presenterParts,candidateParts, mapOf(0 to 1, 1 to 2, 2 to 0))) to reverted
                    3 -> return matchPartsForMap(presenterParts,candidateParts, map_3_3 ) to reverted
                }
            }

            when {
                presenterParts.size == 3 && candidateParts.size == 2 ->
                    //return (matchParts(presenterParts,candidateParts, mapOf(0 to 0,2 to 1)) ||
                    //    matchParts(presenterParts,candidateParts, mapOf(0 to 1,2 to 0)) ||
                    //    matchParts(presenterParts,candidateParts, mapOf(0 to 0,1 to 1)) ||
                    //    matchParts(presenterParts,candidateParts, mapOf(0 to 1,1 to 0)) ) to reverted
                    return matchPartsForMap(presenterParts,candidateParts, map_3_2 ) to reverted
                presenterParts.size == 3 && candidateParts.size == 1 ->
                    //return (matchParts(presenterParts,candidateParts, mapOf(0 to 0)) ||
                    //    matchParts(presenterParts,candidateParts, mapOf(2 to 0))) to reverted
                    return matchPartsForMap(presenterParts,candidateParts, map_3_1 ) to reverted
                presenterParts.size == 2 && candidateParts.size == 1 ->
                    //return (matchParts(presenterParts,candidateParts, mapOf(0 to 0)) ||
                    //    matchParts(presenterParts,candidateParts, mapOf(1 to 0))) to reverted
                    return matchPartsForMap(presenterParts,candidateParts, map_2_1 ) to reverted
            }

            return false to reverted
        }

        fun add(value: String) = list.add(value)

        private val list:MutableList<String> = mutableListOf()

        private fun clearMatchResultMap(){
            for (i in 0..2)
                for (j in 0..2)
                    for (k in 0..1)
                        matchResultMap[i][j][k] = false


        }

        fun toList()  = sequence {
            yield(presenter)
            list.forEach {
                yield(it)
            }
        }

        private fun trimValue(value:String) : String {
            val sb = StringBuilder()
          /*  var ignore = false

            var prev:Char? = null
            for ( (idx,char) in value.withIndex()){

                when(char){
                    //'.' -> continue
                    //',' -> if(!ignore) return sb.toString()
                    //';' -> if(!ignore) return sb.toString()
                    //'(' -> ignore = true
                    //')' -> ignore = false
                    ' ' -> if (!ignore && prev != ' '&& idx != 0 && idx!=value.length-1) {
                        sb.append(char)
                    }
                    else -> {
                        if (!ignore){
                            sb.append(char)
                        }

                    }
                }
                prev = char
            }*/

            for (ch in value)
                if (ch !='.') sb.append(ch)


            return sb.toString()
        }

        //triple.third -> full match required, else prefix match is enough
        private fun matchPartsForMap(presenterParts:List<String>, candidateParts:List<String>,matchRequiredListMap:List<List<Triple<Int,Int,Boolean>>>) : Boolean {
            val matchMap = getMatchMap(presenterParts,candidateParts)
            val result =  matchRequiredListMap.any {  matchRequiredMap ->  matchRequiredMap.all { triple -> matchMap[triple.first][triple.second][if (triple.third) 0 else 1] }}
            clearMatchResultMap()
            return result
        }

        //arrray[i][j][0] = true -> full match, arrray[i][j][1] = true - pref match
        private fun getMatchMap(presenterParts:List<String>,candidateParts:List<String>) : Array<Array<Array<Boolean>>> {

            try {

                for ((prIdx, presenter) in presenterParts.withIndex()) {

                    if (prIdx == 3 ) return matchResultMap

                    val candIdxArr = Array(3) { 0 }

                    for (prCh in presenter) {
                        if (candidateParts.size < 3) candIdxArr[2] = -1
                        if (candidateParts.size < 2) candIdxArr[1] = -1
                        for (c in 0..2) {
                            if (candIdxArr[c] != -1 && candIdxArr[c] != candidateParts[c].length) {
                                if (prCh == candidateParts[c][candIdxArr[c]])
                                    candIdxArr[c] = candIdxArr[c] + 1
                                else candIdxArr[c] = -1
                            }
                        }
                    }

                    for (c in 0..2) {
                        if (candIdxArr[c] != -1)
                            matchResultMap[prIdx][c][0] = candidateParts[c].length <= candIdxArr[c] && presenter.length == candIdxArr[c]
                        matchResultMap[prIdx][c][1] = candIdxArr[c] > 0
                    }
                }
            }
            catch (ex:Exception){
                val y = 0
            }

            return matchResultMap
        }

        //private fun matchParts(presenterParts:List<String>, candidateParts:List<String>, matchRequiredMap:Map<Int,Int>) =
        //    matchRequiredMap.all { entry -> matchAtomValue(presenterParts[entry.key],candidateParts[entry.value]) }

        //private fun matchAtomValue(a:String,b:String)  = (a.startsWith(b,true))

        companion object{
            private val trimChars = setOf(',','.')

            private val matchResultMap : Array<Array<Array<Boolean>>> = Array(3) { Array(3) { Array(2) { false } } }

            private val map_1_1 =  listOf(listOf(Triple(0,0,true)))  // Сидоров - Сидоров
            private val map_2_2 = listOf(
                    listOf(Triple(0,0,true),Triple(1,1,true)), // Сидоров Александр -> Сидоров Александр
                    listOf(Triple(0,1,true),Triple(1,0,true)), // Сидоров Александр -> Александр Сидоров
                    listOf(Triple(0,0,true),Triple(1,1,false)), // Сидоров Александр -> Сидоров А
                    listOf(Triple(0,1,true),Triple(1,0,false))) // Сидоров Александр -> А Сидоров
            private val map_3_3 = listOf (
                    listOf(Triple(0,0,true),Triple(1,1,false),Triple(2,2,false)), // Сидоров Александр Петрович -> Сидоров А П
                    listOf(Triple(0,2,false),Triple(1,0,false),Triple(2,1,false)),// Сидоров Александр Петрович -> А П Сидоров
                    listOf(Triple(0,0,false),Triple(1,1,false),Triple(2,2,true)),// Александр Петрович Сидоров -> А П Сидоров
                    listOf(Triple(0,1,false),Triple(1,2,false),Triple(2,0,true)))// Александр Петрович Сидоров -> Сидоров А П
            private val map_2_1 = listOf(
                    listOf(Triple(0,0,true)), // Сидоров Александр -> Сидоров
                    listOf(Triple(1,0,true)))  // Александр Сидоров -> Сидоров
            private val map_3_1 = listOf(
                    listOf(Triple(0,0,true)), // Сидоров Александр Петрович -> Сидоров
                    listOf(Triple(2,0,true)))  // Александр Петрович Сидоров -> Сидоров
            private val map_3_2 = listOf(
                    listOf(Triple(0,0,false),Triple(2,1,true)), // Александр Петрович Сидоров -> А Сидоров
                    listOf(Triple(0,1,true),Triple(1,0,false)), // Сидоров Александр Петрович -> А Сидоров
                    listOf(Triple(0,0,true),Triple(1,1,false)), // Сидоров Александр Петрович -> Сидоров А
                    listOf(Triple(0,1,false),Triple(2,0,true))) // Александр Петрович Сидоров -> Сидоров А

        }
    }
}