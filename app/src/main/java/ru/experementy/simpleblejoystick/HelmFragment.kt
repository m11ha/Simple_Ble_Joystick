package ru.experementy.simpleblejoystick

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Guideline
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ru.experementy.customview.R
import ru.experementy.simpleblejoystick.joystick.analogjoystick.AnalogJoystick
import ru.experementy.simpleblejoystick.joystick.digitaljoystick.DigitalJoystick
import java.util.*


class HelmFragment : Fragment(),View.OnTouchListener{
    var myViewModel: MyViewModel?=null
    //Настройка взаимодейсрвия с activity*************************

    private var gatt: BluetoothGatt?=null

    private var characteristic: BluetoothGattCharacteristic?=null

    //Информация о соединении


    //Пользовательские кнопки

    private lateinit var udButtons: MutableList<Button>

    //Поток для пользовательских кнопок
    private var mTread: Thread?=null


    private lateinit var aJSignalTv: TextView
    private lateinit var dJSignalTv: TextView
    private lateinit var btnSignalTv: TextView



    override fun onResume() {
        super.onResume()
        //******************Получение Blurtooth Gatt характеристики ********************************

        if(myViewModel!=null){

            gatt=myViewModel?.myGatt

            if(gatt!=null){

                characteristic=null

                characteristic=gatt?.getService(UUID.fromString("0000${myViewModel?.servUUID}-0000-1000-8000-00805f9b34fb"))
                    ?.getCharacteristic(UUID.fromString("0000${myViewModel?.charactUUID}-0000-1000-8000-00805f9b34fb"))
                if(characteristic==null)gatt?.disconnect()
            }


        }
        //****************************************************************************************
    }

    @SuppressLint("RestrictedApi", "ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view=inflater.inflate(R.layout.fragment_helm, container, false)
        val aJoystick=view.findViewById<AnalogJoystick>(R.id.analogJoystick)
        val dJoystick=view.findViewById<DigitalJoystick>(R.id.digitaljoystick)
        //aJoystick.visibility=View.GONE
        //*** Монитор джойстиков и кнопок

        aJSignalTv=view.findViewById(R.id.displAJSignal)
        dJSignalTv=view.findViewById(R.id.dispDJSignal)
        btnSignalTv=view.findViewById(R.id.disButSignal)

        myViewModel= ViewModelProvider(requireActivity()).get(MyViewModel::class.java)





        //Меню***************************************************

        //Показываем пункт меню перехода к списку устройств и настроек
        //Прячем назад
        myViewModel?.apply{
            maynToolbar?.menu?.findItem(R.id.bleDevices)?.isVisible=true
            maynToolbar?.menu?.findItem(R.id.settings)?.isVisible=true
            maynToolbar?.menu?.findItem(R.id.back)?.isVisible=false
            maynToolbar?.menu?.findItem(R.id.save)?.isVisible=false
            maynToolbar?.menu?.findItem(R.id.restore)?.isVisible=false
            maynToolbar?.menu?.findItem(R.id.help)?.isVisible=true

            currentFragment="helm" // текущий фрагмент

        }
        //Отзеркалить джойстики********************************
        if(myViewModel?.mirror==true){
            val gline3=view.findViewById<Guideline>(R.id.guideline3)
            val glene4=view.findViewById<Guideline>(R.id.guideline4)
            var params=aJoystick.layoutParams as ConstraintLayout.LayoutParams
            params.startToStart=glene4.id
            params.endToEnd=view.id
            params=dJoystick.layoutParams as ConstraintLayout.LayoutParams
            params.startToStart=view.id
            params.endToEnd=gline3.id

            params=aJSignalTv.layoutParams as ConstraintLayout.LayoutParams
            params.startToStart=glene4.id
            params.endToEnd=view.id
            params=dJSignalTv.layoutParams as ConstraintLayout.LayoutParams
            params.startToStart=view.id
            params.endToEnd=gline3.id
        }

        //***************************************
        // Инициализация пользовательских кнопок
        udButtons= mutableListOf()

       for (i in 1 .. 9){
            val elemId=resources.getIdentifier("udbut$i","id",context?.packageName)
            udButtons.add(view.findViewById(elemId))
            udButtons[i-1].text=myViewModel?.getBtnText(i-1)

           udButtons[i-1].setOnTouchListener(this)
        }
        //*************************************************





   //************обработчики событий джойстиков***************************************
        aJoystick.setJoystickListener(object: AnalogJoystick.JoystickListener{
            @SuppressLint("SetTextI18n")
            override fun onPush(angle: Int, srength: Int) {

                    val fAnglle=angle.toFloat()

                    val kommand=if(fAnglle>67.5&&fAnglle<112.5) myViewModel?.getJoystOneValue(0)
                    else if(fAnglle>157.5&&fAnglle<202.5) myViewModel?.getJoystOneValue(3)
                    else if(fAnglle>247.5&&fAnglle<295.5) myViewModel?.getJoystOneValue(2)
                    else if(fAnglle>337.5||fAnglle<22.5) myViewModel?.getJoystOneValue(1)

                    else if(fAnglle>22.5&&fAnglle<67.5) myViewModel?.getJoystOneValue(4)
                    else if(fAnglle>295.5&&fAnglle<337.5) myViewModel?.getJoystOneValue(5)
                    else if(fAnglle>202.5&&fAnglle<247.5) myViewModel?.getJoystOneValue(6)
                    else if(fAnglle>112.5&&fAnglle<157.5) myViewModel?.getJoystOneValue(7)
                    else "0"
                    if(srength<20){
                        sendSymbol("0")
                        aJSignalTv.text="0"
                    }
                    else if(kommand!=null) {
                        sendSymbol(kommand)
                        aJSignalTv.text=kommand
                    }


            }
        })

        dJoystick.setJoystickListener((object: DigitalJoystick.JoystickListener{
            override fun onPress(pressedButton: Int) {

                val value: String? = when(pressedButton){

                        1-> myViewModel?.getJoystTwoValue(0)
                        2-> myViewModel?.getJoystTwoValue(3)
                        3-> myViewModel?.getJoystTwoValue(2)
                        4-> myViewModel?.getJoystTwoValue(1)
                        else->  "0"
                    }
                    if(value!=null){
                        sendSymbol(value)
                        dJSignalTv.text=value
                    }

            }
        }))




        //*******************************************
        return view
    }






    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        v as Button

        when (event?.action) {
            MotionEvent.ACTION_UP->{
                btnSignalTv.text="0"
                v.isPressed=false
                mTread?.interrupt()
                for(i in 1..10)sendSymbol("0")
            }
            MotionEvent.ACTION_DOWN -> {
                btnSignalTv.text=v.text
                v.isPressed=true               
                if(mTread?.isAlive==true&& mTread!=null){
                    mTread?.interrupt()
                }
                mTread=Thread {
                    while (!Thread.interrupted()) {

                        sendSymbol(v.text.toString())
                        try {
                            Thread.sleep(20)
                        } catch (e: InterruptedException) {
                            break
                        }

                    }
                }
                mTread?.start()

            }
        }
        return true
    }



    private fun sendSymbol(value: String){
        val b=value.toByteArray()
        send(b)
    }
    private fun send(b: ByteArray){
        if(characteristic!=null){
            characteristic?.writeType=BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE//WRITE_TYPE_DEFAULT
            characteristic?.value= b
            gatt?.writeCharacteristic(characteristic)
        }
    }

}