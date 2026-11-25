package com.panalbit.panalsuite.data.local

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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

@Database(entities = [CachedTicket::class, EvidenceRow::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ticketDao(): TicketDao
    abstract fun evidenceDao(): EvidenceDao

    companion object {
        // Both released v1 layouts are supported without deleting local evidence.
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                fun exists(name: String): Boolean = db.query(
                    "SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?",
                    arrayOf<Any?>(name)
                ).use { it.moveToFirst() }

                db.execSQL("CREATE TABLE IF NOT EXISTS `cached_tickets` (`id` INTEGER NOT NULL, `requesterId` INTEGER NOT NULL, `json` TEXT NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `ticket_evidence` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `ticketId` INTEGER NOT NULL, `filePath` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)")
                if (exists("tickets")) {
                    db.execSQL("INSERT OR IGNORE INTO `cached_tickets` (`id`, `requesterId`, `json`) SELECT `id`, `requesterId`, `json` FROM `tickets`")
                    db.execSQL("DROP TABLE `tickets`")
                }
                if (exists("evidence")) {
                    db.execSQL("INSERT INTO `ticket_evidence` (`ticketId`, `filePath`, `createdAt`) SELECT `ticketId`, `filePath`, `createdAt` FROM `evidence`")
                    db.execSQL("DROP TABLE `evidence`")
                }
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_ticket_evidence_ticketId` ON `ticket_evidence` (`ticketId`)")
            }
        }

        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "panalsuite.db")
                .addMigrations(MIGRATION_1_2)
                .build()
    }
}
