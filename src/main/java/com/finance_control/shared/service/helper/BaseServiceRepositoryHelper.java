package com.finance_control.shared.service.helper;

import com.finance_control.shared.context.UserContext;
import com.finance_control.shared.repository.BaseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Helper class for repository operations in BaseService.
 * Extracted to reduce BaseService file length.
 *
 * @param <T> The entity type
 */
@Slf4j
public class BaseServiceRepositoryHelper<T> {

    private static final String USER_CONTEXT_UNAVAILABLE = "User context not available for user-aware service";

    /**
     * Executes the findAll method with search, handling user-aware repositories.
     *
     * @param repository the repository
     * @param search the search term
     * @param pageable the pageable parameters
     * @param userAware whether the service is user-aware
     * @return a page of entities
     */
    @SuppressWarnings("unchecked")
    public static <T> Page<T> executeFindAllWithSearch(BaseRepository<T, ?> repository, String search,
                                                       Pageable pageable, boolean userAware) {
        if (!userAware) {
            return repository.findAll(search, pageable);
        }

        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            log.error(USER_CONTEXT_UNAVAILABLE);
            throw new SecurityException("User context not available");
        }

        try {
            return (Page<T>) repository.getClass()
                    .getMethod("findAll", String.class, Long.class, Pageable.class)
                    .invoke(repository, search, currentUserId, pageable);
        } catch (NoSuchMethodException e) {
            return repository.findAll(search, pageable);
        } catch (Exception e) {
            log.warn("Error calling findAll with userId, falling back to standard method: {}", e.getMessage());
            return repository.findAll(search, pageable);
        }
    }
}

