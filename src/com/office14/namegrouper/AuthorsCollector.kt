package com.office14.namegrouper

interface AuthorsCollector<Cluster> {
    fun collect(clusters:Iterable<Cluster>)
}