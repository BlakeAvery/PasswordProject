package net.ufoproductions
import java.io.*
import java.security.MessageDigest
import java.util.*
import kotlin.IllegalArgumentException
import kotlin.random.Random

/**
 * AcctBroker - This class deals with higher level account functions. What does that mean? Specifically, AcctBroker deals with:
 * Account login: AcctBroker handles all login requests.
 * Account creation: AcctBroker creates all accounts.
 * Account lockout: Locks out accounts as detailed in planning.txt.
 * Password changes: Can handle password changing.
 *
 * A file called "accounts" is required in the root of the application so this class and by extension the program actually works.
 */

class AcctBroker {
    val accounts = File("accounts")
    val passwd = File("passwd")
    val checker = Checker()

    init {
        if(!accounts.exists()) {
            accounts.createNewFile()
        }
        if(!passwd.exists()) {
            passwd.createNewFile()
        }
    }
    fun login(EIN: String, password: String): Byte {
        //println("We are now in login function! Welcome! Version 1")
        /**
         * Return for this function is as follows:
         * 0 - Log in successful.
         * 1 - Log in failure.
         * 2 - Password requires changing. Signals to frontend that new password is required.
         * 3 - Account is on lockout. TODO: Implement
         * 4 -
         */
        var ret: Byte = 0
        //First, search for account existence
        val acctList = getListofAccts(accounts.readLines())
        for(i in acctList.indices) {
            if(acctList[i].EIN == EIN) {
                //Move to next stage, pogchampion!
                println("EIN: ${acctList[i].EIN}")
                println("First name: ${acctList[i].firstName}")
                println("Last name: ${acctList[i].lastName}")
                //check for account lock
                if(acctList[i].isAccountLocked) {
                    //Check if account lockout is expired
                    if((Date().time - acctList[i].lastLoginAttempt) < 3600 * 1000) {
                        //Account is still locked out.
                        ret = 3
                        return ret
                    } //otherwise keep going
                }
                //Compare hashed passwords
                if(acctList[i].password == byteArrayToString(MessageDigest.getInstance("SHA-256").digest(password.toByteArray()))) {
                    //Let's first check to make sure the password hasn't expired
                    if(Date().time - acctList[i].lastPasswordChange > 5184000.toLong() * 1000) { //More than 60 days since last password change?
                        ret = 2
                        return ret
                    } //else we continue with login.
                    //modify specific line of accounts with new login time and login attemot, set account lockout to zero
                    acctList[i].lastLoginAttempt = Date().time
                    acctList[i].lastSuccessfulLogin = Date().time
                    acctList[i].isAccountLocked = false
                    acctList[i].passwordIncorrectCounter = 0
                    writeListofAccts(acctList)
                    ret = 0
                    return ret
                } else {
                    //password is wrong. Log login attempt, increase incorrect login counter, and then return false.
                    if((Date().time - acctList[i].lastLoginAttempt) < 3600 * 1000) {
                        acctList[i].passwordIncorrectCounter++
                        if(acctList[i].passwordIncorrectCounter >= 3) {
                            acctList[i].isAccountLocked = true
                            acctList[i].lastLoginAttempt = Date().time
                            acctList[i].passwordIncorrectCounter = 0
                            ret = 3
                            return ret
                        }
                    }
                    acctList[i].lastLoginAttempt = Date().time
                    writeListofAccts(acctList)
                    ret = 1
                    return ret
                }
            } else { //EIN was not found.
                println("EIN not located. User does not exist.")
                ret = 1
                return ret
            }
        }
        return ret //This has to be here otherwise it breaks.
    }

    fun createAcct(firstName: String, lastName: String, password: String): Boolean { //TODO: Return user that represents if account was created. If 0000000, then account failed
        //first generate EIN.
        val EIN: StringBuilder = StringBuilder()
        EIN.append(lastName[0])
        var num = Random.nextInt(0, 99999)
        if(num < 10000) { //Too short for an EIN?
            val padded = num.toString().padStart(5, '0')
            EIN.append(padded)
        } else {
            EIN.append(num)
        }
        println("$EIN is our current EIN")
        //Then verify that password is allowed for use.
        val yeah = checker.validatePassword(firstName, lastName, EIN.toString(), password)
        if(yeah[0] == CheckerReturnValues.PASSWORD_OK) {
            println("PASSWORD OK.")
        } else {
            //log what caused error, return false. TODO: Change such that return for this fun is a list of reasons as done in Checker.
            for(x in yeah.indices) {
                when(yeah[x]) {
                    CheckerReturnValues.PASSWORD_TOO_SHORT -> println("Password too short.")
                    CheckerReturnValues.PASSWORD_ALPHANUMERIC_FAIL -> println("Password does not meet complexity reqs.")
                    CheckerReturnValues.PASSWORD_NAME_IN_PASSWORD -> println("Name present in password.")
                    CheckerReturnValues.PASSWORD_EIN_IN_PASSWORD -> println("EIN present in password.")
                }
            }
            return false
        }
        //then check if account exists
        val accts = accounts.readLines()
        for(i in accts.indices) {
            if(accts[i].contains(EIN)) {
                //if this condition is true, it's over so signal that account creation failed.
                return false
            }
        }
        //now if we make it through this loop it means that the account wasn't found. Make it.
        val hashedPasswordBytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray()) ?: throw IllegalArgumentException()
        val hashedPasswordHash = byteArrayToString(hashedPasswordBytes)
        accounts.appendText("${EIN},$firstName,$lastName,$hashedPasswordHash,0,${Date().time},${Date().time},0,${Date().time}\n)
        //For password history purposes
        val userPass = UserPass(EIN.toString(), Array(5) {hashedPasswordHash; ""; ""; ""; ""})
        println(userPass.EIN)
        return true
    }

