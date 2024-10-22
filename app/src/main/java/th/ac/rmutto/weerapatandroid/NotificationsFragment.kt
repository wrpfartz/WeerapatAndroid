package th.ac.rmutto.weerapatandroid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.squareup.picasso.Picasso
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException

class NotificationsFragment : Fragment() {

    var imgViewFile: ImageView? = null
    var edittxtUsername: TextView? = null
    var edittxtFirstName: TextView? = null
    var edittxtLastName: TextView? = null


    var file: File? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_notifications, container, false)

        //For an synchronous task
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val sharedPrefer = requireContext().getSharedPreferences(
            "appPrefer", Context.MODE_PRIVATE)
        val custID = sharedPrefer?.getString("custIDPref", null)


        imgViewFile = root.findViewById(R.id.imgViewFile)
        edittxtUsername = root.findViewById(R.id.edittxtUsername)
        edittxtFirstName = root.findViewById(R.id.edittxtFirstName)
        edittxtLastName = root.findViewById(R.id.edittxtLastName)


        viewUser(custID!!)
        return root

    }

    fun viewUser(custID: String)
    {
        val url: String = getString(R.string.root_url) + getString(R.string.profile_url) + custID
        Log.d("tag", url)
        val okHttpClient = OkHttpClient()
        val request: Request = Request.Builder()
            .url(url) // Assuming it's a Bearer token
            .get()
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (response.isSuccessful) {
            val data = JSONObject(response.body!!.string())
            if (data.length() > 0) {
                Log.d("tag", "x3")
                var username = data.getString("username")
                var firstName = data.getString("firstName")
                var lastName = data.getString("lastName")
                val imageFile = data.getString("imageFile")

                if (!imageFile.equals("null") && !imageFile.equals("")) {//รูปมาโชว
                    val image_url = getString(R.string.root_url) +
                            getString(R.string.customer_image_url) + imageFile
                    Picasso.get().load(image_url).into(imgViewFile)

                    Log.d("tag", image_url)
                }
                if (firstName.equals("null")) firstName = "-"
                edittxtFirstName?.text = firstName

                if (lastName.equals("null")) lastName = "-"
                edittxtLastName?.text = lastName

                if (username.equals("null")) username = "-"
                edittxtUsername?.text = username

            }
        }
    }
}

