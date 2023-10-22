/*
 * MIT License
 *
 * Copyright (c) 2023 Wacky Gem
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package org.example.nicol.infrastructure.repository

import org.babyfish.jimmer.spring.repository.KRepository
import org.babyfish.jimmer.sql.ast.tuple.Tuple2
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.example.nicol.infrastructure.entity.User
import org.example.nicol.infrastructure.entity.emailVerified
import org.example.nicol.infrastructure.entity.fetchBy
import org.example.nicol.infrastructure.entity.hashedPassword
import org.example.nicol.infrastructure.entity.id
import org.example.nicol.infrastructure.entity.username
import org.springframework.stereotype.Repository

/**
 * <p>
 * UserRepository 接口
 * </p>
 *
 * @author aurora
 * @date 2023-09-10
 */
@Repository
interface UserRepository : KRepository<User, Long> {
    fun findByUsername(username: String): User?

    fun findPasswordByUsername(username: String): User? =
        sql.createQuery(User::class){
            where(table.username eq username)
            select(table.fetchBy {
                username()
                hashedPassword()
            })
        }.fetchOneOrNull()

    fun updateEmailVerifyByIdAndVerifyCheck(id: Long, emailVerify: Boolean): Int =
        sql.createUpdate(User::class) {
            set(table.emailVerified, emailVerify)
            where(
                table.id eq id,
                table.emailVerified eq false
            )
        }.execute()
}

