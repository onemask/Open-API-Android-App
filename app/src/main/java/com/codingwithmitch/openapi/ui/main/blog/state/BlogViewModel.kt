package com.codingwithmitch.openapi.ui.main.blog.state

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import com.bumptech.glide.RequestManager
import com.codingwithmitch.openapi.models.BlogPost
import com.codingwithmitch.openapi.repository.main.BlogRepository
import com.codingwithmitch.openapi.session.SessionManager
import com.codingwithmitch.openapi.ui.BaseViewModel
import com.codingwithmitch.openapi.ui.DataState
import com.codingwithmitch.openapi.ui.main.blog.state.BlogStateEvent.*
import com.codingwithmitch.openapi.util.AbsentLiveData
import javax.inject.Inject

class BlogViewModel
@Inject
constructor(
    private val sessionManager: SessionManager,
    private val blogRepository: BlogRepository,
    private val sharedPreferences: SharedPreferences,
    private val requestManager: RequestManager
): BaseViewModel<BlogStateEvent, BlogViewState>(){

    override fun handleStateEvent(stateEvent: BlogStateEvent): LiveData<DataState<BlogViewState>> {
        when(stateEvent){

            is BlogSearchEvent -> {
                return sessionManager.cachedToken.value?.let { authToken ->
                    blogRepository.searchBlogPosts(
                        authToken,
                        viewState.value!!.blogFields.searchQuery,
                        viewState.value!!.blogFields.page
                    )
                }?: AbsentLiveData.create()
            }

            is NextPageEvent -> {
                Log.d(TAG, "BlogViewModel: NextPageEvent detected...")

                if(!viewState.value!!.blogFields.isQueryInProgress
                    && !viewState.value!!.blogFields.isQueryExhausted){
                    Log.d(TAG, "BlogViewModel: Attempting to load next page...")
                    setQueryInProgress(true)
                    incrementPageNumber()
                    return sessionManager.cachedToken.value?.let { authToken ->
                        blogRepository.searchBlogPosts(
                            authToken,
                            viewState.value!!.blogFields.searchQuery,
                            viewState.value!!.blogFields.page
                        )
                    }?: AbsentLiveData.create()
                }
                else{
                    return AbsentLiveData.create()
                }
            }

            is CheckAuthorOfBlogPost -> {
                return AbsentLiveData.create()
            }

            is None ->{
                return AbsentLiveData.create()
            }
        }
    }

    override fun initNewViewState(): BlogViewState {
        return BlogViewState()
    }

    fun cancelActiveJobs(){
        blogRepository.cancelActiveJobs() // cancel active jobs
        handlePendingData() // hide progress bar
    }

    fun handlePendingData(){
        setStateEvent(None())
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }

}










