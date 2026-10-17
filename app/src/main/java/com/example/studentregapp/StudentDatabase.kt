package com.example.studentregapp

import android.content.Context
import androidx.room.*

//Entity representing the student table in the database
@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true) val studentId: Int = 0,
    val name: String,
    val course: String,
    val year: Int
)

//Data Access Object for database operations
@Dao
interface StudentDao{
    //Insert a new student into database
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertStudent(student: Student)

    //Search  for a student by their ID
    @Query("SELECT * FROM students WHERE studentID = :id")
    suspend fun getStudentById(id:Int): Student?
}

//Main databse class
@Database(entities = [Student::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase(){
    abstract fun studentDao(): StudentDao

    companion object{
        @Volatile
        private var INSTANCE: AppDatabase? = null

        //Get database instance (Singleton)
        fun getDatabase(context: Context): AppDatabase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "student_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}