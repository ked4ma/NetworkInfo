package com.github.ked4ama.android.networkinfo.app

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.ked4ama.android.networkinfo.R
import com.github.ked4ama.android.networkinfo.app.data.NetworkInfo
import com.github.ked4ama.android.networkinfo.app.vm.MainViewModel
import com.github.ked4ama.android.networkinfo.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.net.NetworkInterface

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            && !viewModel.checkPermission(this)
        ) {
            return
        }

        lifecycleScope.launchWhenResumed {
            viewModel.networkState.collect {
                bind(it)
            }
        }

        init()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        println(requestCode)
        println(permissions)
        println(grantResults)
    }

    private fun init() {
        viewModel.init()
    }

    private fun bind(info: NetworkInfo) {
        binding.networkName.text = info.name
        binding.networkIp.text = info.ip
    }
}