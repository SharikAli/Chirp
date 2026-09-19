//
//  AppDelegate.swift
//  iosApp
//
//  Created by Sharik Ali on 02/07/2026.
//

import FirebaseCore
import FirebaseMessaging
import Foundation
import Shared
import UIKit
import UserNotifications

class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate, MessagingDelegate {
    // Must match PushNotificationCategory.NEW_MESSAGE on the backend (FirebasePushNotificationService
    // sets this as the APNs `aps.category`, which is what makes iOS attach the actions below).
    static let newMessageCategoryIdentifier = "NEW_MESSAGE"
    static let replyActionIdentifier = "REPLY_ACTION"

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        FirebaseApp.configure()

        UNUserNotificationCenter.current().delegate = self
        Messaging.messaging().delegate = self
        registerNotificationCategories()

        return true
    }

    func registerNotificationCategories() {
        let replyAction = UNTextInputNotificationAction(
            identifier: AppDelegate.replyActionIdentifier,
            title: "Reply",
            options: [],
            textInputButtonTitle: "Send",
            textInputPlaceholder: "Type a message…"
        )

        let newMessageCategory = UNNotificationCategory(
            identifier: AppDelegate.newMessageCategoryIdentifier,
            actions: [replyAction],
            intentIdentifiers: [],
            options: []
        )

        UNUserNotificationCenter.current().setNotificationCategories([newMessageCategory])
    }

    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        print("APNS token received")
        Messaging.messaging().apnsToken = deviceToken

        refreshToken()
    }

    func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
        print("iOS: Failed to register for push notifications: \(error.localizedDescription)")
    }

    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        print("didReceiveRegistrationToken \(fcmToken)")
        guard let token = fcmToken, !token.isEmpty else {
            refreshToken()
            return
        }

        UserDefaults.standard.set(token, forKey: "FCM_TOKEN")
        IosDeviceTokenHolderBridge.shared.updateToken(token: token)
    }

    func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable: Any], fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
        print("didReceiveRemoteNotification")
        Messaging.messaging().appDidReceiveMessage(userInfo)
        completionHandler(.newData)
    }

    func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification, withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        print("willPresent Notification")
        completionHandler([.banner])
    }

    func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse, withCompletionHandler completionHandler: @escaping () -> Void) {
        let userInfo = response.notification.request.content.userInfo

        guard let chatId = userInfo["chatId"] as? String else {
            completionHandler()
            return
        }

        if response.actionIdentifier == AppDelegate.replyActionIdentifier, let textResponse = response as? UNTextInputNotificationResponse {
            let replyText = textResponse.userText.trimmingCharacters(in: .whitespacesAndNewlines)

            if replyText.isEmpty {
                completionHandler()
                return
            }

            // No app UI is opened for this path - the send happens entirely in the
            // background, then the locally cached chat is updated so it's already there
            // next time the app is opened. See IosNotificationReplyBridge / MessageRepository.sendMessageViaRest.
            IosNotificationReplyBridge.shared.sendReply(chatId: chatId, content: replyText) { _ in
                completionHandler()
            }
            return
        }

        let deepLinkUrl = "http://chat_detail/\(chatId)"
        ExternalUriHandler.shared.onNewUri(uri: deepLinkUrl)
        completionHandler()
    }

    func refreshToken() {
        Task {
            do {
                let fcmToken = try await Messaging.messaging().token()
                print("FCM Token: \(fcmToken)")

                UserDefaults.standard.set(fcmToken, forKey: "FCM_TOKEN")
                IosDeviceTokenHolderBridge.shared.updateToken(token: fcmToken)
            } catch {
                print("iOS: Error getting FCM token: \(error.localizedDescription)")
            }
        }
    }
}
