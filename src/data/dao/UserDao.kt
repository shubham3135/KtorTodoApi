package com.shubhamkumarwinner.data.dao

import com.shubhamkumarwinner.data.User

interface UserDao {
    suspend fun createUser(name:String, email:String,password:String):User?
    suspend fun findUserById(userId: Int):User?
    suspend fun findUserByEmail(email: String):User?
    suspend fun deleteUser(userId: Int): Int
    suspend fun updateUser(UserId: Int, name: String, email: String, password: String):Int
}