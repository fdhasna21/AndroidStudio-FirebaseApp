package com.fdhasna21.postitfirebase.dataclass

data class Post(
    var content : String? = null,
    var type    : String?= null,
    var postUrl : String?= null,
    var time    : String?= null,
    var userUid     : String?= null,
    var userName : String? = null,
    var userEmail : String? = null,
    var userPhotoUrl : String? = null
    )
