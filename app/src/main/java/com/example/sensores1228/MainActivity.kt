package com.example.sensores1228

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sensores1228.ui.theme.Sensores1228Theme
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    // Variables de estado para el sensor de aceleración y posición de la imagen
    private var ax by mutableStateOf(0f)
    private var ay by mutableStateOf(0f)
    private var az by mutableStateOf(0f)
    private var posX by mutableStateOf(0f)
    private var posY by mutableStateOf(0f)

    // Variables para manejar la ubicación y los colegios
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private lateinit var placesClient: PlacesClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar Places
        Places.initialize(applicationContext, "AIzaSyB_bYfobbvrxCbcvQ_IYz-i5s4BuPFuFEM")
        placesClient = Places.createClient(this)

        // Solicitar permisos de ubicación
        requestLocationPermission()

        // Configuración del sensor de acelerómetro
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        accelerometer?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
        }

        setContent {
            Sensores1228Theme {
                MyApp()
            }
        }
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    fun buscarColegios(input: String, callback: (List<AutocompletePrediction>) -> Unit) {
        Log.d("PlaceSearch", "Buscando colegios con la consulta: $input")
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(input)
            .setLocationBias(
                RectangularBounds.newInstance(
                    LatLng(-12.046374, -77.042793),  // Coordenadas del suroeste
                    LatLng(-12.021208, -77.005556)   // Coordenadas del noreste
                )
            )
            .build()

        placesClient.findAutocompletePredictions(request).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val response = task.result
                val predictions = response.autocompletePredictions
                Log.d("PlaceSearch", "Predicciones encontradas: ${predictions.size}")
                callback(predictions) // Devuelve los resultados a través del callback
            } else {
                Log.e("Place", "Error al buscar colegios: ${task.exception?.message}")
                callback(emptyList()) // Devuelve una lista vacía en caso de error
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        // Actualizar valores de aceleración con los valores proporcionados por el sensor
        val ax = event.values[0]
        val ay = event.values[1]
        val az = event.values[2]

        // Filtro de paso bajo para reducir el ruido
        val alpha = 0.8f
        posX = alpha * posX + (1 - alpha) * ax
        posY = alpha * posY + (1 - alpha) * ay

        // Actualizar la posición en función de la aceleración
        posX += ax * 0.1f  // El multiplicador ajusta la sensibilidad
        posY += ay * 0.1f

        // Opcional: registra los valores para monitoreo
       // Log.d("SensorData", "Ax: $ax, Ay: $ay, Az: $az")
        //Log.d("Position", "PosX: $posX, PosY: $posY")
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No es necesario manejar cambios de precisión en este caso
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }
}

@Composable
fun MyApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "configuracion") {
        composable("configuracion") { ConfiguracionScreen(navController) }
        composable("monitoreo")
        { MonitoreoScreen(posX = 0f, posY = 0f, ax = 0f, ay = 0f, az = 0f) }
    }
}

@Composable
fun ConfiguracionScreen(navController: NavHostController) {
    var colegioSeleccionado by remember { mutableStateOf("") }
    var horaInicioMonitoreo by remember { mutableStateOf("7:00 AM") }
    var horaIngresoColegio by remember { mutableStateOf("7:45 AM") }
    var distanciaRangoPermitido by remember { mutableFloatStateOf(100f) }
    var colegiosEncontrados by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current // Obtenemos el contexto aquí

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Pantalla de Configuración", style = TextStyle(fontSize = 24.sp))

        Spacer(modifier = Modifier.height(16.dp))

        // Selector de Colegio
        Text("Seleccione su Colegio:")
        TextField(
            value = colegioSeleccionado,
            onValueChange = {
                colegioSeleccionado = it
                if (colegioSeleccionado.isNotBlank()) {
                    // Llama a la función de búsqueda de colegios
                    coroutineScope.launch {
                        (context as MainActivity).buscarColegios(colegioSeleccionado) { predictions ->
                            colegiosEncontrados = predictions // Actualiza el estado con los resultados
                        }
                    }
                } else {
                    colegiosEncontrados = emptyList() // Limpia la lista si el campo está vacío
                }
            },
            placeholder = { Text("Ingrese el nombre de su colegio") },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Muestra los colegios encontrados
        if (colegiosEncontrados.isNotEmpty()) {
            Text("Colegios encontrados:")
            colegiosEncontrados.forEach { prediction ->
                Text(prediction.getFullText(null).toString())
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selector de Hora de Inicio de Monitoreo
        Text("Hora de Inicio de Monitoreo:")
        TextField(
            value = horaInicioMonitoreo,
            onValueChange = { horaInicioMonitoreo = it },
            placeholder = { Text("7:00 AM") },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Selector de Hora de Ingreso al Colegio
        Text("Hora de Ingreso al Colegio:")
        TextField(
            value = horaIngresoColegio,
            onValueChange = { horaIngresoColegio = it },
            placeholder = { Text("7:45 AM") },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Configuración de Distancia de Rango Permitido
        Text("Distancia de Rango Permitido (m):")
        Slider(
            value = distanciaRangoPermitido,
            onValueChange = { distanciaRangoPermitido = it },
            valueRange = 50f..500f,
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )
        Text("${distanciaRangoPermitido.toInt()} metros")

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para Guardar Configuración
        Button(
            onClick = {
                navController.navigate("monitoreo")
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Guardar Configuración")
        }
    }
}


@Composable
fun MonitoreoScreen(posX: Float, posY: Float, ax: Float, ay: Float, az: Float) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Mostrar valores de aceleración
            Text("Aceleración en X: $ax", style = TextStyle(fontSize = 18.sp))
            Text("Aceleración en Y: $ay", style = TextStyle(fontSize = 18.sp))
            Text("Aceleración en Z: $az", style = TextStyle(fontSize = 18.sp))
        }

        // Imagen que se moverá en función de los valores de aceleración
        Image(
            painter = painterResource(id = R.drawable.pelota),
            contentDescription = null,
            modifier = Modifier
                .offset(x = posX.dp, y = posY.dp)
                .align(Alignment.Center)
        )
    }
}
