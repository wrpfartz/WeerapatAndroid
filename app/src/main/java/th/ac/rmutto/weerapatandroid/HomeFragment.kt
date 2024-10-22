package th.ac.rmutto.weerapatandroid

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject


class HomeFragment : Fragment() {

    var recyclerView: RecyclerView? = null
    private var data = ArrayList<Data>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val root = inflater.inflate(R.layout.fragment_home, container, false)

        //For an synchronous task
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        //List data
        recyclerView = root.findViewById(R.id.recyclerView)
        showDataList()

        return root
    }

    //show a data list
    private fun showDataList() {
        val url: String = getString(R.string.root_url) + getString(R.string.listcar)
        val okHttpClient = OkHttpClient()
        val request: Request = Request.Builder().url(url).get().build()
        val response = okHttpClient.newCall(request).execute()

        if (response.isSuccessful) {
            val res = JSONArray(response.body!!.string())
            if (res.length() > 0) {
                for (i in 0 until res.length()) {
                    val item: JSONObject = res.getJSONObject(i)
                    data.add(
                        Data(
                            item.getString("idcar"),
                            item.getString("cname"),
                            item.getString("cbrand"),
                            item.getString("cgear"),
                            item.getString("cprice"),
                            item.getString("cpic")
                        )
                    )
                }

                recyclerView!!.adapter = DataAdapter(data)
            } else {
                Toast.makeText(context, "ไม่สามารถแสดงข้อมูลได้", Toast.LENGTH_LONG).show()
            }
        }
    }


    internal class Data(
        var idcar: String, var cname: String, var cbrand: String,
        var cgear: String, var cprice: String, var cpic: String
    )

    internal inner class DataAdapter(private val list: List<Data>) :
        RecyclerView.Adapter<DataAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view: View = LayoutInflater.from(parent.context).inflate(
                R.layout.item_viewcar,
                parent, false
            )
            return ViewHolder(view)
        }

        internal inner class ViewHolder(itemView: View) :
            RecyclerView.ViewHolder(itemView) {
            var data: Data? = null
            var imageViewFile: ImageView = itemView.findViewById(R.id.imageViewFile)
            var cname: TextView = itemView.findViewById(R.id.car_brand)
            var cbrand: TextView = itemView.findViewById(R.id.car_model)
            var cgear: TextView = itemView.findViewById(R.id.transmission_type)
            var cprice: TextView = itemView.findViewById(R.id.car_price)
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            val data = list[position]
            holder.data = data
            val url = getString(R.string.root_url) +
                    getString(R.string.car_image_url) + data.cpic
            Picasso.get().load(url).into(holder.imageViewFile)
            Picasso.get()
            Log.d("ImageURL", url)


            holder.cname.text = data.cname
            holder.cbrand.text = data.cbrand
            holder.cgear.text = data.cgear
            holder.cprice.text = "฿" + data.cprice

            holder.imageViewFile.setOnClickListener {
                val intent = Intent(context, CarActivity::class.java)
                intent.putExtra("idcar", data.idcar)
                startActivity(intent)
            }

        }
    }

}