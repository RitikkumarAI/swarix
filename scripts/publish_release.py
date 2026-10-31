#!/usr/bin/env python3
import os
import sys
import json
import mimetypes
import urllib.request
import urllib.error

def main():
    token = os.environ.get("GITHUB_TOKEN") or os.environ.get("GH_TOKEN")
    repo = os.environ.get("GITHUB_REPOSITORY", "RitikkumarAI/swarix")
    tag = os.environ.get("RELEASE_TAG", "v1.6")
    name = f"Swarix Music Player {tag} (Official Multi-Platform)"
    
    if not token:
        print("Error: GITHUB_TOKEN not set!")
        sys.exit(1)

    headers = {
        "Authorization": f"Bearer {token}",
        "Accept": "application/vnd.github+json",
        "User-Agent": "Swarix-Publisher"
    }

    # 1. Read release notes
    notes_file = "dist/RELEASE_NOTES.md"
    body = "Swarix High-Fidelity Music Player"
    if os.path.exists(notes_file):
        with open(notes_file, "r") as f:
            body = f.read()

    # 2. Check and delete existing release with this tag if it exists
    print(f"Checking existing release for tag {tag} in {repo}...")
    get_rel_url = f"https://api.github.com/repos/{repo}/releases/tags/{tag}"
    req = urllib.request.Request(get_rel_url, headers=headers)
    existing_id = None
    try:
        with urllib.request.urlopen(req) as resp:
            data = json.loads(resp.read().decode())
            existing_id = data.get("id")
    except urllib.error.HTTPError as e:
        if e.code == 404:
            print(f"No existing release for tag {tag}.")
        else:
            print(f"Check release error: {e}")
    except Exception as e:
        print(f"Check release error: {e}")

    if existing_id:
        print(f"Deleting existing release {existing_id} for tag {tag} to recreate cleanly...")
        del_url = f"https://api.github.com/repos/{repo}/releases/{existing_id}"
        del_req = urllib.request.Request(del_url, headers=headers, method="DELETE")
        try:
            with urllib.request.urlopen(del_req) as resp:
                print(f"Deleted existing release: HTTP {resp.status}")
        except Exception as e:
            print(f"Error deleting release: {e}")

    # 3. Create fresh release
    print(f"Creating fresh release {tag}...")
    create_url = f"https://api.github.com/repos/{repo}/releases"
    payload = json.dumps({
        "tag_name": tag,
        "name": name,
        "body": body,
        "draft": False,
        "prerelease": False,
        "make_latest": "true"
    }).encode("utf-8")
    
    create_req = urllib.request.Request(create_url, data=payload, headers={**headers, "Content-Type": "application/json"})
    try:
        with urllib.request.urlopen(create_req) as resp:
            release_data = json.loads(resp.read().decode())
    except urllib.error.HTTPError as e:
        err_msg = e.read().decode()
        print(f"Create release failed HTTP {e.code}: {err_msg}")
        sys.exit(1)

    release_id = release_data["id"]
    upload_url_template = release_data["upload_url"] # e.g. "https://uploads.github.com/repos/.../assets{?name,label}"
    base_upload_url = upload_url_template.split("{")[0]
    print(f"Successfully created release {tag} (ID: {release_id})")

    # 4. Upload files
    files_to_upload = [
        "dist/swarix.apk",
        "dist/swarix.dmg",
        "dist/swarix.exe",
        "dist/swarix-ios.mobileconfig",
        "dist/Swarix-for-Mac.zip",
        "dist/Swarix-for-Windows.zip"
    ]

    for file_path in files_to_upload:
        if not os.path.exists(file_path):
            print(f"Warning: File {file_path} does not exist, skipping.")
            continue
            
        file_name = os.path.basename(file_path)
        file_size = os.path.getsize(file_path)
        print(f"Uploading {file_name} ({round(file_size / (1024*1024), 2)} MB)...")
        
        target_upload_url = f"{base_upload_url}?name={file_name}"
        with open(file_path, "rb") as f:
            file_bytes = f.read()

        upload_headers = {
            "Authorization": f"Bearer {token}",
            "Content-Type": "application/octet-stream",
            "Content-Length": str(len(file_bytes)),
            "User-Agent": "Swarix-Publisher"
        }
        
        up_req = urllib.request.Request(target_upload_url, data=file_bytes, headers=upload_headers)
        try:
            with urllib.request.urlopen(up_req) as up_resp:
                print(f"✓ Uploaded {file_name}: HTTP {up_resp.status}")
        except urllib.error.HTTPError as e:
            if e.code == 422:
                print(f"Asset {file_name} already exists: HTTP 422 (proceeding).")
            else:
                print(f"✗ Failed to upload {file_name}: {e}")
                sys.exit(1)
        except Exception as e:
            print(f"✗ Failed to upload {file_name}: {e}")
            sys.exit(1)

    print("All release assets uploaded successfully!")

if __name__ == "__main__":
    main()
