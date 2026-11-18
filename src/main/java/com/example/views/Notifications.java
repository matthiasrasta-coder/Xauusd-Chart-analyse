package com.example.views;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

/** Hilfsmethoden für Meldungen. */
final class Notifications {

    private Notifications() {
    }

    static void showError(String message) {
        Notification notification = Notification.show(message, 10_000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.ERROR);
    }
}
