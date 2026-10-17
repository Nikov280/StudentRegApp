package com.example.studentregapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize Room Database
        val database = AppDatabase.getDatabase(this)
        val studentDao = database.studentDao()

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    StudentAppNavHost(studentDao = studentDao)
                }
            }
        }
    }
}

// Navigation Host to manage app screens
@Composable
fun StudentAppNavHost(studentDao: StudentDao) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        // Home Page Route
        composable("home") { HomePage(navController) }
        // Registration Page Route
        composable("registration") { RegistrationPage(navController, studentDao) }
        // Search Page Route
        composable("search") { SearchPage(navController, studentDao) }
    }
}

// Homepage
@Composable
fun HomePage(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("HOME PAGE", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        // Button to navigate to Registration Page
        Button(onClick = { navController.navigate("registration") }) {
            Text("ADD")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Button to navigate to Search Page
        Button(onClick = { navController.navigate("search") }) {
            Text("FIND STUDENT")
        }
    }
}

// Registration Page
@Composable
fun RegistrationPage(navController: NavController, studentDao: StudentDao) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // State variables for form fields
    var name by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("REGISTRATION PAGE", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))



        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") }
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = course,
            onValueChange = { course = it },
            label = { Text("Course") }
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = year,
            onValueChange = { year = it },
            label = { Text("Year (1-4)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val yearInt = year.toIntOrNull()

            // Validation: Check if fields are empty or year is invalid
            if (name.isBlank() || course.isBlank() || yearInt == null) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@Button
            }

            // Validation: Prevent invalid year inserted (1-4 years)
            if (yearInt !in 1..4) {
                Toast.makeText(context, "Year must be between 1 and 4", Toast.LENGTH_SHORT).show()
                return@Button
            }

            // Insert valid data into Room Database
            coroutineScope.launch {
                val newStudent = Student(name = name, course = course, year = yearInt)
                studentDao.insertStudent(newStudent)
                Toast.makeText(context, "Student Registered!", Toast.LENGTH_SHORT).show()
                navController.popBackStack() // Go back to Home
            }
        }) {
            Text("Register Student")
        }
    }
}

//Student Search Page
@Composable
fun SearchPage(navController: NavController, studentDao: StudentDao) {
    val coroutineScope = rememberCoroutineScope()

    // State variables
    var searchId by remember { mutableStateOf("") }
    var resultMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("STUDENT SEARCH PAGE", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchId,
            onValueChange = { searchId = it },
            label = { Text("Enter Student ID") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val id = searchId.toIntOrNull()
            if (id != null) {
                // Search the database
                coroutineScope.launch {
                    val student = studentDao.getStudentById(id)
                    // If student is found, display name and course. if not show error.
                    resultMessage = if (student != null) {
                        "Name: ${student.name}\nCourse: ${student.course}"
                    } else {
                        "Error: Student not found in the database."
                    }
                }
            } else {
                resultMessage = "Please enter a valid numeric ID."
            }
        }) {
            Text("Search")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Display result or error message
        if (resultMessage.isNotEmpty()) {
            Text(resultMessage, style = MaterialTheme.typography.bodyLarge)
        }
    }
}