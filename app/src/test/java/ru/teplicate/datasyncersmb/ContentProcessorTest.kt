package ru.teplicate.datasyncersmb

import androidx.documentfile.provider.DocumentFile
import org.junit.Test
import java.util.*

class ContentProcessorTest {
    private val parentCount = 30

    @Test
    fun isAggregationCorrect() {
        val maxId = 200
        val totalFilesParentExcluded = maxId - maxId / parentCount
        val (max, files) = createTestFilesHierarchyRec(null, 0, maxId)
        val aggregatedFiles = aggregateRecursive(files)
        assert(totalFilesParentExcluded == aggregatedFiles.size)
    }

    private fun aggregateRecursive(files: List<DocumentFileTest>): List<DocumentFileTest> {
        val listData = LinkedList<DocumentFileTest>()
        val allFiles = files.fold(listData) { acc, file ->
            if (file.childes.isNotEmpty()) {
                acc.addAll(aggregateRecursive(file.childes))
                acc
            } else {
                acc.add(file)
                acc
            }
        }

        return allFiles
    }

    inner class DocumentFileTest(
        val id: Int,
        val parent: DocumentFileTest?,
        var childes: List<DocumentFileTest>,
    ) {
        fun addChildes(newChilds: List<DocumentFileTest>) {
            this.childes = newChilds
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as DocumentFileTest

            if (id != other.id) return false

            return true
        }

        override fun hashCode(): Int {
            return id
        }


    }

    fun createTestFilesHierarchyRec(
        parent: DocumentFileTest?,
        total: Int,
        maxId: Int
    ): Pair<Int, List<DocumentFileTest>> {
        val resultFileList = LinkedList<DocumentFileTest>()
        var localId: Int = total
        for (i in 1..100) {
            if (localId >= maxId) {
                return localId to resultFileList
            }
            localId++

            val file = DocumentFileTest(localId, parent, emptyList())
            resultFileList.add(file)


            if (i % parentCount == 0) {
                val (newId, childes) = createTestFilesHierarchyRec(file, localId, maxId)
                localId = newId
                file.addChildes(childes)
            }
        }

        return localId to resultFileList
    }

}