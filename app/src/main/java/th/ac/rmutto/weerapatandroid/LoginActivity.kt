package th.ac.rmutto.weerapatandroid

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.StrictMode
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //For an synchronous task
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)


        var IDuser = findViewById<EditText>(R.id.IDuser)
        var IDpass = findViewById<EditText>(R.id.IDpass)
        var Blogin = findViewById<Button>(R.id.Blogin)


        Blogin.setOnClickListener {
            var username = IDuser.text.toString()
            var password = IDpass.text.toString()

//            if (username == "admin" && password == "1234") {
//                val intent = Intent(this, MainActivity::class.java)
//                startActivity(intent)
//            }

            if(username.isEmpty()) {
                IDuser.error = "กรุณากรอก username"
                return@setOnClickListener
            }

            if(password.isEmpty()) {
                IDpass.error = "กรุณากรอก password"
                return@setOnClickListener
            }

            val url = getString(R.string.root_url) + getString(R.string.login_url)

            val okHttpClient = OkHttpClient()
            val formBody: RequestBody = FormBody.Builder()
                .add("username",IDuser.text.toString())
                .add("password",IDpass.text.toString())
                .build()
            val request: Request = Request.Builder()
                .url(url)
                .post(formBody)
                .build()

            val response = okHttpClient.newCall(request).execute()

            if(response.isSuccessful){
                val obj = JSONObject(response.body!!.string())
                val status = obj["status"].toString()

                if (status == "true") {

                    //Create shared preference to store user data
                    val sharedPrefer: SharedPreferences =
                        getSharedPreferences("appPrefer", Context.MODE_PRIVATE)
                    val editor: SharedPreferences.Editor = sharedPrefer.edit()

                    //Get customer data
                    val custID = obj["custID"].toString()
                    val username = obj["username"].toString()

                    editor.putString("custIDPref", custID)
                    editor.putString("usernamePref", username)
                    editor.apply()


                    //redirect to main page
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val message = obj["message"].toString()
                    Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                }

            }else{
                Toast.makeText(applicationContext, "ไม่สามารถเชื่อต่อกับเซิร์ฟเวอร์ได้", Toast.LENGTH_LONG).show()
            }
        }
    }
}