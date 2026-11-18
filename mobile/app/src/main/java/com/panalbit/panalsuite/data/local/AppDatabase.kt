package com.panalbit.panalsuite.data.local

import android.content.Context
import androidx.room.*

@Entity(tableName = "cached_tickets")
data class CachedTicket(@PrimaryKey val id: Long, val requesterId: Long, val json: String)

@Entity(tableName = "ticket_evidence", indices = [Index("ticketId")])
data class EvidenceRow(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ticketId: Long,
    val filePath: String,
    val createdAt: Long
)

@Dao
abstract class TicketDao {
    @Query("SELECT * FROM cached_tickets WHERE :requesterId IS NULL OR requesterId = :requesterId ORDER BY id DESC")
    abstract suspend fun list(requesterId: Long?): List<CachedTicket>

    @Query("SELECT * FROM cached_tickets WHERE id = :id")
    abstract suspend fun get(id: Long): CachedTicket?

    @Upsert abstract suspend fun save(ticket: CachedTicket)
    @Upsert abstract suspend fun saveAll(tickets: List<CachedTicket>)

    @Query("DELETE FROM cached_tickets WHERE id = :id")
    abstract suspend fun delete(id: Long)

    @Query("DELETE FROM cached_tickets WHERE :requesterId IS NULL OR requesterId = :requesterId")
    abstract suspend fun clear(requesterId: Long?)

    @Transaction
    open suspend fun replace(requesterId: Long?, tickets: List<CachedTicket>) {
        clear(requesterId)
        saveAll(tickets)
    }
}

@Dao
interface EvidenceDao {
    @Query("SELECT * FROM ticket_evidence WHERE ticketId = :ticketId ORDER BY createdAt DESC, id DESC")
    suspend fun list(ticketId: Long): List<EvidenceRow>

    @Insert suspend fun add(evidence: EvidenceRow): Long
}

@Database(entities = [CachedTicket::class, EvidenceRow::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ticketDao(): TicketDao
    abstract fun evidenceDao(): EvidenceDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "panalsuite.db").build()
    }
}
