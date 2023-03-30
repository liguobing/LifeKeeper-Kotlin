package com.lixyz.lifekeeperforkotlin.view.activity

interface ISetUserIconView {
    fun showWaitDialog()
    fun hideWaitDialog()
    fun showSnackBar(message: String)
    fun saveSuccessful(userIconUrl: String)
}