package com.pluxity.file.strategy.storage

interface StorageStrategy {
    @Throws(Exception::class)
    fun save(context: FileProcessingContext): String

    @Throws(Exception::class)
    fun persist(context: FilePersistenceContext): String
}
