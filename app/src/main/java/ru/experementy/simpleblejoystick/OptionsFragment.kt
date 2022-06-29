package ru.experementy.simpleblejoystick

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ru.experementy.customview.R


class OptionsFragment : Fragment() {



    private var myViewModel: MyViewModel?=null

    // Сохраняемые настройки

    private var udbtns: String?=null
    private var jOne: String?=null
    private var jTwo: String?=null

    //Joystick flip переключатель
    private lateinit var flipSwitch: SwitchCompat
    //UUID характеристики
    private lateinit var editUiSwitch: CheckBox
    private lateinit var etUid: EditText
    //UUID сервиса
    private lateinit var editUiServSvitch: CheckBox
    private lateinit var etUiServ: EditText


    //Аналоговый джойстик данные
    private lateinit var joystOneInputs: MutableList<EditText>

    //Дискретный джойстик данные
    private lateinit var joystTwoInputs: MutableList<EditText>

    // Пользовательские кнопки данные
    private lateinit var udBtnNamesInput: MutableList<EditText>
    private lateinit var udBtnNames: MutableList<TextView>



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view=inflater.inflate(R.layout.fragment_options, container, false)

        //ViewModel
        myViewModel= ViewModelProvider(requireActivity()).get(MyViewModel::class.java)

        //Меню********************************
        //Прячем пункты меню перехода к списку устройств и к настройкам
        //Показываем назад
        myViewModel?.apply{
            maynToolbar?.menu?.findItem(R.id.bleDevices)?.isVisible=false
            maynToolbar?.menu?.findItem(R.id.settings)?.isVisible=false
            maynToolbar?.menu?.findItem(R.id.back)?.isVisible=true
            maynToolbar?.menu?.findItem(R.id.save)?.isVisible=true
            maynToolbar?.menu?.findItem(R.id.restore)?.isVisible=true
            maynToolbar?.menu?.findItem(R.id.help)?.isVisible=false

            currentFragment="settings" // текущий фрагмент
        }
        //Отзеркалить джойстики
        flipSwitch=view.findViewById(R.id.switch_flip)
        flipSwitch.isChecked=myViewModel!!.mirror
        flipSwitch.setOnCheckedChangeListener{
            _, isChecked: Boolean ->
            myViewModel?.mirror=isChecked
        }
        //UUID характеристики
        editUiSwitch=view.findViewById(R.id.edit_ui_switch)
        etUid=view.findViewById(R.id.et_uid) as EditText

        etUid.setText(myViewModel?.charactUUID)

        editUiSwitch.setOnCheckedChangeListener{
            _,isChecked: Boolean->
            etUid.isEnabled=isChecked
            if(!isChecked){
                var u=etUid.text.toString()
                if(u.length<4){
                    val pre=Array(4-u.length) { '0' }.joinToString (separator = "")
                    u="$pre$u"
                    etUid.setText(u)
                }
                myViewModel?.charactUUID=etUid.text.toString()
            }
        }
        etUid.imeOptions= IME_ACTION_DONE
        etUid.setOnEditorActionListener { v, actionId, event ->
            v as EditText
            var u=etUid.text.toString()

            if(actionId== IME_ACTION_DONE||event.keyCode==66){
                if(u.length<4){
                    val pre=Array(4-u.length) { '0' }.joinToString (separator = "")
                    u="$pre$u"
                    v.setText(u)

                }
                myViewModel?.charactUUID=v.text.toString()
            }
            false
        }

        //UUID сервиса
        editUiServSvitch=view.findViewById(R.id.edit_ui_serv_switch)
        etUiServ=view.findViewById(R.id.et_serv_uid) as EditText

        etUiServ.setText(myViewModel?.servUUID)

        editUiServSvitch.setOnCheckedChangeListener{
                _,isChecked: Boolean->
            etUiServ.isEnabled=isChecked
            if(!isChecked){
                var u=etUiServ.text.toString()
                if(u.length<4){
                    val pre=Array(4-u.length) { '0' }.joinToString (separator = "")
                    u="$pre$u"
                    etUiServ.setText(u)
                }
                myViewModel?.servUUID=etUiServ.text.toString()
            }
        }
        etUiServ.imeOptions= IME_ACTION_DONE
        etUiServ.setOnEditorActionListener { v, actionId, event ->
            v as EditText
            var u=etUiServ.text.toString()

            if(actionId== IME_ACTION_DONE||event.keyCode==66){
                if(u.length<4){
                    val pre=Array(4-u.length) { '0' }.joinToString (separator = "")
                    u="$pre$u"
                    v.setText(u)

                }
                myViewModel?.servUUID=v.text.toString()
            }
            false
        }



        //***********************************************************
        //Аналоговый джойстик режим команд
        joystOneInputs= mutableListOf()
        for(i in 1..8){
            val inputId=resources.getIdentifier("etJOne$i","id",context?.packageName)
            joystOneInputs.add(view.findViewById(inputId) as EditText)
            joystOneInputs[i-1].setText(myViewModel?.getJoystOneValue(i-1))

            joystOneInputs[i-1].imeOptions= IME_ACTION_DONE

            joystOneInputs[i-1].setOnEditorActionListener{
                    v, actionId, _ ->if(actionId== IME_ACTION_DONE) {
                val ind=joystOneInputs.indexOf(v)
                v as EditText
                myViewModel?.setJoystOneValue(ind,v.text.toString())
                true
            }
            else false
            }

        }

