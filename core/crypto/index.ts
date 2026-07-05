import sodium from 'libsodium-wrappers';
import * as argon2node from 'argon2';

// Constants for cryptographic operations
const SALT_BYTES = 16;
const NONCE_BYTES = 12; // Standard for AES-GCM

/**
 * Ensures libsodium is initialized. Should be called early in the app.
 */
export const initCrypto = async () => {
    await sodium.ready;
};

/**
 * Derives a Master Key using Argon2id.
 *
 * @param password The master password
 * @param salt A unique salt (16 bytes)
 * @returns A 32-byte master key
 */
export const deriveMasterKey = async (password: string, salt: Uint8Array): Promise<Uint8Array> => {
    if (salt.length !== 16) {
        throw new Error(`Salt must be 16 bytes.`);
    }

    try {
        // Native argon2 fallback for Node.js (Vitest) environment.
        // In the true React Native / Web environment, this module will be replaced with
        // an environment specific implementation using react-native-argon2 or WebAssembly.
        const hash = await argon2node.hash(password, {
            type: argon2node.argon2id,
            salt: Buffer.from(salt),
            hashLength: 32,
            timeCost: 3,
            memoryCost: 65536,
            parallelism: 4,
            raw: true
        });
        return new Uint8Array(hash);
    } catch (e) {
         throw new Error('Argon2id hashing failed on all runtimes. ' + e);
    }
};

/**
 * Generates a cryptographically secure random Data Encryption Key (DEK).
 *
 * @returns A 32-byte random key
 */
export const generateDEK = async (): Promise<Uint8Array> => {
    await sodium.ready;
    return sodium.randombytes_buf(32);
};

/**
 * Generates a random salt for Argon2id.
 *
 * @returns A 16-byte random salt
 */
export const generateSalt = async (): Promise<Uint8Array> => {
    await sodium.ready;
    return sodium.randombytes_buf(16);
};

/**
 * Encrypts data using AES-256-GCM via WebCrypto API.
 *
 * @param data The plaintext data (JSON string or raw bytes)
 * @param key The 32-byte encryption key (e.g., DEK)
 * @returns The ciphertext including the IV (nonce), and the auth tag is appended by WebCrypto automatically.
 */
export const encryptData = async (data: string | Uint8Array, key: Uint8Array): Promise<Uint8Array> => {
    const iv = globalThis.crypto.getRandomValues(new Uint8Array(12));

    const cryptoKey = await globalThis.crypto.subtle.importKey(
        'raw',
        key,
        { name: 'AES-GCM' },
        false,
        ['encrypt']
    );

    const encodedData = typeof data === 'string' ? new TextEncoder().encode(data) : data;

    const encryptedBuffer = await globalThis.crypto.subtle.encrypt(
        {
            name: 'AES-GCM',
            iv: iv,
        },
        cryptoKey,
        encodedData
    );

    // Append IV to the beginning of the ciphertext
    const ciphertext = new Uint8Array(encryptedBuffer);
    const result = new Uint8Array(iv.length + ciphertext.length);
    result.set(iv, 0);
    result.set(ciphertext, iv.length);

    return result;
};

/**
 * Decrypts data using AES-256-GCM via WebCrypto API.
 *
 * @param encryptedData The ciphertext with the IV prepended
 * @param key The 32-byte encryption key (e.g., DEK)
 * @returns The decrypted plaintext bytes
 */
export const decryptData = async (encryptedData: Uint8Array, key: Uint8Array): Promise<Uint8Array> => {
    if (encryptedData.length < 12) {
        throw new Error('Invalid encrypted data format');
    }

    const iv = encryptedData.slice(0, 12);
    const ciphertext = encryptedData.slice(12);

    const cryptoKey = await globalThis.crypto.subtle.importKey(
        'raw',
        key,
        { name: 'AES-GCM' },
        false,
        ['decrypt']
    );

    try {
        const decryptedBuffer = await globalThis.crypto.subtle.decrypt(
            {
                name: 'AES-GCM',
                iv: iv,
            },
            cryptoKey,
            ciphertext
        );
        return new Uint8Array(decryptedBuffer);
    } catch (e) {
        throw new Error('Decryption failed. Invalid key or corrupted data.');
    }
};

/**
 * Helper to convert a string to Uint8Array.
 */
export const encodeString = (str: string): Uint8Array => new TextEncoder().encode(str);

/**
 * Helper to convert Uint8Array to string.
 */
export const decodeString = (bytes: Uint8Array): string => new TextDecoder().decode(bytes);
