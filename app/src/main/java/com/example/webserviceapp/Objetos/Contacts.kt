package com.example.webserviceapp.Objetos

import java.io.Serializable

data class Contacts(
    var id: Int = 0,
    var name: String = "",
    var phoneNumber1: String = "",
    var phoneNumber2: String = "",
    var address: String = "",
    var notes: String = "",
    var is_favorite: Int = 0,
    var id_movil: String = "",
) : Serializable