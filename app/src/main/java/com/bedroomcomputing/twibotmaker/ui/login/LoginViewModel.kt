package com.bedroomcomputing.twibotmaker.ui.login

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bedroomcomputing.twibotmaker.db.User
import com.bedroomcomputing.twibotmaker.db.UserDao
import kotlinx.coroutines.launch
import twitter4j.auth.AccessToken

class LoginViewModel(val userDao: UserDao) : ViewModel() {
    val user = MutableLiveData<User>()


    fun saveUser(user: User){
        viewModelScope.launch {
            userDao.insert(user)
        }
    }


    fun storeUserToken(usr: twitter4j.User, accessToken: AccessToken) {

        val userId = usr.screenName
        val name = usr.name
        val token = accessToken.token
        val tokenSecret = accessToken.tokenSecret

        val newUser = User(userId = userId, name = name, token = token, tokenSecret = tokenSecret)
        saveUser(newUser)
        user.postValue(newUser)
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