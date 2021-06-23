package com.office14.namegrouper

class Main {

    companion object {
        @JvmStatic fun main(args: Array<String>){

            val start = System.currentTimeMillis()

            val agr = Aggregator()
            //agr.sanitize("file_in/all_ru_authors.txt","file_out/valid_authors.txt","file_out/invalid_authors.txt")

            agr.clusterByGroups("file_out/valid_authors.txt","file_out/result_groups.txt","file_out/exc_log.txt")
            println("Done in ${(System.currentTimeMillis()-start)/1000} sec")
        }
    }
}