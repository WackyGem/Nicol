package org.example.nicol.infrastructure.repository

import org.example.nicol.infrastructure.entity.Session
import org.babyfish.jimmer.spring.repository.KRepository
import org.springframework.stereotype.Repository
import java.util.UUID

/**
* <p>
 * SessionRepository 接口
 * </p>
*
* @author aurora
* @date 2023-09-22
*/
@Repository
interface SessionRepository :KRepository<Session,UUID> {

}

