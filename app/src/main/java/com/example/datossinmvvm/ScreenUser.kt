package com.example.datossinmvvm

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenUser() {
    val context = LocalContext.current
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    val dataUser = remember { mutableStateOf("") }
    val successMessage = remember { mutableStateOf("") }  // Mensaje de éxito o error

    val db: UserDatabase = crearDatabase(context)
    val dao = db.userDao()
    val coroutineScope = rememberCoroutineScope()

    // Usar Scaffold con TopAppBar
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Management") },
                actions = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            val data = getUsers(dao)
                            dataUser.value = data
                        }
                    }) {
                        Icon(imageVector = Icons.Filled.List, contentDescription = "Listar Usuarios")
                    }
                    IconButton(onClick = {
                        coroutineScope.launch {
                            try {
                                dao.deleteLastUser()  // Eliminar el último usuario
                                successMessage.value = "Último usuario eliminado"
                            } catch (e: Exception) {
                                Log.e("User", "Error al eliminar: ${e.message}")
                                successMessage.value = "Error al eliminar el usuario"
                            }
                        }
                    }) {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = "Eliminar Último")
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color(0xFF6200EE),  // Color del TopAppBar
                    titleContentColor = Color.White  // Color del título
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues)  // Para evitar superposición con la barra superior
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            // TextField para ingresar nombres
            TextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("First Name") },
                singleLine = true
            )

            TextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Last Name") },
                singleLine = true
            )

            // Botón para agregar usuario
            Button(
                onClick = {
                    if (firstName.isNotBlank() && lastName.isNotBlank()) {
                        val user = User(0, firstName, lastName)
                        coroutineScope.launch {
                            AgregarUsuario(user, dao)
                            successMessage.value = "Usuario agregado: $firstName $lastName"
                        }
                        firstName = ""
                        lastName = ""
                    } else {
                        successMessage.value = "Por favor, completa todos los campos."
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))  // Color del botón
            ) {
                Text("Agregar Usuario", color = Color.White)  // Color del texto del botón
            }

            // Mensaje de éxito o error
            Text(text = successMessage.value, fontSize = 16.sp)

            // TextView que muestra los usuarios
            Text(text = dataUser.value, fontSize = 20.sp)
        }
    }
}

// Función para crear la base de datos
fun crearDatabase(context: Context): UserDatabase {
    return Room.databaseBuilder(
        context,
        UserDatabase::class.java,
        "user_db"
    ).build()
}

// Suspended functions para obtener y agregar usuarios
suspend fun getUsers(dao: UserDao): String {
    val users = dao.getAll()
    return users.joinToString(separator = "\n") { "${it.firstName} - ${it.lastName}" }
}

suspend fun AgregarUsuario(user: User, dao: UserDao) {
    try {
        dao.insert(user)
    } catch (e: Exception) {
        Log.e("User", "Error al insertar: ${e.message}")
    }
}

// Añadir Preview
@Preview(showBackground = true)
@Composable
fun PreviewScreenUser() {
    ScreenUser()
}
