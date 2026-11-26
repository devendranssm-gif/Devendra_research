"""
FogMLS dataset security benchmark: RSA vs AES-256-GCM vs RSA-AES hybrid.

Input: .xls, .xlsx, or .csv file (the script encrypts the original file bytes,
so the workbook is preserved exactly; it does not alter spreadsheet cells).

Install:
    pip install cryptography

Run:
    python fogmls_security_benchmark.py "fogmlsdataset.xls"

Optional:
    python fogmls_security_benchmark.py "fogmlsdataset.xls" --rsa-full
    python fogmls_security_benchmark.py "fogmlsdataset.xls" --repeats 5

Outputs in ./fogmls_security_output:
    rsa_encrypted.bin, aes_encrypted.bin, hybrid_encrypted.bin
    rsa_private.pem, rsa_public.pem
    benchmark_results.csv, benchmark_summary.json
    decrypted copies for integrity verification

IMPORTANT:
- RSA-OAEP with a 2048-bit key and SHA-256 can encrypt at most 190 bytes
  per RSA operation. Full-file RSA therefore uses chunked RSA and is expected
  to be extremely slow and produce substantial expansion. Use it as a
  comparative baseline, not as the recommended production design.
- AES-GCM and RSA-AES hybrid provide authenticated encryption. The hybrid
  design encrypts the file with AES-GCM and protects the AES key with RSA-OAEP.
- Keep rsa_private.pem secret. Do not publish private keys or real encrypted
  datasets containing sensitive information.
"""

import argparse
import csv
import json
import os
import statistics
import time
from pathlib import Path

from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import padding, rsa
from cryptography.hazmat.primitives.ciphers.aead import AESGCM


def load_file_bytes(path: Path) -> bytes:
    if not path.exists() or not path.is_file():
        raise FileNotFoundError(f"Input file not found: {path}")
    if path.suffix.lower() not in {".xls", ".xlsx", ".csv"}:
        print(f"Warning: {path.suffix} is not a usual spreadsheet extension; "
              "the file will still be encrypted as raw bytes.")
    return path.read_bytes()


def rsa_oaep():
    return padding.OAEP(
        mgf=padding.MGF1(algorithm=hashes.SHA256()),
        algorithm=hashes.SHA256(),
        label=None,
    )


def rsa_encrypt_all(data: bytes, public_key, max_chunk: int) -> bytes:
    """Direct RSA-OAEP baseline; each plaintext chunk becomes one RSA block."""
    encrypted_blocks = []
    for start in range(0, len(data), max_chunk):
        encrypted_blocks.append(public_key.encrypt(data[start:start + max_chunk], rsa_oaep()))
    return b"".join(encrypted_blocks)


def rsa_decrypt_all(ciphertext: bytes, private_key, cipher_block_size: int) -> bytes:
    if len(ciphertext) % cipher_block_size != 0:
        raise ValueError("Invalid RSA ciphertext length.")
    out = bytearray()
    for start in range(0, len(ciphertext), cipher_block_size):
        block = ciphertext[start:start + cipher_block_size]
        out.extend(private_key.decrypt(block, rsa_oaep()))
    return bytes(out)


def aes_encrypt(data: bytes, key: bytes):
    nonce = os.urandom(12)  # 96-bit nonce recommended for GCM
    ciphertext_and_tag = AESGCM(key).encrypt(nonce, data, None)
    return nonce + ciphertext_and_tag


def aes_decrypt(blob: bytes, key: bytes):
    if len(blob) < 12 + 16:
        raise ValueError("AES-GCM payload is too short.")
    nonce, ciphertext_and_tag = blob[:12], blob[12:]
    return AESGCM(key).decrypt(nonce, ciphertext_and_tag, None)


def hybrid_encrypt(data: bytes, public_key):
    aes_key = AESGCM.generate_key(bit_length=256)
    encrypted_file = aes_encrypt(data, aes_key)
    wrapped_key = public_key.encrypt(aes_key, rsa_oaep())
    # Format: 4-byte wrapped-key length | wrapped AES key | AES-GCM payload
    package = len(wrapped_key).to_bytes(4, "big") + wrapped_key + encrypted_file
    return package


