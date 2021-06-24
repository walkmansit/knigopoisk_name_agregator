package com.office14.namegrouper

import java.io.File
import kotlin.streams.toList

class AuthorsProviderFromFile(private val inpFileName:String) : AuthorsProvider<AuthorRecordText> {

    override fun provide(): Iterable<AuthorRecordText> {
        return File(inpFileName).bufferedReader().lines().toList().map { AuthorRecordText(it) }
        //return File(inpFileName).bufferedReader().lines().toList().map { AuthorRecordText(it) }
    }

}