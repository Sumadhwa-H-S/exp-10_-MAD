package com.example.exp10

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var btnLoadContacts: Button
    private lateinit var tvStatus: TextView
    private lateinit var listContacts: ListView

    private val requestPermission =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                loadContacts()
            } else {
                tvStatus.text =
                    "Contacts permission denied. Grant permission to continue."
                listContacts.adapter = null
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnLoadContacts = findViewById(R.id.btnLoadContacts)
        tvStatus = findViewById(R.id.tvStatus)
        listContacts = findViewById(R.id.listContacts)

        btnLoadContacts.setOnClickListener {
            checkPermissionAndLoadContacts()
        }
    }

    private fun checkPermissionAndLoadContacts() {
        val permission = Manifest.permission.READ_CONTACTS

        if (ContextCompat.checkSelfPermission(
                this, permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            loadContacts()
        } else {
            requestPermission.launch(permission)
        }
    }

    private fun loadContacts() {
        val contactList = mutableListOf<String>()

        try {
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            )

            val cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )

            if (cursor == null) {
                tvStatus.text = "Unable to retrieve contacts."
                return
            }

            cursor.use {
                val nameIndex = it.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                )
                val numberIndex = it.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                )

                while (it.moveToNext()) {
                    val name = it.getString(nameIndex) ?: "Unknown"
                    val number = it.getString(numberIndex) ?: "No number"

                    contactList.add("$name\n$number")
                }
            }

            listContacts.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                contactList
            )

            tvStatus.text = if (contactList.isEmpty()) {
                "No contacts found. Add contacts and try again."
            } else {
                "Total contact records: ${contactList.size}"
            }

        } catch (e: SecurityException) {
            tvStatus.text = "Permission required to access contacts."
        } catch (e: Exception) {
            tvStatus.text = "Error loading contacts: ${e.message}"
        }
    }
}
