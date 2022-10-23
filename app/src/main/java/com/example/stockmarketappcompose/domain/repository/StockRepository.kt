package com.example.stockmarketappcompose.domain.repository

import com.example.stockmarketappcompose.domain.model.CompanyListing
import com.example.stockmarketappcompose.util.Resource
import kotlinx.coroutines.flow.Flow


interface StockRepository {
    suspend fun getCompanyListings(
        fetchFromRemote: Boolean,
        query: String
    ): Flow<Resource<List<CompanyListing>>>
}