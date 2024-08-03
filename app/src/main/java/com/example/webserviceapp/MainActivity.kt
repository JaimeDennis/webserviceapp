package com.example.webserviceapp

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.webserviceapp.Objetos.Contacts
import com.example.webserviceapp.Objetos.Device
import com.example.webserviceapp.Objetos.PHPProcesses
import com.example.webserviceapp.R

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var btnGuardar: Button
    private lateinit var btnListar: Button
    private lateinit var btnLimpiar: Button
    private lateinit var txtNombre: EditText
    private lateinit var txtDireccion: EditText
    private lateinit var txtTelefono1: EditText
    private lateinit var txtTelefono2: EditText
    private lateinit var txtNotas: EditText
    private lateinit var cbkFavorite: CheckBox
    private var savedContacto: Contacts? = null
    private lateinit var php: PHPProcesses
    private var id: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initComponents()
        setEvents()
    }

    private fun initComponents() {
        php = PHPProcesses()
        php.setContext(this)
        txtNombre = findViewById(R.id.txtNombre)
        txtTelefono1 = findViewById(R.id.txtTelefono1)
        txtTelefono2 = findViewById(R.id.txtTelefono2)
        txtDireccion = findViewById(R.id.txtDireccion)
        txtNotas = findViewById(R.id.txtNotas)
        cbkFavorite = findViewById(R.id.cbxFavorito)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnListar = findViewById(R.id.btnListar)
        btnLimpiar = findViewById(R.id.btnLimpiar)
        savedContacto = null
    }

    private fun setEvents() {
        btnGuardar.setOnClickListener(this)
        btnListar.setOnClickListener(this)
        btnLimpiar.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        if (isNetworkAvailable()) {
            when (view.id) {
                R.id.btnGuardar -> {
                    Log.d("MainActivity", "Guardar button clicked")
                    var completo = true
                    if (txtNombre.text.toString().isEmpty()) {
                        txtNombre.error = "Introduce el Nombre"
                        completo = false
                    }
                    if (txtTelefono1.text.toString().isEmpty()) {
                        txtTelefono1.error = "Introduce el Teléfono Principal"
                        completo = false
                    }
                    if (txtDireccion.text.toString().isEmpty()) {
                        txtDireccion.error = "Introduce la Dirección"
                        completo = false
                    }
                    if (completo) {
                        val nuevoContacto = Device.getSecureId(this)?.let {
                            Contacts(
                                name = txtNombre.text.toString(),
                                phoneNumber1 = txtTelefono1.text.toString(),
                                phoneNumber2 = txtTelefono2.text.toString(),
                                address = txtDireccion.text.toString(),
                                notes = txtNotas.text.toString(),
                                is_favorite = if (cbkFavorite.isChecked) 1 else 0,
                                id_movil = it
                            )
                        }
                        if (nuevoContacto != null) {
                            if (savedContacto == null) {
                                Log.d("MainActivity", "Inserting new contact: $nuevoContacto")
                                php.insertarContactoWebService(nuevoContacto)
                            } else {
                                Log.d("MainActivity", "Updating contact: $nuevoContacto")
                                php.actualizarContactoWebService(nuevoContacto, id)
                            }
                            Toast.makeText(applicationContext, R.string.message, Toast.LENGTH_SHORT).show()
                            limpiar()
                        }
                    }
                }
                R.id.btnLimpiar -> limpiar()
                R.id.btnListar -> {
                    Log.d("MainActivity", "Listar button clicked")
                    val intent = Intent(this@MainActivity, ListActivity::class.java)
                    limpiar()
                    startActivityForResult(intent, 0)
                }
            }
        } else {
            Log.d("MainActivity", "No network connection")
            Toast.makeText(applicationContext, "Se necesita tener conexión a internet", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val ni = cm.activeNetworkInfo
        return ni != null && ni.isConnected
    }

    private fun limpiar() {
        savedContacto = null
        txtNombre.setText("")
        txtTelefono1.setText("")
        txtTelefono2.setText("")
        txtNotas.setText("")
        txtDireccion.setText("")
        cbkFavorite.isChecked = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            val contact = data.getSerializableExtra("contact") as? Contacts
            if (contact != null) {
                savedContacto = contact
                id = contact.id
                txtNombre.setText(contact.name)
                txtTelefono1.setText(contact.phoneNumber1)
                txtTelefono2.setText(contact.phoneNumber2)
                txtDireccion.setText(contact.address)
                txtNotas.setText(contact.notes)
                cbkFavorite.isChecked = contact.is_favorite == 1
            }
        } else {
            limpiar()
        }
    }
}