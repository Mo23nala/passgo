import { createStore, StoreApi } from 'zustand/vanilla';

export interface VaultState {
    isUnlocked: boolean;
    vaultId: string | null;
    masterKey: Uint8Array | null;
    dek: Uint8Array | null;

    unlock: (vaultId: string, masterKey: Uint8Array, dek: Uint8Array) => void;
    lock: () => void;
}

export const createVaultStore = (): StoreApi<VaultState> => {
    return createStore<VaultState>((set) => ({
        isUnlocked: false,
        vaultId: null,
        masterKey: null,
        dek: null,

        unlock: (vaultId, masterKey, dek) => {
            // Note: Storing cryptographic keys in memory is necessary while unlocked.
            // When locking, we overwrite with zeros before releasing references.
            set({
                isUnlocked: true,
                vaultId,
                masterKey,
                dek,
            });
        },

        lock: () => {
            set((state) => {
                // Securely wipe memory (best effort in JS)
                if (state.masterKey) state.masterKey.fill(0);
                if (state.dek) state.dek.fill(0);

                return {
                    isUnlocked: false,
                    vaultId: null,
                    masterKey: null,
                    dek: null,
                };
            });
        }
    }));
};

// Expose a singleton store for simplicity in shared modules if needed,
// though React/UI should likely use a bound version.
export const vaultStore = createVaultStore();
