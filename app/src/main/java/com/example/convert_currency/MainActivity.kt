package com.example.convert_currency

import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity

import org.json.JSONObject
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL



class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    // Hàm xử lý khi người dùng nhấn nút "Convert"
    fun convert(view: View) {
        val editTextAmount = findViewById<EditText>(R.id.editTextAmount)
        val amount = editTextAmount.text.toString().toDoubleOrNull()

        if (amount == null) {
            Toast.makeText(this, "Vui lòng nhập số tiền hợp lệ", Toast.LENGTH_SHORT).show()
            return
        }

        val downloadData = Download()
        val url = "https://data.fixer.io/api/latest?access_key=14f224058a577a910b3500964ca0e576"
        downloadData.execute(url, amount.toString())
    }

    // Lớp tải dữ liệu từ API
    inner class Download : AsyncTask<String, Void, Pair<String, Double>>() {

        override fun doInBackground(vararg params: String?): Pair<String, Double> {
            var result = ""
            val amountEUR = params[1]?.toDoubleOrNull() ?: 0.0
            try {
                val url = URL(params[0])
                val httpURLConnection = url.openConnection() as HttpURLConnection
                val inputStream = httpURLConnection.inputStream
                val inputStreamReader = InputStreamReader(inputStream)
                var data: Int = inputStreamReader.read()

                while (data != -1) {
                    val character = data.toChar()
                    result += character
                    data = inputStreamReader.read()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            // Trả về kết quả cùng với số tiền cần chuyển đổi
            return Pair(result, amountEUR)
        }

        override fun onPostExecute(result: Pair<String, Double>) {
            super.onPostExecute(result)

            // Lấy JSON và số tiền EUR từ kết quả trả về
            val jsonObjectString = result.first
            val amountEUR = result.second

            try {
                val jsonObject = JSONObject(jsonObjectString)
                val rates = jsonObject.getJSONObject("rates")

                // Lấy tỷ giá từ EUR sang VND, GBP và EUR
                val vndRate = rates.getDouble("VND")
                val gbpRate = rates.getDouble("GBP")
                val usdRate = rates.getDouble("USD")

                // Tính số tiền đã đổi
                val vndAmount = amountEUR * vndRate
                val gbpAmount = amountEUR * gbpRate
                val usdAmount = amountEUR * usdRate

                // Hiển thị kết quả trong các TextView
                findViewById<TextView>(R.id.textVND).text = "VND: %.3f".format(vndAmount)
                findViewById<TextView>(R.id.textGBP).text = "GBP: %.3f".format(gbpAmount)
                findViewById<TextView>(R.id.textUSD).text = "USD: %.3f".format(usdAmount)

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(applicationContext, "Lỗi khi lấy dữ liệu từ API", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

