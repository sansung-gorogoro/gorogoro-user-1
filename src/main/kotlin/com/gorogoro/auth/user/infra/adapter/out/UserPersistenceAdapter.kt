package com.gorogoro.auth.user.infra.adapter.out

import com.gorogoro.auth.global.exception.BusinessException
import com.gorogoro.auth.global.exception.ErrorCode
import com.gorogoro.auth.user.application.port.out.CheckNicknamePort
import com.gorogoro.auth.user.application.port.out.LoadUserPort
import com.gorogoro.auth.user.application.port.out.ModifyUserPort
import com.gorogoro.auth.authorization.application.port.out.SaveUserPort
import com.gorogoro.auth.user.infra.adapter.out.persistence.UserJpaRepository
import com.gorogoro.auth.user.infra.persistence.entity.toDomain
import com.gorogoro.auth.user.infra.persistence.entity.toEntity
import com.gorogoro.auth.user.model.User
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class UserPersistenceAdapter(
    private val userJpaRepository: UserJpaRepository,
) : LoadUserPort, CheckNicknamePort, SaveUserPort, ModifyUserPort {

    override fun findByEmail(email: String): User {
        val user =
            userJpaRepository.findByEmail(email) ?: throw BusinessException.builder(ErrorCode.USER_NOT_FOUND).build()
        return user.toDomain()
    }

    override fun findById(id: Long): User? {
        val user = userJpaRepository.findByIdOrNull(id) ?: throw BusinessException.builder(ErrorCode.USER_NOT_FOUND).build()
        return user.toDomain()
    }

    override fun existsByNickname(nickname: String): Boolean = userJpaRepository.existsByNickname(nickname)

    override fun saveUser(user: User) {
        userJpaRepository.save(user.toEntity())
    }

    override fun modifyUserInfo(user: User): User {
        val savedEntity = userJpaRepository.save(user.toEntity())
        return savedEntity.toDomain()
    }

    override fun existsByEmail(email: String): Boolean {
        return userJpaRepository.existsByEmail(email)
    }
}
