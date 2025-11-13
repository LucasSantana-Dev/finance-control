package com.finance_control.profile.repository;

import com.finance_control.profile.model.Profile;
import com.finance_control.users.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ProfileRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProfileRepository profileRepository;

    private User testUser;
    private Profile testProfile;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setIsActive(true);
        testUser = entityManager.persistAndFlush(testUser);

        testProfile = new Profile();
        testProfile.setFullName("John Doe");
        testProfile.setBio("Test bio");
        testProfile.setPhone("+1234567890");
        testProfile.setCountry("US");
        testProfile.setAvatarUrl("https://example.com/avatar.jpg");
        testProfile.setUser(testUser);
        testProfile.setCreatedAt(LocalDateTime.now());
        testProfile.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void findByUserId_WithExistingProfile_ShouldReturnProfile() {
        entityManager.persistAndFlush(testProfile);

        Optional<Profile> result = profileRepository.findByUserId(testUser.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getFullName()).isEqualTo("John Doe");
        assertThat(result.get().getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void findByUserId_WithNonExistentProfile_ShouldReturnEmpty() {
        Optional<Profile> result = profileRepository.findByUserId(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void existsByUserId_WithExistingProfile_ShouldReturnTrue() {
        entityManager.persistAndFlush(testProfile);

        boolean exists = profileRepository.existsByUserId(testUser.getId());

        assertThat(exists).isTrue();
    }

    @Test
    void existsByUserId_WithNonExistentProfile_ShouldReturnFalse() {
        boolean exists = profileRepository.existsByUserId(999L);

        assertThat(exists).isFalse();
    }

    @Test
    void save_ShouldPersistProfileCorrectly() {
        Profile savedProfile = profileRepository.save(testProfile);
        entityManager.flush();
        entityManager.clear();

        Profile foundProfile = entityManager.find(Profile.class, savedProfile.getId());

        assertThat(foundProfile).isNotNull();
        assertThat(foundProfile.getFullName()).isEqualTo("John Doe");
        assertThat(foundProfile.getBio()).isEqualTo("Test bio");
        assertThat(foundProfile.getPhone()).isEqualTo("+1234567890");
        assertThat(foundProfile.getCountry()).isEqualTo("US");
        assertThat(foundProfile.getAvatarUrl()).isEqualTo("https://example.com/avatar.jpg");
        assertThat(foundProfile.getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void findById_WithExistingId_ShouldReturnProfile() {
        Profile savedProfile = entityManager.persistAndFlush(testProfile);

        Optional<Profile> result = profileRepository.findById(savedProfile.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedProfile.getId());
        assertThat(result.get().getFullName()).isEqualTo("John Doe");
    }

    @Test
    void findById_WithNonExistentId_ShouldReturnEmpty() {
        Optional<Profile> result = profileRepository.findById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void delete_ShouldRemoveProfile() {
        Profile savedProfile = entityManager.persistAndFlush(testProfile);
        Long profileId = savedProfile.getId();

        profileRepository.delete(savedProfile);
        entityManager.flush();
        entityManager.clear();

        Profile foundProfile = entityManager.find(Profile.class, profileId);
        assertThat(foundProfile).isNull();
    }

    @Test
    void findByUserId_WithMultipleUsers_ShouldReturnCorrectProfile() {
        User secondUser = new User();
        secondUser.setEmail("second@example.com");
        secondUser.setPassword("encodedPassword");
        secondUser.setIsActive(true);
        secondUser = entityManager.persistAndFlush(secondUser);

        Profile secondProfile = new Profile();
        secondProfile.setFullName("Jane Doe");
        secondProfile.setUser(secondUser);
        entityManager.persistAndFlush(secondProfile);

        entityManager.persistAndFlush(testProfile);

        Optional<Profile> result = profileRepository.findByUserId(testUser.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getFullName()).isEqualTo("John Doe");
        assertThat(result.get().getUser().getId()).isEqualTo(testUser.getId());
    }
}








