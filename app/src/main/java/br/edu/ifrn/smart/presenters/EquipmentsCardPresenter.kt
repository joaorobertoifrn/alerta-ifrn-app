package br.edu.ifrn.smart.presenters

import br.edu.ifrn.smart.BuildConfig
import br.edu.ifrn.smart.fragments.EquipmentsFragment
import br.edu.ifrn.smart.models.EquipmentObj
import br.edu.ifrn.smart.utils.ConnectionManager
import com.pawegio.kandroid.d
import com.pawegio.kandroid.i
import io.realm.Realm
import org.eclipse.paho.client.mqttv3.MqttMessage
import rx.lang.kotlin.onError

class EquipmentsCardPresenter constructor(var view: EquipmentsFragment?) {

    private var realm = Realm.getDefaultInstance()

    fun onDestroy() {
        view = null
    }

    fun requestEquipments() {
        realm?.where(EquipmentObj::class.java)?.findAll()?.asObservable()?.distinctUntilChanged()?.
                doOnNext { view?.onEquipmentsSuccess(it) }?.onError { it.printStackTrace() }?.
                subscribe()
    }

    fun messageRecieved(topic: String, message: MqttMessage?) {
        if (BuildConfig.DEBUG) i("Received: ${topic} = ${message.toString()}")
        realm?.where(EquipmentObj::class.java)?.equalTo("topic", topic)?.findAll()?.asObservable()?.distinctUntilChanged()?.
                doOnNext {
                    realm?.beginTransaction()
                    for (i in 0..it.lastIndex) {
                        val equip = it[i]
                        equip.value = message.toString()
                        i(equip.toString())
                    }
                    realm?.commitTransaction()
                    view?.reloadEquipments()
                }?.onError { it.printStackTrace() }?.
                subscribe()
    }

    fun stateChanged(equipment: EquipmentObj, state: Boolean) {
        if (BuildConfig.DEBUG) d { "stateChanged on ${equipment.name}(${equipment.topic}) is now $state" }
        val message = if (state) MqttMessage("on".toByteArray()) else MqttMessage("off".toByteArray())
        message.qos = 1
        ConnectionManager.client?.publish(equipment.topic, message)
    }

}