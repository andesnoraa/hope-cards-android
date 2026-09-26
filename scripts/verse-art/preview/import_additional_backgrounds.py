"""Reproduce the September 26 additions without altering their colors or scenery.

Requires cwebp on PATH. Original PNGs stay unchanged; only resize and encode.
The manifest records source hashes, prompts, encoding settings and text regions.
"""
import hashlib
import json
from pathlib import Path
import shutil
import subprocess

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]
ASSETS = ROOT / 'android/app/src/main/assets/verse-art-renderer'
MANIFEST = HERE / 'selected-backgrounds-2026-09-26.json'


def main():
    entries = json.loads(MANIFEST.read_text())
    profiles_path = ASSETS / 'photo-profiles.json'
    profiles = json.loads(profiles_path.read_text())
    encoder = shutil.which('cwebp')
    if not encoder:
        raise SystemExit('Install libwebp to provide cwebp.')
    total = 0
    for entry in entries:
        source = Path(entry['sourcePath'])
        source_hash = hashlib.sha256(source.read_bytes()).hexdigest()
        if entry.get('sourceSha256') and entry['sourceSha256'] != source_hash:
            raise ValueError(f"Source changed: {source}")
        target = ASSETS / 'backgrounds' / (entry['name'] + '.webp')
        subprocess.run([encoder, '-quiet', '-q', '86', '-m', '6', '-resize', '1080', '1080',
                        str(source), '-o', str(target)], check=True)
        entry.update(sourceSha256=source_hash, asset=str(target.relative_to(ROOT)),
                     outputSize=[1080, 1080], format='WebP', quality=86,
                     bytes=target.stat().st_size,
                     assetSha256=hashlib.sha256(target.read_bytes()).hexdigest())
        profiles[entry['name']] = entry['profile']
        total += target.stat().st_size
    profiles_path.write_text(json.dumps(profiles, indent=2) + '\n')
    MANIFEST.write_text(json.dumps(entries, indent=2) + '\n')
    print(f'{len(entries)} backgrounds. {total:,} bytes ({total / 1024 / 1024:.2f} MiB).')


if __name__ == '__main__':
    main()
