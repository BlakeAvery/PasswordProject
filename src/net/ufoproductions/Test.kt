package net.ufoproductions

import com.sun.xml.internal.ws.api.model.CheckedException

fun main(args: Array<String>) {
    val username = "S12341"
    println("PasswordProject function Tester")
    println("Your username is $username. What is your password?")
    val password: String = readLine() ?: "BaLlZaCk#!21"
    println("First name?")
    val firstName: String = readLine() ?: "Jeff"
    println("Last name?")
    val lastName: String = readLine() ?: "Shegma"
    println("Name: $firstName $lastName. EIN $username.")
    print("Is password valid? ")
    val pingas = Checker().validatePassword(firstName, lastName, username, password)
    if(pingas[0] == CheckerReturnValues.PASSWORD_OK) {
        println("Yes.")
    } else {
        println("Nope. What's wrong with it?")
        for(x in pingas.indices) {
            when(pingas[x]) {
                CheckerReturnValues.PASSWORD_TOO_SHORT -> println("Password too short.")
                CheckerReturnValues.PASSWORD_ALPHANUMERIC_FAIL -> println("Password does not meet complexity reqs.")
                CheckerReturnValues.PASSWORD_NAME_IN_PASSWORD -> println("Name present in password.")
                CheckerReturnValues.PASSWORD_EIN_IN_PASSWORD -> println("EIN present in password.")
            }
        }
    }
}