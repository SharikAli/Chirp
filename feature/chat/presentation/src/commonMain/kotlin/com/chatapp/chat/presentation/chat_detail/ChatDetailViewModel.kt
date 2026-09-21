@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalUuidApi::class)

package com.chatapp.chat.presentation.chat_detail

import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chirp.feature.chat.presentation.generated.resources.Res
import chirp.feature.chat.presentation.generated.resources.chat_was_deleted
import chirp.feature.chat.presentation.generated.resources.image_save_failed
import chirp.feature.chat.presentation.generated.resources.image_saved_successfully
import chirp.feature.chat.presentation.generated.resources.multiple_people_typing
import chirp.feature.chat.presentation.generated.resources.several_people_typing
import chirp.feature.chat.presentation.generated.resources.someone_typing
import chirp.feature.chat.presentation.generated.resources.today
import com.chatapp.chat.domain.chat.ChatConnectionClient
import com.chatapp.chat.domain.chat.ChatRepository
import com.chatapp.chat.domain.message.MessageRepository
import com.chatapp.chat.domain.models.ChatMessage
import com.chatapp.chat.domain.models.ChatParticipant
import com.chatapp.chat.domain.models.ConnectionState
import com.chatapp.chat.domain.models.OutgoingNewMessage
import com.chatapp.chat.domain.models.OutgoingUserTyping
import com.chatapp.chat.presentation.chat_detail.attachment.compressImage
import com.chatapp.chat.presentation.chat_detail.attachment.saveImageToDevice
import com.chatapp.chat.presentation.chat_detail.model.PendingAttachment
import com.chatapp.chat.presentation.chat_detail.model.PendingAttachmentStatus
import com.chatapp.chat.presentation.chat_detail.model.StateWithMessagesInput
import com.chatapp.chat.presentation.mappers.toUi
import com.chatapp.chat.presentation.mappers.toUiList
import com.chatapp.chat.presentation.model.MessageUi
import com.chatapp.chat.presentation.profile.mediapicker.PickedImageData
import com.chatapp.core.domain.auth.SessionStorage
import com.chatapp.core.domain.util.DataErrorException
import com.chatapp.core.domain.util.Paginator
import com.chatapp.core.domain.util.Result
import com.chatapp.core.domain.util.onFailure
import com.chatapp.core.domain.util.onSuccess
import com.chatapp.core.presentation.util.UiText
import com.chatapp.core.presentation.util.toUiText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ChatDetailViewModel(
    private val chatRepository: ChatRepository,
    private val sessionStorage: SessionStorage,
    private val messageRepository: MessageRepository,
    private val connectionClient: ChatConnectionClient
): ViewModel() {

    companion object {
        const val MAX_IMAGE_ATTACHMENTS = 10
    }

    private val eventChannel = Channel<ChatDetailEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _chatId = MutableStateFlow<String?>(null)

    private var hasLoadedInitialData = false

    private val typingUsers = MutableStateFlow<Map<String, Long>>(emptyMap())

    private var typingJob: Job? = null
    private var isTyping = false

    private var currentPaginator: Paginator<String?, ChatMessage>? = null

    private val chatInfoFlow = _chatId
        .onEach { chatId ->
            if (chatId != null) {
                setupPaginatorForChat(chatId)
                loadNextItems()
            } else {
                currentPaginator = null
            }
        }
        .flatMapLatest { chatId ->
            if (chatId != null) {
                chatRepository.getChatInfoById(chatId)
            } else emptyFlow()
        }

    private val chatParticipants = _chatId
        .flatMapLatest { chatId ->
            if (chatId != null) {
                chatRepository.getActiveParticipantsByChatId(chatId)
            } else emptyFlow()
        }
        .distinctUntilChanged()

    private val chatAllKnownParticipants = _chatId
        .flatMapLatest { chatId ->
            if (chatId != null) {
                chatRepository.getAllKnownParticipantsByChatId(chatId)
            } else emptyFlow()
        }
        .distinctUntilChanged()

    private val _state = MutableStateFlow(ChatDetailState())

    private val canSendMessage = snapshotFlow { _state.value.messageTextFieldState.text.toString() }
        .combine(_state) { text, state ->
            text.isNotBlank() || state.pendingAttachments.isNotEmpty()
        }
        .combine(connectionClient.connectionState) { hasContent, connectionState ->
            hasContent && connectionState == ConnectionState.CONNECTED
        }


    private val stateWithMessages = combine(
        _state,
        chatInfoFlow,
        sessionStorage.observeAuthInfo(),
        typingUsers,
        chatParticipants,
    ) { currentState, chatInfo, authInfo, typingUsers, chatParticipants ->
        StateWithMessagesInput(currentState, chatInfo, authInfo, typingUsers, chatParticipants)
    }.combine(chatAllKnownParticipants) { input, allKnownParticipants ->
        val (currentState, chatInfo, authInfo, typingUsers, chatParticipants) = input

        if (authInfo == null) {
            return@combine ChatDetailState()
        }

        val typingText = createTypingText(
            typingUsers = typingUsers,
            chatParticipants = chatParticipants
        )

        currentState.copy(
            chatUi = chatInfo.chat.toUi(authInfo.user.id),
            messages = chatInfo.messages.toUiList(authInfo.user.id, allKnownParticipants),
            typingText = typingText
        )
    }

    val state = _chatId
        .flatMapLatest { chatId ->
            if (chatId != null) {
                stateWithMessages
            } else {
                _state
            }
        }
        .onStart {
            if (!hasLoadedInitialData) {
                observeConnectionState()
                observeChatMessages()
                observeCanSendMessage()
                observeTypingUsers()
                observeMessageTyping()
                observeChatDeletedRemotely()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ChatDetailState()
        )

    fun onAction(action: ChatDetailAction) {
        when (action) {
            is ChatDetailAction.OnSelectChat -> switchChat(action.chatId)
            ChatDetailAction.OnChatOptionsClick -> onChatOptionsClick()
            is ChatDetailAction.OnDeleteMessageClick -> deleteMessage(action.message)
            ChatDetailAction.OnDismissChatOptions -> onDismissChatOptions()
            ChatDetailAction.OnDismissMessageMenu -> onDismissMessageMenu()
            ChatDetailAction.OnLeaveChatClick -> onLeaveChatClick()
            ChatDetailAction.OnConfirmLeaveChat -> confirmLeaveChat()
            ChatDetailAction.OnDismissLeaveChatDialog -> dismissLeaveChatDialog()
            is ChatDetailAction.OnMessageLongClick -> onMessageLongClick(action.message)
            is ChatDetailAction.OnRetryClick -> retryMessage(action.message)
            ChatDetailAction.OnScrollToTop -> onScrollToTop()
            ChatDetailAction.OnSendMessageClick -> sendMessage()
            ChatDetailAction.OnRetryPaginationClick -> retryPagination()
            ChatDetailAction.OnHideBanner -> hideBanner()
            is ChatDetailAction.OnTopVisibleIndexChanged -> updateBanner(action.topVisibleIndex)
            is ChatDetailAction.OnFirstVisibleIndexChanged -> updateNearBottom(action.index)
            is ChatDetailAction.OnImagesPicked -> onImagesPicked(action.images)
            is ChatDetailAction.OnRemoveAttachment -> onRemoveAttachment(action.id)
            is ChatDetailAction.OnImageClick -> onImageClick(action.imageUrls, action.index)
            ChatDetailAction.OnDismissImageViewer -> onDismissImageViewer()
            is ChatDetailAction.OnSaveImageClick -> onSaveImageClick(action.url)
            else -> Unit
        }
    }

    private fun onImagesPicked(images: List<PickedImageData>) {
        val remainingSlots = MAX_IMAGE_ATTACHMENTS - _state.value.pendingAttachments.size
        if (remainingSlots <= 0) return

        images.take(remainingSlots).forEachIndexed { index, image ->
            val attachmentId = Uuid.random().toString()
            _state.update {
                it.copy(
                    pendingAttachments = it.pendingAttachments + PendingAttachment(
                        id = attachmentId,
                        fileName = "image_${it.pendingAttachments.size + index + 1}.jpg",
                        mimeType = image.mimeType ?: "image/jpeg",
                        status = PendingAttachmentStatus.PROCESSING
                    )
                )
            }

            viewModelScope.launch {
                val compressed = try {
                    compressImage(image.bytes)
                } catch (_: Exception) {
                    null
                }

                _state.update { state ->
                    state.copy(
                        pendingAttachments = state.pendingAttachments.map { attachment ->
                            if (attachment.id == attachmentId) {
                                attachment.copy(
                                    status = if (compressed != null) {
                                        PendingAttachmentStatus.READY
                                    } else {
                                        PendingAttachmentStatus.FAILED
                                    },
                                    compressedBytes = compressed
                                )
                            } else attachment
                        }
                    )
                }
            }
        }
    }

    private fun onRemoveAttachment(id: String) {
        _state.update {
            it.copy(pendingAttachments = it.pendingAttachments.filterNot { attachment -> attachment.id == id })
        }
    }

    private fun onImageClick(imageUrls: List<String>, index: Int) {
        _state.update {
            it.copy(
                viewerImageUrls = imageUrls,
                viewerStartIndex = index
            )
        }
    }

    private fun onDismissImageViewer() {
        _state.update {
            it.copy(viewerImageUrls = null)
        }
    }

    private fun onSaveImageClick(url: String) {
        viewModelScope.launch {
            val message = messageRepository
                .downloadImage(url)
                .let { result ->
                    when (result) {
                        is Result.Success -> {
                            val saved = try {
                                saveImageToDevice(result.data, "chirp_${Uuid.random()}.jpg")
                            } catch (_: Exception) {
                                false
                            }
                            if (saved) {
                                Res.string.image_saved_successfully
                            } else {
                                Res.string.image_save_failed
                            }
                        }

                        is Result.Failure -> Res.string.image_save_failed
                    }
                }

            eventChannel.send(ChatDetailEvent.OnImageSaved(UiText.Resource(message)))
        }
    }

    private fun updateNearBottom(firstVisibleIndex: Int) {
        _state.update {
            it.copy(
                isNearBottom = firstVisibleIndex <= 3
            )
        }
    }

    private fun updateBanner(topVisibleIndex: Int) {
        val visibleDate = calculateBannerDateFromIndex(
            messages = state.value.messages,
            index = topVisibleIndex
        )

        _state.update {
            it.copy(
                bannerState = BannerState(
                    formattedDate = visibleDate,
                    isVisible = visibleDate != null
                )
            )
        }
    }

    private fun calculateBannerDateFromIndex(
        messages: List<MessageUi>,
        index: Int
    ): UiText? {
        if (messages.isEmpty() || index < 0 || index >= messages.size) {
            return null
        }

        val nearestDateSeparator = (index until messages.size)
            .asSequence()
            .mapNotNull { index ->
                val item = messages.getOrNull(index)
                if (item is MessageUi.DateSeparator) item.date else null
            }
            .firstOrNull()

        return when (nearestDateSeparator) {
            is UiText.Resource -> {
                if (nearestDateSeparator.id == Res.string.today) null else nearestDateSeparator
            }

            else -> nearestDateSeparator
        }
    }

    private fun hideBanner() {
        _state.update {
            it.copy(
                bannerState = it.bannerState.copy(
                    isVisible = false
                )
            )
        }
    }

    private fun retryPagination() = loadNextItems()

    private fun onScrollToTop() = loadNextItems()

    private fun loadNextItems() {
        viewModelScope.launch {
            currentPaginator?.loadNextItems()
        }
    }

    private fun onDismissMessageMenu() {
        _state.update {
            it.copy(
                messageWithOpenMenu = null
            )
        }
    }

    private fun onMessageLongClick(message: MessageUi.LocalUserMessage) {
        _state.update {
            it.copy(
                messageWithOpenMenu = message
            )
        }
    }

    private fun deleteMessage(message: MessageUi.LocalUserMessage) {
        viewModelScope.launch {
            messageRepository
                .deleteMessage(message.id)
                .onFailure { error ->
                    eventChannel.send(ChatDetailEvent.OnError(error.toUiText()))
                }
        }
    }

    private fun retryMessage(message: MessageUi.LocalUserMessage) {
        viewModelScope.launch {
            messageRepository
                .retryMessage(message.id)
                .onFailure { error ->
                    eventChannel.send(ChatDetailEvent.OnError(error.toUiText()))
                }
        }
    }

    private fun createTypingText(
        typingUsers: Map<String, Long>,
        chatParticipants: List<ChatParticipant>
    ): UiText? {

        val now = Clock.System.now().toEpochMilliseconds()

        val activeUsers = typingUsers.keys
            .filter { userId ->
                now - (typingUsers[userId] ?: 0) < 3000
            }
            .mapNotNull { userId ->
                chatParticipants
                    .firstOrNull { it.userId == userId }
                    ?.username
            }

        if (activeUsers.isEmpty()) return null

        return when {
            activeUsers.size == 1 -> UiText.Resource(
                Res.string.someone_typing,
                arrayOf(activeUsers.first())
            )

            activeUsers.size <= 3 -> UiText.Resource(
                Res.string.multiple_people_typing,
                arrayOf(activeUsers.joinToString(", "))
            )

            else -> UiText.Resource(Res.string.several_people_typing)
        }
    }

    private fun sendMessage() {
        val currentChatId = _chatId.value
        val content = state.value.messageTextFieldState.text.toString().trim()
        val readyAttachments = state.value.pendingAttachments
            .filter { it.status == PendingAttachmentStatus.READY && it.compressedBytes != null }

        if ((content.isBlank() && readyAttachments.isEmpty()) || currentChatId == null) {
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSendingMessage = true) }

            val imageUrls = coroutineScope {
                readyAttachments
                    .map { attachment ->
                        async {
                            messageRepository
                                .uploadChatImage(
                                    chatId = currentChatId,
                                    imageBytes = attachment.compressedBytes!!,
                                    mimeType = attachment.mimeType
                                )
                                .let { result -> (result as? Result.Success)?.data }
                        }
                    }
                    .awaitAll()
                    .filterNotNull()
            }

            val message = OutgoingNewMessage(
                chatId = currentChatId,
                messageId = Uuid.random().toString(),
                content = content,
                imageUrls = imageUrls
            )

            messageRepository
                .sendMessage(message)
                .onSuccess {
                    stopTyping(currentChatId)

                    state.value.messageTextFieldState.clearText()
                    _state.update { it.copy(pendingAttachments = emptyList()) }
                }
                .onFailure { error ->
                    eventChannel.send(ChatDetailEvent.OnError(error.toUiText()))
                }

            _state.update { it.copy(isSendingMessage = false) }
        }
    }

    private fun observeCanSendMessage() {
        canSendMessage.onEach { canSend ->
            _state.update {
                it.copy(
                    canSendMessage = canSend
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun observeChatMessages() {
        val currentMessages = state
            .map { it.messages }
            .distinctUntilChanged()

        val newMessages = _chatId.flatMapLatest { chatId ->
            if (chatId != null) {
                messageRepository.getMessagesForChat(chatId)
            } else emptyFlow()
        }

        val isNearBottom = state.map { it.isNearBottom }.distinctUntilChanged()

        combine(
            currentMessages,
            newMessages,
            isNearBottom
        ) { currentMessages, newMessages, isNearBottom ->
            val newestMessageId = newMessages.firstOrNull()?.message?.id
            val currentNewestId = currentMessages
                .asSequence()
                .filterNot { it is MessageUi.DateSeparator }
                .firstOrNull()
                ?.id

            if (newestMessageId != null && newestMessageId != currentNewestId && isNearBottom) {
                eventChannel.send(ChatDetailEvent.OnNewMessage)
            }
        }.launchIn(viewModelScope)
    }

    private fun observeConnectionState() {
        connectionClient
            .connectionState
            .onEach { connectionState ->
                if (connectionState == ConnectionState.CONNECTED) {
                    currentPaginator?.loadNextItems()
                }

                _state.update {
                    it.copy(
                        connectionState = connectionState
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeTypingUsers() {
        connectionClient.typingEvents
            .combine(_chatId) { event, chatId ->
                if (event.chatId == chatId) {
                    event
                } else {
                    null
                }
            }
            .filterNotNull()
            .onEach { event ->
                typingUsers.update { current ->
                    if (event.isTyping) {
                        current + (event.userId to Clock.System.now().toEpochMilliseconds())
                    } else {
                        current - event.userId
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun startTyping(chatId: String) {
        if (typingJob != null) {
            return
        }

        typingJob = viewModelScope.launch {
            isTyping = true

            while (isActive) {
                messageRepository.sendTypingIndicator(
                    OutgoingUserTyping(
                        chatId = chatId,
                        isTyping = true
                    )
                )

                delay(2000.milliseconds)
            }
        }
    }

    private fun stopTyping(chatId: String) {
        typingJob?.cancel()
        typingJob = null

        if (!isTyping) {
            return
        }

        isTyping = false

//        viewModelScope.launch {
//            messageRepository.sendTypingIndicator(
//                OutgoingUserTyping(
//                    chatId = chatId,
//                    isTyping = false
//                )
//            )
//        }
    }

    private fun observeMessageTyping() {
        snapshotFlow { _state.value.messageTextFieldState.text.toString() }
            .distinctUntilChanged()
            .onEach { text ->
                val chatId = _chatId.value ?: return@onEach

                if (text.isBlank()) {
                    stopTyping(chatId)
                } else {
                    startTyping(chatId)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun setupPaginatorForChat(chatId: String) {
        currentPaginator = Paginator(
            initialKey = null,
            onLoadUpdated = { isLoading ->
                _state.update { it.copy(isPaginationLoading = isLoading) }
            },
            onRequest = { beforeTimestamp ->
                messageRepository.fetchMessages(chatId, beforeTimestamp)
            },
            getNextKey = { messages ->
                messages.minOfOrNull { it.createdAt }?.toString()
            },
            onError = { throwable ->
                if (throwable is DataErrorException) {
                    _state.update {
                        it.copy(
                            paginationError = throwable.error.toUiText()
                        )
                    }
                }
            },
            onSuccess = { messages, _ ->
                _state.update {
                    it.copy(
                        endReached = messages.isEmpty(),
                        paginationError = null
                    )
                }
            }
        )

        _state.update {
            it.copy(
                endReached = false,
                isPaginationLoading = false,
            )
        }
    }

    private fun onLeaveChatClick() {
        _state.update {
            it.copy(
                isChatOptionsOpen = false,
                showLeaveChatConfirmation = true
            )
        }
    }

    private fun dismissLeaveChatDialog() {
        _state.update {
            it.copy(
                showLeaveChatConfirmation = false
            )
        }
    }

    private fun confirmLeaveChat() {
        val chatId = _chatId.value ?: return

        stopTyping(chatId)

        _state.update {
            it.copy(
                showLeaveChatConfirmation = false
            )
        }

        viewModelScope.launch {
            chatRepository
                .leaveChat(chatId)
                .onSuccess {
                    _state.value.messageTextFieldState.clearText()

                    _chatId.update { null }
                    _state.update {
                        it.copy(
                            chatUi = null,
                            messages = emptyList(),
                            bannerState = BannerState()
                        )
                    }

                    eventChannel.send(ChatDetailEvent.OnChatLeft)
                }
                .onFailure { error ->
                    eventChannel.send(
                        ChatDetailEvent.OnError(
                            error.toUiText()
                        )
                    )
                }
        }
    }

    private fun observeChatDeletedRemotely() {
        connectionClient.chatDeletedEvents
            .combine(_chatId) { deletedChatId, currentChatId -> deletedChatId == currentChatId }
            .filter { it }
            .onEach { onChatGoneRemotely() }
            .launchIn(viewModelScope)

        _chatId
            .flatMapLatest { chatId ->
                if (chatId != null) {
                    // Track whether we've observed the chat existing at least once so that
                    // a brand new chat that hasn't synced into Room yet isn't mistaken for
                    // one that was deleted out from under us.
                    var hasSeenChatExist = false
                    chatRepository.observeChatExists(chatId)
                        .onEach { exists -> if (exists) hasSeenChatExist = true }
                        .filter { exists -> !exists && hasSeenChatExist }
                } else emptyFlow()
            }
            .onEach { onChatGoneRemotely() }
            .launchIn(viewModelScope)
    }

    private suspend fun onChatGoneRemotely() {
        val chatId = _chatId.value ?: return
        stopTyping(chatId)

        _chatId.update { null }
        _state.update {
            it.copy(
                chatUi = null,
                messages = emptyList(),
                bannerState = BannerState()
            )
        }

        eventChannel.send(
            ChatDetailEvent.OnChatDeletedRemotely(
                UiText.Resource(Res.string.chat_was_deleted)
            )
        )
    }

    private fun onDismissChatOptions() {
        _state.update {
            it.copy(
                isChatOptionsOpen = false
            )
        }
    }

    private fun onChatOptionsClick() {
        _state.update {
            it.copy(
                isChatOptionsOpen = true
            )
        }
    }

    private fun switchChat(chatId: String?) {
        _chatId.value?.let {
            stopTyping(it)
        }

        _chatId.update { chatId }
        viewModelScope.launch {
            chatId?.let {
                chatRepository.fetchChatById(chatId)
            }
        }
    }

}