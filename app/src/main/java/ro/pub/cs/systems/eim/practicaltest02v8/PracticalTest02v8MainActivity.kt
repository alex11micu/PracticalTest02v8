package ro.pub.cs.systems.eim.practicaltest02v8

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread
import android.content.Intent

class PracticalTest02v8MainActivity : AppCompatActivity() {

    // Cache pentru stocarea ratei și a timpului ultimei actualizări
    private var cache: MutableMap<String, Pair<String, Long>> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_practical_test02v8_main)

        val currencyInput: EditText = findViewById(R.id.currencyInput)
        val requestButton: Button = findViewById(R.id.requestButton)
        val resultText: TextView = findViewById(R.id.resultText)
        val navigateButton: Button = findViewById(R.id.navigateButton)

        requestButton.setOnClickListener {
            val currency = currencyInput.text.toString().uppercase()
            if (currency.isNotEmpty()) {
                checkCacheAndFetch(currency, resultText)
            } else {
                resultText.text = "Introduceți o valută validă!"
                Log.w("INPUT_VALIDATION", "Câmpul de valută este gol.")
            }
        }

        navigateButton.setOnClickListener {
            val intent = Intent(this, CalculatorActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkCacheAndFetch(currency: String, resultText: TextView) {
        val cachedData = cache[currency]
        if (cachedData != null) {
            val (rate, timestamp) = cachedData
            val currentTime = System.currentTimeMillis()

            // Verificăm dacă datele din cache sunt mai vechi de 1 minut
            if (currentTime - timestamp < 60000) {
                Log.d("CACHE_HIT", "Using cached data for $currency: $rate")
                resultText.text = "1 Bitcoin = $rate $currency (din cache)"
                return
            } else {
                Log.d("CACHE_EXPIRED", "Cache expired for $currency. Fetching new data...")
            }
        } else {
            Log.d("CACHE_MISS", "No cached data for $currency. Fetching new data...")
        }

        // Dacă datele nu sunt în cache sau sunt expirate, facem cererea
        fetchCurrencyRate(currency, resultText)
    }

    private fun fetchCurrencyRate(currency: String, resultText: TextView) {
        thread {
            try {
                val apiUrl = "https://api.coindesk.com/v1/bpi/currentprice/$currency.json"
                val url = URL(apiUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d("FETCH_WEBSERVICE", "Response received: $response")

                    val jsonResponse = JSONObject(response)
                    val rate = jsonResponse.getJSONObject("bpi")
                        .getJSONObject(currency)
                        .getString("rate")

                    // Salvăm datele în cache
                    val currentTime = System.currentTimeMillis()
                    cache[currency] = Pair(rate, currentTime)
                    Log.d("CACHE_SAVE", "Data cached for $currency: $rate at $currentTime")

                    runOnUiThread {
                        resultText.text = "1 Bitcoin = $rate $currency"
                        Log.d("UI_UPDATE", "Displayed rate: $rate for $currency")
                    }
                } else {
                    Log.e("FETCH_WEBSERVICE", "Failed to fetch data. Response code: ${connection.responseCode}")
                    runOnUiThread {
                        resultText.text = "Eroare la obținerea datelor (Cod răspuns: ${connection.responseCode})"
                    }
                }
            } catch (e: Exception) {
                Log.e("FETCH_EXCEPTION", "Exception occurred: ${e.message}")
                runOnUiThread {
                    resultText.text = "Eroare la obținerea datelor: ${e.message}"
                }
            }
        }
    }
}
