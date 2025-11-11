package com.finance_control.users.service;

import com.finance_control.shared.service.BaseService;
import com.finance_control.shared.util.EntityMapper;
import com.finance_control.users.validation.UserValidation;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.CriteriaBuilder;

import com.finance_control.users.dto.UserDTO;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class for managing user operations.
 * Provides CRUD operations for users and user-specific business logic
 * like email validation and user lookup by email.
 */
@Service
@Transactional
@Slf4j
public class UserService extends BaseService<User, Long, UserDTO> {

    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_IS_ACTIVE = "isActive";
    private static final String FIELD_FULL_NAME = "fullName";

    /** The user repository for data access operations */
    private final UserRepository userRepository;

    /** The password encoder for secure password handling */
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructs a new UserService with the specified repository and password encoder.
     *
     * @param userRepository the repository to use for user data access
     * @param passwordEncoder the password encoder for secure password handling
     */
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        super(userRepository);
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Find users with dynamic filtering.
     *
     * @param email    optional email filter (case-insensitive)
     * @param isActive optional active status filter
     * @param pageable pagination parameters
     * @return a page of user DTOs
     */
    public Page<UserDTO> findAllWithFilters(String email, Boolean isActive, Pageable pageable) {
        Specification<User> spec = (root, query, criteriaBuilder) -> {
            var predicates = new ArrayList<Predicate>();

            if (email != null && !email.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get(FIELD_EMAIL)),
                        "%" + email.toLowerCase() + "%"));
            }

            if (isActive != null) {
                predicates.add(isActive ? criteriaBuilder.isTrue(root.get(FIELD_IS_ACTIVE))
                        : criteriaBuilder.isFalse(root.get(FIELD_IS_ACTIVE)));
            }

            return predicates.isEmpty() ? null
                    : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // Default sorting by email if no sort is specified
        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                    Sort.by(Sort.Direction.ASC, FIELD_EMAIL));
        }

        return userRepository.findAll(spec, pageable)
                .map(this::mapToResponseDTO);
    }

    /**
     * Finds a user by their email address.
     *
     * @param email the email address to search for
     * @return an Optional containing the user DTO if found, empty otherwise
     * @throws IllegalArgumentException if the email is null or empty
     */
    public Optional<UserDTO> findByEmail(String email) {
        UserValidation.validateEmail(email);
        Specification<User> spec = (root, query, criteriaBuilder) -> criteriaBuilder.like(
                criteriaBuilder.lower(root.get(FIELD_EMAIL)),
                email.toLowerCase());

        return userRepository.findOne(spec)
                .map(this::mapToResponseDTO);
    }

    /**
     * Checks if a user exists with the given email address.
     *
     * @param email the email address to check
     * @return true if a user exists with the email, false otherwise
     * @throws IllegalArgumentException if the email is null or empty
     */
    public boolean existsByEmail(String email) {
        UserValidation.validateEmail(email);
        return userRepository.existsByEmail(email);
    }

    // BaseService abstract method implementations
    @Override
    protected User mapToEntity(UserDTO createDTO) {
        User user = new User();
        user.setEmail(createDTO.getEmail());
        // Hash the password before storing
        user.setPassword(passwordEncoder.encode(createDTO.getPassword()));
        user.setIsActive(createDTO.getIsActive());
        return user;
    }

    @Override
    protected void updateEntityFromDTO(User entity, UserDTO updateDTO) {
        if (updateDTO.getEmail() != null) {
            entity.setEmail(updateDTO.getEmail());
        }
        if (updateDTO.getPassword() != null) {
            // Hash the password before storing
            entity.setPassword(passwordEncoder.encode(updateDTO.getPassword()));
        }
        if (updateDTO.getIsActive() != null) {
            entity.setIsActive(updateDTO.getIsActive());
        }
    }

    @Override
    protected UserDTO mapToResponseDTO(User entity) {
        UserDTO dto = new UserDTO();

        // Map common fields using reflection
        EntityMapper.mapCommonFields(entity, dto);

        return dto;
    }

    @Override
    protected void validateCreateDTO(UserDTO createDTO) {
        UserValidation.validateEmailUnique(createDTO.getEmail(), userRepository::existsByEmail);
        UserValidation.validatePassword(createDTO.getPassword());
    }

    @Override
    protected void validateUpdateDTO(UserDTO updateDTO) {
        UserValidation.validateEmailForUpdate(updateDTO.getEmail());
        UserValidation.validatePasswordForUpdate(updateDTO.getPassword());
    }

    @Override
    protected String getEntityName() {
        return "User";
    }

    /**
     * Soft delete a user by setting isActive to false.
     *
     * @param id the ID of the user to deactivate
     * @throws EntityNotFoundException if the user is not found
     */
    public void softDelete(Long id) {
        validateId(id);
        User user = getEntityById(id);
        user.setIsActive(false);
        userRepository.save(user);
    }

    /**
     * Reactivate a user by setting isActive to true.
     *
     * @param id the ID of the user to reactivate
     * @throws EntityNotFoundException if the user is not found
     */
    public void reactivate(Long id) {
        validateId(id);
        User user = getEntityById(id);
        user.setIsActive(true);
        userRepository.save(user);
        log.info("User reactivated with ID: {}", id);
    }

    /**
     * Reset a user's password by administrator.
     *
     * @param id the ID of the user
     * @param newPassword the new password
     * @param reason the reason for the password reset
     * @throws EntityNotFoundException if the user is not found
     */
    public void resetPassword(Long id, String newPassword, String reason) {
        validateId(id);
        User user = getEntityById(id);

        // Hash the password before storing
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Do not log free-form reasons or sensitive context to avoid PII leakage
        log.info("Password reset performed");
    }

    /**
     * Update a user's active status.
     *
     * @param id the ID of the user
     * @param active the new active status
     * @param reason the reason for the status change
     * @return the updated user DTO
     * @throws EntityNotFoundException if the user is not found
     */
    public UserDTO updateStatus(Long id, Boolean active, String reason) {
        validateId(id);
        User user = getEntityById(id);
        user.setIsActive(active);
        userRepository.save(user);

        // Avoid logging free-form 'reason' to prevent PII leakage
        log.info("User status updated - Active: {}", active);
        return mapToResponseDTO(user);
    }

    @Override
    protected Specification<User> createSpecificationFromFilters(String search,
            Map<String, Object> filters) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new ArrayList<Predicate>();

            addSearchPredicates(predicates, search, root, criteriaBuilder);
            addFilterPredicates(predicates, filters, root, criteriaBuilder);

            return predicates.isEmpty() ? null
                    : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void addSearchPredicates(List<Predicate> predicates, String search,
            Root<User> root, CriteriaBuilder criteriaBuilder) {
        if (search != null && !search.trim().isEmpty()) {
            predicates.add(criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get(FIELD_FULL_NAME)),
                            "%" + search.toLowerCase() + "%"),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get(FIELD_EMAIL)),
                            "%" + search.toLowerCase() + "%")));
        }
    }

    private void addFilterPredicates(List<Predicate> predicates,
            Map<String, Object> filters, Root<User> root,
            CriteriaBuilder criteriaBuilder) {
        if (filters != null) {
            filters.forEach((key, value) -> {
                if (value != null) {
                    addFilterPredicate(predicates, key, value, root, criteriaBuilder);
                }
            });
        }
    }

    private void addFilterPredicate(List<Predicate> predicates, String key,
            Object value, Root<User> root,
            CriteriaBuilder criteriaBuilder) {
        switch (key) {
            case FIELD_EMAIL ->
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get(FIELD_EMAIL)),
                        "%" + value.toString().toLowerCase() + "%"));
            case FIELD_FULL_NAME ->
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get(FIELD_FULL_NAME)),
                        "%" + value.toString().toLowerCase() + "%"));
            case FIELD_IS_ACTIVE ->
                predicates.add(criteriaBuilder.equal(root.get(FIELD_IS_ACTIVE), value));
            default -> throw new IllegalArgumentException("Invalid filter key: " + key);
        }
    }
}
