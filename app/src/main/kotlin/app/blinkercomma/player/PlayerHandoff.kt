package app.blinkercomma.player

/**
 * 플레이어를 언제 놓나(release) — 순서만 정하고 실제 놓기는 [ClipDeck]이 한다. 까닭(D4 리허설 실측): 에뮬레이터에서
 * 놓기가 가끔 2.2~5.6초 막히는데, 그동안 다른 플레이어의 준비(prepare)도 같이 막히고 준비된 플레이어의 시작(start)은
 * 안 막혔다. 그래서 시계로 끝난 문장의 플레이어는 다음 문장 준비가 끝날 때까지 놓지 않는다. 멈춤은 소리를 바로 끊어야
 * 하므로(옛 플레이어엔 release 말고 아무것도 부르지 않는다) 기다리지 않고 다 놓는다.
 * 반환값 = 지금 놓을 플레이어들.
 */
internal class PlayerHandoff<T : Any> {
    private var playing: T? = null
    private var outgoing: T? = null

    val current: T? get() = playing

    /** 새 플레이어가 소리를 냄. 재생 중이던 것이 있으면(건너뛰기 등) 그것은 바로 놓는다. */
    fun start(player: T): List<T> = listOfNotNull(playing).also { playing = player }

    /** 시계로 문장이 끝남 — 놓기를 미룬다. 이미 미뤄 둔 것이 있으면 그것은 지금 놓는다. */
    fun finish(): List<T> = listOfNotNull(outgoing).also {
        outgoing = playing
        playing = null
    }

    /** 다음 문장 준비가 끝남 — 미뤄 둔 것을 이제 놓는다. */
    fun afterPreload(): List<T> = listOfNotNull(outgoing).also { outgoing = null }

    /** 멈춤 — 소리를 바로 끊는다. */
    fun stop(): List<T> = listOfNotNull(playing, outgoing).also {
        playing = null
        outgoing = null
    }
}
