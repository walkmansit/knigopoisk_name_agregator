package com.office14.namegrouper

import java.lang.Exception
import java.sql.SQLException
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

class AuthorsCollectorDb(private val dbManager: DbManager,private val dbName:String) : AuthorsCollector<Cluster> {

    override fun collect(clusters: Iterable<Cluster>) {
        dbManager.connectToServer()

        println("AuthorsCollectorDb start updating records")
        val start = System.currentTimeMillis()

        val groupedRecords = groupProductsByDatabase(clusters)


        var total = 0
        groupedRecords.forEach { (table, records) ->
            val  batchData:MutableList<List<BatchUnit>> = mutableListOf()
            for ((idx,record) in records.withIndex()){
                val batchRow:List<BatchUnit> = listOf(BatchUnit(1,FieldType.STRING,record.groupAuthor),BatchUnit(2,FieldType.INT,record.id))
                batchData.add(batchRow)

                //execute every 1000 records
                if (idx != 0 && idx % 1000 == 0){
                    val queryTemplate = getUpdateProductTemplate(table)
                    dbManager.executeBatch(queryTemplate,batchData)
                    batchData.clear()
                    if (total % 10000 == 0)
                        println("first $total records has been updated in db")
                }
                total++
            }
            //execute last data
            if (batchData.isNotEmpty()){
                val queryTemplate = getUpdateProductTemplate(table)
                dbManager.executeBatch(queryTemplate,batchData)
            }
        }

        println("AuthorsCollectorDb updating records complete. total updated = $total in ${(System.currentTimeMillis()-start)/1000} sec")
    }

    private fun groupProductsByDatabase(clusters: Iterable<Cluster>) : MutableMap<String,MutableList<AuthorRecordDb>> {
        val result:MutableMap<String,MutableList<AuthorRecordDb>> = mutableMapOf()

        clusters.forEach { cluster -> cluster.members.forEach { authorRecord ->
                val record = authorRecord as AuthorRecordDb

                record.groupAuthor = cluster.presenter.value //обновление автора группы у записи

                if(result.containsKey((record).table)){
                    result[record.table]!!.add(record)
                }
                else {
                    result[record.table] = mutableListOf(record)
                }
            }
        }
        return result
    }


    private fun getUpdateProductTemplate(table:String) = "UPDATE `$dbName`.$table product SET product.group_author = ? WHERE product.id = ?"

    private fun getUpdateNullGroupQuery(table:String) =
            "UPDATE `$dbName`.$table product SET product.group_author = product.author WHERE product.group_author = NULL"

    //private fun composeAuthorUpdateQuery(record: AuthorRecordDb, newAuthor:String) =           ""
            //"UPDATE $dbName.${record.table} product SET product.product_properties = JSON_SET(product.product_properties,\"\$.author\",\"${newAuthor}\") WHERE product.id = ${record.id}"
}