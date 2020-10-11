package net.ufoproductions

enum class CheckerReturnValues(val retcode: Int) {
    PASSWORD_OK(0),
    PASSWORD_TOO_SHORT(1),
    PASSWORD_ALPHANUMERIC_FAIL(2),
    PASSWORD_NAME_IN_PASSWORD(3),
    PASSWORD_EIN_IN_PASSWORD(4)
}