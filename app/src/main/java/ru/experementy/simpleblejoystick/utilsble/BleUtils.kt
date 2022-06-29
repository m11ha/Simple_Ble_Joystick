package ru.experementy.simpleblejoystick.utilsble

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import ru.experementy.simpleblejoystick.DevicesListFragment

class MyBleUtils {
    companion object{

        private const val REQUEST_ENABLE_BT=1
        const val GATT_CONNECTED=1
        const val GATT_DISCONNECTED=0
        const val GATT_CONNECT_ERROR=-1


        fun checkBleEnabled(activity: Activity): Boolean{
            return activity.run{
                packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
            } && BluetoothAdapter.getDefaultAdapter()!=null
        }

        fun checkPermissions(activity: Activity){
            val permissionCheck = ContextCompat.checkSelfPermission(activity.applicationContext, android.Manifest.permission.ACCESS_FINE_LOCATION)
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                   // Toast.makeText(activity.applicationContext, "The permission to get BLE location data is required", Toast.LENGTH_LONG).show()
                    requestPermissions(activity,arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
                }
            } /*else {
                Toast.makeText(activity.applicationContext, "Location permissions already granted", Toast.LENGTH_SHORT).show()
            }*/
        }


        fun requestEnablebt(activity: Activity,bAdapter: BluetoothAdapter?){
            if (bAdapter == null || !bAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                activity.startActivityForResult(enableBtIntent, DevicesListFragment.REQUEST_ENABLE_BT)
            }
        }




    }
}