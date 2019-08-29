package com.darkraha.backend.helpers

import org.junit.Test

import org.junit.Assert.*

class AtomicFlagTest {


    @Test
    fun setFlag() {

        val f = AtomicFlag()

        f.setFlag(Units.BIT5)
        assertTrue(f.isFlag(Units.BIT5))

        f.clearFlag(Units.BIT5)
        assertTrue(!f.isFlag(Units.BIT5))

        f.setFlag(true, Units.BIT5)
        assertTrue(f.isFlag(Units.BIT5))

        f.setFlag(false, Units.BIT5)
        assertTrue(!f.isFlag(Units.BIT5))

        f.clear()
        assertTrue(f.flags == 0)
    }


    @Test
    fun isFlagAndAny() {
        val f = AtomicFlag()
        f.setFlag(Units.BIT4)
        f.setFlag(Units.BIT7)
        f.setFlag(Units.BIT10)
        f.setFlag(Units.BIT11)

        assertTrue(!f.isFlagAndAny(Units.BIT2, Units.BIT7, Units.BIT10))
        assertTrue(!f.isFlagAndAny(Units.BIT4, Units.BIT5, Units.BIT6))
        assertTrue(f.isFlagAndAny(Units.BIT4, Units.BIT10, Units.BIT6))
        assertTrue(f.isFlagAndAny(Units.BIT4, Units.BIT5, Units.BIT10))
        assertTrue(f.isFlagAndAny(Units.BIT4, Units.BIT10, Units.BIT11))


        assertTrue(!f.isFlagAndAny(Units.BIT2, Units.BIT7, Units.BIT10, Units.BIT3))
        assertTrue(!f.isFlagAndAny(Units.BIT4, Units.BIT5, Units.BIT6, Units.BIT3))
        assertTrue(f.isFlagAndAny(Units.BIT4, Units.BIT10, Units.BIT6, Units.BIT3))
        assertTrue(f.isFlagAndAny(Units.BIT4, Units.BIT5, Units.BIT10, Units.BIT3))
        assertTrue(f.isFlagAndAny(Units.BIT4, Units.BIT10, Units.BIT11, Units.BIT3))
        assertTrue(f.isFlagAndAny(Units.BIT4, Units.BIT5, Units.BIT6, Units.BIT7))
    }

    @Test
    fun isFlagAndNotAny() {
        val f = AtomicFlag()
        f.setFlag(Units.BIT4)
        f.setFlag(Units.BIT7)
        f.setFlag(Units.BIT10)
        f.setFlag(Units.BIT11)


        assertTrue(!f.isFlagAndNotAny(Units.BIT2, Units.BIT7, Units.BIT10))
        assertTrue(f.isFlagAndNotAny(Units.BIT4, Units.BIT5, Units.BIT6))
        assertTrue(!f.isFlagAndNotAny(Units.BIT4, Units.BIT10, Units.BIT6))
        assertTrue(!f.isFlagAndNotAny(Units.BIT4, Units.BIT5, Units.BIT10))
        assertTrue(!f.isFlagAndNotAny(Units.BIT4, Units.BIT10, Units.BIT11))

        assertTrue(!f.isFlagAndNotAny(Units.BIT2, Units.BIT7, Units.BIT10, Units.BIT3))
        assertTrue(f.isFlagAndNotAny(Units.BIT4, Units.BIT5, Units.BIT6, Units.BIT3))
        assertTrue(!f.isFlagAndNotAny(Units.BIT4, Units.BIT10, Units.BIT6, Units.BIT3))
        assertTrue(!f.isFlagAndNotAny(Units.BIT4, Units.BIT5, Units.BIT10, Units.BIT3))
        assertTrue(!f.isFlagAndNotAny(Units.BIT4, Units.BIT10, Units.BIT11, Units.BIT3))
        assertTrue(!f.isFlagAndNotAny(Units.BIT4, Units.BIT5, Units.BIT6, Units.BIT7))

    }


