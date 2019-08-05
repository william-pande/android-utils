package com.wilsofts.utilities.dialogs

import java.io.Serializable

interface ReturnResponse : Serializable {
    fun response(proceed: Boolean)
}
