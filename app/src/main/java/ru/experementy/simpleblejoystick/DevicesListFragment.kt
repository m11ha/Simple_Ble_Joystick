package ru.experementy.simpleblejoystick

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.experementy.customview.R
import ru.experementy.simpleblejoystick.utilsble.MyBleUtils


class DevicesListFragment : Fragment(),View.OnClickListener {
    private var handler: Handler?=null
    private var myViewModel: MyViewModel?=null

    var devlist= mutableListOf<ScanResult>()
    var isScanning=false
    private lateinit  var btn: Button
    private lateinit var tvScan: TextView
    private lateinit var  tv :RecyclerView
    private lateinit var tvNoDevMess: TextView
    private lateinit var scanprogress: ProgressBar

    //Gatt сервер****************************

    var pressedRVItem: Int?=null

    var bAdapter :BluetoothAdapter?=null

    //************************************





    //Настройка взаимодейсрвия с activity*************************

    interface DLFragmentListener{
        fun onDLFragmentSelDevice(explDevice: ScanResult,onOff: Boolean)
    }
    var dlfragmentlistener: DLFragmentListener?=null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            dlfragmentlistener=context as DLFragmentListener
        }catch (e: ClassCastException){
            throw ClassCastException("KAKA")
        }
    }


    //Обьект класса ScanCallback

   private val scan = object : ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            if(result?.device!=null){
                if(result.device.name!=null){
                    var isContain=false
                    for(elem in devlist) {
                        if(result.device.address==elem.device.address){
                            isContain=true
                        }
                    }
                    if(!isContain) devlist.add(result)
                }
            }

        }
    }
    object stopscan: ScanCallback(){
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
        }
    }






    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        handler= Handler(Looper.getMainLooper())

        // Inflate the layout for this fragment
        myViewModel= ViewModelProvider(requireActivity()).get(MyViewModel::class.java)

        //Инициализация Bluetooth Adapter***
        bAdapter=myViewModel?.bAdapter
        //**************************************

        //Меню************************************************

        //Прячем пункты меню перехода к списку устройств и к настройкам
        //Показываем назад
        myViewModel?.apply{
            maynToolbar?.menu?.findItem(R.id.bleDevices)?.isVisible=false
            maynToolbar?.menu?.findItem(R.id.settings)?.isVisible=false
            maynToolbar?.menu?.findItem(R.id.back)?.isVisible=true
            maynToolbar?.menu?.findItem(R.id.help)?.isVisible=false

            currentFragment="devicelist" // текущий фрагмент
        }

        //**************************************************
        val view=inflater.inflate(R.layout.fragment_devices_list, container, false)



        //Список устройств /Recycler View/*************

        tv=view.findViewById<RecyclerView>(R.id.rvDevicesList1)
        tv.layoutManager=LinearLayoutManager(context)


      
        tvNoDevMess=view.findViewById(R.id.nodevicesmess)
        tvNoDevMess.visibility=View.GONE

        scanprogress=view.findViewById(R.id.scanprogress)
        scanprogress.visibility=View.GONE



        btn=view.findViewById(R.id.button)
        btn.setOnClickListener(this)

        tvScan=view.findViewById(R.id.txt_rescan)

       if(bAdapter?.bluetoothLeScanner!=null&&myViewModel?.myGatt==null)scanControl(tvScan)
        hideScanBut(false)
        return view
    }

    override fun onStop() {
        handler?.removeCallbacksAndMessages(null)
        super.onStop()
    }



    //*********************************** Сканирование **********************************

    //*********Старт/стоп сканирования**************
    private fun scanning(bAdapter: BluetoothAdapter){
        scanprogress.visibility=View.VISIBLE
        tv.visibility=View.GONE
        tvNoDevMess.visibility=View.GONE
        devlist.clear()
        bAdapter.bluetoothLeScanner.startScan(scan)
    }
    private fun stopScannning(bAdapter: BluetoothAdapter){
        scanprogress.visibility=View.GONE
        bAdapter.bluetoothLeScanner.stopScan(stopscan)
        if(devlist.size==0){
            tvNoDevMess.visibility=View.VISIBLE
            tv.visibility=View.GONE
        }else{
            tvNoDevMess.visibility=View.GONE
            tv.visibility=View.VISIBLE
            tv.adapter=myAdapter(devlist)
        }


    }

    //Запустить / прервать сканирование****************
    private fun scanControl(v: View?)

    {
        val tv=v as TextView?
        when(isScanning){
            false->{
                scanning(bAdapter!!)
                isScanning=true
                tv?.text= resources.getString(R.string.interrupt)


                handler?.postDelayed({
                    stopScannning(bAdapter!!)
                    isScanning=false
                    tv?.text= resources.getString(R.string.scan)
                },5000)

            }
            true->{
                stopScannning(bAdapter!!)
                isScanning=false
                tv?.text= resources.getString(R.string.scan)
            }
        }

    }
    //******************************************************
    //Сканировать устройства
    override fun onClick(v: View?) {
        scanControl(tvScan)
    }


    companion object {

        const val REQUEST_ENABLE_BT=1

        const val GATT_CONNECTED= MyBleUtils.GATT_CONNECTED
        const val GATT_DISCONNECTED= MyBleUtils.GATT_DISCONNECTED

    }

    //RecycklerView Adapter**********************************************

    inner class myAdapter(private val devices: List<ScanResult>): RecyclerView.Adapter<myAdapter.ViewHolder>(){
        inner class ViewHolder(listItem: View): RecyclerView.ViewHolder(listItem){
            val nameTextView=listItem.findViewById<TextView>(R.id.devicename)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val context=parent.context
            val inflater=LayoutInflater.from(context)
            val deviceView=inflater.inflate(R.layout.item_layout,parent,false)
            //Обработка нажатия элемента списка
            deviceView.setOnClickListener{

                    val i=parent.indexOfChild(it)
                    if(pressedRVItem!=null&&pressedRVItem!=i) UInt

                    else if(pressedRVItem==null){
                        pressedRVItem=i
                       // gatt=devices[i].device
                               // .connectGatt(activity?.applicationContext,false,bluetoothGattCallback,BluetoothDevice.TRANSPORT_LE)
                        dlfragmentlistener?.onDLFragmentSelDevice(devices[i],true)
                    }

                    else{
                        //gatt?.disconnect()
                        dlfragmentlistener?.onDLFragmentSelDevice(devices[i],false)

                    }

            }
            return ViewHolder(deviceView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val device=devices[position]
            val textView=holder.nameTextView
            textView.text=device.device.name
        }

        override fun getItemCount(): Int {
            return devices.size
        }
    }

    //****************************************************

    //Вызывается из  MainActivity
    fun deviceNameColorChange(status: Int){
        val color=when(status){

            GATT_CONNECTED -> Color.GREEN
            GATT_DISCONNECTED -> Color.GRAY

            else -> Color.BLACK}
        tv[pressedRVItem!!].findViewById<TextView>(R.id.devicename).setTextColor(color)
        if(status!= GATT_CONNECTED) pressedRVItem=null
    }

    fun hideScanBut(hide: Boolean){
        if(hide){
            tvScan.visibility=View.GONE
            btn.visibility=View.GONE
        }
        else{
            tvScan.visibility=View.VISIBLE
            btn.visibility=View.VISIBLE
        }
    }



}