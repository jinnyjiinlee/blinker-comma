package app.blinkercomma.ui.park

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.blinkercomma.R
import app.blinkercomma.ui.component.TallyNumber
import app.blinkercomma.ui.theme.CommaColors
import app.blinkercomma.ui.theme.CommaSpace
import app.blinkercomma.ui.theme.CommaType

/** 「오늘 N문장 · 쉼표 M번」(설계서 P 화면) — 실제 카운트 숫자 둘. 쉼표 수만 호박색(앱이 한 일). */
@Composable
fun TodayTally(sentences: Int, commas: Int, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(stringResource(R.string.tally_title), style = CommaType.bodyStrong, color = CommaColors.onSurface)
        Row(horizontalArrangement = Arrangement.spacedBy(CommaSpace.xlarge)) {
            TallyNumber(
                value = sentences,
                label = stringResource(R.string.tally_sentences),
                description = stringResource(R.string.tally_sentences_description, sentences),
            )
            TallyNumber(
                value = commas,
                label = stringResource(R.string.tally_commas),
                description = stringResource(R.string.tally_commas_description, commas),
                isAccent = true,
            )
        }
    }
}
