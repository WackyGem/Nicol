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

package org.example.nicol.infrastructure.entity

import java.time.OffsetDateTime
import org.babyfish.jimmer.sql.*


/**
 * <p>
 *  app_user

 * </p>
 *
 * @author aurora
 * @date 2023-09-22
 */
@Entity
@Table(name = "users")
interface User : BaseEntity {

    /**
     *  auto increment id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    /**
     *  username
     */
    @Key
    val username: String

    /**
     *  hashed_password
     */
    @Column(name = "hashed_password")
    val hashedPassword: String

    /**
     *  nickname
     */
    val nickname: String

    /**
     *  email
     */
    @Key
    val email: String

    /**
     *  email_verified
     */
    @Column(name = "email_verified")
    val emailVerified: Boolean

    /**
     *  password_changed_time
     */
    @Column(name = "password_changed_at")
    val passwordChangedAt: OffsetDateTime


    @OneToMany(mappedBy = "user")
    val verifyEmails:List<VerifyEmail>
}
