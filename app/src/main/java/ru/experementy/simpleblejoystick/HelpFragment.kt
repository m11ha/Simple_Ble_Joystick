package ru.experementy.simpleblejoystick

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ru.experementy.customview.R


class HelpFragment : Fragment() {
    var myViewModel: MyViewModel?=null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val v= inflater.inflate(R.layout.fragment_helpk, container, false)
        val help=v.findViewById<WebView>(R.id.help)
        try {
            help.loadUrl(getString(R.string.help_url))
        }catch (err :Exception){
            Log.e("Eggog",err.toString())
        }
        //Разрешить увеличение
        help.settings.builtInZoomControls=true

        
        myViewModel=ViewModelProvider(requireActivity()).get(MyViewModel::class.java)
        myViewModel?.apply{
            maynToolbar?.menu?.findItem(R.id.bleDevices)?.isVisible=false
            maynToolbar?.menu?.findItem(R.id.settings)?.isVisible=false
            maynToolbar?.menu?.findItem(R.id.back)?.isVisible=true
            maynToolbar?.menu?.findItem(R.id.save)?.isVisible=false
            maynToolbar?.menu?.findItem(R.id.restore)?.isVisible=false
            maynToolbar?.menu?.findItem(R.id.help)?.isVisible=false

            currentFragment="help" // текущий фрагмент

        }
        return v
    }

}