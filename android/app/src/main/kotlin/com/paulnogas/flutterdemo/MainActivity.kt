package com.paulnogas.flutterdemo

import android.os.Bundle

import android.Manifest
import android.content.pm.PackageManager
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat

import android.annotation.TargetApi
import android.os.Build
import java.util.List
import android.os.AsyncTask
import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import android.content.Context
import io.flutter.app.FlutterActivity
import io.flutter.plugins.GeneratedPluginRegistrant
import java.util.HashMap
import java.util.ArrayList
import android.system.Os.close

class MainActivity() : FlutterActivity() {
    private val CHANNEL_CONTACTS = "runtimepermissiontutorial/contacts"
    private val GET_CONTACTS_PERMISSION_REQUEST_ID = 1234

    var getContactsPermissionCallback: PermissionCallback? = null
    var contactsCallback: ContactsCallback? = null
    var rationaleJustShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GeneratedPluginRegistrant.registerWith(this)

        MethodChannel(flutterView, CHANNEL_CONTACTS).setMethodCallHandler { call, result ->
            getContactsPermissionCallback = object : PermissionCallback {
                override fun granted() {
                    rationaleJustShown = false
                    result.success(0)
                }

                override fun denied() {
                    rationaleJustShown = false
                    result.success(1)
                }

                override fun showRationale() {
                    rationaleJustShown = true
                    result.success(2)
                }
            }

            contactsCallback = object : ContactsCallback {
                override fun onSuccess(contacts: ArrayList<HashMap<String, String>>) {
                    result.success(contacts)
                }

                override fun onError() {
                    result.success(null)
                }
            }

            if (call.method.equals("hasPermission")) {
                hasPermission()
            } else if (call.method.equals("getContacts")) {
                GetContactsTask(getApplicationContext(), this).execute()
            }
        }
    }

    fun hasPermission() {
        if (rationaleJustShown) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), GET_CONTACTS_PERMISSION_REQUEST_ID)
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                    getContactsPermissionCallback?.showRationale()
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), GET_CONTACTS_PERMISSION_REQUEST_ID)
                }
            } else {
                getContactsPermissionCallback?.granted()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == GET_CONTACTS_PERMISSION_REQUEST_ID) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getContactsPermissionCallback?.granted()
            } else {
                getContactsPermissionCallback?.denied()
            }
            return
        }
    }

    class GetContactsTask(context: Context, mainActivity: MainActivity) : AsyncTask<Void, Void, ArrayList<HashMap<String, String>>>() {
        private val context: Context
        private val mainActivity: MainActivity

        init {
            this.context = context
            this.mainActivity = mainActivity
        }

        override fun doInBackground(vararg urls: Void): ArrayList<HashMap<String, String>> {
            try {
                val cr = context.getContentResolver()
                val uri = ContactsContract.Contacts.CONTENT_URI
                val projection = arrayOf<String>(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
                val selection = ContactsContract.Contacts.HAS_PHONE_NUMBER + " = '1'"
                val sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC"
                val contacts = ArrayList<HashMap<String, String>>()

                val users = cr.query(uri, projection, selection, null, sortOrder)

                while (users != null && users!!.moveToNext()) {
                    val contactId = users!!.getInt(users!!.getColumnIndex(ContactsContract.Contacts._ID))
                    val displayName = users!!.getString(users!!.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))

                    var mobileNumber: String? = null
                    val contactNumbers = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null)
                    while (contactNumbers != null && contactNumbers!!.moveToNext()) {
                        val number = contactNumbers!!.getString(contactNumbers!!.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        val type = contactNumbers!!.getInt(contactNumbers!!.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE))
                        when (type) {
                            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE -> mobileNumber = number
                            else -> {
                            }
                        }// Ignore that number
                    }
                    if (contactNumbers != null) {
                        contactNumbers!!.close()
                    }

                    val contact = HashMap<String, String>()
                    contact.put("NAME", displayName)
                    contact.put("MOBILE", mobileNumber ?: "null")
                    contacts.add(contact)
                }

                if (users != null) {
                    users!!.close()
                }

                return contacts

            } catch (e: Exception) {
                Log.e("DEBUG", "exception $e")
            }

            return ArrayList<HashMap<String, String>>()
        }

        override fun onPostExecute(result: ArrayList<HashMap<String, String>>?) {
            if (result == null) {
                mainActivity.contactsCallback?.onError()
            } else {
                mainActivity.contactsCallback?.onSuccess(result)
            }
        }
    }

    interface PermissionCallback {
        fun granted()

        fun denied()

        fun showRationale()
    }

    interface ContactsCallback {
        fun onSuccess(contacts: ArrayList<HashMap<String, String>>)

        fun onError()
    }

}
