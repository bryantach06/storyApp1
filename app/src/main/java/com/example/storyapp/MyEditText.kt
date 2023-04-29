package com.example.storyapp

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

class MyEditText : AppCompatEditText {

    private var hasBeenTouched = false

    init {
        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        setOnTouchListener { _, _ ->
            hasBeenTouched = true
            false
        }
    }

    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
    }

    private fun validatePassword() {
        if (hasBeenTouched) {
            val password = text.toString()
            if (password.length < 8) {
                error = "Password must be at least 8 characters long"
            } else {
                error = null
            }
        }
    }

    override fun onTextChanged(
        text: CharSequence?,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        validatePassword()
    }

}
