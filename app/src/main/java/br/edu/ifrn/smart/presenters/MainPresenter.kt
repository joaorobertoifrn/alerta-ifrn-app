package br.edu.ifrn.smart.presenters

import android.support.v7.app.AppCompatActivity
import br.edu.ifrn.smart.activities.MainActivity
import br.edu.ifrn.smart.models.ConnectionObj
import br.edu.ifrn.smart.utils.ConnectionManager
import br.edu.ifrn.smart.utils.MyConstants
import com.pawegio.kandroid.longToast
import kotlinx.android.synthetic.main.activity_main.*
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.jetbrains.anko.toast

/**
 * Created by marciogranzotto on 21/01/17.
 */
class MainPresenter(var view: MainActivity?) {

    companion object {
        val SERVER_URL = ""
    }

    private var serverUrl: String? = null
    private var serverPort: String? = null
    private var user: String? = null
    private var password: String? = null

    private var oldConnectObject: ConnectionObj? = null

    fun onCreate() {
        getDataFromSharedPreferences()
    }

    fun onDestroy() {
        view = null
    }

    fun onConnectButtonClicked() {
        connect()
    }

    private fun shouldEnableButton() {
        view?.connectButton?.isEnabled = !serverUrl.isNullOrEmpty() && !serverPort.isNullOrEmpty()
    }


    fun getDataFromSharedPreferences() {
        view?.showProgressDialog()

        val prefs = view?.getSharedPreferences(MyConstants.SHARED_PREFERENCES, AppCompatActivity.MODE_PRIVATE)
        serverUrl = prefs?.getString(MyConstants.SERVER_URL, null) ?: oldConnectObject?.serverUrl
        view?.etServer?.setText(serverUrl)
        serverPort = prefs?.getString(MyConstants.SERVER_PORT, null) ?: oldConnectObject?.serverPort
        view?.etPort?.setText(serverPort ?: "")
        user = prefs?.getString(MyConstants.SERVER_USER, null) ?: oldConnectObject?.user
        view?.etUser?.setText(user)
        password = prefs?.getString(MyConstants.SERVER_USER_PASSWORD, null) ?: oldConnectObject?.password
        view?.etPassword?.setText(password)

        if (oldConnectObject == null && !serverUrl.isNullOrBlank() && !serverPort.isNullOrBlank())
            connect()
        else
            view?.dismissProgressDialog()
    }

    fun onPortTextChanged(text: String) {
        serverPort = text.trim()
        shouldEnableButton()
    }

    fun onServerTextChanged(text: String) {
        serverUrl = text.trim()
        shouldEnableButton()
    }

    private fun connect() {
        val userName = view?.etUser?.text.toString()
        val password = view?.etPassword?.text.toString()
        ConnectionManager.setUp(serverUrl, serverPort, userName, password)
        ConnectionManager.connectionListener = object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                saveDataToSharedPreferences(userName, password)
                view?.toast("Conectado")
                view?.dismissProgressDialog()
                view?.goToDashboardScreen()
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                view?.longToast("Erro ao conectar")
                exception?.printStackTrace()
                view?.dismissProgressDialog()
            }

        }
        if (view == null) return
        ConnectionManager.connect(view!!.applicationContext)
    }

    private fun saveDataToSharedPreferences(userName: String?, password: String?) {
        val prefs = view?.getSharedPreferences(MyConstants.SHARED_PREFERENCES, AppCompatActivity.MODE_PRIVATE)
        val editor = prefs?.edit()
        editor?.putString(MyConstants.SERVER_URL, serverUrl)
        editor?.putString(MyConstants.SERVER_PORT, serverPort)
        editor?.putString(MyConstants.SERVER_USER, userName)
        editor?.putString(MyConstants.SERVER_USER_PASSWORD, password)
        editor?.apply()
    }

    fun onIntentExtras(parcelable: ConnectionObj?) {
        oldConnectObject = parcelable
    }

}