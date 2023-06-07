package com.example.storyapp.data
import android.content.Context
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.storyapp.LoginSession
import com.example.storyapp.api.ApiService
import com.example.storyapp.responses.ListStoryItem

class StoryPagingSource(private val apiService: ApiService, context: Context): PagingSource<Int, ListStoryItem>() {

    private var loginSession: LoginSession = LoginSession(context)

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }

    override fun getRefreshKey(state: PagingState<Int, ListStoryItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListStoryItem> {
        return try {
            val token = loginSession.passToken().toString()
            val position = params.key ?: INITIAL_PAGE_INDEX
            val responseData = apiService.getStoriesPaging("Bearer $token", position, params.loadSize).listStory
            LoadResult.Page(
                data = responseData,
                prevKey = if (position == INITIAL_PAGE_INDEX) null else position - 1,
                nextKey = if (responseData.isEmpty()) null else position + 1
            )
        } catch (exception: Exception) {
            return LoadResult.Error(exception)
        }
    }
}