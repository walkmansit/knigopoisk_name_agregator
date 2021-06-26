package com.office14.namegrouper

import java.sql.*
import java.util.*

class DbManager {

    private var conn: Connection? = null

    fun connectToServer() : Boolean {

        println("connecting to mysql server...")

        val connectionProps = Properties()
        connectionProps["user"] = "nagaevav" //"root" //
        connectionProps["password"] = "nJNOIPOP{230f9hj}|)(J3fk2ck023jf-fj2]f23" //"136316iq"
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance()
            conn = DriverManager.getConnection(
                    "jdbc:mysql://185.228.235.67:3306/",connectionProps) //"jdbc:mysql://185.228.235.67:3306/"
            println("connecting to mysql server complete")
            return true
        } catch (ex: SQLException) {
            // handle any errors
            println("connecting to mysql server failed")
            ex.printStackTrace()
            return false
        } catch (ex: Exception) {
            // handle any errors
            println("connecting to mysql server failed")
            ex.printStackTrace()
            return false
        }
    }

    fun execute(query:String) : ResultSet ? {
        var statement: Statement? = null

        return try {
            statement = conn!!.createStatement()
            statement!!.executeQuery(query)
        } catch (ex: SQLException) {
            // handle any errors
            ex.printStackTrace()
            null
        }
        /*finally {
            statement?.close()
        }*/
    }

    fun executeUpdate(query:String) : Int {
        var statement: Statement? = null

        return try {
            statement = conn!!.createStatement()
            statement!!.executeUpdate(query)
        } catch (ex: SQLException) {
            // handle any errors
            ex.printStackTrace()
            0
        }
    }

    fun executeBatch(queryTemplate:String,batchData:List<List<BatchUnit>>) : IntArray {
        var statement: Statement? = null

        return try {
            statement = conn!!.prepareStatement(queryTemplate)

            for (row in batchData){
                for (unit in row){
                    when(unit.type){
                        FieldType.INT -> statement.setInt(unit.idx,unit.value as Int)
                        FieldType.STRING -> statement.setString(unit.idx,unit.value as String)
                    }
                }
                statement.addBatch()
            }

            statement.executeBatch()
        } catch (ex: SQLException) {
            // handle any errors
            ex.printStackTrace()
            intArrayOf(0)
        }
        finally {
            statement?.close()
        }
    }
}
