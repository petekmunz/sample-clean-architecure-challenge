package com.safeboda.data.repo.datasource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.safeboda.data.Constants
import com.safeboda.data.apiservice.ApiService
import com.safeboda.data.mappers.UserMapper
import com.safeboda.data.models.Followers
import javax.inject.Inject

class FollowersPagingSource @Inject constructor(
    private val username: String,
    private val apiService: ApiService
): PagingSource<Int, Followers>() {

    override fun getRefreshKey(state: PagingState<Int, Followers>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Followers> {
        val pageNumber = params.key ?: 1
        return try {
            val response = apiService.getFollowers(username, Constants.NETWORK_PAGE_SIZE, pageNumber)
            val prevKey = if (pageNumber == 1) null else pageNumber - 1
            val nextKey = if (response.body()?.size == Constants.NETWORK_PAGE_SIZE) pageNumber + 1 else null
            if (response.isSuccessful) {
                LoadResult.Page(
                    data = response.body()?.map {
                        UserMapper().toFollower(username, it)
                    } ?: ArrayList(),
                    prevKey = prevKey,
                    nextKey = nextKey
                )
            } else {
                LoadResult.Page(
                    data = ArrayList(),
                    prevKey = prevKey,
                    nextKey = nextKey
                )
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}