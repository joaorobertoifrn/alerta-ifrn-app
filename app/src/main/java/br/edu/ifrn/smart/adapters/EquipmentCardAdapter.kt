package br.edu.ifrn.smart.adapters

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import br.edu.ifrn.smart.BuildConfig
import br.edu.ifrn.smart.R
import br.edu.ifrn.smart.models.EquipmentObj
import br.edu.ifrn.smart.utils.extensions.inflate
import com.pawegio.kandroid.i
import io.realm.RealmResults
import kotlinx.android.synthetic.main.equipment_cell.view.*

class EquipmentCardAdapter(var items: RealmResults<EquipmentObj>, val listener: EquipmentListener) : RecyclerView.Adapter<EquipmentCardViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): EquipmentCardViewHolder? {
        val v = inflate(R.layout.equipment_cell, parent)
        return EquipmentCardViewHolder(v)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: EquipmentCardViewHolder?, position: Int) {
        holder?.bindViewHolder(items[position], listener)
    }

}

class EquipmentCardViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

    fun bindViewHolder(equipmentObj: EquipmentObj, listener: EquipmentListener) {
        i { "default alpha: ${itemView.alpha}" }
        itemView.stateSwitch.setOnCheckedChangeListener(null)
        itemView.progressWheel.visibility = View.GONE

        itemView.title.text = equipmentObj.name
        itemView.subTitle.text = equipmentObj.topic
        itemView.stateSwitch.isChecked = equipmentObj.getValueAsBoolean()

        itemView.stateSwitch.setOnCheckedChangeListener(
                { v, isChecked ->
                    if (BuildConfig.DEBUG) i { "Estado alterado para ${equipmentObj.name}! Agora eh $isChecked" }
                    itemView.progressWheel.visibility = View.VISIBLE
                    itemView.progressWheel.spin()
                    listener.onStateChanged(equipmentObj, isChecked)
                }
        )

        itemView.setOnClickListener { listener.onEquipmentClicked(equipmentObj) }
    }
}

interface EquipmentListener {
    fun onStateChanged(equipment: EquipmentObj, state: Boolean)

    fun onEquipmentClicked(equipment: EquipmentObj)
}