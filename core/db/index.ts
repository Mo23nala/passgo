// Platform-agnostic interfaces for database operations

export interface EncryptedItem {
    id: string;
    vaultId: string;
    ciphertext: Uint8Array;
    createdAt: number;
    updatedAt: number;
}

export interface VaultMetadata {
    id: string;
    salt: Uint8Array;
    encryptedDEK: Uint8Array;
    createdAt: number;
}

export interface IDatabaseDriver {
    init(): Promise<void>;

    // Vault
    saveVault(vault: VaultMetadata): Promise<void>;
    getVault(id: string): Promise<VaultMetadata | null>;

    // Items
    saveItem(item: EncryptedItem): Promise<void>;
    getItem(id: string): Promise<EncryptedItem | null>;
    getItemsByVault(vaultId: string): Promise<EncryptedItem[]>;
    deleteItem(id: string): Promise<void>;
}

// Memory driver for fallback
export class MemoryDatabaseDriver implements IDatabaseDriver {
    private vaults: Map<string, VaultMetadata> = new Map();
    private items: Map<string, EncryptedItem> = new Map();

    async init(): Promise<void> {
        // No-op for memory
    }

    async saveVault(vault: VaultMetadata): Promise<void> {
        this.vaults.set(vault.id, vault);
    }

    async getVault(id: string): Promise<VaultMetadata | null> {
        return this.vaults.get(id) || null;
    }

    async saveItem(item: EncryptedItem): Promise<void> {
        this.items.set(item.id, item);
    }

    async getItem(id: string): Promise<EncryptedItem | null> {
        return this.items.get(id) || null;
    }

    async getItemsByVault(vaultId: string): Promise<EncryptedItem[]> {
        return Array.from(this.items.values()).filter(i => i.vaultId === vaultId);
    }

    async deleteItem(id: string): Promise<void> {
        this.items.delete(id);
    }
}
