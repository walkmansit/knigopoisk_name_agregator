package com.office14.namegrouper

import java.sql.*
import java.util.*

class DbManager {

    private var conn: Connection? = null

    fun init() : Boolean {
        val connectionProps = Properties()
        connectionProps["user"] = "root"
        connectionProps["password"] = "136316iq"
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance()
            conn = DriverManager.getConnection(
                    "jdbc:" + "mysql" + "://" +
                            "127.0.0.1" +
                            ":" + "3306" + "/" +
                            "",
                    connectionProps)
            return true
        } catch (ex: SQLException) {
            // handle any errors
            ex.printStackTrace()
            return false
        } catch (ex: Exception) {
            // handle any errors
            ex.printStackTrace()
            return false
        }
    }

    fun execute(){
        var stmt: Statement? = null
        var resultset: ResultSet? = null

        try {
            stmt = conn!!.createStatement()
            resultset = stmt!!.executeQuery("SELECT bp.id,bp.product_properties FROM knigopoiskdb.book24_products bp LIMIT 100")

            while (resultset?.next() == true){
                resultset.getInt("id")
            }

           /* if (stmt.execute("SELECT bp.id,bp.product_properties FROM knigopoiskdb.book24_products bp LIMIT 100")) {
                resultset = stmt.resultSet
            }*/

            /*while (resultset!!.next()) {
                println(resultset.getString("id"))
            }*/
        } catch (ex: SQLException) {
            // handle any errors
            ex.printStackTrace()
        }
    }
}