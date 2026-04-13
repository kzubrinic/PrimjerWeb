package hr.unidu.kz.primjerweb

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import hr.unidu.kz.primjerweb.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import java.util.Scanner

class MainActivity : AppCompatActivity() {
    private val baseUrl = "https://date.nager.at/api/v3/PublicHolidays"
    private lateinit var binding: ActivityMainBinding
    private val praznici = mutableListOf<Praznik>()
    private lateinit var mAdapter: PrazniciAdapter
    private var odabraniKod: String = "HR"
    private lateinit var listaDrzava : List<Drzava>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Postavljanje View Bindinga
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val tekucaGodina = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        binding.godina.setText(tekucaGodina.toString())

        listaDrzava = vratiListuDrzava()
        // Inicijalizacija Spinnera
        val adapterDrzava = ArrayAdapter(this, R.layout.item_spinner, listaDrzava)
        adapterDrzava.setDropDownViewResource(R.layout.item_spinner)
        binding.spDrzava.adapter = adapterDrzava

        // Slušalac promjena na Spinneru
        binding.spDrzava.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val drzava = listaDrzava[position]
                odabraniKod = drzava.countryCode // Spremi kod države za kasnije (npr. "HR")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val indexHrvatska = listaDrzava.indexOfFirst { it.countryCode == "HR" }

        if (indexHrvatska != -1) {
            binding.spDrzava.setSelection(indexHrvatska)
        }

        setupRecyclerView()


        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        mAdapter = PrazniciAdapter(praznici)

