package com.scrolltax.data.repository

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.scrolltax.data.model.TaxBracket
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TaxBracketRepository(private val context: Context) {
    private val moshi = Moshi.Builder().build()
    private val type = Types.newParameterizedType(List::class.java, TaxBracket::class.java)
    private val adapter = moshi.adapter<List<TaxBracket>>(type)

    fun getTaxBrackets(): Flow<List<TaxBracket>> = flow {
        val json = context.assets.open("tax_brackets.json").bufferedReader().use { it.readText() }
        val brackets = adapter.fromJson(json) ?: emptyList()
        emit(brackets)
    }
}