def hybrid_decrypt(package: bytes, private_key):
    if len(package) < 4:
        raise ValueError("Invalid hybrid package.")
    key_len = int.from_bytes(package[:4], "big")
    if key_len <= 0 or len(package) < 4 + key_len + 28:
        raise ValueError("Invalid hybrid package length.")
    wrapped_key = package[4:4 + key_len]
    encrypted_file = package[4 + key_len:]
    aes_key = private_key.decrypt(wrapped_key, rsa_oaep())
    return aes_decrypt(encrypted_file, aes_key)


def measure(fn, repeats):
    durations = []
    result = None
    for _ in range(repeats):
        start = time.perf_counter()
        result = fn()
        durations.append(time.perf_counter() - start)
    return result, statistics.mean(durations), min(durations), max(durations)


def main():
    parser = argparse.ArgumentParser(description="Benchmark RSA, AES-GCM and RSA-AES on a FogMLS spreadsheet.")
    parser.add_argument("input_file", help="Path to fogmlsdataset.xls/.xlsx/.csv")
    parser.add_argument("--repeats", type=int, default=3, help="Number of timing repetitions (default: 3)")
    parser.add_argument("--rsa-full", action="store_true",
                        help="Encrypt/decrypt the entire file using chunked RSA (very slow for large files).")
    parser.add_argument("--output-dir", default="fogmls_security_output")
    args = parser.parse_args()

    if args.repeats < 1:
        parser.error("--repeats must be at least 1")

    input_path = Path(args.input_file)
    data = load_file_bytes(input_path)
    output_dir = Path(args.output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)

    print(f"Input: {input_path}")
    print(f"File size: {len(data):,} bytes")
    print("Generating RSA-2048 key pair...")
    private_key = rsa.generate_private_key(public_exponent=65537, key_size=2048)
    public_key = private_key.public_key()

    private_pem = private_key.private_bytes(
        encoding=serialization.Encoding.PEM,
        format=serialization.PrivateFormat.PKCS8,
        encryption_algorithm=serialization.NoEncryption(),
    )
    public_pem = public_key.public_bytes(
        encoding=serialization.Encoding.PEM,
        format=serialization.PublicFormat.SubjectPublicKeyInfo,
    )
    (output_dir / "rsa_private.pem").write_bytes(private_pem)
    (output_dir / "rsa_public.pem").write_bytes(public_pem)

    # RSA-2048 OAEP/SHA-256 maximum input = 256 - 2*32 - 2 = 190 bytes.
    rsa_chunk_size = 2048 // 8 - 2 * 32 - 2
    rsa_cipher_block_size = 2048 // 8

    results = []

    # AES-256-GCM: encrypt and decrypt the complete original spreadsheet bytes.
    aes_key = AESGCM.generate_key(bit_length=256)
    aes_blob, aes_enc_avg, aes_enc_min, aes_enc_max = measure(
        lambda: aes_encrypt(data, aes_key), args.repeats)
    aes_plain, aes_dec_avg, aes_dec_min, aes_dec_max = measure(
        lambda: aes_decrypt(aes_blob, aes_key), args.repeats)
    if aes_plain != data:
        raise RuntimeError("AES-GCM integrity/round-trip verification failed.")
    (output_dir / "aes_encrypted.bin").write_bytes(aes_blob)
    (output_dir / ("aes_decrypted" + input_path.suffix)).write_bytes(aes_plain)
    results.append({
        "method": "AES-256-GCM",
        "scope": "Full dataset",
        "encryption_time_seconds_mean": aes_enc_avg,
        "encryption_time_seconds_min": aes_enc_min,
        "encryption_time_seconds_max": aes_enc_max,
        "decryption_time_seconds_mean": aes_dec_avg,
        "decryption_time_seconds_min": aes_dec_min,
        "decryption_time_seconds_max": aes_dec_max,
        "ciphertext_size_bytes": len(aes_blob),
        "overhead_bytes": len(aes_blob) - len(data),
        "round_trip_verified": True,
    })

    # RSA-AES hybrid: AES-GCM encrypts the file; RSA-OAEP wraps the AES key.
    hybrid_blob, hy_enc_avg, hy_enc_min, hy_enc_max = measure(
        lambda: hybrid_encrypt(data, public_key), args.repeats)
    hy_plain, hy_dec_avg, hy_dec_min, hy_dec_max = measure(
        lambda: hybrid_decrypt(hybrid_blob, private_key), args.repeats)
    if hy_plain != data:
        raise RuntimeError("Hybrid integrity/round-trip verification failed.")
    (output_dir / "hybrid_encrypted.bin").write_bytes(hybrid_blob)
    (output_dir / ("hybrid_decrypted" + input_path.suffix)).write_bytes(hy_plain)
    results.append({
        "method": "RSA-2048-OAEP + AES-256-GCM",
        "scope": "Full dataset",
        "encryption_time_seconds_mean": hy_enc_avg,
        "encryption_time_seconds_min": hy_enc_min,
        "encryption_time_seconds_max": hy_enc_max,
        "decryption_time_seconds_mean": hy_dec_avg,
        "decryption_time_seconds_min": hy_dec_min,
        "decryption_time_seconds_max": hy_dec_max,
        "ciphertext_size_bytes": len(hybrid_blob),
        "overhead_bytes": len(hybrid_blob) - len(data),
        "round_trip_verified": True,
    })

    # Direct RSA baseline. Full-file mode is optional because it is slow.
    rsa_input = data if args.rsa_full else data[:rsa_chunk_size]
    if not rsa_input:
        raise ValueError("Input file is empty.")
    rsa_blob, rsa_enc_avg, rsa_enc_min, rsa_enc_max = measure(
        lambda: rsa_encrypt_all(rsa_input, public_key, rsa_chunk_size), args.repeats)
    rsa_plain, rsa_dec_avg, rsa_dec_min, rsa_dec_max = measure(
        lambda: rsa_decrypt_all(rsa_blob, private_key, rsa_cipher_block_size), args.repeats)
    if rsa_plain != rsa_input:
        raise RuntimeError("RSA round-trip verification failed.")
    (output_dir / "rsa_encrypted.bin").write_bytes(rsa_blob)
    (output_dir / "rsa_decrypted_sample.bin").write_bytes(rsa_plain)
    results.append({
        "method": "RSA-2048-OAEP-SHA256 (chunked)",
        "scope": "Full dataset" if args.rsa_full else f"First {len(rsa_input)} bytes only (RSA size limit baseline)",
        "encryption_time_seconds_mean": rsa_enc_avg,
        "encryption_time_seconds_min": rsa_enc_min,
        "encryption_time_seconds_max": rsa_enc_max,
        "decryption_time_seconds_mean": rsa_dec_avg,
        "decryption_time_seconds_min": rsa_dec_min,
        "decryption_time_seconds_max": rsa_dec_max,
        "ciphertext_size_bytes": len(rsa_blob),
        "overhead_bytes": len(rsa_blob) - len(rsa_input),
        "round_trip_verified": True,
    })

    # Verify AES-GCM detects unauthorized modification.
    tamper_detected = False
    tampered = bytearray(aes_blob)
    tampered[-1] ^= 1
    try:
        aes_decrypt(bytes(tampered), aes_key)
    except Exception:
        tamper_detected = True
    if not tamper_detected:
        raise RuntimeError("AES-GCM tamper test failed: modified ciphertext was accepted.")

    with (output_dir / "benchmark_results.csv").open("w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=list(results[0].keys()))
        writer.writeheader()
        writer.writerows(results)

    summary = {
        "input_file": str(input_path),
        "input_size_bytes": len(data),
        "rsa_key_bits": 2048,
        "rsa_oaep_hash": "SHA-256",
        "rsa_max_plaintext_chunk_bytes": rsa_chunk_size,
        "aes": "AES-256-GCM",
        "repetitions": args.repeats,
        "rsa_full_file_mode": bool(args.rsa_full),
        "aes_gcm_tamper_detected": tamper_detected,
        "results": results,
        "note": "Times are measurements from this run and hardware; do not report them as universal values."
    }
    (output_dir / "benchmark_summary.json").write_text(json.dumps(summary, indent=2), encoding="utf-8")

    print("\nBENCHMARK RESULTS")
    for row in results:
        print(f"\n{row['method']} [{row['scope']}]")
        print(f"  Encryption mean: {row['encryption_time_seconds_mean']:.6f} s")
        print(f"  Decryption mean: {row['decryption_time_seconds_mean']:.6f} s")
        print(f"  Ciphertext size: {row['ciphertext_size_bytes']:,} bytes")
        print(f"  Round-trip verified: {row['round_trip_verified']}")
    print(f"\nAES-GCM tamper detection: {tamper_detected}")
    print(f"Results saved in: {output_dir.resolve()}")
    print("\nKeep rsa_private.pem secure. Do not upload it to GitHub or include it in a paper supplement.")


if __name__ == "__main__":
    main()
