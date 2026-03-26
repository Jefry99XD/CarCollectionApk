package com.example.carcollection.featurecar.data

/**
 * Estrategia de caché con TTL (Time-To-Live)
 */
data class CacheEntry<T>(
    val value: T,
    val timestamp: Long = System.currentTimeMillis(),
    val ttlMillis: Long
) {
    fun isExpired(): Boolean {
        return (System.currentTimeMillis() - timestamp) > ttlMillis
    }
}

/**
 * Caché genérico con soporte para expiración por tiempo
 * @param ttlMillis Tiempo de vida del caché en milisegundos
 */
class TTLCache<K, V>(private val ttlMillis: Long) {
    private val cache = mutableMapOf<K, CacheEntry<V>>()
    private val lock = Any()

    fun put(key: K, value: V) {
        synchronized(lock) {
            cache[key] = CacheEntry(value, ttlMillis = ttlMillis)
        }
    }

    fun get(key: K): V? {
        synchronized(lock) {
            val entry = cache[key]
            return if (entry != null && !entry.isExpired()) {
                entry.value
            } else {
                cache.remove(key)
                null
            }
        }
    }

    fun invalidateExpired() {
        synchronized(lock) {
            cache.entries.removeAll { (_, entry) -> entry.isExpired() }
        }
    }

    fun clear() {
        synchronized(lock) {
            cache.clear()
        }
    }

    fun getAll(): Map<K, V> {
        synchronized(lock) {
            invalidateExpired()
            return cache.mapValues { it.value.value }
        }
    }
}

