package bassamalim.hidaya.database.dbs

class TelawatDB(reciter_id: Int, version_id: Int, reciter_name: String, rewaya: String,
                url: String, count: Int, suras: String) {
    private var reciterId = 0
    private var versionId = 0
    private var reciterName: String
    private var rewaya: String
    private var url: String
    private var count = 0
    private var suras: String

    init {
        this.reciterId = reciter_id
        this.versionId = version_id
        this.reciterName = reciter_name
        this.rewaya = rewaya
        this.url = url
        this.count = count
        this.suras = suras
    }

    fun setReciterId(reciter_id: Int) {
        this.reciterId = reciter_id
    }

    fun setVersionId(version_id: Int) {
        this.versionId = version_id
    }

    fun setReciterName(reciter_name: String) {
        this.reciterName = reciter_name
    }

    fun setRewaya(rewaya: String) {
        this.rewaya = rewaya
    }

    fun setUrl(url: String) {
        this.url = url
    }

    fun setCount(count: Int) {
        this.count = count
    }

    fun setSuras(suras: String) {
        this.suras = suras
    }

    fun getReciterId(): Int {
        return reciterId
    }

    fun getVersionId(): Int {
        return versionId
    }

    fun getReciterName(): String {
        return reciterName
    }

    fun getRewaya(): String {
        return rewaya
    }

    fun getUrl(): String {
        return url
    }

    fun getCount(): Int {
        return count
    }

    fun getSuras(): String {
        return suras
    }
}