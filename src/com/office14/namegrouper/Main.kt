package com.office14.namegrouper

class Main {

    companion object {
        @JvmStatic fun main(args: Array<String>){

            val provider = AuthorsProviderFromFile("file_out/valid_authors.txt")
            val collector = AuthorsCollectorToFile("file_out/result_groups.txt")

            val start = System.currentTimeMillis()

            val agr = Aggregator(provider,collector)
            agr.clusterByGroups("file_out/exc_log.txt")
            //agr.sanitize(,"file_out/valid_authors.txt","file_out/invalid_authors.txt")

            //agr.clusterByGroups("file_out/valid_authors.txt","file_out/result_groups.txt","file_out/exc_log.txt")
            println("Done in ${(System.currentTimeMillis()-start)/1000} sec")

            /*val db = DbManager()
            if (db.init())
                db.execute()*/
        }
    }
}