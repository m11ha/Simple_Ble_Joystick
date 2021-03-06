package ru.experementy.simpleblejoystick

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import ru.experementy.customview.R
import ru.experementy.simpleblejoystick.utilsble.MyBleUtils


class MainActivity : FragmentActivity(),/* HelmFragment.HelmFragmentListener,*/
    DevicesListFragment.DLFragmentListener {
    companion object{
        const val U_BTNS="uBtns"
        const val J_ONE="jOne"
        const val J_TWO="jTwo"
        const val FLIP="flip"
        const val UUID="uuid"
        const val SERVUUID="servuuid"

    }

    private var bAdapter: BluetoothAdapter?=null

    private var uBtns: String?=null
    private var jOne: String?=null
    private var jTwo: String?=null


    private var helmFragment: HelmFragment?=null
    private var devicesListFragment: DevicesListFragment?=null
    private var optionsFragment: OptionsFragment?=null
    private var helpFragment: HelpFragment?=null

    private var myViewModel: MyViewModel?=null
    private var gatt: BluetoothGatt?=null

    private lateinit var maynToolbar: Toolbar
    private lateinit var imgConnected: ImageView
    private lateinit var txtConnected: TextView

//*********************************************************************


    private fun toDeviceList(){ // ?????????????? ?? ???????????? ??????????????????
        val gt=myViewModel?.myGatt
        if(gt!=null){
            gt.disconnect()
            myViewModel?.myGatt=null
        }
        val transaction=supportFragmentManager.beginTransaction()
        transaction.addToBackStack(null)
        transaction.replace(R.id.clMain,devicesListFragment!!).commit()
    }
//**********************************************************************
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //???????????????? ?????????????????? ?????????????????????? BLE**************

        if(!MyBleUtils.checkBleEnabled(this)) {
            Toast.makeText(this,"Bluetooth Low Energy unsupported",Toast.LENGTH_LONG).show()
            finish()
        }

        //???????????????? ????????????????????*******************
        MyBleUtils.checkPermissions(this)
        //************************************************

        //?????????????????????????? Mobile ADS Sdk (??????????????)
        MobileAds.initialize(this)
        val adView=findViewById<AdView>(R.id.adView)
        val adRequest=AdRequest.Builder().build()
        adView.loadAd(adRequest)

        //???????????? ???? ViewModel****************************
        myViewModel= ViewModelProvider(this).get(MyViewModel::class.java)

        //?????????????????? Bluetooth***************************
        val bm: BluetoothManager=getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bAdapter=bm.adapter
        myViewModel?.bAdapter=bAdapter //Bluetooth Adapter ?? ?????????? ????????????
        if(bAdapter!=null && !bAdapter?.isEnabled!!){
            val btIn=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                result: ActivityResult ->
                if(result.resultCode==0){
                    Toast.makeText(this, "Bluetooth is disabled", Toast.LENGTH_LONG).show()

                }
            }
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            btIn.launch(enableBtIntent)
        }


        //********************************************

        //???????????? ?????????????????????? ????????????????*********************

        val spref: SharedPreferences=getPreferences(MODE_PRIVATE)
        uBtns=spref.getString(U_BTNS,"")
        jOne=spref.getString(J_ONE,"")
        jTwo=spref.getString(J_TWO,"")

        myViewModel?.initArrays(uBtns,jOne,jTwo)
        myViewModel?.mirror=spref.getBoolean(FLIP,false)
        myViewModel?.setChUUID(spref.getString(UUID,null))
        myViewModel?.setSrvUUID(spref.getString(SERVUUID,null))


        //************************************************

        //??????????????????***************************************
        helmFragment= HelmFragment() // ?????????????? ????????????????


        devicesListFragment= DevicesListFragment() //???????????? ??????????????????


        optionsFragment= OptionsFragment() //??????????????????


        helpFragment=HelpFragment()


        //??????????????????************************
        imgConnected=findViewById(R.id.connectCheckImage)
        txtConnected=findViewById(R.id.connectCheckText)

        //Toolbar*************************
         maynToolbar=findViewById(R.id.mayn_toolbar)
        myViewModel?.maynToolbar=maynToolbar // Toolbar ?? ?????????? ????????????
        //????????**************
        maynToolbar.inflateMenu(R.menu.skan_menu)
        maynToolbar.setOnMenuItemClickListener{
            when(it.itemId){
                R.id.bleDevices -> {
                    val permissionCheck = ContextCompat.checkSelfPermission(this.applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
                    if(permissionCheck!= PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ),
                            1
                        )
                    }
                    else  toDeviceList()

                    true
                }
                R.id.settings -> {
                    val transaction=supportFragmentManager.beginTransaction()
                    transaction.addToBackStack(null)
                    transaction.replace(R.id.clMain,optionsFragment!!).commit()
                    true
                }
                R.id.back-> { //?????????????????? ?? ?????????????? ????????????????
                    this.supportFragmentManager.popBackStack()

                    //if(myViewModel?.currentFragment=="devicelist") this.supportFragmentManager.popBackStack()
                    //if(myViewModel?.currentFragment=="settings") this.supportFragmentManager.popBackStack()
                    true
                }
                R.id.help-> {
                    val transaction=supportFragmentManager.beginTransaction()
                    transaction.addToBackStack(null)
                    transaction.replace(R.id.clMain,helpFragment!!).commit()
                    true
                }
                R.id.restore -> { // ???????????????????????? ?????????????????? ???? ??????????????????
                    optionsFragment?.resData()
                    true
                }

                R.id.save -> {
                    optionsFragment?.saveData()
                    true
                }

                else -> false

            }
        }


        //*****************************

        //?????????????? ?? ???????????????? ??????????????????
        val transaction=supportFragmentManager.beginTransaction()
        transaction.add(R.id.clMain, helmFragment!!).commit()





    
    }

    override fun onDestroy() {
        super.onDestroy()
        if(gatt!=null&&gatt?.connectedDevices?.size!! >0){
            gatt?.disconnect()
        }
    }

    override fun onDLFragmentSelDevice(explDevice: ScanResult,onOff: Boolean) {

        if(onOff){
           gatt=explDevice.device.connectGatt(this,false,bluetoothGattCallback,BluetoothDevice.TRANSPORT_LE)
       }
        else gatt?.disconnect()

    }




    //******************???????????????? ????????????????????*********************

    private val bluetoothGattCallback= object : BluetoothGattCallback(){

        @SuppressLint("UseCompatLoadingForDrawables")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if(status== BluetoothGatt.GATT_SUCCESS){
                if(newState== BluetoothProfile.STATE_CONNECTED){
                    //????????????????????
                    gatt?.discoverServices()
                    devicesListFragment?.deviceNameColorChange(MyBleUtils.GATT_CONNECTED)

                   runOnUiThread{
                       devicesListFragment?.hideScanBut(true)
                       imgConnected.setImageDrawable(getDrawable(R.drawable.connect))
                       txtConnected.text=resources.getString(R.string.connected)
                   }

                }
                else{
                    //??????????????????
                    devicesListFragment?.deviceNameColorChange(MyBleUtils.GATT_DISCONNECTED)

                    runOnUiThread{
                        devicesListFragment?.hideScanBut(false)
                       // myViewModel?.maynToolbar?.logo=getDrawable(R.drawable.disconnect)
                        //myViewModel?.maynToolbar?.title="?????? ??????????????????????"
                        imgConnected.setImageDrawable(getDrawable(R.drawable.disconnect))
                        txtConnected.text=resources.getString(R.string.unconnected)
                    }
                    gatt?.close()


                }
            }
            else {
                //????????????
                devicesListFragment?.deviceNameColorChange(MyBleUtils.GATT_CONNECT_ERROR)

                runOnUiThread{
                    devicesListFragment?.hideScanBut(false)
                   // myViewModel?.maynToolbar?.logo=getDrawable(R.drawable.connecterror)
                    //myViewModel?.maynToolbar?.title="???????? ??????????????????????"
                    imgConnected.setImageDrawable(getDrawable(R.drawable.connecterror))
                    txtConnected.text=resources.getString(R.string.connErr)}
                gatt?.close()

            }

            super.onConnectionStateChange(gatt, status, newState)

        }


        //???????????????????? ?????????? ??????????????????????*********************
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            myViewModel?.myGatt=gatt
            //Gatt ???????????????????????????? ?????????? ???????????????? ?? HelmFragment

        }
        //***************************************************
    }


}








