package bassamalim.hidaya.features.radio

import bassamalim.hidaya.core.data.repositories.LiveContentRepository
import javax.inject.Inject

class RadioDomain @Inject constructor(
    private val liveContentRepository: LiveContentRepository
) {

    fun getUrl() = liveContentRepository.getRadioUrl()

}
