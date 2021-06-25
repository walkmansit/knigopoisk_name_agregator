package com.office14.namegrouper

import java.io.File

class Sanitizer(private val invalidFileName:String) {

    companion object {
        private val validChars = "абвгдеёжзийклмнопрстуфхцчшщъыьэюяАБВГДЕЁЖЗИКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ -.‘".toSet()

        //A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z.
        private val engChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toSet()

        private val illegalChars = ",;()".toSet()
    }

    fun sanitize(authors:Iterable<AuthorRecord>) : Iterable<AuthorRecord> {

        val result = mutableListOf<AuthorRecord>()

        var  validCount = 0
        var  invalidCount = 0

        val invalidFile = File(invalidFileName)

        //val outFileWriter = outFile.bufferedWriter()
        val invalidFileWriter = invalidFile.bufferedWriter()


        /*if (outFile.exists()) outFile.delete()
        outFile.createNewFile()*/
        if (invalidFile.exists()) invalidFile.delete()
        invalidFile.createNewFile()

        var totalCount = 0
        for (author in authors){
            totalCount++

            val (valid, error) = validateSanityName(author.value)
            if (valid) {
                validCount++
                result.add(author)
                //outFileWriter.appendLine(it)
            }
            else {
                invalidCount++
                invalidFileWriter.appendLine("$author - $error")
            }
        }

        /*outFileWriter.flush()
        outFileWriter.close()*/

        invalidFileWriter.flush()
        invalidFileWriter.close()

        println("Sanitizer: total:$totalCount valid:$validCount invalid:$invalidCount")
        return result
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