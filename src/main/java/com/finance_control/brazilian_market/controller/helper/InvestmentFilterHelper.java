package com.finance_control.brazilian_market.controller.helper;

import com.finance_control.brazilian_market.model.Investment;
import com.finance_control.brazilian_market.model.InvestmentSubtype;
import com.finance_control.brazilian_market.model.InvestmentType;
import com.finance_control.brazilian_market.service.InvestmentService;
import com.finance_control.shared.util.RangeUtils;
import com.finance_control.users.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Helper class for investment filtering and pagination logic.
 * Extracted from InvestmentController to reduce class fan-out complexity.
 */
@Component
@RequiredArgsConstructor
public class InvestmentFilterHelper {

  private final InvestmentService investmentService;

  public List<Investment> getInvestmentsByFilters(User user, String search, InvestmentType type,
      InvestmentSubtype subtype, String sector, String industry) {
    if (search != null && !search.trim().isEmpty()) {
      return investmentService.searchInvestments(user, search);
    }

    if (type != null && subtype != null) {
      return investmentService.getInvestmentsByTypeAndSubtype(user, type, subtype);
    }

    if (type != null) {
      return investmentService.getInvestmentsByType(user, type);
    }

    if (sector != null && !sector.trim().isEmpty()) {
      return investmentService.getInvestmentsBySector(user, sector);
    }

    if (industry != null && !industry.trim().isEmpty()) {
      return investmentService.getInvestmentsByIndustry(user, industry);
    }

    return investmentService.getAllInvestments(user);
  }

  public List<Investment> applyPriceAndDividendFilters(List<Investment> investments,
      BigDecimal minPrice,
      BigDecimal maxPrice,
      BigDecimal minDividendYield,
      BigDecimal maxDividendYield) {
    if (minPrice == null && maxPrice == null && minDividendYield == null && maxDividendYield == null) {
      return investments;
    }

    return investments.stream()
        .filter(inv -> isPriceInRange(inv.getCurrentPrice(), minPrice, maxPrice))
        .filter(inv -> isDividendYieldInRange(inv.getDividendYield(), minDividendYield, maxDividendYield))
        .toList();
  }

  public <T> Page<T> paginateList(List<T> items, Pageable pageable) {
    int start = (int) pageable.getOffset();
    int end = Math.min((start + pageable.getPageSize()), items.size());
    List<T> pagedItems = start < items.size() ? items.subList(start, end) : List.of();
    return new PageImpl<>(pagedItems, pageable, items.size());
  }

  private boolean isPriceInRange(BigDecimal currentPrice, BigDecimal minPrice, BigDecimal maxPrice) {
    return RangeUtils.isInRange(currentPrice, minPrice, maxPrice);
  }

  private boolean isDividendYieldInRange(BigDecimal dividendYield, BigDecimal minDividendYield,
      BigDecimal maxDividendYield) {
    return RangeUtils.isInRange(dividendYield, minDividendYield, maxDividendYield);
  }
}
