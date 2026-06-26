package com.factlens.history

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import com.factlens.model.ScanHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM ScanHistory ORDER BY timestamp DESC")
    fun getAll(): Flow<List<ScanHistory>>

    @Query("SELECT * FROM ScanHistory WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavorites(): Flow<List<ScanHistory>>

    @Query("SELECT * FROM ScanHistory WHERE id = :id")
    suspend fun getById(id: Long): ScanHistory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(scan: ScanHistory): Long

    @Update
    suspend fun update(scan: ScanHistory)

    @Delete
    suspend fun delete(scan: ScanHistory)

    @Query("DELETE FROM ScanHistory")
    suspend fun deleteAll()

    @Query("UPDATE ScanHistory SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: Long, isFavorite: Boolean)
}

@Database(entities = [ScanHistory::class], version = 1, exportSchema = false)
abstract class HistoryDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: HistoryDatabase? = null

        fun getInstance(context: Context): HistoryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HistoryDatabase::class.java,
                    "factlens_history"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
