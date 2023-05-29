package com.example.calcfromstatemachine

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    var sm = CalculatorStateMachine()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sm = CalculatorStateMachine()
        sm.start()
    }

    override fun onDestroy() {
        sm.destroy()

        super.onDestroy()
    }
}