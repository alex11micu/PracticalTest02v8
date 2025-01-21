package ro.pub.cs.systems.eim.practicaltest02v8

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class CalculatorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator)

        val number1: EditText = findViewById(R.id.number1)
        val number2: EditText = findViewById(R.id.number2)
        val calculateButton: Button = findViewById(R.id.calculateButton)
        val resultText: TextView = findViewById(R.id.resultText)

        calculateButton.setOnClickListener {
            val num1 = number1.text.toString()
            val num2 = number2.text.toString()

            if (num1.isNotEmpty() && num2.isNotEmpty()) {
                val operation = "plus" // Poți schimba în "minus", "mul" sau "div"
                val serverIp = "172.25.7.36"
                val url = "http://$serverIp:8080/expr/expr_get.py?operation=$operation&t1=$num1&t2=$num2"

                fetchCalculationResult(url, resultText)
            } else {
                resultText.text = "Introduceți ambele numere!"
            }
        }
    }

    private fun fetchCalculationResult(url: String, resultText: TextView) {
        thread {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d("HTTP_RESPONSE", "Response received: $response")

                    runOnUiThread {
                        resultText.text = "Rezultatul: $response"
                    }
                } else {
                    Log.e("HTTP_ERROR", "Failed to fetch data. Response code: ${connection.responseCode}")
                    runOnUiThread {
                        resultText.text = "Eroare la calcul (cod răspuns: ${connection.responseCode})"
                    }
                }
            } catch (e: Exception) {
                Log.e("HTTP_EXCEPTION", "Exception occurred: ${e.message}")
                runOnUiThread {
                    resultText.text = "Eroare: ${e.message}"
                }
            }
        }
    }
}
