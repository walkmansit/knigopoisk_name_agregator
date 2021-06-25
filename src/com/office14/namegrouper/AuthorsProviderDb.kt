package com.office14.namegrouper

import com.mysql.cj.jdbc.result.ResultSetImpl
import java.lang.StringBuilder

class AuthorsProviderDb(private val dbManager: DbManager,private val dbName:String, private val tableNames:Iterable<String>) : AuthorsProvider<AuthorRecordDb> {

    override fun provide(): Iterable<AuthorRecordDb> {

        val result = mutableListOf<AuthorRecordDb>()
        if (dbManager.connectToServer()){



            println("AuthorsProviderDb start fetching data from $dbName...")

            var emptyAuthorCount = 0
            for (table in tableNames){
                var localEmptyCount = 0
                var rowsAdded = 0
                println("AuthorsProviderDb start fetching data from $table...")
                val resultSet = dbManager.execute(composeSelectQueryByDbName(dbName,table))
                resultSet?.let {
                    while (it.next()) {
                        val id = it.getInt("id")
                        val author = it.getString("author")
                        if (author != null) {
                            //author = sanitizeAuthor(author)
                            if (author.isNotEmpty()) {
                                result.add(AuthorRecordDb(author, id, table))
                                rowsAdded++
                            }
                            else {
                                localEmptyCount++
                                emptyAuthorCount++
                            }
                        }
                        else {
                            localEmptyCount++
                            emptyAuthorCount++
                        }
                    }
                }

                resultSet?.let {
                    println("AuthorsProviderDb $table rows fetched: ${(resultSet as ResultSetImpl)?.rows.size() ?: 0}; empty authors:$localEmptyCount; rows selected:$rowsAdded")
                }
                println("AuthorsProviderDb fetching data from $table comlete")
            }

            println("AuthorsProviderDb fetching data from $dbName comlete")
            println("AuthorsProviderDb emptyAuthorCount = $emptyAuthorCount")
        }
        return result
    }

    private fun sanitizeAuthor(author:String) : String {
        val sb = StringBuilder()

        for (ch in author)
            if (ch != '"') sb.append(ch)

        return sb.toString()
    }

    private fun composeSelectQueryByDbName(dbName:String,tableName:String) =
            "SELECT product.id, product.author FROM `$dbName`.$tableName product"
            //"SELECT product.id, JSON_EXTRACT(product.product_properties,\"\$.author\") AS author FROM $dbName.$tableName product WHERE LENGTH(product.product_properties) < 65535"
}