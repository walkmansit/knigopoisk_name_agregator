package com.office14.namegrouper

interface AuthorsProvider<AuthorRecord> {
    fun provide() : Iterable<out AuthorRecord>
}