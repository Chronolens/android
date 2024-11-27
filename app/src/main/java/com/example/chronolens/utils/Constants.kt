package com.example.chronolens.utils

object Prefs {
        const val SERVER = "SERVER"
        const val ACCESS_TOKEN = "ACCESS_TOKEN"
        const val REFRESH_TOKEN = "REFRESH_TOKEN"
        const val EXPIRES_AT = "EXPIRES_AT"
        const val LAST_SYNC = "LAST_SYNC"
        const val USERNAME = "USERNAME"
        const val ALBUMS = "ALBUMS"
        const val ALBUMS_ASK = "ALBUMS_ASK"
}

object Json {
        const val ACCESS_TOKEN = "access_token"
        const val REFRESH_TOKEN = "refresh_token"
        const val EXPIRES_AT = "expires_at"
        const val UPLOADED = "uploaded"
        const val DELETED = "deleted"

        const val ID = "id"
        const val HASH = "hash"
        const val CREATED_AT = "created_at"

        const val USERNAME = "username"
        const val PASSWORD = "password"

        const val KNOWN_PEOPLE = "knownPeople"
        const val UNKNOWN_PEOPLE = "unknownPeople"

        const val MEDIA_URL = "media_url"
        const val PREVIEW_URL = "preview_url"

}

object Work {
        const val PERIODIC_BACKGROUND_UPLOAD_WORK_NAME = "PERIODIC_BACKGROUND_UPLOAD_WORK_NAME"
        const val ONE_TIME_BACKGROUND_UPLOAD_WORK_NAME = "ONE_TIME_BACKGROUND_UPLOAD_WORK_NAME"
}
