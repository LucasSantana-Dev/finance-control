package com.finance_control.usercategories.repository;

import com.finance_control.usercategories.enums.CategoryType;
import com.finance_control.usercategories.model.UserCategory;
import com.finance_control.shared.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCategoryRepository extends BaseRepository<UserCategory, Long> {

    Page<UserCategory> findByUserIdOrderByNameAsc(Long userId, Pageable pageable);

    Page<UserCategory> findByUserIdAndTypeOrderByNameAsc(Long userId, CategoryType type, Pageable pageable);

    List<UserCategory> findByUserIdAndType(Long userId, CategoryType type);

    @Query("SELECT uc FROM UserCategory uc WHERE uc.user.id = :userId AND uc.isDefault = true")
    List<UserCategory> findDefaultCategoriesByUserId(@Param("userId") Long userId);

    @Query("SELECT uc FROM UserCategory uc WHERE uc.user.id = :userId AND uc.name = :name AND uc.type = :type")
    Optional<UserCategory> findByUserIdAndNameAndType(@Param("userId") Long userId,
                                                       @Param("name") String name,
                                                       @Param("type") CategoryType type);

    boolean existsByUserIdAndNameAndType(Long userId, String name, CategoryType type);
}
