# Paging refresh issue

A sample repo showing off paging `refresh()` calls being dropped.

If I call `refresh()` from a `LaunchedEffect` on the current page, the call is dropped instead of
making it to the `RemoteMediator`.

```
val list = pagingFlow.collectAsLazyPagingItems()
LaunchedEffect(Unit) {
    // expected: this should trigger a refresh of the remote mediator every
    // time you navigate back to this page
    Log.d("TEST", "refresh called")
    list.refresh()
}
```

Looking at the implementation, this appears to be because there's a tick after collection where
a `uiRecfeiver` is not set.

```
public abstract class PagingDataDiffer...

    public suspend fun collectFrom(pagingData: PagingData<T>) {
        collectFromRunner.runInIsolation {
            uiReceiver = pagingData.uiReceiver
    ...


    public fun refresh() {
        log(DEBUG) { "Refresh signal received" }
        uiReceiver?.refresh()
    }

```