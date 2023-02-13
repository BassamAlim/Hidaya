package bassamalim.hidaya.data.database.dbs

class TelawatDB(
    reciter_id: Int, version_id: Int, reciter_name: String,
    rewaya: String, url: String, count: Int, suras: String
) {
    private var reciter_id = 0
    private var version_id = 0
    private var reciter_name: String
    private var rewaya: String
    private var url: String
    private var count = 0
    private var suras: String

    init {
        this.reciter_id = reciter_id
        this.version_id = version_id
        this.reciter_name = reciter_name
        this.rewaya = rewaya
        this.url = url
        this.count = count
        this.suras = suras
    }

    fun setReciterId(reciter_id: Int) {
        this.reciter_id = reciter_id
    }

    fun setVersionId(version_id: Int) {
        this.version_id = version_id
    }

    fun setReciterName(reciter_name: String) {
        this.reciter_name = reciter_name
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
        return reciter_id
    }

    fun getVersionId(): Int {
        return version_id
    }

    fun getReciterName(): String {
        return reciter_name
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