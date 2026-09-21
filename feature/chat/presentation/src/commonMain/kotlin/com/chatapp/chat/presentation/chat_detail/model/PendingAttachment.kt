package com.chatapp.chat.presentation.chat_detail.model

enum class PendingAttachmentStatus {
    PROCESSING,
    READY,
    FAILED
}

data class PendingAttachment(
    val id: String,
    val fileName: String,
    val mimeType: String,
    val status: PendingAttachmentStatus,
    val compressedBytes: ByteArray? = null
) {
    // compressedBytes is compared by reference rather than content: it can be several MB and
    // Compose re-checks state equality on every emission, so contentEquals would make every
    // recomposition O(size). The array is never mutated in place - it's only ever assigned
    // once, together with the READY status - so a different instance always means different
    // content, which makes reference equality both correct and O(1) here.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PendingAttachment) return false

        return id == other.id
                && fileName == other.fileName
                && mimeType == other.mimeType
                && status == other.status
                && compressedBytes === other.compressedBytes
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + fileName.hashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + status.hashCode()
        result = 31 * result + (compressedBytes?.contentHashCode() ?: 0)
        return result
    }
}
