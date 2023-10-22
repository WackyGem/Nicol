package org.example.nicol.infrastructure.repository

import org.example.nicol.infrastructure.entity.VerifyEmail
import org.babyfish.jimmer.spring.repository.KRepository
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.sql
import org.example.nicol.infrastructure.entity.expiredAt
import org.example.nicol.infrastructure.entity.id
import org.example.nicol.infrastructure.entity.secretCode
import org.example.nicol.infrastructure.entity.used
import org.example.nicol.infrastructure.entity.userId
import org.springframework.stereotype.Repository

/**
 * <p>
 * VerifyEmailRepository 接口
 * </p>
 *
 * @author aurora
 * @date 2023-09-22
 */
@Repository
interface VerifyEmailRepository : KRepository<VerifyEmail, Long> {

    fun findUserIdByExpiredTimeCheck(
        id: Long,
        secretCode: String
    ): Long? = sql.createQuery(VerifyEmail::class) {
        where(
            table.id eq id,
            table.secretCode eq secretCode,
            table.used eq false,
            sql(Boolean::class, "%e > now()") {
                expression(table.expiredAt)
            }
        )
        select(table.userId)
    }.fetchOneOrNull()

    fun updateUsedByExpiredTimeCheck(
        id: Long,
        secretCode: String,
        used: Boolean
    ): Int = sql.createUpdate(VerifyEmail::class) {
        set(table.used, used)
        where(
            table.id eq id,
            table.secretCode eq secretCode,
            table.used eq false,
            sql(Boolean::class, "%e > now()") {
                expression(table.expiredAt)
            }
        )
    }.execute()
}


