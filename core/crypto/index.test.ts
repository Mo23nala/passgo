import { describe, it, expect, beforeAll } from 'vitest';
import {
    initCrypto,
    deriveMasterKey,
    generateDEK,
    generateSalt,
    encryptData,
    decryptData,
    encodeString,
    decodeString,
} from './index';
import crypto from 'crypto';

// Polyfill WebCrypto for Node environment tests if necessary
if (!globalThis.crypto) {
    // @ts-ignore
    globalThis.crypto = crypto.webcrypto;
}

describe('core/crypto', () => {
    beforeAll(async () => {
        await initCrypto();
    });

    describe('Key Derivation (Argon2id)', () => {
        it('should derive a consistent 32-byte master key given the same password and salt', async () => {
            const password = 'my_super_secret_password';
            const salt = await generateSalt();

            const key1 = await deriveMasterKey(password, salt);
            const key2 = await deriveMasterKey(password, salt);

            expect(key1.length).toBe(32);
            expect(key1).toEqual(key2);
        });

        it('should derive different keys for different salts', async () => {
            const password = 'my_super_secret_password';
            const salt1 = await generateSalt();
            const salt2 = await generateSalt();

            const key1 = await deriveMasterKey(password, salt1);
            const key2 = await deriveMasterKey(password, salt2);

            expect(key1).not.toEqual(key2);
        });

        it('should throw an error if the salt is invalid length', async () => {
            const password = 'my_super_secret_password';
            const invalidSalt = new Uint8Array(10);

            await expect(deriveMasterKey(password, invalidSalt)).rejects.toThrow('Salt must be 16 bytes.');
        });
    });

    describe('DEK Generation', () => {
        it('should generate a 32-byte DEK', async () => {
            const dek = await generateDEK();
            expect(dek.length).toBe(32);
        });

        it('should generate random DEKs', async () => {
            const dek1 = await generateDEK();
            const dek2 = await generateDEK();
            expect(dek1).not.toEqual(dek2);
        });
    });

    describe('AES-256-GCM Encryption/Decryption', () => {
        it('should correctly encrypt and decrypt a string', async () => {
            const key = await generateDEK();
            const plaintext = 'This is a secret message.';

            const ciphertext = await encryptData(plaintext, key);

            // Ciphertext should be larger than plaintext due to IV and Auth Tag
            expect(ciphertext.length).toBeGreaterThan(plaintext.length);

            const decryptedBytes = await decryptData(ciphertext, key);
            const decryptedText = decodeString(decryptedBytes);

            expect(decryptedText).toBe(plaintext);
        });

        it('should correctly encrypt and decrypt raw bytes', async () => {
            const key = await generateDEK();
            const dekToEncrypt = await generateDEK(); // E.g., encrypting the DEK with the Master Key

            const ciphertext = await encryptData(dekToEncrypt, key);
            const decryptedBytes = await decryptData(ciphertext, key);

            expect(decryptedBytes).toEqual(dekToEncrypt);
        });

        it('should fail to decrypt if data is corrupted', async () => {
            const key = await generateDEK();
            const plaintext = 'This is a secret message.';
            const ciphertext = await encryptData(plaintext, key);

            // Corrupt the ciphertext
            ciphertext[ciphertext.length - 1] ^= 1;

            await expect(decryptData(ciphertext, key)).rejects.toThrow('Decryption failed.');
        });

        it('should fail to decrypt with the wrong key', async () => {
            const key1 = await generateDEK();
            const key2 = await generateDEK();
            const plaintext = 'This is a secret message.';
            const ciphertext = await encryptData(plaintext, key1);

            await expect(decryptData(ciphertext, key2)).rejects.toThrow('Decryption failed.');
        });
    });
});
