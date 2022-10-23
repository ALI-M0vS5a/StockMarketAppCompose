package com.example.stockmarketappcompose.presentation.company_listings

import com.example.stockmarketappcompose.domain.model.CompanyListing


data class CompanyListingsState(
    val companies: List<CompanyListing> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val searchQuery: String = ""
)
