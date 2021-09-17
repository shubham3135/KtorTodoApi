package com.shubhamkumarwinner.route

import com.shubhamkumarwinner.auth.MySession
import com.shubhamkumarwinner.repository.TodoRepository
import com.shubhamkumarwinner.repository.UserRepository
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*

fun Route.todoRoute(
    userDb: UserRepository,
    todoDb: TodoRepository
){
    authenticate("jwt"){
        post("/v1/todo"){
            val parameter = call.receive<Parameters>()

            val todo = parameter["todo"] ?: return@post call.respondText("missing parameter", status = HttpStatusCode.Unauthorized)
            val done = parameter["done"] ?: return@post call.respondText("missing parameter", status = HttpStatusCode.Unauthorized)

            val user = call.sessions.get<MySession>()?.let {
                userDb.findUserById(it.userId)
            }
            if (user == null){
                call.respondText("Problem getting user....")
            }
            try {
                val currentTodo = user?.userId?.let { it1 -> todoDb.createTodo(it1, todo, done.toBoolean()) }
                currentTodo?.id?.let {
                    call.respond(currentTodo)
                }
            }catch (e: Exception){
                call.respondText("Problem creating todo..")
            }
        }
    }

    get("/v1/todo"){
        val user = call.sessions.get<MySession>()?.let {
            userDb.findUserById(it.userId)
        }
        if (user == null){
            call.respondText("Problem getting user....")
        }

        try {
            val allTodo = user?.userId?.let { it1 -> todoDb.getAllTodo(it1) }
            if (allTodo?.isNotEmpty() == true){
                call.respond(allTodo)
            }
        }catch (e: Exception){
            call.respondText("Problem getting todos..")
        }
    }

    delete("/v1/todo{id}"){
        val id = call.parameters["id"]
        val user = call.sessions.get<MySession>()?.let {
            userDb.findUserById(it.userId)
        }
        if (user == null){
            call.respondText("Problem getting user....")
        }

        try {
            val allTodo = user?.let { it1 -> todoDb.getAllTodo(it1.userId) }
            allTodo?.forEach {
                if (it.id == id?.toInt()){
                    todoDb.deleteTodo(id.toInt())
                    call.respondText("deleted todo..")
                }else{
                    call.respondText("Problem getting todo...")
                }
            }
        }catch (e: Exception){
            call.respondText("Problem getting todo..")
        }
    }

    delete("/v1/todo"){
        val user = call.sessions.get<MySession>()?.let {
            userDb.findUserById(it.userId)
        }
        if (user == null){
            call.respondText("Problem getting user....")
        }

        try {
            val allTodo = user?.userId?.let { it1 -> todoDb.deleteAllTodo(it1) }
            if (allTodo != null){
                if (allTodo>0){
                    call.respondText("deleted successfully...")
                }else{
                    call.respondText("Problem getting todo....")
                }
            }

        }catch (e: Exception){
            call.respondText("Problem getting todo..")
        }
    }

    put("/v1/todo/{id}"){
        val id = call.parameters["id"]
        val user = call.sessions.get<MySession>()?.let {
            userDb.findUserById(it.userId)
        }
        if (user == null){
            call.respondText("Problem getting user....")
        }

        val parameter = call.receive<Parameters>()

        val todo = parameter["todo"] ?: return@put call.respondText("missing parameter", status = HttpStatusCode.Unauthorized)
        val done = parameter["done"] ?: return@put call.respondText("missing parameter", status = HttpStatusCode.Unauthorized)

        try {
            val allTodo = user?.let { it1 -> todoDb.getAllTodo(it1.userId) }
            allTodo?.forEach {
                if (it.id == id?.toInt()){
                    todoDb.updateTodo(id?.toInt(), todo, done.toBoolean())
                    call.respondText("updated successfully...")
                } else{
                    call.respondText("getting problem")
                }
            }
        }catch (e: Exception){
            call.respondText("Problem getting todo..")
        }
    }
}