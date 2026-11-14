package app.blinkercomma

import android.content.Context
import app.blinkercomma.data.LoadedPack
import app.blinkercomma.data.PackRepository
import app.blinkercomma.data.PositionStore
import app.blinkercomma.data.ReaderSettings
import app.blinkercomma.data.SpeedPermissionStore
import app.blinkercomma.data.TallyStore
import app.blinkercomma.engine.comma.CommaCardParams
import app.blinkercomma.engine.reader.ReaderState
import app.blinkercomma.session.DemoInjectionMarks
import app.blinkercomma.session.ReaderSession

/** 수동 DI — 앱 전체에서 하나씩만. 팩(JSON 수 KB)·오늘 카운트·이어 듣기 위치는 작아서 앱 시작 때 바로 읽는다. */
class AppContainer(context: Context) {
    val pack: LoadedPack = PackRepository(context.assets).loadPack()
    val tallyStore = TallyStore(context)
    val positionStore = PositionStore(context, packKey = "${(pack as? LoadedPack.Ready)?.folder}/${pack.clips.size}")
    val settings = ReaderSettings(context)
    val injection = DemoInjectionMarks()
    val speedPermission = SpeedPermissionStore(context)
    val session = ReaderSession(
        initial = ReaderState(
            clipCount = pack.clips.size,
            index = positionStore.load(pack.clips.size),
            tally = tallyStore.loadToday(),
        ),
        params = { CommaCardParams.DEFAULT.copy(isAutoResume = settings.isAutoResume.value) },
        now = ::monotonicNow,
    )
}
