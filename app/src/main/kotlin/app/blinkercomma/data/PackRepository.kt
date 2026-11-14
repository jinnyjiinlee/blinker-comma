package app.blinkercomma.data

import android.content.res.AssetManager
import android.util.Log
import app.blinkercomma.engine.pack.PackFile
import app.blinkercomma.engine.pack.SentenceClip
import app.blinkercomma.engine.pack.SentencePack
import app.blinkercomma.engine.pack.classifyPackProblems
import app.blinkercomma.engine.pack.toPackFile

/** 에셋 팩 읽기 결과. 깨진 팩은 예상되는 실패라 결과로 — 그땐 클립 0개(재생 눌러도 소리 없음, 로그에 이유). */
sealed interface LoadedPack {
    val clips: List<SentenceClip>

    data class Ready(val pack: SentencePack, val folder: String) : LoadedPack {
        override val clips: List<SentenceClip> get() = pack.clips
    }

    data class Broken(val reason: String) : LoadedPack {
        override val clips: List<SentenceClip> get() = emptyList()
    }
}

class PackRepository(private val assets: AssetManager) {
    fun loadPack(folder: String = DEFAULT_FOLDER): LoadedPack {
        val text = assets.open("$folder/$PACK_FILE").bufferedReader().use { it.readText() }
        val loaded = when (val file = toPackFile(text)) {
            is PackFile.Unreadable -> LoadedPack.Broken(file.detail)
            is PackFile.Parsed -> classifyPackProblems(file.pack).let { problems ->
                if (problems.isEmpty()) LoadedPack.Ready(file.pack, folder) else LoadedPack.Broken("$problems")
            }
        }
        if (loaded is LoadedPack.Broken) Log.e(TAG, "pack $folder broken: ${loaded.reason}")
        return loaded
    }

    private companion object {
        const val DEFAULT_FOLDER = "packs/aesop"
        const val PACK_FILE = "pack.json"
        const val TAG = "CommaPack"
    }
}
