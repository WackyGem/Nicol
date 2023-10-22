package org.example.nicol.infrastructure.entity

import java.time.OffsetDateTime
import org.babyfish.jimmer.sql.*


/**
 * <p>
 *  verify_email

 * </p>
 *
 * @author aurora
 * @date 2023-09-22
 */
@Entity
@Table(name = "verify_emails")
interface VerifyEmail : BaseEntity {

    /**
     *  auto increment id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    /**
     *  email expired time
     */
    @Column(name = "expired_at")
    val expiredAt: OffsetDateTime

    /**
     *  email
     */
    @Key
    val email: String

    /**
     *  used to verify the mail
     */
    @Key
    @Column(name = "secret_code")
    val secretCode: String

    /**
     *  is active
     */
    @Key
    val used: Boolean

    /**
     *  user id
     */
    @ManyToOne
    val user: User

    /**
     *  user id view
     */
    @IdView
    val userId: Long
}
