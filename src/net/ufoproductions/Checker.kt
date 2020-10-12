package net.ufoproductions
import kotlin.experimental.xor
//import net.ufoproductions.CheckerReturnValues

class Checker {
    /**
     * Checker is the class that does the primary password and account verification in this program.
     * It performs password validation and is capable of initiating a lockout when conditions allow.
     */
    fun validatePassword(firstName: String, lastName: String, EIN: String, password: String): ArrayList<CheckerReturnValues> { //ret value should be either true or false
        val ret: ArrayList<CheckerReturnValues> = ArrayList<CheckerReturnValues>()
        //First check that password meets beginning reqs
        if(password.length < 14) ret.add(CheckerReturnValues.PASSWORD_TOO_SHORT)
        //next validate alphnaumeric parameters
        val passStatus: Byte = validateAlphanumericParams(password)
        println("Validation return code: $passStatus")
        if(passStatus xor 0b00001111 != 0.toByte()) ret.add(CheckerReturnValues.PASSWORD_ALPHANUMERIC_FAIL)
        //next hit up the name and EIN validation
        if(password.toLowerCase().contains(firstName.toLowerCase()) || //Is this good practice? 🤔
                password.toLowerCase().contains(lastName.toLowerCase())) ret.add(CheckerReturnValues.PASSWORD_NAME_IN_PASSWORD)
        if(password.toUpperCase().contains(EIN)) ret.add(CheckerReturnValues.PASSWORD_EIN_IN_PASSWORD)
        if(ret.isEmpty()) ret.add(CheckerReturnValues.PASSWORD_OK) //If empty, then we add password OK to let everyone know
        return ret
    }
    private fun validateAlphanumericParams(password: String): Byte {
        /*
            Tests passwords for character requirements.
            Returns a byte with the first four bytes to the right set depending on which conditions were met.
            Ideally for this one we have a value of 15 on the wire.
         */
        var ret: Byte = 0
        var upperCounter: Int = 0
        var lowerCounter: Int = 0
        var numCounter: Int = 0
        var specialCharCounter: Int = 0
        for(i in 0 until password.length - 1) {
            when(password[i]) {
                in 'A'..'Z' -> upperCounter++
                in 'a'..'z' -> lowerCounter++
                in '0'..'9' -> numCounter++
                in ' '..'/' -> specialCharCounter++
                in ':'..'@' -> specialCharCounter++
                in '['..'`' -> specialCharCounter++
                in '{'..'~' -> specialCharCounter++
                else -> specialCharCounter++
            }
        }
        println("UpperCounter: $upperCounter")
        println("LowerCounter: $lowerCounter")
        println("NumCounter: $numCounter")
        println("SpecialCharCounter: $specialCharCounter")
        if(upperCounter > 1) ret = ret xor 0b00000001
        if(lowerCounter > 1) ret = ret xor 0b00000010
        if(numCounter > 1) ret = ret xor 0b00000100
        if(specialCharCounter > 1) ret = ret xor 0b00001000
        return ret
    }
}