package com.finance_control.transactions.service.source;

import com.finance_control.shared.enums.TransactionSource;
import com.finance_control.shared.exception.EntityNotFoundException;
import com.finance_control.shared.service.BaseService;
import com.finance_control.shared.util.EntityMapper;
import com.finance_control.shared.util.SpecificationUtils;
import com.finance_control.shared.util.ValidationUtils;
import com.finance_control.transactions.dto.source.TransactionSourceDTO;
import com.finance_control.transactions.model.source.TransactionSourceEntity;
import com.finance_control.transactions.repository.source.TransactionSourceRepository;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional
public class TransactionSourceService extends
        BaseService<TransactionSourceEntity, Long, TransactionSourceDTO> {

    private static final String FIELD_IS_ACTIVE = "isActive";
    private static final String FIELD_SOURCE_TYPE = "sourceType";
    private static final String FIELD_NAME = "name";

    private final UserRepository userRepository;

    public TransactionSourceService(TransactionSourceRepository transactionSourceRepository,
            UserRepository userRepository) {
        super(transactionSourceRepository);
        this.userRepository = userRepository;
    }

    @Override
    protected boolean isUserAware() {
        return true;
    }

    @Override
    protected boolean isNameBased() {
        return true;
    }

    /**
     * Maps common fields from DTO to entity.
     * Used by both mapToEntity and updateEntityFromDTO methods.
     */
    private void mapCommonFields(TransactionSourceEntity entity, TransactionSourceDTO dto) {
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setSourceType(dto.getSourceType());
        entity.setBankName(dto.getBankName());
        entity.setAccountNumber(dto.getAccountNumber());
        entity.setCardType(dto.getCardType());
        entity.setCardLastFour(dto.getCardLastFour());
        entity.setAccountBalance(dto.getAccountBalance());
    }

    @Override
    protected TransactionSourceEntity mapToEntity(TransactionSourceDTO createDTO) {
        TransactionSourceEntity entity = new TransactionSourceEntity();

        // Set additional fields specific to TransactionSource
        mapCommonFields(entity, createDTO);
        entity.setIsActive(true);

        return entity;
    }

    @Override
    protected void updateEntityFromDTO(TransactionSourceEntity entity, TransactionSourceDTO updateDTO) {
        mapCommonFields(entity, updateDTO);
    }

    @Override
    protected TransactionSourceDTO mapToResponseDTO(TransactionSourceEntity entity) {
        TransactionSourceDTO dto = new TransactionSourceDTO();
        EntityMapper.mapCommonFields(entity, dto);
        dto.setUserId(entity.getUser().getId());
        return dto;
    }

    @Override
    protected void validateCreateDTO(TransactionSourceDTO createDTO) {
        ValidationUtils.validateString(createDTO.getName(), "Name");
        ValidationUtils.validateId(createDTO.getUserId());
    }

    @Override
    protected void validateUpdateDTO(TransactionSourceDTO updateDTO) {
        ValidationUtils.validateString(updateDTO.getName(), "Name");
        ValidationUtils.validateId(updateDTO.getUserId());
    }

    @Override
    protected String getEntityName() {
        return "TransactionSource";
    }

    @Override
    protected boolean belongsToUser(TransactionSourceEntity entity, Long userId) {
        return entity.getUser().getId().equals(userId);
    }

    @Override
    protected void setUserId(TransactionSourceEntity entity, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        entity.setUser(user);
    }

    @Override
    protected Specification<TransactionSourceEntity> createSpecificationFromFilters(String search,
            Map<String, Object> filters) {
        Specification<TransactionSourceEntity> spec = super.createSpecificationFromFilters(search, filters);

        if (filters != null) {
            if (filters.containsKey(FIELD_IS_ACTIVE)) {
                Boolean isActive = (Boolean) filters.get(FIELD_IS_ACTIVE);
                spec = spec.and((root, query, cb) -> cb.equal(root.get(FIELD_IS_ACTIVE), isActive));
            }
            if (filters.containsKey(FIELD_SOURCE_TYPE)) {
                TransactionSource sourceType = (TransactionSource) filters.get(FIELD_SOURCE_TYPE);
                spec = spec.and(SpecificationUtils.<TransactionSourceEntity>fieldEqual(FIELD_SOURCE_TYPE, sourceType));
            }
            if (filters.containsKey(FIELD_NAME)) {
                String name = (String) filters.get(FIELD_NAME);
                if (name != null && !name.trim().isEmpty()) {
                    spec = spec.and(SpecificationUtils.<TransactionSourceEntity>likeIgnoreCase(FIELD_NAME, name));
                }
            }
        }

        return spec;
    }
}
