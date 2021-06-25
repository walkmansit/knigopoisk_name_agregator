package com.office14.namegrouper

interface AuthorsProvider<out AuthorRecord> {
    fun provide() : Iterable<out AuthorRecord>
}