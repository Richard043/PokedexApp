package com.example.pokedexapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import com.example.pokedexapp.ui.theme.PokedexAppTheme
import org.json.JSONArray

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PokedexAppTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    var selectedOption by remember { mutableStateOf<String?>(null) }

    val imageUrl = "https://i.imgur.com/KSTncXC.jpeg"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray)
    ) {
        Image(
            painter = rememberAsyncImagePainter(imageUrl),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        when (selectedOption) {
            null -> MainMenu(onOptionSelected = { selectedOption = it })
            "Data" -> SearchScreen(option = selectedOption!!, onBack = { selectedOption = null })
            "Moveset" -> SearchScreen(option = selectedOption!!, onBack = { selectedOption = null })
            "Stats" -> SearchScreen(option = selectedOption!!, onBack = { selectedOption = null })
            "Type Stats" -> SearchScreen(option = selectedOption!!, onBack = { selectedOption = null })
            "Type Moves" -> SearchScreen(option = selectedOption!!, onBack = { selectedOption = null })
        }


    }
}


@Composable
fun MainMenu(onOptionSelected: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Pokedex App", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { onOptionSelected("Data") }) {
            Text("Check Pokémon Data")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { onOptionSelected("Moveset") }) {
            Text("Check Pokémon Moveset")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { onOptionSelected("Stats") }) {
            Text("Check Pokémon Stats")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { onOptionSelected("Type Stats") }) {
            Text("Check Type Stats")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { onOptionSelected("Type Moves") }) {
            Text("Check Type Moves")
        }
    }
}

