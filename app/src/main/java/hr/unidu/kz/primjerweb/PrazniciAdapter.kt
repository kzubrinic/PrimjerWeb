package hr.unidu.kz.primjerweb

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import hr.unidu.kz.primjerweb.databinding.ItemPraznikBinding

class PrazniciAdapter(
    private val mDataset: List<Praznik>
) : RecyclerView.Adapter<PrazniciAdapter.MyViewHolder>() {

    // ViewHolder koristi generirani Binding klasu za item_praznik.xml
    class MyViewHolder(val binding: ItemPraznikBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemPraznikBinding.inflate(layoutInflater, parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val praznik = mDataset[position]

        // Povezivanje podataka s UI elementima preko bindinga
        holder.binding.apply {
            tvDatum.text = praznik.date
            tvNaziv.text = praznik.localName
            tvEnNaziv.text = praznik.name

            // Postavljanje klika na cijeli redak (root)
            root.setOnClickListener {
                Toast.makeText(it.context, praznik.localName, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int = mDataset.size
}