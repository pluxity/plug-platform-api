package com.pluxity.file.strategy.storage

interface StorageStrategy {
    fun save(context: FileProcessingContext): String

    fun persist(context: FilePersistenceContext): String
}
