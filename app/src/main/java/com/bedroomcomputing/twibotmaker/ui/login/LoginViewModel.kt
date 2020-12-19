package com.bedroomcomputing.twibotmaker.ui.login

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bedroomcomputing.twibotmaker.db.User
import com.bedroomcomputing.twibotmaker.db.UserDao
import kotlinx.coroutines.launch

class LoginViewModel(val userDao: UserDao) : ViewModel() {
    val user = MutableLiveData<User>()


    fun saveUser(user: User){
        viewModelScope.launch {
            Log.i("Login", "user:${user.screenName}")
            userDao.insert(user)
        }
    }

}

class LoginViewModelFactory(val userDao: UserDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(userDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}