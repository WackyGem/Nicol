package org.example.nicol.infrastructure.entity

import java.util.UUID
import java.time.OffsetDateTime
import org.babyfish.jimmer.sql.meta.UUIDIdGenerator
import org.babyfish.jimmer.sql.*


/**
 * <p>
 *  session

 * </p>
 *
 * @author aurora
 * @date 2023-09-22
 */
@Entity
@Table(name = "sessions")
interface Session {

    /**
     *  session id
     */
    @Id
    @GeneratedValue(generatorType = UUIDIdGenerator::class)
    val id: UUID

    /**
     *  data created time
     */
    @Column(name = "created_at")
    val createdAt: OffsetDateTime

    /**
     *  expired_time
     */
    @Column(name = "expired_at")
    val expiredAt: OffsetDateTime

    /**
     *  username
     */
    val username: String

    /**
     *  used to refresh access token
     */
    @Column(name = "refresh_token")
    val refreshToken: String

    /**
     *  user client user-agent
     */
    @Column(name = "user_agent")
    val userAgent: String

    /**
     *  user client x-forward-for
     */
    @Column(name = "client_ip")
    val clientIp: String

    /**
     *  blocked
     */
    val blocked: Boolean

}
