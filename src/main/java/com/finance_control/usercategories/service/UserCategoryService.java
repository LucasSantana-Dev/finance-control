package com.finance_control.usercategories.service;

import com.finance_control.shared.context.UserContext;
import com.finance_control.shared.exception.EntityNotFoundException;
import com.finance_control.shared.service.BaseService;
import com.finance_control.usercategories.dto.UserCategoryDTO;
import com.finance_control.usercategories.enums.CategoryType;
import com.finance_control.usercategories.model.UserCategory;
import com.finance_control.usercategories.repository.UserCategoryRepository;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
public class UserCategoryService extends BaseService<UserCategory, Long, UserCategoryDTO> {

    private final UserCategoryRepository userCategoryRepository;
    private final UserRepository userRepository;

    public UserCategoryService(UserCategoryRepository userCategoryRepository, UserRepository userRepository) {
        super(userCategoryRepository);
        this.userCategoryRepository = userCategoryRepository;
        this.userRepository = userRepository;
    }

    @Override
    protected boolean isUserAware() {
        return true;
    }

    @Override
    protected UserCategory mapToEntity(UserCategoryDTO dto) {
        UserCategory category = new UserCategory();
        if (dto.getId() != null) {
            category.setId(dto.getId());
        }
        category.setName(dto.getName());
        category.setType(dto.getType());
        category.setColor(dto.getColor());
        category.setIcon(dto.getIcon());
        category.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false);

        if (dto.getUserId() != null) {
            User user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new EntityNotFoundException("User", "id", dto.getUserId()));
            category.setUser(user);
        }

        return category;
    }

    @Override
    protected void updateEntityFromDTO(UserCategory entity, UserCategoryDTO dto) {
        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        if (dto.getType() != null) {
            entity.setType(dto.getType());
        }
        if (dto.getColor() != null) {
            entity.setColor(dto.getColor());
        }
        if (dto.getIcon() != null) {
            entity.setIcon(dto.getIcon());
        }
        if (dto.getIsDefault() != null) {
            entity.setIsDefault(dto.getIsDefault());
        }
    }

    @Override
    protected UserCategoryDTO mapToResponseDTO(UserCategory entity) {
        UserCategoryDTO dto = UserCategoryDTO.builder()
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .name(entity.getName())
                .type(entity.getType())
                .color(entity.getColor())
                .icon(entity.getIcon())
                .isDefault(entity.getIsDefault())
                .build();
        dto.setId(entity.getId());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    @Override
    protected boolean belongsToUser(UserCategory entity, Long userId) {
        return entity.getUser() != null && entity.getUser().getId().equals(userId);
    }

    @Override
    protected void setUserId(UserCategory entity, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", "id", userId));
        entity.setUser(user);
    }

    @Override
    protected String getEntityName() {
        return "UserCategory";
    }

    @Override
    protected void validateCreateDTO(UserCategoryDTO dto) {
        super.validateCreateDTO(dto);
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User context not available");
        }
        if (dto.getName() != null && dto.getType() != null) {
            if (userCategoryRepository.existsByUserIdAndNameAndType(userId, dto.getName(), dto.getType())) {
                throw new IllegalArgumentException("Category with this name and type already exists for the user");
            }
        }
    }

    @Override
    protected void validateUpdateDTO(UserCategoryDTO dto) {
        super.validateUpdateDTO(dto);
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User context not available");
        }
        if (dto.getName() != null && dto.getType() != null) {
            UserCategory current = getEntityById(dto.getId());
            if (!current.getName().equals(dto.getName()) || !current.getType().equals(dto.getType())) {
                if (userCategoryRepository.existsByUserIdAndNameAndType(userId, dto.getName(), dto.getType())) {
                    throw new IllegalArgumentException("Category with this name and type already exists for the user");
                }
            }
        }
    }

    public Page<UserCategoryDTO> findByUser(Pageable pageable) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User context not available");
        }
        log.debug("Finding user categories for user {}", userId);
        Page<UserCategory> categories = userCategoryRepository.findByUserIdOrderByNameAsc(userId, pageable);
        return categories.map(this::mapToResponseDTO);
    }

    public Page<UserCategoryDTO> findByUserAndType(CategoryType type, Pageable pageable) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User context not available");
        }
        log.debug("Finding user categories for user {} and type {}", userId, type);
        Page<UserCategory> categories = userCategoryRepository.findByUserIdAndTypeOrderByNameAsc(userId, type, pageable);
        return categories.map(this::mapToResponseDTO);
    }

    public List<UserCategoryDTO> findDefaultCategories() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User context not available");
        }
        log.debug("Finding default categories for user {}", userId);
        List<UserCategory> categories = userCategoryRepository.findDefaultCategoriesByUserId(userId);
        return categories.stream().map(this::mapToResponseDTO).toList();
    }
}
