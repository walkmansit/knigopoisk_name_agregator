package com.office14.namegrouper

import java.lang.Exception
import java.sql.SQLException

class AuthorsCollectorDb(private val dbManager: DbManager,private val dbName:String) : AuthorsCollector<Cluster> {

    override fun collect(clusters: Iterable<Cluster>) {
        dbManager.connectToServer()

        println("AuthorsCollectorDb start updating records")

        var total = 0
        for (cluster in clusters){
            for (record in cluster.toList()){
                if (record is AuthorRecordDb){
                    val query = composeAuthorUpdateQuery(record,cluster.presenter.value)
                    try {
                        val count = dbManager.executeUpdate(query)
                        total+=count
                    }
                    catch (ex: SQLException) {
                        // handle any errors
                        println("AuthorsCollectorDb query=$query failed")
                        ex.printStackTrace()
                    } catch (ex: Exception) {
                        // handle any errors
                        println("AuthorsCollectorDb query=$query failed")
                        ex.printStackTrace()
                    }

                }
            }
        }
        println("AuthorsCollectorDb updating records complete. total updated = $total")
    }

    private fun composeAuthorUpdateQuery(record: AuthorRecordDb, newAuthor:String) =
            "UPDATE `$dbName`.${record.table} product SET product.group_author = '$newAuthor' WHERE product.id = ${record.id}"
            //"UPDATE $dbName.${record.table} product SET product.product_properties = JSON_SET(product.product_properties,\"\$.author\",\"${newAuthor}\") WHERE product.id = ${record.id}"
}