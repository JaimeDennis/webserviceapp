package com.example.webserviceapp.Objetos

import android.content.Context
import android.util.Log
import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.util.ArrayList

class PHPProcesses : Response.Listener<JSONObject>, Response.ErrorListener {
    private var request: RequestQueue? = null
    private var jsonObjectRequest: JsonObjectRequest? = null
    private val contactos = ArrayList<Contacts>()
    private val serverip = "http://3.132.72.238/WebService/" // Actualiza con tu IP local

    fun setContext(context: Context) {
        request = Volley.newRequestQueue(context)
    }

    fun insertarContactoWebService(c: Contacts) {
        val url = "$serverip/wsRegistro.php"
        val params = HashMap<String, String>()
        params["name"] = c.name
        params["phoneNumber1"] = c.phoneNumber1
        params["phoneNumber2"] = c.phoneNumber2
        params["address"] = c.address
        params["notes"] = c.notes
        params["is_favorite"] = c.is_favorite.toString()
        params["id_movil"] = "1"

        val requestBody = params.entries.joinToString("&") { "${it.key}=${it.value}" }

        val stringRequest = object : StringRequest(Method.POST, url,
            Response.Listener<String> { response ->
                Log.d("PHPProcesses", "Response: $response")
            },
            Response.ErrorListener { error ->
                Log.e("PHPProcesses", "Error: ${error.message}", error)
            }) {
            override fun getBody(): ByteArray {
                return requestBody.toByteArray(Charsets.UTF_8)
            }

            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/x-www-form-urlencoded"
                return headers
            }
        }

        request?.add(stringRequest)
    }

    fun actualizarContactoWebService(c: Contacts, id: Int) {
        val url = "$serverip/wsActualizar.php"
        val params = HashMap<String, String>()
        params["id"] = id.toString()
        params["name"] = c.name
        params["address"] = c.address
        params["phoneNumber1"] = c.phoneNumber1
        params["phoneNumber2"] = c.phoneNumber2
        params["notes"] = c.notes
        params["is_favorite"] = c.is_favorite.toString()
        params["id_movil"] = "1"

        val requestBody = params.entries.joinToString("&") { "${it.key}=${it.value}" }

        val stringRequest = object : StringRequest(Method.POST, url,
            Response.Listener<String> { response ->
                Log.d("PHPProcesses", "Response: $response")
            },
            Response.ErrorListener { error ->
                Log.e("PHPProcesses", "Error: ${error.message}", error)
            }) {
            override fun getBody(): ByteArray {
                return requestBody.toByteArray(Charsets.UTF_8)
            }

            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/x-www-form-urlencoded"
                return headers
            }
        }

        request?.add(stringRequest)
    }

    fun borrarContactoWebService(id: Int) {
        val url = "$serverip/wsEliminar.php?id=$id"
        jsonObjectRequest = CustomJsonObjectRequest(Request.Method.GET, url, null, this, this)
        request?.add(jsonObjectRequest)
    }

    override fun onErrorResponse(error: VolleyError) {
        Log.e("PHPProcesses", "Error: ${error.message}", error)
        if (error.networkResponse != null) {
            Log.e("PHPProcesses", "HTTP Status Code: ${error.networkResponse.statusCode}")
            Log.e("PHPProcesses", "Response Data: ${String(error.networkResponse.data)}")
        }
    }

    override fun onResponse(response: JSONObject) {
        Log.d("PHPProcesses", "Response: $response")
        try {
            val success = response.getBoolean("success")
            if (success) {
                Log.d("PHPProcesses", "Operation successful")
            } else {
                Log.d("PHPProcesses", "Operation failed: ${response.getString("message")}")
            }
        } catch (e: Exception) {
            Log.e("PHPProcesses", "Response handling error: ${e.message}", e)
        }
    }

    private class CustomJsonObjectRequest(
        method: Int,
        url: String,
        jsonRequest: JSONObject?,
        listener: Response.Listener<JSONObject>,
        errorListener: Response.ErrorListener
    ) : JsonObjectRequest(method, url, jsonRequest, listener, errorListener) {
        override fun parseNetworkResponse(response: NetworkResponse): Response<JSONObject> {
            return try {
                val jsonString = String(response.data, Charset.forName(HttpHeaderParser.parseCharset(response.headers, "utf-8")))
                Log.d("PHPProcesses", "Raw Response: $jsonString")
                Response.success(JSONObject(jsonString), HttpHeaderParser.parseCacheHeaders(response))
            } catch (e: UnsupportedEncodingException) {
                Response.error(ParseError(e))
            } catch (je: Exception) {
                Response.error(ParseError(je))
            }
        }
    }
}
