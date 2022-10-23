package com.example.stockmarketappcompose.data.repository

import com.example.stockmarketappcompose.R
import com.example.stockmarketappcompose.data.csv.CSVParser
import com.example.stockmarketappcompose.data.local.StockDatabase
import com.example.stockmarketappcompose.data.mapper.toCompanyListing
import com.example.stockmarketappcompose.data.mapper.toCompanyListingEntity
import com.example.stockmarketappcompose.data.remote.StockApi
import com.example.stockmarketappcompose.domain.model.CompanyListing
import com.example.stockmarketappcompose.domain.repository.StockRepository
import com.example.stockmarketappcompose.util.Resource
import com.example.stockmarketappcompose.util.UiText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepositoryImpl @Inject constructor(
    private val api: StockApi,
    db: StockDatabase,
    private val companyListingParser: CSVParser<CompanyListing>
) : StockRepository {

    private val dao = db.dao

    override suspend fun getCompanyListings(
        fetchFromRemote: Boolean,
        query: String
    ): Flow<Resource<List<CompanyListing>>> {
        return flow {
            emit(Resource.Loading(isLoading = true))
            val localListing = dao.searchCompanyListing(query)
            emit(Resource.Success(
                data = localListing.map { it.toCompanyListing() }
            ))

            val isDbEmpty = localListing.isEmpty() && query.isEmpty()
            val shouldJustLoadFromCache = !isDbEmpty && !fetchFromRemote
            if (shouldJustLoadFromCache) {
                emit(Resource.Loading(isLoading = false))
                return@flow
            }
            val remoteListings = try {
                val response = api.getListings()
                companyListingParser.parse(response.byteStream())
            } catch (e: IOException) {
                emit(
                    Resource.Error(
                        message = UiText.StringResource(
                            resId = R.string.please_check_your_connection
                        )
                    )
                )
                null
            } catch (e: HttpException) {
                emit(
                    Resource.Error(
                        message = (e.localizedMessage ?: UiText.StringResource(
                            resId = R.string.Oops_something_went_wrong
                        )) as UiText
                    )
                )
                null
            }
            remoteListings?.let { listings ->
                dao.clearCompanyListings()
                dao.insertCompanyListings(
                    listings.map { it.toCompanyListingEntity() }
                )
                emit(Resource.Success(
                    data = dao
                        .searchCompanyListing("")
                        .map { it.toCompanyListing() }
                ))
                emit(Resource.Loading(isLoading = false))
            }
        }
    }
}