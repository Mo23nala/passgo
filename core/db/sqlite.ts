import Database from 'better-sqlite3';
import { IDatabaseDriver, EncryptedItem, VaultMetadata } from './index';
import * as path from 'path';

export class SQLiteDatabaseDriver implements IDatabaseDriver {
    private db: Database.Database;

    constructor(dbPath: string = ':memory:') {
        this.db = new Database(dbPath);
    }

    async init(): Promise<void> {
        this.db.exec(`
            CREATE TABLE IF NOT EXISTS vaults (
                id TEXT PRIMARY KEY,
                salt BLOB NOT NULL,
                encrypted_dek BLOB NOT NULL,
                created_at INTEGER NOT NULL
            );

            CREATE TABLE IF NOT EXISTS items (
                id TEXT PRIMARY KEY,
                vault_id TEXT NOT NULL,
                ciphertext BLOB NOT NULL,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                FOREIGN KEY(vault_id) REFERENCES vaults(id) ON DELETE CASCADE
            );

            CREATE INDEX IF NOT EXISTS idx_items_vault_id ON items(vault_id);
        `);
    }

    async saveVault(vault: VaultMetadata): Promise<void> {
        const stmt = this.db.prepare(`
            INSERT OR REPLACE INTO vaults (id, salt, encrypted_dek, created_at)
            VALUES (?, ?, ?, ?)
        `);
        stmt.run(vault.id, Buffer.from(vault.salt), Buffer.from(vault.encryptedDEK), vault.createdAt);
    }

    async getVault(id: string): Promise<VaultMetadata | null> {
        const stmt = this.db.prepare(`SELECT * FROM vaults WHERE id = ?`);
        const row = stmt.get(id) as any;
        if (!row) return null;

        return {
            id: row.id,
            salt: new Uint8Array(row.salt),
            encryptedDEK: new Uint8Array(row.encrypted_dek),
            createdAt: row.created_at
        };
    }

    async saveItem(item: EncryptedItem): Promise<void> {
        const stmt = this.db.prepare(`
            INSERT OR REPLACE INTO items (id, vault_id, ciphertext, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?)
        `);
        stmt.run(item.id, item.vaultId, Buffer.from(item.ciphertext), item.createdAt, item.updatedAt);
    }

    async getItem(id: string): Promise<EncryptedItem | null> {
        const stmt = this.db.prepare(`SELECT * FROM items WHERE id = ?`);
        const row = stmt.get(id) as any;
        if (!row) return null;

        return {
            id: row.id,
            vaultId: row.vault_id,
            ciphertext: new Uint8Array(row.ciphertext),
            createdAt: row.created_at,
            updatedAt: row.updated_at
        };
    }

    async getItemsByVault(vaultId: string): Promise<EncryptedItem[]> {
        const stmt = this.db.prepare(`SELECT * FROM items WHERE vault_id = ? ORDER BY updated_at DESC`);
        const rows = stmt.all(vaultId) as any[];

        return rows.map(row => ({
            id: row.id,
            vaultId: row.vault_id,
            ciphertext: new Uint8Array(row.ciphertext),
            createdAt: row.created_at,
            updatedAt: row.updated_at
        }));
    }

    async deleteItem(id: string): Promise<void> {
        const stmt = this.db.prepare(`DELETE FROM items WHERE id = ?`);
        stmt.run(id);
    }
}
