package com.chatapp.chat.data.notification

import com.chatapp.chat.domain.notification.PushNotificationService
import com.chatapp.core.domain.logging.ChirpLogger
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.coroutineContext

actual class FirebasePushNotificationService(
    private val logger: ChirpLogger
) : PushNotificationService {

    actual override fun observeDeviceToken(): Flow<String?> = flow {
        try {
            val fcmToken = Firebase.messaging.token.await()
            logger.info("Initial FCM token received: $fcmToken")
            emit(fcmToken)
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            logger.error("Failed to get FCM token", e)
            emit(null)
        }
    }
}