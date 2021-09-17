package com.shubhamkumarwinner.route

import com.shubhamkumarwinner.auth.JwtService
import com.shubhamkumarwinner.auth.MySession
import com.shubhamkumarwinner.auth.hashPassword
import com.shubhamkumarwinner.data.User
import com.shubhamkumarwinner.repository.TodoRepository
import com.shubhamkumarwinner.repository.UserRepository
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*

fun Route.userRoute(
    userDb: UserRepository,
    todoDb: TodoRepository,
    jwt: JwtService,
    hash: (String)->String
){
    post("/v1/create"){
        val parameter = call.receive<Parameters>()
        val name = parameter["name"] ?: return@post call.respondText("missing data", status = HttpStatusCode.Unauthorized)
        val email = parameter["email"] ?: return@post call.respondText("missing data", status = HttpStatusCode.Unauthorized)
        val password = parameter["password"] ?: return@post call.respondText("missing data", status = HttpStatusCode.Unauthorized)

        val hashPassword = hash(password)

        val currentUser = userDb.createUser(name, email, hashPassword)
        try {
            currentUser?.userId?.let {
                call.sessions.set(MySession(it))
                call.respondText(
                    jwt.generateToken(currentUser),
                    status = HttpStatusCode.Created
                )
            }
        }catch (e: Throwable){
            call.respondText("Problem creating user..")
        }
    }

    post("/v1/login"){
        val parameter = call.receive<Parameters>()
        val email = parameter["email"] ?: return@post call.respondText("missing data", status = HttpStatusCode.Unauthorized)
        val password = parameter["password"] ?: return@post call.respondText("missing data", status = HttpStatusCode.Unauthorized)
        val hashPassword = hash(password)
        try {
            val currentUser = userDb.findUserByEmail(email)

            currentUser?.userId?.let {
                if (currentUser.password == hashPassword){
                    call.sessions.set(MySession(userId = it))
                    call.respondText(jwt.generateToken(currentUser))
                }else{
                    call.respondText("wrong password")
                }
            }
        }catch (e: Exception){
            call.respondText("Problem signing in..$e")
            println("exception is $e")
        }
    }

    delete("v1/user"){
        val user = call.sessions.get<MySession>()?.let {
            userDb.findUserById(it.userId)
        }

        if (user == null){
            call.respondText("Problem getting user", status = HttpStatusCode.BadRequest)
        }

        try {
            user?.userId?.let { it1 ->
                todoDb.deleteAllTodo(it1)
            }
            val currentUser = user?.userId?.let { it1 -> userDb.deleteUser(it1) }
            if (currentUser == 1){
                call.respondText("user deleted..")
            }else{
                call.respondText("Problem deleting..")
            }

        }catch (e: Throwable){
            call.respondText("Problem deleting..")
        }
    }

    put("/v1/user"){
        val parameter = call.receive<Parameters>()
        val name = parameter["name"] ?: return@put call.respondText("missing data", status = HttpStatusCode.Unauthorized)
        val email = parameter["email"] ?: return@put call.respondText("missing data", status = HttpStatusCode.Unauthorized)
        val password = parameter["password"] ?: return@put call.respondText("missing data", status = HttpStatusCode.Unauthorized)
        val user = call.sessions.get<MySession>()?.let {
            userDb.findUserById(it.userId)
        }
        if (user == null){
            call.respondText("Problem getting user", status = HttpStatusCode.BadRequest)
        }
        val hashPassword = hash(password)
        try {
            val currentUser = user?.userId?.let { it1 -> userDb.updateUser(it1, name, email, hashPassword) }
            if (currentUser == 1){
                call.respondText("Updated successfully")
            }else{
                call.respondText("Problem updating user")
            }
        }catch (e: Throwable){
            call.respondText("Problem updating..")
        }
    }
}