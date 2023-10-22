package org.example.nicol.infrastructure.computation

import org.junit.jupiter.api.Test
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.emptyOrNullString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not

class HashUtilsTest {
    @Test
    fun test_password(){
        val password:String = randomValue<String>(8)
        val hashedPassword = HashUtils.hashPassword(password)
        assertThat(hashedPassword, not(emptyOrNullString()))
        val checkPassword = HashUtils.checkPassword(password, hashedPassword)
        assertThat(checkPassword,`is`(true))

        val wrongPassword = randomValue<String>(10)
        val checkWrongPassword = HashUtils.checkPassword(wrongPassword, hashedPassword)
        assertThat(checkWrongPassword,`is`(false))

        val hashPassword2 = HashUtils.hashPassword(password)
        assertThat(hashPassword2, not(emptyOrNullString()))
        assertThat(hashPassword2, not(equalTo(password)))
    }

}