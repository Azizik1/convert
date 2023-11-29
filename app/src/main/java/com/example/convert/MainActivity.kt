package com.example.convert
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.DecimalFormat
import android.util.Log
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private lateinit var fromCurrencySpinner: Spinner
    private lateinit var toCurrencySpinner: Spinner
    private lateinit var amountEditText: EditText

    private var fromCurrency: String = "USD"
    private var toCurrency: String = "EUR"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fromCurrencySpinner = findViewById(R.id.fromCurrencySpinner)
        toCurrencySpinner = findViewById(R.id.toCurrencySpinner)
        amountEditText = findViewById(R.id.amountEditText)

        val currencies = arrayOf("USD", "EUR", "JPY", "GBP", "AUD", "RUB", "KZT")

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        fromCurrencySpinner.adapter = adapter
        toCurrencySpinner.adapter = adapter

        fromCurrencySpinner.setSelection(adapter.getPosition(fromCurrency))
        toCurrencySpinner.setSelection(adapter.getPosition(toCurrency))

        fromCurrencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                fromCurrency = parent?.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        toCurrencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                toCurrency = parent?.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    fun convertCurrency(view: View) {
        val amount = amountEditText.text.toString().toDouble()

        val apiKey = "f1c64409981d4a30b0f83dd5e8a3e31c"
        val url = "https://open.er-api.com/v6/latest?base=$fromCurrency&symbols=$toCurrency&apikey=$apiKey"

        CurrencyConverterTask().execute(url, amount)

    }

    inner class CurrencyConverterTask : AsyncTask<Any, Void, Double>() {

        override fun doInBackground(vararg params: Any?): Double {
            try {
                val url = URL(params[0].toString())
                val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                val reader = BufferedReader(InputStreamReader(connection.inputStream))

                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }

                val jsonResponse = JSONObject(response.toString())
                val rates = jsonResponse.getJSONObject("rates")
                val conversionRate = rates.getDouble(toCurrency)

                return conversionRate * params[1] as Double
            } catch (e: Exception) {
                Log.e("CurrencyConverter", "Error converting currency", e)
                return 0.0
            }
        }

        override fun onPostExecute(result: Double?) {
            super.onPostExecute(result)
            if (result != null) {
                Log.d("CurrencyConverter", "Conversion result: $result")

                runOnUiThread {
                    val df = DecimalFormat("#.##")
                    val convertedAmount = df.format(result)
                    Toast.makeText(this@MainActivity, "Converted Amount: $convertedAmount", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.e("CurrencyConverter", "Conversion result is null")
            }
        }
    }}


