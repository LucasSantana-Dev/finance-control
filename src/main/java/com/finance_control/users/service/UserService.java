package com.finance_control.users.service;

import com.finance_control.shared.service.BaseService;
import com.finance_control.shared.util.EntityMapper;
import com.finance_control.shared.util.ValidationUtils;
import com.finance_control.users.validation.UserValidation;
import com.finance_control.users.dto.UserDTO;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service class for managing user operations.
 * Provides CRUD operations for users and user-specific business logic
 * like email validation and user lookup by email.
 */
@Service
@Transactional
public class UserService extends BaseService<User, Long, UserDTO> {
    
    /** The user repository for data access operations */
    private final UserRepository userRepository;
    
    /**
     * Constructs a new UserService with the specified repository.
     * 
     * @param userRepository the repository to use for user data access
     */
    public UserService(UserRepository userRepository) {
        super(userRepository);
        this.userRepository = userRepository;
    }
    
    /**
     * Find users with dynamic filtering.
     * 
     * @param email optional email filter (case-insensitive)
     * @param fullName optional full name filter (case-insensitive)
     * @param isActive optional active status filter
     * @param pageable pagination parameters
     * @return a page of user DTOs
     */
    public Page<UserDTO> findAllWithFilters(String email, String fullName, Boolean isActive, Pageable pageable) {
        Specification<User> spec = (root, query, criteriaBuilder) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            
            if (email != null && !email.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("email")), 
                    "%" + email.toLowerCase() + "%"
                ));
            }
            
            if (fullName != null && !fullName.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("fullName")), 
                    "%" + fullName.toLowerCase() + "%"
                ));
            }
            
            if (isActive != null) {
                predicates.add(isActive ? criteriaBuilder.isTrue(root.get("isActive")) : criteriaBuilder.isFalse(root.get("isActive")));
            }
            
            return predicates.isEmpty() ? null : criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        
        // Default sorting by full name if no sort is specified
        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.ASC, "fullName"));
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
        Specification<User> spec = (root, query, criteriaBuilder) -> 
            criteriaBuilder.like(
                criteriaBuilder.lower(root.get("email")), 
                email.toLowerCase()
            );
        
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
        user.setFullName(createDTO.getFullName());
        user.setEmail(createDTO.getEmail());
        user.setPassword(createDTO.getPassword());
        return user;
    }
    
    @Override
    protected void updateEntityFromDTO(User entity, UserDTO updateDTO) {
        if (updateDTO.getFullName() != null) {
            entity.setFullName(updateDTO.getFullName());
        }
        if (updateDTO.getEmail() != null) {
            entity.setEmail(updateDTO.getEmail());
        }
        if (updateDTO.getPassword() != null) {
            entity.setPassword(updateDTO.getPassword());
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
        UserValidation.validateFullName(createDTO.getFullName());
        UserValidation.validatePassword(createDTO.getPassword());
    }
    
    @Override
    protected void validateUpdateDTO(UserDTO updateDTO) {
        UserValidation.validateFullNameForUpdate(updateDTO.getFullName());
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
    }
    
    @Override
    protected org.springframework.data.jpa.domain.Specification<User> createSpecificationFromFilters(String search, java.util.Map<String, Object> filters) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            
            // Handle search term across searchable fields
            if (search != null && !search.trim().isEmpty()) {
                predicates.add(criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("fullName")), "%" + search.toLowerCase() + "%"),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), "%" + search.toLowerCase() + "%")
                ));
            }
            
            // Handle specific filters
            if (filters != null) {
                filters.forEach((key, value) -> {
                    if (value != null) {
                        switch (key) {
                            case "email" -> predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), "%" + value.toString().toLowerCase() + "%"));
                            case "fullName" -> predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("fullName")), "%" + value.toString().toLowerCase() + "%"));
                            case "isActive" -> predicates.add((Boolean) value ? criteriaBuilder.isTrue(root.get("isActive")) : criteriaBuilder.isFalse(root.get("isActive")));
                        }
                    }
                });
            }
            
            return predicates.isEmpty() ? null : criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
} 