        binding.pregledPraznika.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = mAdapter
            setHasFixedSize(true)
            addItemDecoration(DividerItemDecoration(this@MainActivity, DividerItemDecoration.VERTICAL))
        }
    }

    // Poziva se na klik gumba u XML-u (android:onClick="praznici")
    fun praznici(v: View) {
        val god = binding.godina.text.toString()
        //val drz = binding.drzava.text.toString()

        if (god.isEmpty() || odabraniKod.isEmpty()) {
            Toast.makeText(this, "Godina i oznaka države moraju se popuniti!", Toast.LENGTH_LONG).show()
            return
        }

        val wsUrl = "$baseUrl/$god/$odabraniKod"

        // Pokretanje korutine umjesto AsyncTaska
        lifecycleScope.launch {
            binding.progressBar.isIndeterminate = true

            val rezultat = dohvatiPodatkeSPraznicima(wsUrl)

            obradiRezultat(rezultat)
        }
    }

    private suspend fun dohvatiPodatkeSPraznicima(urlString: String): String = withContext(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        try {
            val url = URL(urlString)
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 10000
            conn.readTimeout = 10000

            // Provjeri HTTP status kod
            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                conn.inputStream.bufferedReader().use { it.readText() }
            } else {
                // Logiraj grešku ako server vrati npr. 404 ili 500
                android.util.Log.e("MREZA", "Server vratio status: $responseCode")
                ""
            }
        } catch (e: Exception) {
            android.util.Log.e("MREZA", "Greška: ${e.message}")
            ""
        } finally {
            conn?.disconnect()
        }
    }

    private fun inputStreamToString(inputStream: java.io.InputStream): String {
        val s = Scanner(inputStream).useDelimiter("\\A")
        return if (s.hasNext()) s.next() else ""
    }

    // Konfiguracija JSON parsera (npr. da ignorira polja koja nismo definirali u klasi)
    private val jsonParser = Json {
        ignoreUnknownKeys = true // OBAVEZNO: zanemari polja koja nismo opisali u data klasi
        coerceInputValues = true // Ako je polje null u JSON-u, a u klasi nije, koristi defaultnu vrijednost
        isLenient = true
    }

    private fun obradiRezultat(jsonRezultat: String) {
        binding.progressBar.isIndeterminate = false

        if (jsonRezultat.isBlank()) {
            prikaziGresku("Problem s dohvaćanjem podataka s mreže.")
            return
        }

        try {
            val dohvaceniPraznici = jsonParser.decodeFromString<List<Praznik>>(jsonRezultat)

            if (dohvaceniPraznici.isEmpty()) {
                prikaziGresku("Nema praznika za odabrane parametre.")
            } else {
                praznici.clear()
                praznici.addAll(dohvaceniPraznici)
                mAdapter.notifyDataSetChanged() // Ovo mora biti ovdje
            }
        } catch (e: Exception) {
            android.util.Log.e("PARSIRANJE", "Greška u JSON-u: ${e.message}")
            prikaziGresku("Greška u obradi podataka.")
        }
    }

    private fun prikaziGresku(poruka: String) {
        val msg = "$poruka (Godina: ${binding.godina.text}, Država: ${odabraniKod})!"
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    private fun vratiListuDrzava() : List<Drzava>{
        return listOf(
            Drzava("Andorra", "AD"),
            Drzava("Albania", "AL"),
            Drzava("Armenia", "AM"),
            Drzava("Argentina", "AR"),
            Drzava("Austria", "AT"),
            Drzava("Australia", "AU"),
            Drzava("Åland Islands", "AX"),
            Drzava("Bosnia and Herzegovina", "BA"),
            Drzava("Barbados", "BB"),
            Drzava("Bangladesh", "BD"),
            Drzava("Belgium", "BE"),
            Drzava("Bulgaria", "BG"),
            Drzava("Benin", "BJ"),
            Drzava("Bolivia", "BO"),
            Drzava("Brazil", "BR"),
            Drzava("Bahamas", "BS"),
            Drzava("Botswana", "BW"),
            Drzava("Belarus", "BY"),
            Drzava("Belize", "BZ"),
            Drzava("Canada", "CA"),
            Drzava("DR Congo", "CD"),
            Drzava("Congo", "CG"),
            Drzava("Switzerland", "CH"),
            Drzava("Chile", "CL"),
            Drzava("China", "CN"),
            Drzava("Colombia", "CO"),
            Drzava("Costa Rica", "CR"),
            Drzava("Cuba", "CU"),
            Drzava("Cyprus", "CY"),
            Drzava("Czechia", "CZ"),
            Drzava("Germany", "DE"),
            Drzava("Denmark", "DK"),
            Drzava("Dominican Republic", "DO"),
            Drzava("Ecuador", "EC"),
            Drzava("Estonia", "EE"),
            Drzava("Egypt", "EG"),
            Drzava("Spain", "ES"),
            Drzava("Finland", "FI"),
            Drzava("Faroe Islands", "FO"),
            Drzava("France", "FR"),
            Drzava("Gabon", "GA"),
            Drzava("United Kingdom", "GB"),
            Drzava("Grenada", "GD"),
            Drzava("Georgia", "GE"),
            Drzava("Guernsey", "GG"),
            Drzava("Ghana", "GH"),
            Drzava("Gibraltar", "GI"),
            Drzava("Greenland", "GL"),
            Drzava("Gambia", "GM"),
            Drzava("Greece", "GR"),
            Drzava("Guatemala", "GT"),
            Drzava("Guyana", "GY"),
            Drzava("Hong Kong", "HK"),
            Drzava("Honduras", "HN"),
            Drzava("Croatia", "HR"),
            Drzava("Haiti", "HT"),
            Drzava("Hungary", "HU"),
            Drzava("Indonesia", "ID"),
            Drzava("Ireland", "IE"),
            Drzava("Isle of Man", "IM"),
            Drzava("Iceland", "IS"),
            Drzava("Italy", "IT"),
            Drzava("Jersey", "JE"),
            Drzava("Jamaica", "JM"),
            Drzava("Japan", "JP"),
            Drzava("Kenya", "KE"),
            Drzava("South Korea", "KR"),
            Drzava("Kazakhstan", "KZ"),
            Drzava("Liechtenstein", "LI"),
            Drzava("Lesotho", "LS"),
            Drzava("Lithuania", "LT"),
            Drzava("Luxembourg", "LU"),
            Drzava("Latvia", "LV"),
            Drzava("Morocco", "MA"),
            Drzava("Monaco", "MC"),
            Drzava("Moldova", "MD"),
            Drzava("Montenegro", "ME"),
            Drzava("Madagascar", "MG"),
            Drzava("North Macedonia", "MK"),
            Drzava("Mongolia", "MN"),
            Drzava("Montserrat", "MS"),
            Drzava("Malta", "MT"),
            Drzava("Mexico", "MX"),
            Drzava("Mozambique", "MZ"),
            Drzava("Namibia", "NA"),
            Drzava("Niger", "NE"),
            Drzava("Nigeria", "NG"),
            Drzava("Nicaragua", "NI"),
            Drzava("Netherlands", "NL"),
            Drzava("Norway", "NO"),
            Drzava("New Zealand", "NZ"),
            Drzava("Panama", "PA"),
            Drzava("Peru", "PE"),
            Drzava("Papua New Guinea", "PG"),
            Drzava("Philippines", "PH"),
            Drzava("Poland", "PL"),
            Drzava("Puerto Rico", "PR"),
            Drzava("Portugal", "PT"),
            Drzava("Paraguay", "PY"),
            Drzava("Romania", "RO"),
            Drzava("Serbia", "RS"),
            Drzava("Russia", "RU"),
            Drzava("Seychelles", "SC"),
            Drzava("Sweden", "SE"),
            Drzava("Singapore", "SG"),
            Drzava("Slovenia", "SI"),
            Drzava("Svalbard and Jan Mayen", "SJ"),
            Drzava("Slovakia", "SK"),
            Drzava("San Marino", "SM"),
            Drzava("Suriname", "SR"),
            Drzava("El Salvador", "SV"),
            Drzava("Tunisia", "TN"),
            Drzava("Türkiye", "TR"),
            Drzava("Ukraine", "UA"),
            Drzava("Uganda", "UG"),
            Drzava("United States", "US"),
            Drzava("Uruguay", "UY"),
            Drzava("Vatican City", "VA"),
            Drzava("Venezuela", "VE"),
            Drzava("Vietnam", "VN"),
            Drzava("South Africa", "ZA"),
            Drzava("Zimbabwe", "ZW")
        )
    }
}