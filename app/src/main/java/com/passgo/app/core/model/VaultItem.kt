package com.passgo.app.core.model

data class VaultItem(
    val id: String,
    val vaultId: String,
    val folderId: String? = null,
    val type: ItemType,
    val name: String,
    val username: String = "",
    val password: String = "",
    val url: String = "",
    val notes: String = "",
    val favorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null,
    val syncVersion: Int = 0,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)

enum class ItemType(val displayName: String) {
    LOGIN("Login"),
    NOTE("Secure Note"),
    CREDIT_CARD("Credit Card"),
    IDENTITY("Identity"),
    WIFI("Wi-Fi"),
    LICENSE("Software License"),
    TOTP("Authenticator"),
    CUSTOM("Custom")
}
