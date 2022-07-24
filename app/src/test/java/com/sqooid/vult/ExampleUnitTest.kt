package com.sqooid.vult

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.mindrot.jbcrypt.BCrypt

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val one = BCrypt.hashpw("hello", BCrypt.gensalt())
        println(one)
        val two = BCrypt.hashpw("hello", BCrypt.gensalt())
        println(two)
        assertNotEquals(one, two)
    }
}