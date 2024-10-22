package th.ac.rmutto.weerapatandroid

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.squareup.picasso.Picasso
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CarActivity : AppCompatActivity() {
    var imageViewFile: ImageView? = null
    var car_brand: TextView? = null
    var car_seats: TextView? = null
    var car_model: TextView? = null
    var car_transmission: TextView? = null
    var car_year: TextView? = null
    var car_price: TextView? = null
    var totalprice: TextView? = null
    var starttime: EditText? = null
    var endtime: EditText? = null
    var startdate: EditText? = null
    var enddate: EditText? = null
    val myCalendar: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car)


        //For an synchronous task
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val sharedPrefer = getSharedPreferences(
            "appPrefer", Context.MODE_PRIVATE)
        val custID = sharedPrefer?.getString("custIDPref", null)

        //Receive variables from caller
        val idcar = intent.extras!!.getString("idcar")

        imageViewFile = findViewById(R.id.imageViewFile)
        car_brand = findViewById(R.id.car_brand)
        car_seats = findViewById(R.id.car_seats)
        car_model = findViewById(R.id.car_model)
        car_year = findViewById(R.id.car_year)
        car_transmission = findViewById(R.id.car_transmission)
        car_price = findViewById(R.id.car_price)
        totalprice = findViewById(R.id.totalprice)
        starttime = findViewById(R.id.starttime)
        endtime = findViewById(R.id.endtime)
        startdate = findViewById(R.id.startdate)
        enddate = findViewById(R.id.enddate)

        val rent_button: Button = findViewById(R.id.rent_button)

        // ตั้งค่าการเลือกวัน
        val dateSetListenerStart = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, day)
            updateLabel(startdate!!)
        }

        val dateSetListenerEnd = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, day)
            updateLabel(enddate!!)
        }

        startdate!!.setOnClickListener {
            DatePickerDialog(
                this, dateSetListenerStart, myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
            calculateTotalPrice() // เรียกใช้ฟังก์ชันหลังจากเลือกวัน
        }

        enddate!!.setOnClickListener {
            DatePickerDialog(
                this, dateSetListenerEnd, myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
            calculateTotalPrice() // เรียกใช้ฟังก์ชันหลังจากเลือกวัน
        }


        // Select start and end time
        starttime!!.setOnClickListener {
            getTime(starttime!!, this)
        }

        endtime!!.setOnClickListener {
            getTime(endtime!!, this)
        }

        showViewcar(idcar)
        rent_button.setOnClickListener {
            calculateTotalPrice() // คำนวณยอดรวมก่อนทำการเช่า
            addrent(custID!!, idcar!!, car_price?.text.toString())
            val refresh = Intent(this, MainActivity::class.java)
            startActivity(refresh)
            finish()
        }


    }

    private fun addrent(custID: String, idcar: String, price: String){
        val url = getString(R.string.root_url) + getString(R.string.rentcargo)

        val okHttpClient = OkHttpClient()
        val formBody: RequestBody = FormBody.Builder()
            .add("idcar",idcar)
            .add("price",price)
            .add("custID",custID)
            .add("sdate",startdate?.text.toString())
            .add("edate",enddate?.text.toString())
            .add("stime",starttime?.text.toString())
            .add("etime",endtime?.text.toString())
            .build()
        val request: Request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()
        val response = okHttpClient.newCall(request).execute()

        if(response.isSuccessful){
            val obj = JSONObject(response.body!!.string())
            val message = obj["message"].toString()
            val status = obj["status"].toString()

            if (status == "true") {
                Toast.makeText(this, "คุณได้ทำการเช่ารถเรียบร้อยแล้ว", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }
    }




    // ฟังก์ชันสำหรับแสดงข้อมูลรถยนต์
    private fun showViewcar(idcar: String?) {
        val url: String = getString(R.string.root_url) + getString(R.string.viewcar) + idcar

        val okHttpClient = OkHttpClient()
        val request: Request = Request.Builder()
            .url(url)
            .get()
            .build()
        val response = okHttpClient.newCall(request).execute()

        if (response.isSuccessful) {
            val data = JSONObject(response.body!!.string())
            if (data.length() > 0) {
                val urlImage = getString(R.string.root_url) +
                        getString(R.string.car_image_url) + data.getString("cpic")

                // โหลดภาพของรถยนต์
                Picasso.get().load(urlImage).into(imageViewFile)
                car_brand?.text = data.getString("cname")
                car_year?.text = data.getString("cyear")
                car_seats?.text = data.getString("cseats")
                car_model?.text = data.getString("cbrand")
                car_transmission?.text = data.getString("cgear")
                car_price?.text = data.getString("cprice")
            }
        }
    }

    // ฟังก์ชันสำหรับเลือกเวลา
    private fun getTime(textView: TextView, context: Context) {
        val cal = Calendar.getInstance()

        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            textView.text = SimpleDateFormat("HH:mm", Locale.US).format(cal.time)
        }

        TimePickerDialog(context, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
    }

    private fun updateLabel(textView: TextView) {
        val myFormat = "yyyy-MM-dd" // format
        val dateFormat = SimpleDateFormat(myFormat, Locale.US)
        textView.text = dateFormat.format(myCalendar.time)
    }

    private fun calculateTotalPrice() {
        // Format to parse the selected date
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        // Parse start and end date
        val startDateStr = startdate!!.text.toString()
        val endDateStr = enddate!!.text.toString()

        // ตรวจสอบค่าที่ถูกต้องจาก startdate และ enddate
        Log.d("StartDate", startDateStr)
        Log.d("EndDate", endDateStr)

        try {
            val startDate: Date = sdf.parse(startDateStr)!!
            val endDate: Date = sdf.parse(endDateStr)!!

            // Calculate the difference in milliseconds and convert to days
            val diffInMillis = endDate.time - startDate.time
            val diffInDays = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()

            // Get car price and calculate total
            val pricePerDay = car_price!!.text.toString().toInt()
            val total = (diffInDays + 1) * pricePerDay // Adding 1 to include the starting day

            // ตรวจสอบค่าที่ถูกคำนวณ
            Log.d("TotalPrice", total.toString())

            // Show total price
            totalprice!!.text = "Total Price: $total"
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("CalculateError", e.message.toString())
        }
    }
}