        //Дискретный джойстик
        joystTwoInputs= mutableListOf()
        for(i in 1..4){
            val inputId=resources.getIdentifier("etJTwo$i","id",context?.packageName)
            joystTwoInputs.add(view.findViewById(inputId) as EditText)
            joystTwoInputs[i-1].setText(myViewModel?.getJoystTwoValue(i-1))

            joystTwoInputs[i-1].imeOptions= IME_ACTION_DONE
            joystTwoInputs[i-1].setOnEditorActionListener{
                    v, actionId, _ ->if(actionId== IME_ACTION_DONE) {
                val ind=joystTwoInputs.indexOf(v)
                v as EditText
                myViewModel?.setJoystTwoValue(ind,v.text.toString())
                true
            }
            else false
            }

        }


        //***********************************************************
        //инициализация пользовательских кнопок
        udBtnNamesInput= mutableListOf()
        udBtnNames= mutableListOf()
        for(i in 1..9){
            val inputNameId=resources.getIdentifier("tebut$i","id",context?.packageName)
            val nameId=resources.getIdentifier("tvbut$i","id",context?.packageName)
            udBtnNamesInput.add(view.findViewById(inputNameId) as EditText)
            udBtnNames.add(view.findViewById(nameId) as TextView)
            udBtnNamesInput[i-1].setText(myViewModel?.getBtnText(i-1))
            udBtnNames[i-1].text=myViewModel?.getBtnText(i-1)

            udBtnNamesInput[i-1].imeOptions= IME_ACTION_DONE

            udBtnNamesInput[i-1].setOnEditorActionListener{
                    v, actionId, _ ->if(actionId== IME_ACTION_DONE) {
                    val ind=udBtnNamesInput.indexOf(v)
                    v as EditText
                    udBtnNames[ind].text=v.text
                    myViewModel?.setBtnText(ind,v.text.toString())
                    true
                }
                else false
            }

            udBtnNamesInput[i-1].onFocusChangeListener= View.OnFocusChangeListener{
                v,hf-> if(!hf){
                    val ind=udBtnNamesInput.indexOf(v)
                    v as EditText
                    udBtnNames[ind].text=v.text
                    myViewModel?.setBtnText(ind,v.text.toString())
                }
            }
        }




        //*******************************************************

        return view
    }

        //*********************************************************



        fun saveData(){
        udbtns= Array(9){ i -> myViewModel?.getBtnText(i)!! }.joinToString(",")
        jOne=Array(8){i->myViewModel?.getJoystOneValue(i)!!}.joinToString ( "," )
        jTwo=Array(4){i->myViewModel?.getJoystTwoValue(i)!!}.joinToString ( "," )
        val spref:SharedPreferences=requireActivity().getPreferences(Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor=spref.edit()
        editor.putString(U_BTNS,udbtns)
        editor.putString(J_ONE,jOne)
        editor.putString(J_TWO,jTwo)
        editor.putBoolean(FLIP,myViewModel!!.mirror) //Расположение джойстиков
        editor.putString(UUID,etUid.text.toString()) // UUID характеристики
        editor.putString(SERVUUID,etUiServ.text.toString())//UUID сервиса
        editor.apply()
    }

    //Cброс сохраненных настроек
        fun resData(){
        val spref: SharedPreferences=requireActivity().getPreferences(Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor=spref.edit()
        editor.clear()
        editor.apply()
        myViewModel?.initArrays(uBtn = "1,2,3,4,5,6,7,8,9",jOne = "a,b,c,d,k,l,m,n",jTwo="A,B,C,D")
        for (i in 1 .. 9){
            udBtnNames[i-1].text="$i"
            udBtnNamesInput[i-1].setText("$i")
        }
        val arrJ1=arrayOf("a","b","c","d","k","l","m","n")
        for(i in 0 until arrJ1.size) joystOneInputs[i].setText(arrJ1[i])
        val arrJ2=arrayOf("A","B","C","D")
        for(i in 0 until arrJ2.size) joystTwoInputs[i].setText(arrJ2[i])
        //Расположение джойстиков по умолчанию
        myViewModel?.mirror=false
        flipSwitch.isChecked=false
        //UUID характеристики по умолчанию
        myViewModel?.charactUUID=myViewModel?.defCharactUUID
        etUid.setText(myViewModel?.defCharactUUID)
        myViewModel?.servUUID=myViewModel?.defServUUID
        etUiServ.setText(myViewModel?.defServUUID)

    }

    companion object {

        const val U_BTNS="uBtns"
        const val J_ONE="jOne"
        const val J_TWO="jTwo"
        const val FLIP="flip"
        const val UUID="uuid"
        const val SERVUUID="servuuid"
    }
}