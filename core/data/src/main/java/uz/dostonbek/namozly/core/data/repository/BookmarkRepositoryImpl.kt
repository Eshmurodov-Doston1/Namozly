package uz.dostonbek.namozly.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uz.dostonbek.namozly.core.datastore.BookmarkDataSource
import uz.dostonbek.namozly.core.domain.repository.BookmarkRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkRepositoryImpl @Inject constructor(private val source: BookmarkDataSource) : BookmarkRepository {
    override fun observe(): Flow<List<Pair<Int, Int>>> = source.entries.map { set ->
        set.mapNotNull { e ->
            val s = e.substringBefore(':').toIntOrNull() ?: return@mapNotNull null
            val a = e.substringAfter(':').toIntOrNull() ?: return@mapNotNull null
            s to a
        }.sortedWith(compareBy({ it.first }, { it.second }))
    }

    override suspend fun toggle(surah: Int, ayah: Int) = source.toggle("$surah:$ayah")
}
