package net.ufoproductions

data class User(val EIN: String,
                val firstName: String,
                val lastName: String,
                var password: String,
                var passwordIncorrectCounter: Byte,
                var lastLoginAttempt: Long,
                var lastSuccessfulLogin: Long,
                var isAccountLocked: Boolean,
                var lastPasswordChange: Long) {
}