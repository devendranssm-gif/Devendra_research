#!/usr/bin/env python3
"""Benchmark RSA-OAEP, AES-256-GCM and hybrid RSA+AES on a FogMLS CSV.

Install: pip install cryptography
Run:     python fogmls_crypto_benchmark.py --input fogmls_dataset_3x9_seed42_5000slots.csv

The three algorithms are compared on the same leading byte sample. The complete
CSV is encrypted with AES-GCM and hybrid RSA-AES. RSA direct encryption is
chunked for the sample because RSA-OAEP can encrypt only short messages.
"""
import argparse, csv, json, os, statistics, time
from pathlib import Path
from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import rsa, padding
from cryptography.hazmat.primitives.ciphers.aead import AESGCM

def clock():
    return time.perf_counter()

def oaep():
    return padding.OAEP(mgf=padding.MGF1(hashes.SHA256()),
                        algorithm=hashes.SHA256(), label=None)

def rsa_max_bytes(pub):
    return (pub.key_size + 7)//8 - 2*hashes.SHA256().digest_size - 2

def make_keys(folder, bits):
    folder.mkdir(parents=True, exist_ok=True)
    t = clock()
    private = rsa.generate_private_key(public_exponent=65537, key_size=bits)
    elapsed = clock()-t
    (folder/"rsa_private.pem").write_bytes(private.private_bytes(
        serialization.Encoding.PEM, serialization.PrivateFormat.PKCS8,
        serialization.NoEncryption()))
    (folder/"rsa_public.pem").write_bytes(private.public_key().public_bytes(
        serialization.Encoding.PEM, serialization.PublicFormat.SubjectPublicKeyInfo))
    try: os.chmod(folder/"rsa_private.pem", 0o600)
    except OSError: pass
    return private, private.public_key(), elapsed

def rsa_enc(data, pub):
    limit = rsa_max_bytes(pub)
    chunks = [data[i:i+limit] for i in range(0, len(data), limit)]
    return b"".join(pub.encrypt(x, oaep()) for x in chunks), len(chunks)

def rsa_dec(ct, priv):
    block = (priv.key_size+7)//8
    if len(ct) % block: raise ValueError("Invalid RSA ciphertext length")
    return b"".join(priv.decrypt(ct[i:i+block], oaep())
                    for i in range(0, len(ct), block))

def aes_enc(data):
    key = AESGCM.generate_key(bit_length=256)
    nonce = os.urandom(12)
    t = clock(); ct = AESGCM(key).encrypt(nonce, data, None); elapsed = clock()-t
    return key, nonce, ct, elapsed

def aes_dec(key, nonce, ct):
    t = clock(); pt = AESGCM(key).decrypt(nonce, ct, None)
    return pt, clock()-t

def hybrid_enc(data, pub):
    key = AESGCM.generate_key(bit_length=256); nonce = os.urandom(12)
    t = clock()
    ct = AESGCM(key).encrypt(nonce, data, None)
    wrapped = pub.encrypt(key, oaep())
    return {"wrapped": wrapped, "nonce": nonce, "ct": ct}, clock()-t

def hybrid_dec(env, priv):
    t = clock()
    key = priv.decrypt(env["wrapped"], oaep())
    pt = AESGCM(key).decrypt(env["nonce"], env["ct"], None)
    return pt, clock()-t

