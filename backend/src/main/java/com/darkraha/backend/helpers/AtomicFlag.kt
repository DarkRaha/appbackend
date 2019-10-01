package com.darkraha.backend.helpers

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class AtomicFlag(argLock: ReentrantLock = ReentrantLock()) {
    val lock: ReentrantLock = argLock

    var _flags: Int = 0

    var flags: Int
        protected set(value) {
            lock.withLock { _flags = value }
        }
        get() {
            lock.withLock { return _flags }
        }


    /**
     * @param values as 1,2,4,8,16,32, etc
     */
    fun setFlag(flagValue: Int) {
        lock.withLock { flags = flags.or(flagValue) }
    }


    /**
     * @param values as 1,2,4,8,16,32, etc
     */
    fun setFlag(v: Boolean, flagValue: Int) {
        lock.withLock { flags = if (v) flags.or(flagValue) else flags.and(flagValue.inv()) }
    }


    fun clearFlag(flagValue: Int) {
        lock.withLock { flags = flags.and(flagValue.inv()) }
    }

    /**
     * @param values as 1,2,4,8,16,32, etc
     */
    fun isFlag(flagValue: Int): Boolean {
        lock.withLock { return flags.and(flagValue) > 0 }
    }


    fun isFlagAndAny(flagValue: Int, flagValue1: Int, flagValue2: Int): Boolean {
        lock.withLock {
            return flags and flagValue > 0
                    && flags and (flagValue1 or flagValue2) > 0
        }
    }

    fun isFlagAndNotAny(flagValue: Int, flagValue1: Int, flagValue2: Int): Boolean {
        lock.withLock {
            return flags and flagValue > 0
                    && !(flags and (flagValue1 or flagValue2) > 0)
        }
    }

    fun isFlagAndAny(flagValue: Int, flagValue1: Int, flagValue2: Int, flagValue3: Int): Boolean {
        lock.withLock { return flags and flagValue > 0 && flags and (flagValue1 or flagValue2 or flagValue3) > 0 }
    }


    fun isFlagAndNotAny(
        flagValue: Int,
        flagValue1: Int,
        flagValue2: Int,
        flagValue3: Int
    ): Boolean {
        lock.withLock { return flags and flagValue > 0 && !(flags and (flagValue1 or flagValue2 or flagValue3) > 0) }
    }


    /**
     * @bits for passing several bits use or like Units.BIT1 or Units.BIT8
     */
    fun isAnyFlag(bits: Int): Boolean {
        lock.withLock {
            return flags.and(bits) > 0
        }
    }

    fun isAnyFlag(flagValue1: Int, flagValue2: Int): Boolean {
        lock.withLock {
            return flags.and(flagValue1 or flagValue2) > 0
        }
    }


    fun isAnyFlag(flagValue1: Int, flagValue2: Int, flagValue3: Int): Boolean {
        lock.withLock {
            return flags.and(flagValue1 or flagValue2 or flagValue3) > 0
        }
    }

    fun setFlagIfNonAny(flagValue: Int, flagValue1: Int, flagValue2: Int): Boolean {
        lock.withLock {
            if (flags and (flagValue1 or flagValue2) == 0) {
                flags = flags or flagValue
                return true
            }
            return false
        }
    }


    fun setFlagIfNonAny(
        flagValue: Int,
        flagValue1: Int,
        flagValue2: Int,
        flagValue3: Int
    ): Boolean {
        lock.withLock {
            if (flags and (flagValue1 or flagValue2 or flagValue3) == 0) {
                flags = flags or flagValue
                return true
            }
            return false
        }
    }

    fun setFlagIfAny(flagValue: Int, flagValue1: Int): Boolean {
        lock.withLock {
            if (flags and flagValue1 > 0) {
                flags = flags or flagValue
                return true
            }
            return false
        }
    }

    fun setFlagIfAny(v: Boolean, flagValue: Int, flagValue1: Int): Boolean {
        lock.withLock {
            if (flags and flagValue1 > 0) {
                flags = if (v) flags or flagValue else flags and flagValue.inv()
                return true
            }
            return false
        }
    }


    fun setFlagIfAny(flagValue: Int, flagValue1: Int, flagValue2: Int): Boolean {
        lock.withLock {
            if (flags and (flagValue1 or flagValue2) > 0) {
                flags = flags or flagValue
                return true
            }
            return false
        }
    }

    /**
     * @param flagValueMust exception will be thrown if these flags cleared
     * @param str string for exception
     */
    fun setFlagMustAnyAndNon(
        flagValue: Int,
        flagValueMust: Int,
        str: String,
        flagValueNo: Int
    ): Boolean {
        lock.withLock {

            if (flags and flagValueMust > 0) {
                if (flags and flagValueNo == 0) {
                    flags = flags or flagValue
                    return true
                }
            } else {
                throw IllegalStateException(str)
            }

            return false
        }
    }


    fun setFlagIfAny(v: Boolean, flagValue: Int, flagValue1: Int, flagValue2: Int): Boolean {
        lock.withLock {
            if (flags and (flagValue1 or flagValue2) > 0) {
                flags = if (v) flags or flagValue else flags and flagValue.inv()
                return true
            }
            return false
        }
    }


    fun setFlagIfAny(flagValue: Int, flagValue1: Int, flagValue2: Int, flagValue3: Int): Boolean {
        lock.withLock {
            if (flags and (flagValue1 or flagValue2 or flagValue3) > 0) {
                flags = flags or flagValue
                return true
            }
            return false
        }
    }

    fun setFlagIfAny(
        v: Boolean,
        flagValue: Int,
        flagValue1: Int,
        flagValue2: Int,
        flagValue3: Int
    ): Boolean {
        lock.withLock {
            if (flags and (flagValue1 or flagValue2 or flagValue3) > 0) {
                flags = if (v) flags or flagValue else flags and flagValue.inv()
                return true
            }
            return false
        }
    }


    fun clear() {
        flags = 0
    }
}