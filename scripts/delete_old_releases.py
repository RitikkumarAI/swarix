#!/usr/bin/env python3
import os
import sys
import json
import urllib.request
import urllib.error

def main():
    token = os.environ.get("GITHUB_TOKEN") or os.environ.get("GH_TOKEN")
    repo = os.environ.get("GITHUB_REPOSITORY", "RitikkumarAI/swarix")
    current_tag = os.environ.get("CURRENT_RELEASE_TAG", "v1.6")
    
    if not token:
        print("No GITHUB_TOKEN found; skipping remote cleanup.")
        return

    headers = {
        "Authorization": f"Bearer {token}",
        "Accept": "application/vnd.github+json",
        "User-Agent": "Swarix-Release-Cleanup"
    }

    print(f"Checking existing releases for repository {repo}...")
    url = f"https://api.github.com/repos/{repo}/releases"
    req = urllib.request.Request(url, headers=headers)
    
    try:
        with urllib.request.urlopen(req) as resp:
            releases = json.loads(resp.read().decode())
    except Exception as e:
        print(f"Failed to fetch releases: {e}")
        releases = []

    for rel in releases:
        rel_id = rel.get("id")
        tag_name = rel.get("tag_name")
        if tag_name == current_tag:
            print(f"Keeping current active release: {tag_name}")
            continue
        print(f"Found obsolete release ID {rel_id} (tag: {tag_name}). Deleting...")
        del_url = f"https://api.github.com/repos/{repo}/releases/{rel_id}"
        del_req = urllib.request.Request(del_url, headers=headers, method="DELETE")
        try:
            with urllib.request.urlopen(del_req) as del_resp:
                print(f"Deleted obsolete release {rel_id} ({tag_name}): HTTP {del_resp.status}")
        except Exception as e:
            print(f"Error deleting release {rel_id}: {e}")

    # Delete old tags from git
    old_tags = ["latest", "v1.0", "v1.1", "v1.2", "v1.3", "v1.4", "v1.5"]
    for t in old_tags:
        tag_url = f"https://api.github.com/repos/{repo}/git/refs/tags/{t}"
        tag_req = urllib.request.Request(tag_url, headers=headers, method="DELETE")
        try:
            with urllib.request.urlopen(tag_req) as tag_resp:
                print(f"Deleted remote tag {t}: HTTP {tag_resp.status}")
        except urllib.error.HTTPError as e:
            if e.code == 404:
                print(f"Remote tag {t} already removed (404).")
            else:
                print(f"Could not delete tag {t}: {e}")
        except Exception as e:
            print(f"Tag delete error: {e}")

    print("Cleanup completed successfully.")

if __name__ == "__main__":
    main()
