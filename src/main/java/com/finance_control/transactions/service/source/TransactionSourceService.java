package com.finance_control.transactions.service.source;

import com.finance_control.shared.exception.EntityNotFoundException;
import com.finance_control.shared.service.UserAwareBaseService;
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
        UserAwareBaseService<TransactionSourceEntity, Long, TransactionSourceDTO> {

    private final TransactionSourceRepository transactionSourceRepository;
    private final UserRepository userRepository;

    public TransactionSourceService(TransactionSourceRepository transactionSourceRepository,
            UserRepository userRepository) {
        super(transactionSourceRepository);
        this.transactionSourceRepository = transactionSourceRepository;
        this.userRepository = userRepository;
    }



    @Override
    protected TransactionSourceEntity mapToEntity(TransactionSourceDTO createDTO) {
        User user = userRepository.findById(createDTO.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        if (transactionSourceRepository.existsByNameAndUserId(createDTO.getName(), createDTO.getUserId())) {
            throw new IllegalArgumentException("Transaction source with this name already exists for this user");
        }
        
        TransactionSourceEntity entity = new TransactionSourceEntity();
        entity.setName(createDTO.getName());
        entity.setDescription(createDTO.getDescription());
        entity.setSourceType(createDTO.getSourceType());
        entity.setBankName(createDTO.getBankName());
        entity.setAccountNumber(createDTO.getAccountNumber());
        entity.setCardType(createDTO.getCardType());
        entity.setCardLastFour(createDTO.getCardLastFour());
        entity.setAccountBalance(createDTO.getAccountBalance());
        entity.setUser(user);
        entity.setIsActive(true);
        return entity;
    }

    @Override
    protected void updateEntityFromDTO(TransactionSourceEntity entity, TransactionSourceDTO updateDTO) {
        entity.setName(updateDTO.getName());
        entity.setDescription(updateDTO.getDescription());
        entity.setSourceType(updateDTO.getSourceType());
        entity.setBankName(updateDTO.getBankName());
        entity.setAccountNumber(updateDTO.getAccountNumber());
        entity.setCardType(updateDTO.getCardType());
        entity.setCardLastFour(updateDTO.getCardLastFour());
        entity.setAccountBalance(updateDTO.getAccountBalance());
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

    private void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found");
        }
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
    protected Specification<TransactionSourceEntity> createSpecificationFromFilters(String search, Map<String, Object> filters) {
        Specification<TransactionSourceEntity> spec = super.createSpecificationFromFilters(search, filters);
        
        if (filters != null) {
            if (filters.containsKey("isActive")) {
                Boolean isActive = (Boolean) filters.get("isActive");
                spec = spec.and((root, _, cb) -> isActive ? cb.isTrue(root.get("isActive")) : cb.isFalse(root.get("isActive")));
            }
            if (filters.containsKey("sourceType")) {
                com.finance_control.shared.enums.TransactionSource sourceType = (com.finance_control.shared.enums.TransactionSource) filters.get("sourceType");
                spec = spec.and(SpecificationUtils.<TransactionSourceEntity>fieldEqual("sourceType", sourceType));
            }
            if (filters.containsKey("name")) {
                String name = (String) filters.get("name");
                if (name != null && !name.trim().isEmpty()) {
                    spec = spec.and(SpecificationUtils.<TransactionSourceEntity>likeIgnoreCase("name", name));
                }
            }
        }
        
        return spec;
    }
}