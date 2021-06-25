package com.office14.namegrouper

import java.sql.*
import java.util.*

class DbManager {

    private var conn: Connection? = null

    //mysql server: 185.228.235.67:3306
    //nagaevav / nJNOIPOP{230f9hj}|)(J3fk2ck023jf-fj2]f23

    fun connectToServer() : Boolean {

        println("connecting to mysql server...")

        val connectionProps = Properties()
        connectionProps["user"] = "nagaevav"
        connectionProps["password"] = "nJNOIPOP{230f9hj}|)(J3fk2ck023jf-fj2]f23"
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance()
            conn = DriverManager.getConnection(
                    "jdbc:" + "mysql" + "://" +
                            "185.228.235.67" +
                            ":" + "3306" + "/" +
                            "",
                    connectionProps)
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
        var stmt: Statement? = null
        var resultset: ResultSet? = null

        return try {
            stmt = conn!!.createStatement()
            resultset = stmt!!.executeQuery(query)
            resultset
        } catch (ex: SQLException) {
            // handle any errors
            ex.printStackTrace()
            null
        }
    }

    fun executeUpdate(query:String) : Int {
        var stmt: Statement? = null

        return try {
            stmt = conn!!.createStatement()
            stmt!!.executeUpdate(query)


        } catch (ex: SQLException) {
            // handle any errors
            ex.printStackTrace()
            0
        }
    }
}