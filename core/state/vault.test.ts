import { describe, it, expect } from 'vitest';
import { createVaultStore } from './vault';

describe('core/state/vault', () => {
    it('should initialize locked', () => {
        const store = createVaultStore();
        const state = store.getState();

        expect(state.isUnlocked).toBe(false);
        expect(state.vaultId).toBeNull();
        expect(state.masterKey).toBeNull();
        expect(state.dek).toBeNull();
    });

    it('should unlock with keys and set state', () => {
        const store = createVaultStore();

        const vaultId = 'vault-123';
        const masterKey = new Uint8Array([1, 2, 3]);
        const dek = new Uint8Array([4, 5, 6]);

        store.getState().unlock(vaultId, masterKey, dek);

        const state = store.getState();
        expect(state.isUnlocked).toBe(true);
        expect(state.vaultId).toBe(vaultId);
        expect(state.masterKey).toEqual(masterKey);
        expect(state.dek).toEqual(dek);
    });

    it('should lock and attempt to wipe memory', () => {
        const store = createVaultStore();

        const vaultId = 'vault-123';
        const masterKey = new Uint8Array([1, 2, 3]);
        const dek = new Uint8Array([4, 5, 6]);

        store.getState().unlock(vaultId, masterKey, dek);

        // Ensure they are set before locking
        expect(store.getState().isUnlocked).toBe(true);

        store.getState().lock();

        const state = store.getState();
        expect(state.isUnlocked).toBe(false);
        expect(state.vaultId).toBeNull();
        expect(state.masterKey).toBeNull();
        expect(state.dek).toBeNull();

        // Ensure original arrays were zeroed out (wiped in place)
        expect(masterKey).toEqual(new Uint8Array([0, 0, 0]));
        expect(dek).toEqual(new Uint8Array([0, 0, 0]));
    });
});
