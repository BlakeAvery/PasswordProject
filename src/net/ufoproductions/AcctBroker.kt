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
    val accounts = File("C:\\Users\\Blake\\IdeaProjects\\PasswordProject\\src\\accounts")
    val checker = Checker()
    fun login(EIN: String, password: String): Byte {
        /**
         * Return for this function is as follows:
         * 0 - Log in successful.
         * 1 - Log in failure.
         * 2 - Password requires changing. Signals to frontend that new password is required.
         */
        var ret: Byte = 0
        //First, search for account existence
        val acctList = accounts.readLines()
        for(i in acctList.indices) {
            if(acctList[i].contains(EIN)) {
                //Move to next stage, pogchampion!
                val eee = csvParse(acctList[i])
                println("EIN: ${eee[0]}")
                println("First name: ${eee[1]}")
                println("Last name: ${eee[2]}")
                //check for account lock
                if(eee[7].toInt() != 0) {
                    //Check if account lockout is expired
                    if((Date().time - eee[5].toLong()) < 3600) {
                        //Account is still locked out.
                        ret = 1
                        return ret
                    } //otherwise keep going
                }
                //Compare hashed passwords
                if(eee[3] == byteArrayToString(MessageDigest.getInstance("SHA-256").digest(password.toByteArray()))) {
                    //modify specific line of accounts with new login time and login attemot, set account lockout to zero
                    val data = getListofAccts(accounts.readLines())
                    for(i in data.indices) {
                        if(data[i].EIN == EIN) {
                            data[i].lastLoginAttempt = Date().time
                            data[i].lastSuccessfulLogin = Date().time
                            data[i].isAccountLocked = false
                            data[i].passwordIncorrectCounter = 0
                        }
                    }
                    writeListofAccts(data)
                    ret = 0
                    return ret
                } else {
                    //password is wrong. Log login attempt, increase incorrect login counter, and then return false.
                    val data = getListofAccts(accounts.readLines())
                    for(i in data.indices) {
                        if(data[i].EIN == EIN) {
                            if((Date().time - data[i].lastLoginAttempt) < 3600) {
                                data[i].passwordIncorrectCounter++
                                if(data[i].passwordIncorrectCounter >= 3) {
                                    data[i].isAccountLocked = true
                                }
                            }
                            data[i].lastLoginAttempt = Date().time
                        }
                    }
                    writeListofAccts(data)
                    ret = 1
                    return ret
                }
            }
        }
        return ret
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
        accounts.appendText("${EIN.toString()},$firstName,$lastName,$hashedPasswordHash,0,${Date().time},${Date().time},0,${Date().time}\n")
        return true
    }

    fun isAcctOnLockout(EIN: String): Boolean {
        return false //TODO: Implement
    }

    fun changePassword(EIN: String, curPassword: String): Boolean {
        return true //TODO: Implement
    }

    private fun csvParse(line: String): Array<String> {
        val lines = line.split(",")
        return lines.toTypedArray()
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
        accounts.writeText("${list[0].EIN},${list[0].firstName},${list[0].lastName},${list[0].password},${list[0].passwordIncorrectCounter},${list[0].lastLoginAttempt},${list[0].lastSuccessfulLogin},${if(!list[0].isAccountLocked) 0 else 1},${list[0].lastPasswordChange}")
        for(i in 1 until list.size) {
            accounts.appendText("${list[i].EIN},${list[i].firstName},${list[i].lastName},${list[i].password},${list[i].passwordIncorrectCounter},${list[i].lastLoginAttempt},${list[i].lastSuccessfulLogin},${if(!list[i].isAccountLocked) 0 else 1},${list[i].lastPasswordChange}")
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