def mean(xs): return statistics.mean(xs)

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--input", required=True, help="FogMLS CSV path")
    ap.add_argument("--outdir", default="fogmls_crypto_output")
    ap.add_argument("--rsa-bits", type=int, choices=[2048,3072,4096], default=2048)
    ap.add_argument("--sample-bytes", type=int, default=4096)
    ap.add_argument("--repeats", type=int, default=5)
    args = ap.parse_args()
    src = Path(args.input)
    if not src.is_file(): raise FileNotFoundError(src)
    data = src.read_bytes()
    if not data: raise ValueError("Dataset file is empty")
    out = Path(args.outdir); out.mkdir(parents=True, exist_ok=True)
    priv, pub, keygen_s = make_keys(out/"keys", args.rsa_bits)
    sample = data[:min(len(data), args.sample_bytes)]
    records = []

    # Direct RSA-OAEP baseline, chunked to OAEP's permitted plaintext size.
    encs=[]; decs=[]; last_ct=b""; chunk_count=0
    for _ in range(args.repeats):
        t=clock(); last_ct, chunk_count = rsa_enc(sample,pub); encs.append(clock()-t)
        t=clock(); recovered=rsa_dec(last_ct,priv); decs.append(clock()-t)
        assert recovered == sample
    records.append(("RSA-OAEP-SHA256 (chunked)",len(sample),len(last_ct),mean(encs),mean(decs),chunk_count))

    # AES-256-GCM baseline.
    encs=[]; decs=[]; last_ct=b""
    for _ in range(args.repeats):
        key,nonce,last_ct,et=aes_enc(sample); encs.append(et)
        recovered,dt=aes_dec(key,nonce,last_ct); decs.append(dt); assert recovered==sample
    records.append(("AES-256-GCM",len(sample),len(last_ct)+12,mean(encs),mean(decs),0))

    # Hybrid: RSA wraps the AES key; AES-GCM encrypts the dataset bytes.
    encs=[]; decs=[]; last_env=None
    for _ in range(args.repeats):
        last_env,et=hybrid_enc(sample,pub); encs.append(et)
        recovered,dt=hybrid_dec(last_env,priv); decs.append(dt); assert recovered==sample
    records.append(("Hybrid RSA-OAEP + AES-256-GCM",len(sample),
                    4+len(last_env["wrapped"])+len(last_env["nonce"])+len(last_env["ct"]),
                    mean(encs),mean(decs),1))

    # Full-file AES encryption.
    key,nonce,ct,aes_full_enc=aes_enc(data)
    (out/"fogmls_dataset.aesgcm").write_bytes(nonce+ct)
    recovered,aes_full_dec=aes_dec(key,nonce,ct); assert recovered==data
    # Store key separately for this reproducible experiment only.
    (out/"aes256_key_for_experiment.bin").write_bytes(key)

    # Full-file hybrid encryption.
    env,hy_full_enc=hybrid_enc(data,pub)
    hybrid_file = (len(env["wrapped"]).to_bytes(4,"big")+env["wrapped"]+
                   env["nonce"]+env["ct"])
    (out/"fogmls_dataset.hybrid").write_bytes(hybrid_file)
    recovered,hy_full_dec=hybrid_dec(env,priv); assert recovered==data

    # Verify GCM detects tampering.
    tampered=bytearray(ct); tampered[0]^=1
    try:
        AESGCM(key).decrypt(nonce,bytes(tampered),None); tamper_detected=False
    except Exception:
        tamper_detected=True

    csv_path=out/"crypto_benchmark_results.csv"
    fields=["algorithm","input_bytes","ciphertext_bytes","encryption_ms","decryption_ms",
            "encryption_MB_per_s","decryption_MB_per_s","overhead_percent","rsa_chunks",
            "round_trip_verified"]
    with csv_path.open("w",newline="",encoding="utf-8") as f:
        w=csv.DictWriter(f,fieldnames=fields); w.writeheader()
        for name,n,c,et,dt,chunks in records:
            w.writerow({"algorithm":name,"input_bytes":n,"ciphertext_bytes":c,
                "encryption_ms":round(et*1000,4),"decryption_ms":round(dt*1000,4),
                "encryption_MB_per_s":round(n/et/1e6,6),
                "decryption_MB_per_s":round(n/dt/1e6,6),
                "overhead_percent":round((c-n)/max(n,1)*100,4),
                "rsa_chunks":chunks,"round_trip_verified":True})

    summary={"dataset_file":src.name,"dataset_bytes":len(data),
      "comparison_sample_bytes":len(sample),"rsa_bits":args.rsa_bits,
      "rsa_oaep_sha256_max_plaintext_bytes":rsa_max_bytes(pub),
      "rsa_key_generation_ms":round(keygen_s*1000,4),
      "full_dataset_aes_gcm":{"encryption_ms":round(aes_full_enc*1000,4),
        "decryption_ms":round(aes_full_dec*1000,4),"verified":True,
        "ciphertext_file":"fogmls_dataset.aesgcm"},
      "full_dataset_hybrid":{"encryption_ms":round(hy_full_enc*1000,4),
        "decryption_ms":round(hy_full_dec*1000,4),"verified":True,
        "ciphertext_file":"fogmls_dataset.hybrid"},
      "aes_gcm_tamper_detection":tamper_detected,
      "warning":"Protect rsa_private.pem and aes256_key_for_experiment.bin. Never publish keys."}
    (out/"summary.json").write_text(json.dumps(summary,indent=2),encoding="utf-8")
    print("Results:",csv_path.resolve())
    print("Summary:",(out/"summary.json").resolve())
    print("Full-file AES and hybrid decryption verified:",True)
    print("AES-GCM tampering detected:",tamper_detected)
    print("RSA direct benchmark uses equal-size sample only; full-file RSA is intentionally omitted.")

if __name__=="__main__": main()
