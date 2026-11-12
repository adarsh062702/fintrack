package com.adarsh.fintrack.repository;

import java.math.BigDecimal;

public interface CategorySpendingProjection {

    Long getCategoryId();

    String getCategoryName();

    BigDecimal getTotalAmount();
}