    @Test
    fun isAnyFlag() {
        val f = AtomicFlag()
        f.setFlag(Units.BIT4)
        f.setFlag(Units.BIT7)
        f.setFlag(Units.BIT10)

        assertTrue(!f.isAnyFlag(Units.BIT3, Units.BIT5))
        assertTrue(!f.isAnyFlag(Units.BIT3, Units.BIT5, Units.BIT6))
        assertTrue(!f.isAnyFlag(Units.BIT3 or Units.BIT5 or Units.BIT6))

        assertTrue(f.isAnyFlag(Units.BIT4, Units.BIT5))
        assertTrue(f.isAnyFlag(Units.BIT3, Units.BIT7))

        assertTrue(f.isAnyFlag(Units.BIT4, Units.BIT5, Units.BIT6))
        assertTrue(f.isAnyFlag(Units.BIT3, Units.BIT7, Units.BIT6))
        assertTrue(f.isAnyFlag(Units.BIT3, Units.BIT5, Units.BIT10))

        assertTrue(f.isAnyFlag(Units.BIT4 or Units.BIT5 or Units.BIT6))
        assertTrue(f.isAnyFlag(Units.BIT3 or Units.BIT7 or Units.BIT6))
        assertTrue(f.isAnyFlag(Units.BIT3 or Units.BIT5 or Units.BIT10))
    }


    @Test
    fun setFlagIfNonAny() {
        val f = AtomicFlag()
        f.setFlag(Units.BIT4)
        f.setFlag(Units.BIT7)
        f.setFlag(Units.BIT10)

        assertFalse(f.setFlagIfNonAny(Units.BIT5, Units.BIT9, Units.BIT10))
        assertFalse(f.isFlag(Units.BIT5))
        assertTrue(f.setFlagIfNonAny(Units.BIT5, Units.BIT5, Units.BIT6))
        assertTrue(f.isFlag(Units.BIT5))
        f.clearFlag(Units.BIT5)
        assertFalse(f.isFlag(Units.BIT5))


        assertFalse(f.setFlagIfNonAny(Units.BIT5, Units.BIT9, Units.BIT8, Units.BIT10))
        assertFalse(f.isFlag(Units.BIT5))
        assertTrue(f.setFlagIfNonAny(Units.BIT5, Units.BIT5, Units.BIT6, Units.BIT11))
        assertTrue(f.isFlag(Units.BIT5))
        f.clearFlag(Units.BIT5)
        assertFalse(f.isFlag(Units.BIT5))


    }


    @Test
    fun setFlagIfAny() {

        val f = AtomicFlag()
        f.setFlag(Units.BIT4)
        f.setFlag(Units.BIT7)
        f.setFlag(Units.BIT10)


        assertFalse(f.setFlagIfAny(Units.BIT5, Units.BIT8, Units.BIT6))
        assertFalse(f.isFlag(Units.BIT5))

        assertTrue(f.setFlagIfAny(Units.BIT5, Units.BIT7, Units.BIT6))
        assertTrue(f.isFlag(Units.BIT5))
        f.clearFlag(Units.BIT5)
        assertFalse(f.isFlag(Units.BIT5))

        assertFalse(f.setFlagIfAny(Units.BIT5, Units.BIT8, Units.BIT6, Units.BIT9))
        assertFalse(f.isFlag(Units.BIT5))
        assertTrue(f.setFlagIfAny(Units.BIT5, Units.BIT8, Units.BIT6, Units.BIT10))
        assertTrue(f.isFlag(Units.BIT5))
        f.clearFlag(Units.BIT5)

        assertFalse(f.setFlagIfAny(Units.BIT5, Units.BIT8 or Units.BIT6 or Units.BIT9))
        assertFalse(f.isFlag(Units.BIT5))
        f.clearFlag(Units.BIT5)
        assertTrue(f.setFlagIfAny(Units.BIT5, Units.BIT8 or Units.BIT6 or Units.BIT10))
        assertTrue(f.isFlag(Units.BIT5))

    }


    @Test
    fun setFlagMustAnyAndNon() {

        val f = AtomicFlag()
        f.setFlag(Units.BIT4)
        f.setFlag(Units.BIT7)
        f.setFlag(Units.BIT10)

        assertTrue(
            f.setFlagMustAnyAndNon(
                Units.BIT5, Units.BIT4, "bit4 cleared",
                Units.BIT8 or Units.BIT9
            )
        )

        assertTrue(f.isFlag(Units.BIT5))
        f.clearFlag(Units.BIT5)
        assertFalse(f.isFlag(Units.BIT5))

        assertFalse(
            f.setFlagMustAnyAndNon(
                Units.BIT5, Units.BIT4, "bit4 cleared",
                Units.BIT8 or Units.BIT10
            )
        )
        assertFalse(f.isFlag(Units.BIT5))

        var e: Throwable? = null

        runCatching {
            f.setFlagMustAnyAndNon(
                Units.BIT5, Units.BIT3, "bit3 cleared",
                Units.BIT8 or Units.BIT9
            )
        }.onFailure { e = it }

        assertTrue(e != null)
    }


}