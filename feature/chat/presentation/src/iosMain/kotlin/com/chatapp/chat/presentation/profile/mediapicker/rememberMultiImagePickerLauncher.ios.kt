@file:OptIn(ExperimentalForeignApi::class)

package com.chatapp.chat.presentation.profile.mediapicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerConfigurationSelectionOrdered
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UniformTypeIdentifiers.UTType
import platform.darwin.NSObject
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_group_create
import platform.darwin.dispatch_group_enter
import platform.darwin.dispatch_group_leave
import platform.darwin.dispatch_group_notify
import platform.posix.memcpy

@Composable
actual fun rememberMultiImagePickerLauncher(
    maxItems: Int,
    onResult: (List<PickedImageData>) -> Unit
): ImagePickerLauncher {
    val scope = rememberCoroutineScope()
    val delegate = remember {
        MultiImagePickerDelegate(scope, onResult)
    }

    return remember {
        val pickerViewController = PHPickerViewController(
            configuration = PHPickerConfiguration().apply {
                setSelectionLimit(maxItems.toLong())
                setFilter(PHPickerFilter.imagesFilter)
                setSelection(PHPickerConfigurationSelectionOrdered)
            }
        )
        pickerViewController.delegate = delegate

        ImagePickerLauncher(
            onLaunch = {
                UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
                    pickerViewController,
                    true,
                    null
                )
            }
        )
    }
}

private class MultiImagePickerDelegate(
    private val scope: CoroutineScope,
    private val onResult: (List<PickedImageData>) -> Unit
) : NSObject(), PHPickerViewControllerDelegateProtocol {

    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        picker.dismissViewControllerAnimated(true, null)

        val results = didFinishPicking.filterIsInstance<PHPickerResult>()
        if (results.isEmpty()) {
            onResult(emptyList())
            return
        }

        val dispatchGroup = dispatch_group_create()
        // Indexed so the picked order is preserved even though items load asynchronously
        // and out of order.
        val imageDataByIndex = arrayOfNulls<PickedImageData>(results.size)

        results.forEachIndexed { index, result ->
            dispatch_group_enter(dispatchGroup)

            val itemProvider = result.itemProvider
            val primaryType = itemProvider.registeredTypeIdentifiers.firstOrNull() as? String
            val mimeType = primaryType?.let { UTType.typeWithIdentifier(it)?.preferredMIMEType }

            if (primaryType == null || mimeType == null) {
                dispatch_group_leave(dispatchGroup)
                return@forEachIndexed
            }

            itemProvider.loadDataRepresentationForTypeIdentifier(
                typeIdentifier = primaryType
            ) { nsData, _ ->
                if (nsData != null) {
                    val bytes = ByteArray(nsData.length.toInt())
                    memcpy(bytes.refTo(0), nsData.bytes, nsData.length)
                    imageDataByIndex[index] = PickedImageData(bytes = bytes, mimeType = mimeType)
                }
                dispatch_group_leave(dispatchGroup)
            }
        }

        dispatch_group_notify(dispatchGroup, dispatch_get_main_queue()) {
            scope.launch {
                onResult(imageDataByIndex.filterNotNull())
            }
        }
    }
}