@Composable
fun SearchScreen(option: String, onBack: () -> Unit) {
    var input by remember { mutableStateOf(TextFieldValue("")) }
    var result by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val client = OkHttpClient()

    fun fetchData(query: String) {
        if (query.isBlank()) {
            errorMessage = "Please enter a valid Pokémon name or type."
            return
        }

        errorMessage = null // Reset error message

        when (option) {
            "Data" -> {
                val request = Request.Builder().url("https://pokeapi.co/api/v2/pokemon/$query").build()
                client.newCall(request).enqueue(object : okhttp3.Callback {
                    override fun onFailure(call: okhttp3.Call, e: IOException) {
                        result = ""
                        errorMessage = "Request failed: ${e.message}"
                    }

                    override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                        if (!response.isSuccessful) {
                            errorMessage = "Pokémon not found!"
                            result = ""
                            return
                        }

                        val json = response.body?.string()
                        val jsonObject = JSONObject(json!!)
                        val name = jsonObject.getString("name")
                        val order = jsonObject.getInt("order")
                        val weight = jsonObject.getInt("weight")
                        val height = jsonObject.getInt("height")
                        val xp = jsonObject.getInt("base_experience")
                        val types = jsonObject.getJSONArray("types")
                        val typesList = mutableListOf<String>()
                        for (i in 0 until types.length()) {
                            typesList.add(types.getJSONObject(i).getJSONObject("type").getString("name"))
                        }
                        result = """
                            Name: ${name.capitalize()}
                            Pokedex Order: $order
                            Weight: $weight hectograms
                            Height: $height decimetres
                            XP Base: $xp
                            Types: ${typesList.joinToString(", ")}
                        """.trimIndent()
                        imageUrl = "https://play.pokemonshowdown.com/sprites/ani/$name.gif"
                    }
                })
            }
            "Moveset" -> {
                val request = Request.Builder()
                    .url("https://pokeapi.co/api/v2/pokemon/$query")
                    .build()

                client.newCall(request).enqueue(object : okhttp3.Callback {
                    override fun onFailure(call: okhttp3.Call, e: IOException) {
                        result = ""
                        errorMessage = "Request failed: ${e.message}"
                    }

                    override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                        if (!response.isSuccessful) {
                            errorMessage = "Pokémon not found!"
                            result = ""
                            return
                        }

                        val json = response.body?.string()
                        val jsonObject = JSONObject(json!!)
                        val name = jsonObject.getString("name")
                        val moves = jsonObject.getJSONArray("moves")
                        val movesList = mutableListOf<String>()

                        for (i in 0 until moves.length()) {
                            val move = moves.getJSONObject(i)
                            val moveName = move.getJSONObject("move").getString("name")
                            val levelLearnedAt = move
                                .getJSONArray("version_group_details")
                                .getJSONObject(0)
                                .getInt("level_learned_at")
                            val learnMethod = move
                                .getJSONArray("version_group_details")
                                .getJSONObject(0)
                                .getJSONObject("move_learn_method")
                                .getString("name")

                            movesList.add("$moveName: Level $levelLearnedAt by $learnMethod")
                        }

                        result = """
                ${name.capitalize()}'s Movesets:
                ${movesList.joinToString("\n")}
            """.trimIndent()

                        imageUrl = "https://play.pokemonshowdown.com/sprites/ani/$name.gif"
                    }
                })
            }
            "Stats" -> {
                val request = Request.Builder()
                    .url("https://pokeapi.co/api/v2/pokemon/$query")
                    .build()

                client.newCall(request).enqueue(object : okhttp3.Callback {
                    override fun onFailure(call: okhttp3.Call, e: IOException) {
                        result = ""
                        errorMessage = "Request failed: ${e.message}"
                    }

                    override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                        if (!response.isSuccessful) {
                            errorMessage = "Pokémon not found!"
                            result = ""
                            return
                        }

                        val json = response.body?.string()
                        val jsonObject = JSONObject(json!!)
                        val name = jsonObject.getString("name")
                        val stats = jsonObject.getJSONArray("stats")
                        val statsList = mutableListOf<String>()

                        for (i in 0 until stats.length()) {
                            val stat = stats.getJSONObject(i)
                            val statName = stat.getJSONObject("stat").getString("name")
                            val baseStat = stat.getInt("base_stat")
                            statsList.add("$statName: $baseStat")
                        }

                        result = """
                ${name.capitalize()}'s Stats:
                ${statsList.joinToString("\n")}
            """.trimIndent()

                        imageUrl = "https://play.pokemonshowdown.com/sprites/ani/$name.gif"
                    }
                })
            }
            "Type Stats" -> {
                val request = Request.Builder()
                    .url("https://pokeapi.co/api/v2/type/$query")
                    .build()

                client.newCall(request).enqueue(object : okhttp3.Callback {
                    override fun onFailure(call: okhttp3.Call, e: IOException) {
                        result = ""
                        errorMessage = "Request failed: ${e.message}"
                    }

                    override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                        if (!response.isSuccessful) {
                            errorMessage = "Type not found!"
                            result = ""
                            return
                        }

                        val json = response.body?.string()
                        val jsonObject = JSONObject(json!!)

                        // Damage relations
                        val doubleDamageFrom = jsonObject.getJSONObject("damage_relations")
                            .getJSONArray("double_damage_from")
                        val doubleDamageTo = jsonObject.getJSONObject("damage_relations")
                            .getJSONArray("double_damage_to")
                        val halfDamageFrom = jsonObject.getJSONObject("damage_relations")
                            .getJSONArray("half_damage_from")
                        val halfDamageTo = jsonObject.getJSONObject("damage_relations")
                            .getJSONArray("half_damage_to")
                        val noDamageFrom = jsonObject.getJSONObject("damage_relations")
                            .getJSONArray("no_damage_from")
                        val noDamageTo = jsonObject.getJSONObject("damage_relations")
                            .getJSONArray("no_damage_to")

                        // Helper function to parse JSON arrays into lists
                        fun parseDamageRelations(array: JSONArray): List<String> {
                            return List(array.length()) { array.getJSONObject(it).getString("name") }
                        }

                        val doubleDamageFromList = parseDamageRelations(doubleDamageFrom)
                        val doubleDamageToList = parseDamageRelations(doubleDamageTo)
                        val halfDamageFromList = parseDamageRelations(halfDamageFrom)
                        val halfDamageToList = parseDamageRelations(halfDamageTo)
                        val noDamageFromList = parseDamageRelations(noDamageFrom)
                        val noDamageToList = parseDamageRelations(noDamageTo)

                        result = """
                ${query.capitalize()} Type Stats:
                Double Damage From: ${doubleDamageFromList.joinToString(", ")}
                Double Damage To: ${doubleDamageToList.joinToString(", ")}
                Half Damage From: ${halfDamageFromList.joinToString(", ")}
                Half Damage To: ${halfDamageToList.joinToString(", ")}
                No Damage From: ${noDamageFromList.joinToString(", ")}
                No Damage To: ${noDamageToList.joinToString(", ")}
            """.trimIndent()
                    }
                })
            }
            "Type Moves" -> {
                val request = Request.Builder()
                    .url("https://pokeapi.co/api/v2/type/$query")
                    .build()

                client.newCall(request).enqueue(object : okhttp3.Callback {
                    override fun onFailure(call: okhttp3.Call, e: IOException) {
                        result = ""
                        errorMessage = "Request failed: ${e.message}"
                    }

                    override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                        if (!response.isSuccessful) {
                            errorMessage = "Type not found!"
                            result = ""
                            return
                        }

                        val json = response.body?.string()
                        val jsonObject = JSONObject(json!!)
                        val moves = jsonObject.getJSONArray("moves")
                        val movesList = mutableListOf<String>()

                        for (i in 0 until moves.length()) {
                            movesList.add(moves.getJSONObject(i).getString("name"))
                        }

                        result = """
                ${query.capitalize()} Type Moves:
                ${movesList.joinToString("\n")}
            """.trimIndent()
                    }
                })
            }




            // Handle other options (Moveset, Stats, Type Stats, Type Moves) similarly...
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = input,
            onValueChange = { input = it },
            placeholder = { Text("Enter the name of a Pokémon or type") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { fetchData(input.text) }) {
            Text("Search")
        }
        Spacer(modifier = Modifier.height(16.dp))

        errorMessage?.let {
            Text(text = it, color = Color.Red, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(16.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = result,
                textAlign = TextAlign.Center,
                color = Color.Black,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (imageUrl.isNotEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(imageUrl),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(200.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { onBack() }) {
            Text("Back to Menu")
        }

    }
}


