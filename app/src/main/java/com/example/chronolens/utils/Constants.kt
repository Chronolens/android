package com.example.chronolens.utils

object Prefs {
        const val SERVER = "SERVER"
        const val ACCESS_TOKEN = "ACCESS_TOKEN"
        const val REFRESH_TOKEN = "REFRESH_TOKEN"
        const val EXPIRES_AT = "EXPIRES_AT"
        const val LAST_SYNC = "LAST_SYNC"
        const val USERNAME = "USERNAME"
}

object Json {
        const val ACCESS_TOKEN = "access_token"
        const val REFRESH_TOKEN = "refresh_token"
        const val EXPIRES_AT = "expires_at"
        const val UPLOADED = "uploaded"
        const val DELETED = "deleted"
}

object Work {
        const val PERIODIC_BACKGROUND_UPLOAD_WORK_NAME = "PERIODIC_BACKGROUND_UPLOAD_WORK_NAME"
        const val ONE_TIME_BACKGROUND_UPLOAD_WORK_NAME = "ONE_TIME_BACKGROUND_UPLOAD_WORK_NAME"
}

object Notifications {
        const val SYNC_CHANNEL_ID = "SYNC_CHANNEL_ID"
        const val UPLOAD_CHANNEL_ID = "UPLOAD_CHANNEL_ID"
        const val FINISHED_CHANNEL_ID = "FINISHED_CHANNEL_ID"
        const val SYNC_PROGRESS = "Sync Progress"
        const val UPLOAD_PROGRESS = "Upload Progress"
        const val FINISHED = "Finished"
}