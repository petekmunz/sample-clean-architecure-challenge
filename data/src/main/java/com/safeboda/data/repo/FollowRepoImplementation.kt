package com.safeboda.data.repo

import androidx.paging.*
import com.safeboda.data.apiservice.ApiService
import com.safeboda.data.db.FollowersDao
import com.safeboda.data.db.FollowingDao
import com.safeboda.data.db.SafeBodaDatabase
import com.safeboda.data.mappers.UserMapper
import com.safeboda.data.models.Followers
import com.safeboda.data.repo.mediator.FollowersRemoteMediator
import com.safeboda.data.repo.mediator.FollowingRemoteMediator
import com.safeboda.domain.models.FollowerOrFollowingModel
import com.safeboda.domain.repositories.FollowRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class FollowRepoImplementation @Inject constructor(
    private val followersDao: FollowersDao,
    private val followingDao: FollowingDao,
    private val dB: SafeBodaDatabase,
    private val apiService: ApiService
) : FollowRepository {

    override suspend fun getFollowers(userName: String): Flow<PagingData<FollowerOrFollowingModel>> {
        return Pager(
            config = PagingConfig(pageSize = 10),
            remoteMediator = FollowersRemoteMediator(userName, followersDao, dB, apiService),
        ) {
            followersDao.getFollowersOfAUser(userName)
        }.flow.map { pagingData ->
            pagingData.map {
                UserMapper().toFollowerOrFollowing(it)
            }
        }
    }

    override suspend fun getFollowing(userName: String): Flow<PagingData<FollowerOrFollowingModel>> {
        return Pager(
            config = PagingConfig(pageSize = 10),
            remoteMediator = FollowingRemoteMediator(userName, followingDao, dB, apiService),
        ) {
            followingDao.getFollowingOfAUser(userName)
        }.flow.map { pagingData ->
            pagingData.map {
                UserMapper().toFollowerOrFollowing(it)
            }
        }
    }
}