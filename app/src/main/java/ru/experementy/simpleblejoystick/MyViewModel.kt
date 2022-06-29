package ru.experementy.simpleblejoystick

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModel

@SuppressLint("StaticFieldLeak")
class MyViewModel: ViewModel() {




    //Toolbar*************
    @SuppressLint("StaticFieldLeak")
    //Расположение джойстиков
    var mirror: Boolean=false //Отзеркалить джойстики
    //UUID характеристики
    val defCharactUUID="ffe1"
    val defServUUID="ffe0"
    var charactUUID :String?=defCharactUUID
    var servUUID :String?=defServUUID


    fun setChUUID(uuid: String?){
        if(uuid!=null)charactUUID=uuid
        else charactUUID=defCharactUUID
    }

    fun setSrvUUID(uuid: String?){
        if(uuid!=null)servUUID=uuid
        else servUUID=defServUUID
    }

    var maynToolbar: Toolbar?=null
    var currentFragment: String?=null

    var bAdapter: BluetoothAdapter?=null
    var myGatt: BluetoothGatt?=null

    //Пользовательские кнопки******************
    private var btnsText= arrayOf("1","2","3","4","5","6","7","8","9")
    fun setBtnText(i: Int, txt: String){
        btnsText[i] = txt

    }
    fun getBtnText(i: Int)= btnsText.get(i)
    //*******************************************************
    //Первый джойстик****************
    private var joystOneVals= arrayOf("a","b","c","d","k","l","m","n")
    fun  getJoystOneValue(i: Int)= joystOneVals.get(i)
    fun setJoystOneValue(i: Int, txt: String){
        joystOneVals[i] = txt

    }

    private var joystTwoVals= arrayOf("A","B","C","D")
    fun  getJoystTwoValue(i: Int)= joystTwoVals.get(i)
    fun setJoystTwoValue(i: Int, txt: String){
        joystTwoVals[i] = txt
    }

    fun initArrays(uBtn: String?, jOne: String?, jTwo: String?){
        if(!uBtn?.isEmpty()!! &&!jOne?.isEmpty()!! &&!jTwo?.isEmpty()!!){
            btnsText=uBtn.split(",").toTypedArray()
            joystOneVals=jOne.split(",").toTypedArray()
            joystTwoVals=jTwo.split(",").toTypedArray()

        }
    }


}