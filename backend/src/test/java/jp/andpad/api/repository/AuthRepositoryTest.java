package jp.andpad.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jp.andpad.api.AbstractIntegrationTest;

import jp.andpad.api.repository.AuthRepository;

class AuthRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    jp.andpad.api.repository.AuthRepository authRepository;

    @Test
    void findsDemoUser() {
        assertThat(authRepository.findUserByEmail("demo@sakura-dental.jp")).isPresent();
    }

}
