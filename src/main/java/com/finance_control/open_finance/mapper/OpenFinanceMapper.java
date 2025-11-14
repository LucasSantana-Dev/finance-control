package com.finance_control.open_finance.mapper;

import com.finance_control.open_finance.dto.*;
import com.finance_control.open_finance.model.*;
import org.mapstruct.*;

import java.util.Arrays;
import java.util.List;

/**
 * MapStruct mapper for Open Finance DTO-Entity conversion.
 */
@Mapper(componentModel = "spring")
public interface OpenFinanceMapper {

    // Institution mappings
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    OpenFinanceInstitution toEntity(OpenFinanceInstitutionDTO dto);

    OpenFinanceInstitutionDTO toDTO(OpenFinanceInstitution entity);

    List<OpenFinanceInstitutionDTO> toInstitutionDTOList(List<OpenFinanceInstitution> institutions);

    // Consent mappings
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "institution", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "accessToken", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    OpenFinanceConsent toEntity(ConsentDTO dto);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "institutionId", source = "institution.id")
    @Mapping(target = "institutionName", source = "institution.name")
    @Mapping(target = "institutionCode", source = "institution.code")
    @Mapping(target = "scopeList", expression = "java(parseScopes(entity.getScopes()))")
    @Mapping(target = "active", expression = "java(entity.isActive())")
    @Mapping(target = "expired", expression = "java(entity.isExpired())")
    ConsentDTO toDTO(OpenFinanceConsent entity);

    List<ConsentDTO> toConsentDTOList(List<OpenFinanceConsent> consents);

    // Connected Account mappings
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "consent", ignore = true)
    @Mapping(target = "institution", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ConnectedAccount toEntity(ConnectedAccountDTO dto);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "consentId", source = "consent.id")
    @Mapping(target = "institutionId", source = "institution.id")
    @Mapping(target = "institutionName", source = "institution.name")
    @Mapping(target = "institutionCode", source = "institution.code")
    @Mapping(target = "lastSyncedAt", source = "lastSyncedAt")
    @Mapping(target = "syncStatus", source = "syncStatus")
    @Mapping(target = "syncable", expression = "java(entity.isSyncable())")
    ConnectedAccountDTO toDTO(ConnectedAccount entity);

    List<ConnectedAccountDTO> toAccountDTOList(List<ConnectedAccount> accounts);

    // Account Sync Log mappings
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", source = "syncStatus")
    @Mapping(target = "syncedAt", source = "lastSyncedAt")
    AccountSyncLog toEntity(SyncStatusDTO dto);

    @Mapping(target = "accountId", source = "account.id")
    @Mapping(target = "syncStatus", source = "status")
    @Mapping(target = "syncType", source = "syncType")
    @Mapping(target = "recordsImported", source = "recordsImported")
    @Mapping(target = "errorMessage", source = "errorMessage")
    @Mapping(target = "lastSyncedAt", source = "syncedAt")
    @Mapping(target = "success", expression = "java(\"SUCCESS\".equals(entity.getStatus()))")
    SyncStatusDTO toDTO(AccountSyncLog entity);

    List<SyncStatusDTO> toSyncStatusDTOList(List<AccountSyncLog> logs);

    default List<String> parseScopes(String scopes) {
        if (scopes == null || scopes.isEmpty()) {
            return List.of();
        }
        return Arrays.asList(scopes.split(","));
    }
}
