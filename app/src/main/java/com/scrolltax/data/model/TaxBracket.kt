package com.scrolltax.data.model

data class TaxBracket(
    val minIncome: Long,      // inclusive
    val maxIncome: Long?,     // null indicates no upper bound
    val rate: Double          // e.g., 0.05 for 5%
)
