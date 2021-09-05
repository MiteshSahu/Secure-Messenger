package com.example.securemessanger.model

class ChatMessage(val id:String, var text: String, val fromId: String, val toId: String, val timestamp: Long)
{
    constructor(): this("","","","",-1)
}