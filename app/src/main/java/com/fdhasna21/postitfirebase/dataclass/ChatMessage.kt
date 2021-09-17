package com.fdhasna21.postitfirebase.dataclass

data class ChatMessage(
    var date : String? = null,
    var content : String? = null,
    var receiverUID : String? = null,
    var messageUrl : String? = null,
    var senderUID : String? = null,
    var time : String? = null,
    var type    : String?= null
)
