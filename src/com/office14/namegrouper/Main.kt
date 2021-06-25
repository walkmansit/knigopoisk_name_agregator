package com.office14.namegrouper

class Main {

    companion object {
        @JvmStatic fun main(args: Array<String>){
            val start = System.currentTimeMillis()

            val dbManager = DbManager()
            //val provider = AuthorsProviderFromFile("file_out/valid_authors.txt")

            val dbName = "low-cost-books" //knigopoiskdb
            val tableNames = listOf("book24_products","chaconne_products","chitai_gorod_products","labirint_products","ozon_products")
            //val tableNames = listOf("labirint_products")
            val provider = AuthorsProviderDb(dbManager,dbName,tableNames)

            //val collector = AuthorsCollectorToFile("file_out/result_groups.txt")
            val collector = AuthorsCollectorDb(dbManager,dbName)

            val sanitizer = Sanitizer("file_out/invalid_authors.txt")

            val agr = Aggregator(provider,collector,sanitizer)
            agr.clusterByGroups("file_out/exc_log.txt")
            //agr.sanitize(,"file_out/valid_authors.txt","file_out/invalid_authors.txt")

            //agr.clusterByGroups("file_out/valid_authors.txt","file_out/result_groups.txt","file_out/exc_log.txt")
            println("Done in ${(System.currentTimeMillis()-start)/1000} sec")

        }
    }
}