    fun changePassword(EIN: String, curPassword: String, newPassword: String): Boolean {
        //First confirm that curPassword matches the password on file for that specific EIN
        var ret = false
        val acctList = getListofAccts(accounts.readLines())
        for(i in acctList.indices) {
            if(acctList[i].EIN == EIN) {
                if(acctList[i].password == byteArrayToString((MessageDigest.getInstance("SHA-256").digest(curPassword.toByteArray())))) {
                    //IF EIN is found AND IF hashed password matches hashed inputted password
                    val pingas = checker.validatePassword(acctList[i].firstName, acctList[i].lastName, acctList[i].EIN, newPassword)
                    if(pingas[0] != CheckerReturnValues.PASSWORD_OK) {
                        for(x in pingas.indices) {
                            when(pingas[x]) {
                                CheckerReturnValues.PASSWORD_TOO_SHORT -> println("Password too short.")
                                CheckerReturnValues.PASSWORD_ALPHANUMERIC_FAIL -> println("Password does not meet complexity reqs.")
                                CheckerReturnValues.PASSWORD_NAME_IN_PASSWORD -> println("Name present in password.")
                                CheckerReturnValues.PASSWORD_EIN_IN_PASSWORD -> println("EIN present in password.")
                            }
                        }
                        return ret
                    } else {
                        println("PASSWORD OK")
                    }
                    acctList[i].password = byteArrayToString((MessageDigest.getInstance("SHA-256").digest(newPassword.toByteArray())))
                    acctList[i].lastPasswordChange = Date().time
                    ret = true
                }
            }
        }
        writeListofAccts(acctList)
        return ret
    }

    private fun getListofAccts(list: List<String>): ArrayList<User> {
        val ret = ArrayList<User>()
        for(i in list.indices) {
            val sheckma = list[i].split(",")
            ret.add(User(sheckma[0],sheckma[1],sheckma[2],sheckma[3],sheckma[4].toByte(),sheckma[5].toLong(),sheckma[6].toLong(),sheckma[7].toInt() != 0,sheckma[8].toLong()))
        }
        return ret
    }
    private fun writeListofAccts(list: ArrayList<User>) {
        println("Writing accounts file...")
        //println("${list[0].EIN},${list[0].firstName},${list[0].lastName},${list[0].password},${list[0].passwordIncorrectCounter},${list[0].lastLoginAttempt},${list[0].lastSuccessfulLogin},${if(!list[0].isAccountLocked) 0 else 1},${list[0].lastPasswordChange}")
        accounts.writeText("${list[0].EIN},${list[0].firstName},${list[0].lastName},${list[0].password},${list[0].passwordIncorrectCounter},${list[0].lastLoginAttempt},${list[0].lastSuccessfulLogin},${if(list[0].isAccountLocked) 1 else 0},${list[0].lastPasswordChange}\n")
        if(list.size > 1) {
            for(i in 1 until list.size) {
                //println("${list[i].EIN},${list[i].firstName},${list[i].lastName},${list[i].password},${list[i].passwordIncorrectCounter},${list[i].lastLoginAttempt},${list[i].lastSuccessfulLogin},${if(!list[i].isAccountLocked) 0 else 1},${list[i].lastPasswordChange}")
                accounts.appendText("${list[i].EIN},${list[i].firstName},${list[i].lastName},${list[i].password},${list[i].passwordIncorrectCounter},${list[i].lastLoginAttempt},${list[i].lastSuccessfulLogin},${if(list[i].isAccountLocked) 1 else 0},${list[i].lastPasswordChange}\n")
            }
        }
    }
    private fun getListOfPasswds(list: List<String>): ArrayList<UserPass> {
        val ret = ArrayList<UserPass>()
        for(i in list.indices) {
            val yeah = list[i].split(",")
            val array = Array(5) {yeah[1]; yeah[2]; yeah[3]; yeah[4]; yeah[5]}
            ret.add(UserPass(yeah[0], array))
        }
        return ret
    }
    private fun writeListOfPasswds(list: ArrayList<UserPass>) {
        println("Writing passwd file...")
        passwd.writeText("${list[0].EIN},${list[0].passHistory[0]},${list[0].passHistory[1]},${list[0].passHistory[2]},${list[0].passHistory[3]},${list[0].passHistory[4]}\n")
        if(list.size > 1) {
            for(i in 1 until list.size) {
                passwd.appendText("${list[0].EIN},${list[0].passHistory[0]},${list[0].passHistory[1]},${list[0].passHistory[2]},${list[0].passHistory[3]},${list[0].passHistory[4]}\n")
            }
        }
    }
    private fun byteArrayToString(array: ByteArray): String { //here to deal with the byte arrays passed from createAcct's hash function
        val ret = StringBuilder()
        for(i in array.indices) {
            ret.append(String.format("%02x", array[i]))
        }
        return ret.toString()
    }
}