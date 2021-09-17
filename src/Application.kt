package com.shubhamkumarwinner

import com.shubhamkumarwinner.auth.JwtService
import com.shubhamkumarwinner.auth.MySession
import com.shubhamkumarwinner.auth.hashPassword
import com.shubhamkumarwinner.repository.DatabaseFactory
import com.shubhamkumarwinner.repository.TodoRepository
import com.shubhamkumarwinner.repository.UserRepository
import com.shubhamkumarwinner.route.todoRoute
import com.shubhamkumarwinner.route.userRoute
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.locations.*
import io.ktor.routing.*
import io.ktor.sessions.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module() {

    DatabaseFactory.init()
    val userDb = UserRepository()
    val todoDb = TodoRepository()
    val jwt = JwtService()
    val hash = {s: String -> hashPassword(s)}

    install(Locations) {
    }

    install(Sessions) {
        cookie<MySession>("MY_SESSION") {
            cookie.extensions["SameSite"] = "lax"
        }
    }

    install(Authentication) {
        jwt("jwt"){
            verifier(jwt.verifier)
            realm = "Todo Server"
            validate {
                val payload = it.payload
                val claim = payload.getClaim("userId")
                val claimInt = claim.asInt()
                val user = userDb.findUserById(claimInt)
                user
            }
        }
    }

    install(ContentNegotiation) {
        gson {
        }
    }

    routing {
        userRoute(userDb, todoDb, jwt, hash)
        todoRoute(userDb, todoDb)
    }
}

