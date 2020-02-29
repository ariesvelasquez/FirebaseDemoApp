package com.example.firebasedemoapp.repository.main

import com.example.firebasedemoapp.model.Item
import com.example.firebasedemoapp.repository.Listing

interface IMainRepository {

    fun items() : Listing<Item>

}