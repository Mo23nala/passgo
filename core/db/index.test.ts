import { describe, it, expect, beforeEach } from 'vitest';
import { MemoryDatabaseDriver, VaultMetadata, EncryptedItem } from './index';

describe('core/db', () => {
    let db: MemoryDatabaseDriver;

    beforeEach(() => {
        db = new MemoryDatabaseDriver();
    });

    it('should save and retrieve a vault', async () => {
        const vault: VaultMetadata = {
            id: 'vault-1',
            salt: new Uint8Array([1, 2, 3]),
            encryptedDEK: new Uint8Array([4, 5, 6]),
            createdAt: Date.now()
        };

        await db.saveVault(vault);
        const retrieved = await db.getVault('vault-1');

        expect(retrieved).not.toBeNull();
        expect(retrieved?.id).toBe('vault-1');
        expect(retrieved?.salt).toEqual(vault.salt);
    });

    it('should return null for non-existent vault', async () => {
        const retrieved = await db.getVault('non-existent');
        expect(retrieved).toBeNull();
    });

    it('should save, retrieve, and delete an item', async () => {
        const item: EncryptedItem = {
            id: 'item-1',
            vaultId: 'vault-1',
            ciphertext: new Uint8Array([7, 8, 9]),
            createdAt: Date.now(),
            updatedAt: Date.now()
        };

        await db.saveItem(item);

        let retrieved = await db.getItem('item-1');
        expect(retrieved).not.toBeNull();
        expect(retrieved?.ciphertext).toEqual(item.ciphertext);

        await db.deleteItem('item-1');
        retrieved = await db.getItem('item-1');
        expect(retrieved).toBeNull();
    });

    it('should retrieve multiple items by vault', async () => {
        const item1: EncryptedItem = {
            id: 'item-1',
            vaultId: 'vault-1',
            ciphertext: new Uint8Array([1]),
            createdAt: Date.now(),
            updatedAt: Date.now()
        };
        const item2: EncryptedItem = {
            id: 'item-2',
            vaultId: 'vault-1',
            ciphertext: new Uint8Array([2]),
            createdAt: Date.now(),
            updatedAt: Date.now()
        };
        const item3: EncryptedItem = {
            id: 'item-3',
            vaultId: 'vault-2',
            ciphertext: new Uint8Array([3]),
            createdAt: Date.now(),
            updatedAt: Date.now()
        };

        await db.saveItem(item1);
        await db.saveItem(item2);
        await db.saveItem(item3);

        const vault1Items = await db.getItemsByVault('vault-1');
        expect(vault1Items).toHaveLength(2);
        expect(vault1Items.map(i => i.id)).toContain('item-1');
        expect(vault1Items.map(i => i.id)).toContain('item-2');
    });
});
