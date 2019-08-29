package com.darkraha.backend.helpers

import java.util.concurrent.locks.ReentrantLock

object LockManager {

    internal val locks = mutableMapOf<Any, ReentrantLock>()


    fun lock(res: Any): ReentrantLock {
        var ret: ReentrantLock? = null

        synchronized(locks) {
            ret = locks.get(res)
            if (ret == null) {
                ret = ReentrantLock()
                locks.put(res, ret as ReentrantLock)
            }
        }

        ret!!.lock()
        return ret!!
    }


    fun unLock(res: Any) {
        synchronized(locks) {
            val ret = locks.get(res)
            if (ret != null && ret.holdCount > 0) {
                ret.unlock()
                if (!ret.isLocked) {
                    locks.remove(res)
                }
            }
        }
    }


    fun size(): Int {
        synchronized(locks) {
            return locks.size;
        }
    }

}