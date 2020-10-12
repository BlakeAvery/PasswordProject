package net.ufoproductions

fun main(args: Array<String>) {
    val acctBroker = AcctBroker()
    println("PasswordProject function Tester")
    println("Enter function:\n0 for login\n1 for new account")
    while(true) {
        val input = readLine()?.get(0) ?: '0'
        when(input) {
            '0' -> {
                println("What is your username?")
                val username = readLine() ?: ""
                println("What is your password?")
                val password = readLine() ?: ""
                when(acctBroker.login(username, password)) {
                    0.toByte() -> {
                        println("Login successful.")
                    }
                    1.toByte() -> {
                        println("Login failed. Check password or if account is in lockout.")
                    }
                    2.toByte() -> {

                    }
                }
            }
            '1' -> {
                println("What is your first name?")
                val firstName = readLine() ?: ""
                println("What is your last name?")
                val lastName = readLine() ?: ""
                println("Enter password.")
                val password = readLine() ?: ""
                println("Enter password again.")
                if((readLine() ?: "") != password) {
                    println("Passwords didn't match. Cause of you, nothing good happens.")
                } else {
                    if(acctBroker.createAcct(firstName, lastName, password)) {
                        println("Account created.")
                    } else {
                        println("Account creation failed.")
                    }
                }
            }
        }
    }
}