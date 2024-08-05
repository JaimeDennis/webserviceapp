package com.example.webserviceapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.webserviceapp.Objetos.Device
import com.example.webserviceapp.Objetos.PHPProcesses
import com.example.webserviceapp.Objetos.Contacts
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList

class ListActivity : android.app.ListActivity(), Response.Listener<JSONObject>, Response.ErrorListener {
    private lateinit var btnNuevo: Button
    private val context: Context = this
    private val php = PHPProcesses()
    private lateinit var request: RequestQueue
    private lateinit var jsonObjectRequest: JsonObjectRequest
    private var listaContactos: ArrayList<Contacts> = ArrayList()
    private val serverip = "http://3.132.72.238/WebService/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        request = Volley.newRequestQueue(context)
        php.setContext(context)
        consultarTodosWebService()
        btnNuevo = findViewById(R.id.btnNew)
        btnNuevo.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun consultarTodosWebService() {
        val url = "$serverip/wsConsultarTodos.php"
        jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null, this, this)
        request.add(jsonObjectRequest)
    }

    override fun onErrorResponse(error: VolleyError) {
        // Manejo del error
        Toast.makeText(context, "Error al consultar los contactos", Toast.LENGTH_SHORT).show()
    }

    override fun onResponse(response: JSONObject) {
        listaContactos.clear() // Limpiar la lista antes de agregar nuevos datos

        try {
            val jsonArray: JSONArray = response.optJSONArray("data") ?: return

            for (i in 0 until jsonArray.length()) {
                val jsonObject: JSONObject = jsonArray.getJSONObject(i)
                val contacto = Contacts().apply {
                    id = jsonObject.optInt("id")
                    name = jsonObject.optString("name")
                    phoneNumber1 = jsonObject.optString("phoneNumber1")
                    phoneNumber2 = jsonObject.optString("phoneNumber2")
                    address = jsonObject.optString("address")
                    notes = jsonObject.optString("notes")
                    is_favorite = jsonObject.optInt("is_favorite")
                    id_movil = jsonObject.optString("id_movil")
                }
                listaContactos.add(contacto)
            }

            val adapter = MyArrayAdapter(context, R.layout.layout_contact, listaContactos)
            listAdapter = adapter
        } catch (e: JSONException) {
            e.printStackTrace()
            Toast.makeText(context, "Error al procesar la respuesta", Toast.LENGTH_SHORT).show()
        }
    }

    inner class MyArrayAdapter(
        context: Context,
        private val textViewRecursoId: Int,
        private val objects: ArrayList<Contacts>
    ) : ArrayAdapter<Contacts>(context, textViewRecursoId, objects) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = layoutInflater.inflate(textViewRecursoId, null)

            val lblNombre: TextView = view.findViewById(R.id.lblNombreContacto)
            val lblTelefono: TextView = view.findViewById(R.id.lblTelefonoContacto)
            val btnModificar: Button = view.findViewById(R.id.btnModificar)
            val btnBorrar: Button = view.findViewById(R.id.btnBorrar)

            if (objects[position].is_favorite > 0) {
                lblNombre.setTextColor(Color.BLUE)
                lblTelefono.setTextColor(Color.BLUE)
            } else {
                lblNombre.setTextColor(Color.BLACK)
                lblTelefono.setTextColor(Color.BLACK)
            }

            lblNombre.text = objects[position].name
            lblTelefono.text = objects[position].phoneNumber1

            btnBorrar.setOnClickListener {
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Confirmar eliminación")
                builder.setMessage("¿Estás seguro de que quieres eliminar este contacto?")
                builder.setPositiveButton("Sí") { dialog, _ ->
                    php.borrarContactoWebService(objects[position].id)
                    objects.removeAt(position)
                    notifyDataSetChanged()
                    Toast.makeText(applicationContext, "Contacto eliminado con éxito", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                builder.setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                val alert = builder.create()
                alert.show()
            }

            btnModificar.setOnClickListener {
                val bundle = Bundle().apply {
                    putSerializable("contact", objects[position])
                }
                val intent = Intent().apply {
                    putExtras(bundle)
                }
                setResult(Activity.RESULT_OK, intent)
                finish()
            }

            return view
        }
    }
}
