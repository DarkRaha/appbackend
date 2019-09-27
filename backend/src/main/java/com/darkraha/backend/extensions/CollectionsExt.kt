package com.darkraha.backend.extensions

/**
 * For android api < 24.
 */
fun <K, T> Map<K, T>.getWithDefault(key: K, defValue: T) = get(key) ?: defValue