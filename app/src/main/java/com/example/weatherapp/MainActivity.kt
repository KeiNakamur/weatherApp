package com.example.weatherapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.weatherapp.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.text.SimpleDateFormat
import java.util.stream.Stream

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var placeLat = 36.313951
    private var placeLon = 139.813527

    private var format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //id.text = "結果"
        val apiKey = "3af4d639ff13708b1346e4a3d6bc6e8f"
        val mainUrl = "https://api.openweathermap.org/data/2.5/weather?lang=ja"

        binding.button.setOnClickListener(){
            //小山のURLを渡す
            val url = "$mainUrl&q=tokyo&appid=$apiKey"
            //urlを引数に渡してgetWeatherIndo関数で取得する
            getWeatherInfo(url)
        }
    }

    private fun getWeatherInfo(url: String) {
        //今回はlifecycleScope.launchでコルーチンを使用する
        //非同期処理の領域の用意
        lifecycleScope.launch {
            val result = weatherBackgroundTask(url)
            //上記の結果を受け取ってお天気データ(Jsonデータ)を表示する
            weatherJsonTask(result)
        }
    }

    //suspendは中断する可能性がある関数につける
    private suspend fun weatherBackgroundTask(url: String): String{
        //url変数(小山市のurlが入っている)を読み込む必要がある
        //読み込んだ結果を返す必要がある
        //まずDispatcher.IOでスレッドを分岐しますよってことを宣言？
        val response = withContext(Dispatchers.IO){
            //スレッドを分岐したところでお天気データを読み込む
            var httpResult = ""

            try{
                //ただの文字列をURLに変換
                val urlObj = URL(url)

                val br = BufferedReader(InputStreamReader(urlObj.openStream()))

                //読み込んだデータを文字列に変換してhttpResultに代入
                httpResult = br.readText()
            }catch(e: IOException){
                e.printStackTrace()
            }catch(e: JSONException){
                e.printStackTrace()
            }
            return@withContext httpResult
        }
        return response
    }

    //weatherBackgroundTaskでの値をUI上に表示する(Jsonデータの取り扱い)
    private fun weatherJsonTask(result: String){

        val temperature = findViewById<TextView>(R.id.temp)
        val cityName = findViewById<TextView>(R.id.cityName)
        val resultHumid = findViewById<TextView>(R.id.resultHumidity)
        val resultPressure = findViewById<TextView>(R.id.resultPressure)
        val resultWind = findViewById<TextView>(R.id.resultWind)
        //全体のJsonデータを取得
        val jsonObj = JSONObject(result)

        val city_name = jsonObj.getString("name")

        //最高気温、最低気温(１時間ごと)
        //湿度(%)、気圧、風圧？を取得したい
        val main = jsonObj.getJSONObject("main")


        //最高気温
        val max = main.getInt("temp_max")

        //最低気温
        val min = main.getInt("temp_min")

        //気温
        val temp = main.getInt("temp")

        //湿度
        val humidity = main.getInt("humidity")

        //気圧
        val pressure = main.getInt("pressure")

        //風
        val wind = jsonObj.getJSONObject("wind")
        val windSpeed = wind.getInt("speed")

        //UIへ反映
        cityName.text = city_name.toString()

        var tempInt = temp - 273
        temperature.text = "$tempInt℃"

        var humidPercentage = humidity.toShort()
        resultHumid.text = "${humidPercentage}%"

        var pressurePercentage = pressure.toString()
        resultPressure.text = "${pressurePercentage}hPa"

        var windspeed = windSpeed.toString()
        resultWind.text = "${windspeed}m/s"



    }

}