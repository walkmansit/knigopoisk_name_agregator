package com.office14.namegrouper

class AuthorRecordDb(override var value: String, val id:Int, val table:String,var groupAuthor:String = value) : AuthorRecord