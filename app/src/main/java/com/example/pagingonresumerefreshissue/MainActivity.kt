@file:OptIn(ExperimentalPagingApi::class)

package com.example.pagingonresumerefreshissue

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.paging.cachedIn
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.pagingonresumerefreshissue.ui.theme.PagingOnResumeRefreshIssueTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PagingOnResumeRefreshIssueTheme {
                val scope = rememberCoroutineScope()
                val pagingFlow = remember { pager.flow.cachedIn(scope) }
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var page2 by remember { mutableStateOf(false) }
                    if (page2) {
                        Text("Page 2")
                        BackHandler(onBack = { page2 = false })
                    } else {
                        Column {
                            Text("Page 1")
                            val list = pagingFlow.collectAsLazyPagingItems()
                            LaunchedEffect(Unit) {
                                // expected: this should trigger a refresh of the remote mediator every
                                // time you navigate back to this page
                                Log.d("TEST", "refresh called")
                                list.refresh()
                            }
                            LazyColumn {
                                items(list.itemCount, key = list.itemKey { it }) { index ->
                                    val item = list[index]
                                    Text(
                                        text = item ?: "...",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { page2 = true }
                                            .padding(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

val pager = Pager(
    config = PagingConfig(pageSize = 20),
    remoteMediator = FakeRemoteMediator(),
    pagingSourceFactory = {
        Log.d("TEST", "page source invalidated")
        FakePagingSource()
    }
)

class FakeRemoteMediator : RemoteMediator<Int, String>() {
    override suspend fun load(loadType: LoadType, state: PagingState<Int, String>): MediatorResult {
        if (loadType == LoadType.REFRESH) {
            Log.d("TEST", "refreshed!")
        }
        return MediatorResult.Success(endOfPaginationReached = true)
    }
}

class FakePagingSource : PagingSource<Int, String>() {

    private val items = List(50) { "Item $it" }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, String> {
        val offset = params.key ?: 0
        return LoadResult.Page(
            data = items.asSequence().drop(offset).take(params.loadSize).toList(),
            prevKey = null,
            nextKey = if (offset + params.loadSize < items.size) offset + params.loadSize else null
        )
    }

    override fun getRefreshKey(state: PagingState<Int, String>): Int {
        return ((state.anchorPosition ?: (0 - state.config.pageSize / 2))).coerceAtLeast(0)
    }
}