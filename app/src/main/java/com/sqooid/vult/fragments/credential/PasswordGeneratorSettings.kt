package com.sqooid.vult.fragments.credential

data class PasswordGeneratorSettings(
    var length: Int,
    var useUppercase: Boolean,
    var useNumbers: Boolean,
    var useSymbols: Boolean
) {
    var lengthStr: String
        get() = length.toString()
        set(value) {
            length = try {
                value.toInt()
            } catch (e: NumberFormatException) {
                8
            }
        }
}
