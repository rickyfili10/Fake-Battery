package com.example.myapplication

import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.example.myapplication.databinding.ActivityMainBinding
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.fab.setOnClickListener { view ->
            showBatteryLevelDialog(view)
        }
    }

    private fun showBatteryLevelDialog(view: android.view.View) {
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER

        AlertDialog.Builder(this)
            .setTitle("Set Battery Level")
            .setMessage("Enter the battery level to set (0-100):")
            .setView(input)
            .setPositiveButton("OK") { dialog, which ->
                val batteryLevel = input.text.toString()
                if (batteryLevel.isNotEmpty() && batteryLevel.toIntOrNull() != null) {
                    executeShell(batteryLevel.toInt())
                    Snackbar.make(view, "Battery level set to $batteryLevel", Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .setAnchorView(binding.fab).show()
                } else {
                    Snackbar.make(view, "Invalid input", Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .setAnchorView(binding.fab).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.cancel()
            }
            .show()
    }

    private fun executeShell(batteryLevel: Int) {
        val suCommand = "su"
        val dumpsysCommand = "dumpsys battery set level $batteryLevel"

        try {
            val suProcess = Runtime.getRuntime().exec(suCommand)
            suProcess.outputStream.use { outputStream ->
                outputStream.write("$dumpsysCommand\n".toByteArray())
                outputStream.flush()
            }

            val reader = BufferedReader(InputStreamReader(suProcess.inputStream))
            val output = StringBuilder()

            reader.forEachLine {
                output.append(it).append("\n")
            }

            suProcess.waitFor()

            println("Command output: $output")
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error executing command: ${e.message}")
        }
    }
}
