package com.sqooid.vult.auth

class PasswordValidator {
    enum class PasswordWeakness {
        None,
        TooShort,
        NotEnoughVariety
    }

    companion object {
        const val PASSWORD_MIN_LENGTH = 8
        fun validate(password: String): PasswordWeakness {
            if (password.length < PasswordValidator.PASSWORD_MIN_LENGTH) {
                return PasswordWeakness.TooShort
            }
            if (Regex("[A-Z]").containsMatchIn(password) && Regex("[a-z]").containsMatchIn(password) && Regex(
                    "[-!\$%^&*()_+|~=`{}\\[\\]:\";'<>?,./@#]"
                ).containsMatchIn(password) && Regex("[0-9]").containsMatchIn(password)
            ) {
                return PasswordWeakness.None
            }
            return PasswordWeakness.NotEnoughVariety
        }
    }
}