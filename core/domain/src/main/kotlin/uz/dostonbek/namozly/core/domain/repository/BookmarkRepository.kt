package uz.dostonbek.namozly.core.domain.repository

import kotlinx.coroutines.flow.Flow

/** Saqlangan oyatlar: (sura, oyat). */
interface BookmarkRepository {
    fun observe(): Flow<List<Pair<Int, Int>>>
    suspend fun toggle(surah: Int, ayah: Int)
}
