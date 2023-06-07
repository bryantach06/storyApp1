package com.example.storyapp.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.datastore.core.DataStore
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.recyclerview.widget.ListUpdateCallback
import com.example.storyapp.adapters.MainAdapter
import com.example.storyapp.data.StoryRepository
import com.example.storyapp.models.UserPreference
import com.example.storyapp.responses.ListStoryItem
import com.example.storyapp.utils.DataDummy
import com.example.storyapp.utils.MainDispatcherRule
import com.example.storyapp.utils.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var storyRepository: StoryRepository

    private lateinit var userPrefs: UserPreference
    private lateinit var storyViewModel: MainViewModel
    private val dummyStory = DataDummy.generateDummyStories()

    @Mock
    private lateinit var dataStore: DataStore<androidx.datastore.preferences.core.Preferences>

    @Before
    fun setUp() {
        userPrefs = UserPreference(dataStore)
        storyViewModel = MainViewModel(userPrefs, storyRepository)
    }

    @Test
    fun `when Get Stories Should Not Null and Return Success`() = runTest {
        val expectedStories = MutableLiveData<PagingData<ListStoryItem>>()
        expectedStories.value = PagingData.from(dummyStory)

        `when`(storyRepository.getStory()).thenReturn(expectedStories)

        val actualStories = storyViewModel.story.getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = MainAdapter.DIFF_CALLBACK,
            updateCallback = listUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(actualStories)

        Mockito.verify(storyRepository).getStory()

        //data tidak null
        assertNotNull(actualStories)

        //jumlah data sesuai dengan yang diharapkan
        assertEquals(dummyStory.size, differ.snapshot().size)

        //data pertama yang dikembalikan sesuai
        assertEquals(dummyStory[0], differ.snapshot()[0])
    }

    @Test
    fun `when Get Story Empty Should Return No Data`() = runTest{
        val data: PagingData<ListStoryItem> = PagingData.from(emptyList())
        val expectedStory = MutableLiveData<PagingData<ListStoryItem>>()
        expectedStory.value = data
        `when`(storyRepository.getStory()).thenReturn(expectedStory)

        val actualStory: PagingData<ListStoryItem> = storyViewModel.story.getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = MainAdapter.DIFF_CALLBACK,
            updateCallback = listUpdateCallback,
            workerDispatcher = Dispatchers.Main
        )
        differ.submitData(actualStory)
        assertEquals(0, differ.snapshot().size)
    }

    private val listUpdateCallback = object : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {}
        override fun onRemoved(position: Int, count: Int) {}
        override fun onMoved(fromPosition: Int, toPosition: Int) {}
        override fun onChanged(position: Int, count: Int, payload: Any?) {}
    }
}
