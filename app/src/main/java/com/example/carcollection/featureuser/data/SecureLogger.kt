package com.example.carcollection.featureuser.data

import android.util.Log

/**
 * Logger seguro que sanitiza datos sensibles antes de registrar
 * Evita exponer passwords, emails, tokens, etc en los logs
 */
object SecureLogger {
    private const val TAG = "CarCollection"

    // 🔹 Patrones de datos sensibles a remover
    private val SENSITIVE_PATTERNS = listOf(
        Regex("password[\"']?\\s*[:=]\\s*[\"']?[^\"'\\s,}]+", RegexOption.IGNORE_CASE),
        Regex("email[\"']?\\s*[:=]\\s*[^\"'\\s,}]+", RegexOption.IGNORE_CASE),
        Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", RegexOption.IGNORE_CASE),  // Email
        Regex("token[\"']?\\s*[:=]\\s*[\"']?[^\"'\\s,}]+", RegexOption.IGNORE_CASE),
        Regex("uid[\"']?\\s*[:=]\\s*[\"']?([A-Za-z0-9]{20,})[\"']?", RegexOption.IGNORE_CASE),
        Regex("photoUrl[\"']?\\s*[:=]\\s*[^\"'\\s,}]+", RegexOption.IGNORE_CASE),
        Regex("\\\"[a-zA-Z0-9]+@[a-zA-Z0-9.]+\\\""),
    )

    /**
     * Sanitizar un mensaje removiendo datos sensibles
     */
    fun sanitize(message: String): String {
        var sanitized = message
        SENSITIVE_PATTERNS.forEach { pattern ->
            sanitized = sanitized.replace(pattern, "[REDACTED]")
        }
        return sanitized
    }

    /**
     * Log de información segura
     */
    fun info(message: String) {
        val sanitized = sanitize(message)
        Log.i(TAG, sanitized)
    }

    /**
     * Log de advertencia segura
     */
    fun warn(message: String) {
        val sanitized = sanitize(message)
        Log.w(TAG, sanitized)
    }

    /**
     * Log de error segura
     */
    fun error(message: String, throwable: Throwable? = null) {
        val sanitized = sanitize(message)
        if (throwable != null) {
            Log.e(TAG, sanitized, throwable)
        } else {
            Log.e(TAG, sanitized)
        }
    }

    /**
     * Log de debug segura (solo en BuildConfig.DEBUG)
     */
    fun debug(message: String) {
        val sanitized = sanitize(message)
        Log.d(TAG, sanitized)
    }

    /**
     * Log de acciones exitosas (sin sensibles)
     */
    fun success(action: String) {
        Log.i(TAG, "✅ $action")
    }

    /**
     * Log de acciones fallidas (sin sensibles)
     */
    fun failure(action: String, reason: String? = null) {
        val msg = if (reason != null) "❌ $action: ${sanitize(reason)}" else "❌ $action"
        Log.e(TAG, msg)
    }
}

