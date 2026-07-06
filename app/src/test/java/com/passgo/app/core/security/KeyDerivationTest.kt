package com.passgo.app.core.security

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class KeyDerivationTest {

    private val keyDerivation = KeyDerivation()

    @Test
    fun generateSalt_returns32Bytes() {
        val salt = keyDerivation.generateSalt()
        assertEquals(32, salt.size)
    }

    @Test
    fun generateSalt_returnsUniqueValues() {
        val salt1 = keyDerivation.generateSalt()
        val salt2 = keyDerivation.generateSalt()
        assertNotEquals(salt1.contentToString(), salt2.contentToString())
    }

    @Test
    fun deriveKey_returns256BitKey() {
        val password = "testPassword123!".toCharArray()
        val salt = keyDerivation.generateSalt()
        val key = keyDerivation.deriveKey(password, salt)
        assertEquals(32, key.size)
    }

    @Test
    fun deriveKey_differentSalt_differentKey() {
        val password = "testPassword123!".toCharArray()
        val salt1 = keyDerivation.generateSalt()
        val salt2 = keyDerivation.generateSalt()
        val key1 = keyDerivation.deriveKey(password, salt1)
        val key2 = keyDerivation.deriveKey(password, salt2)
        assertNotEquals(key1.contentToString(), key2.contentToString())
    }

    @Test
    fun deriveKey_sameInput_sameKey() {
        val password = "testPassword123!".toCharArray()
        val salt = keyDerivation.generateSalt()
        val key1 = keyDerivation.deriveKey(password, salt)
        val key2 = keyDerivation.deriveKey("testPassword123!".toCharArray(), salt)
        assertArrayEquals(key1, key2)
    }

    @Test
    fun clearPassword_zerosCharArray() {
        val password = "testPassword123!".toCharArray()
        keyDerivation.clearPassword(password)
        for (char in password) {
            assertEquals('\u0000', char)
        }
    }
}
