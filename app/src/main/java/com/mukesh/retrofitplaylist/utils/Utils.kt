package com.mukesh.retrofitplaylist.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mukesh.retrofitplaylist.network.ApiCall
import com.mukesh.retrofitplaylist.network.ApiInterface
import com.mukesh.retrofitplaylist.network.NetworkStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import retrofit2.Response

/**
 * Observe Data
 * */
fun <T> LiveData<NetworkStatus<T>>.observeData(
    lifecycleOwner: LifecycleOwner,
    onLoading: () -> Unit = {},
    onSuccess: (data: T) -> Unit,
    onError: (error: String) -> Unit = {}
) {
    observe(lifecycleOwner) {
        when (it) {
            is NetworkStatus.Loading -> {
                onLoading()
            }

            is NetworkStatus.Error -> {
                onError(it.error)
                //Toast Add
            }

            is NetworkStatus.Success -> {
                onSuccess(it.data)
            }
        }
    }
}


suspend fun <T> Flow<Response<T>>.setData(mutableLiveData: MutableLiveData<NetworkStatus<T?>>) {
    onStart {
        mutableLiveData.postValue(NetworkStatus.Loading())
    }.catch {
        mutableLiveData.postValue(NetworkStatus.Error(it.localizedMessage.toString()))
    }.collectLatest {
        if (it.isSuccessful) {
            mutableLiveData.postValue(NetworkStatus.Success(it.body()))
        } else {
            mutableLiveData.postValue(NetworkStatus.Error(it.errorBody()?.string().toString()))
        }
    }
}
