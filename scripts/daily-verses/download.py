"""Download pinned edition inputs; fail rather than accept a changed source silently."""
import concurrent.futures
import hashlib
import json
import subprocess
import sys
from pathlib import Path
from urllib.parse import urlparse
HERE = Path(__file__).resolve().parent

def main(directory):
    directory = Path(directory)
    directory.mkdir(parents=True, exist_ok=True)
    manifests = json.loads((HERE / 'sources.json').read_text())
    jobs = []
    for source in manifests.values():
        jobs.append((source['url'], source['sha256']))
        if 'crossCheck' in source:
            jobs.append((source['crossCheck']['url'], source['crossCheck']['sha256']))
    def download(job):
        url, expected = job
        file = directory / Path(urlparse(url).path).name
        if not file.exists():
            subprocess.run(['curl', '-L', '--fail', '--silent', '--show-error', '--retry', '2', url, '-o', str(file)], check=True)
        actual = hashlib.sha256(file.read_bytes()).hexdigest()
        if actual != expected:
            raise ValueError(f'{file.name}: source changed; review the edition update before changing the recorded hash.')
        return file.name
    with concurrent.futures.ThreadPoolExecutor(max_workers=4) as pool:
        names = list(pool.map(download, jobs))
    print(f'Verified {len(names)} source files against recorded SHA-256 hashes.')

if __name__ == '__main__':
    main(sys.argv[1])
