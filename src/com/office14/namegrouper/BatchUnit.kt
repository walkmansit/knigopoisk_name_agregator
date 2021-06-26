package com.office14.namegrouper


enum class FieldType { STRING,INT } //add more types if required

class BatchUnit(val idx:Int,val type:FieldType,val value:Any)