package com.scrolltax.domain

import com.scrolltax.data.repository.TaxBracketRepository
import com.scrolltax.data.model.TaxBracket
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case to calculate progressive tax based on income and tax brackets.
 * It retrieves the list of [TaxBracket] from [TaxBracketRepository] and
 * applies the rates sequentially.
 */
class CalculateProgressiveTaxUseCase @Inject constructor(
    private val taxBracketRepository: TaxBracketRepository
) {
    /**
     * Calculates the total tax for the given [income].
     *
     * @param income The taxable income in cents (or any consistent unit).
     * @return The computed tax amount as a double.
     */
    suspend fun invoke(income: Long): Double {
        // Retrieve the tax brackets (first emission) and sort by minIncome.
        val brackets: List<TaxBracket> = taxBracketRepository.getTaxBrackets().first()
            .sortedBy { it.minIncome }

        var remainingIncome = income
        var totalTax = 0.0

        for (bracket in brackets) {
            if (remainingIncome <= 0) break
            val lower = bracket.minIncome
            val upper = bracket.maxIncome ?: Long.MAX_VALUE
            // Determine the amount of income that falls within this bracket.
            if (income >= lower) {
                val taxable = kotlin.math.min(remainingIncome, upper - lower + 1)
                totalTax += taxable * bracket.rate
                remainingIncome -= taxable
            }
        }
        return totalTax
    }
}
