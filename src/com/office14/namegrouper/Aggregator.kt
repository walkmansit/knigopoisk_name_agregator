package com.office14.namegrouper

import java.io.File
import java.lang.Exception
import java.lang.StringBuilder

class Aggregator {

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

    fun clusterByGroups(inpFileName:String, outFileName:String,logFileName:String){

        val outFile = File(outFileName)
        val logFile = File(logFileName)

        if (outFile.exists()) outFile.delete()
        if (logFile.exists()) logFile.delete()

        val clusters = mutableListOf<Cluster>()

        var count = 0
        File(inpFileName).bufferedReader().forEachLine {

            if (++count > 10000)
                return@forEachLine

            var match = false

            for (cluster in clusters){
                val pair = cluster.canAdd(it)
                if (pair.first) {
                    if (pair.second){
                        cluster.add(cluster.presenter)
                        cluster.updatePresenter(it)
                    }
                    else
                        cluster.add(it)

                    match = true
                    break
                }
            }
            if (!match) clusters.add(Cluster(it))


        }

        val outFileWriter = outFile.bufferedWriter()
        val logFileWriter = logFile.bufferedWriter()

        for (cluster in clusters){
            for (name in cluster.toList()) outFileWriter.appendLine(name)
            outFileWriter.appendLine()
        }
        outFileWriter.flush()
        outFileWriter.close()

        logFileWriter.flush()
        logFileWriter.close()
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

    private class Cluster(var presenter:String){

        private var presenterTrimmedBase = trimValue(presenter)

        fun updatePresenter(value:String){
            presenter = value
            presenterTrimmedBase = trimValue(presenter)
        }

        //определяет можем ли мы добавить представителя в группу
        //если кандидат матчится с представителеи и заменяет его также проверяются матчи нового представителя с каждым кандидатом //TODO
        fun canAdd(candidate:String) : Pair< Boolean, Boolean > {

            var reverted = false
            var candidateTrimmed = trimValue(candidate)
            var presenterTrimmed = trimValue(presenter)

            var presenterParts = presenterTrimmed.split(' ')
            var candidateParts = candidateTrimmed.split(' ')

            //приоритет замены представителя новым кандидатом определяется сначачала по количеству его юнитов потом (если они равны) по общему размеру
            if (candidateParts.size > presenterParts.size) {
                val v = candidateParts
                candidateParts = presenterParts
                presenterParts = v
                reverted = !reverted
            }
            else if (candidateParts.size == presenterParts.size && candidateTrimmed.length > presenterTrimmed.length) {
                val v = candidateParts
                candidateParts = presenterParts
                presenterParts = v
                reverted = !reverted

            }

            var isMatch = matchParts(presenterParts,candidateParts)

            if (isMatch && reverted)
               isMatch =  matchNewPresenterWithList(presenterParts)

            return isMatch to reverted
        }

        //матчит все члены группы (кроме представителя) с новым представителемЮ при условии что новый приоритетнее по размеру
        private fun matchNewPresenterWithList(presenterParts:List<String>) =
                list.all { author-> matchParts(presenterParts,trimValue(author).split(' ')) }

        fun add(value: String) = list.add(value)

        private val list:MutableList<String> = mutableListOf()

       /* private fun clearMatchResultMap(){
            for (i in 0..2)
                for (j in 0..2)
                    for (k in 0..1)
                        matchResultMap[i][j][k] = false


        }*/

        fun toList()  = sequence {
            yield(presenter)
            list.forEach {
                yield(it)
            }
        }

        private fun trimValue(value:String) : String {
            val sb = StringBuilder()
            var prev:Char? = null
          /*  var ignore = false


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

            for ( (idx,ch) in value.withIndex()) {

                if (ch ==' ' && (idx == 0 || idx == value.length-1 || prev == ' ')) {
                    prev = ch
                    continue
                }

                if (ch != '.') sb.append(ch)
                prev = ch
            }


            return sb.toString()
        }

        //матчит представителя и кандидата при условии что представитель правильно предопределен в соответстви с приоритетом
        private fun matchParts(presenterParts:List<String>, candidateParts:List<String>) : Boolean {
            val map11 =  listOf(listOf(Triple(0,0,true)))  // Сидоров - Сидоров
            val map22 = listOf(
                    listOf(Triple(0,0,true),Triple(1,1,true)), // Сидоров Александр -> Сидоров Александр
                    listOf(Triple(0,1,true),Triple(1,0,true)), // Сидоров Александр -> Александр Сидоров
                    listOf(Triple(0,0,true),Triple(1,1,false)), // Сидоров Александр -> Сидоров А
                    listOf(Triple(0,1,true),Triple(1,0,false))) // Сидоров Александр -> А Сидоров
            val map33 = listOf (
                    listOf(Triple(0,0,true),Triple(1,1,false),Triple(2,2,false)), // Сидоров Александр Петрович -> Сидоров А П
                    listOf(Triple(0,2,false),Triple(1,0,false),Triple(2,1,false)),// Сидоров Александр Петрович -> А П Сидоров
                    listOf(Triple(0,0,false),Triple(1,1,false),Triple(2,2,true)),// Александр Петрович Сидоров -> А П Сидоров
                    listOf(Triple(0,1,false),Triple(1,2,false),Triple(2,0,true)))// Александр Петрович Сидоров -> Сидоров А П
            val map21 = listOf(
                    listOf(Triple(0,0,true)), // Сидоров Александр -> Сидоров
                    listOf(Triple(1,0,true)))  // Александр Сидоров -> Сидоров
            val map31 = listOf(
                    listOf(Triple(0,0,true)), // Сидоров Александр Петрович -> Сидоров
                    listOf(Triple(2,0,true)))  // Александр Петрович Сидоров -> Сидоров
            val map32 = listOf(
                    listOf(Triple(0,0,false),Triple(2,1,true)), // Александр Петрович Сидоров -> А Сидоров
                    listOf(Triple(0,1,true),Triple(1,0,false)), // Сидоров Александр Петрович -> А Сидоров
                    listOf(Triple(0,0,true),Triple(1,1,false)), // Сидоров Александр Петрович -> Сидоров А
                    listOf(Triple(0,1,false),Triple(2,0,true))) // Александр Петрович Сидоров -> Сидоров А

            return when {
                presenterParts.size == candidateParts.size -> return when (presenterParts.size){
                    1 -> matchPartsWithMap(presenterParts,candidateParts, map11 )
                    2 -> matchPartsWithMap(presenterParts,candidateParts, map22 )
                    3 -> matchPartsWithMap(presenterParts,candidateParts, map33 )
                    else -> false
                }
                presenterParts.size == 3 && candidateParts.size == 2 -> matchPartsWithMap(presenterParts,candidateParts, map32 )
                presenterParts.size == 3 && candidateParts.size == 1 -> matchPartsWithMap(presenterParts,candidateParts, map31 )
                presenterParts.size == 2 && candidateParts.size == 1 -> matchPartsWithMap(presenterParts,candidateParts, map21 )
                else -> false
            }
        }

        //матчит презентера с кандидатом в соответствии с заданным мапом
        //triple.third -> full match required, else prefix match is enough
        private fun matchPartsWithMap(presenterParts:List<String>, candidateParts:List<String>,matchRequiredListMap:List<List<Triple<Int,Int,Boolean>>>) : Boolean {
            val matchMap = getMatchMap(presenterParts,candidateParts)
            return  matchRequiredListMap.any {  matchRequiredMap ->  matchRequiredMap.all { triple -> matchMap[triple.first][triple.second][if (triple.third) 0 else 1] }}
        }


        /*
            Возвращает масссив совпадений 3х3 array[i][j][k]
            где i и j индексы в представителе и кандидате соответственно, k - тип совпадения
        */
        //arrray[i][j][0] = true -> full match, полное совпадение
        //arrray[i][j][1] = true - pref match, кандидат частично матчится на предстаителя
        private fun getMatchMap(presenterParts:List<String>, candidateParts:List<String>) : Array<Array<Array<Boolean>>> {

            val matchResultMap : Array<Array<Array<Boolean>>> = Array(3) { Array(3) { Array(2) { false } } }
            //clearMatchResultMap()
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
            //private val trimChars = setOf(',','.')

            //private val matchResultMap : Array<Array<Array<Boolean>>> = Array(3) { Array(3) { Array(2) { false } } }

            /*private val map_1_1 =  listOf(listOf(Triple(0,0,true)))  // Сидоров - Сидоров
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
                    listOf(Triple(0,1,false),Triple(2,0,true))) // Александр Петрович Сидоров -> Сидоров А*/

        }
    }
}