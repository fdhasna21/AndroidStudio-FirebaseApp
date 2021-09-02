package com.fdhasna21.postitfirebase.dataclass

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Profile(
    var name    : String? = null,
    var photoUrl : String? = null,
    var uid     : String? = null,
    var email   : String? = null,
    var bio     : String? = null
) : Parcelable
