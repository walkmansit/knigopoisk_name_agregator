package com.office14.namegrouper

import java.io.Console

class Main {

    companion object {
        @JvmStatic fun main(args: Array<String>){
            val agr = Agregator()
            //agr.sanitize("file_in/all_ru_authors.txt","file_out/valid_authors.txt","file_out/invalid_authors.txt")


            agr.clusterByGroups("file_out/valid_authors.txt","file_out/result_groups.txt")
            println("Done!!!")
        }
    }
}