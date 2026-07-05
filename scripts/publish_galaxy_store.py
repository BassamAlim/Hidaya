#!/usr/bin/env python3
"""Publish an APK to Samsung Galaxy Store via the Galaxy Store Developer API.

Flow (per https://developer.samsung.com/galaxy-store/galaxy-store-developer-api/content-publish-api.html):
  1. Mint a JWT with the Seller Portal service account and exchange it for an
     access token.
  2. Create an upload session and upload the APK.
  3. Add the uploaded binary via POST /seller/v2/content/binary (contentUpdate's
     binaryList parameter is no longer accepted). The app must be in REGISTERING
     state; if the add fails, contentUpdate is called to enter that state and the
     add is retried once.
  4. Delete every other binary from the draft (this app ships one universal APK,
     so an update always replaces the previous binary; leftover binaries make
     contentSubmit fail).
  5. Submit the app for review via contentSubmit.

Error responses are printed verbatim to make failures diagnosable from CI logs.

Environment variables:
  SAMSUNG_SERVICE_ACCOUNT_ID  service account id from Seller Portal (API Service)
  SAMSUNG_PRIVATE_KEY         PEM private key for that service account
  SAMSUNG_CONTENT_ID          the app's content id in Seller Portal
  SAMSUNG_GMS                 "Y" if the app uses Google mobile services (default "Y")
"""

import argparse
import os
import sys
import time

import jwt
import requests

DEV_API = "https://devapi.samsungapps.com"
UPLOAD_API = "https://seller.samsungapps.com/galaxyapi/fileUpload"


def require_env(name: str) -> str:
    value = os.environ.get(name)
    if not value:
        sys.exit(f"Missing required environment variable: {name}")
    return value


def fail(step: str, response: requests.Response):
    sys.exit(f"{step} failed with HTTP {response.status_code}:\n{response.text}")


def get_access_token(service_account_id: str, private_key: str) -> str:
    now = int(time.time())
    assertion = jwt.encode(
        {"iss": service_account_id, "scopes": ["publishing"], "iat": now, "exp": now + 1200},
        private_key,
        algorithm="RS256",
    )
    response = requests.post(
        f"{DEV_API}/auth/accessToken",
        headers={"Authorization": f"Bearer {assertion}"},
    )
    if not response.ok:
        fail("accessToken", response)
    return response.json()["createdItem"]["accessToken"]


def api_headers(access_token: str, service_account_id: str) -> dict:
    return {
        "Authorization": f"Bearer {access_token}",
        "service-account-id": service_account_id,
    }


def create_upload_session(headers: dict) -> str:
    response = requests.post(f"{DEV_API}/seller/createUploadSessionId", headers=headers)
    if not response.ok:
        fail("createUploadSessionId", response)
    return response.json()["sessionId"]


def upload_apk(headers: dict, session_id: str, apk_path: str) -> str:
    with open(apk_path, "rb") as apk:
        response = requests.post(
            UPLOAD_API,
            headers=headers,
            data={"sessionId": session_id},
            files={"file": (os.path.basename(apk_path), apk)},
        )
    if not response.ok:
        fail("fileUpload", response)
    return response.json()["fileKey"]


def add_binary(headers: dict, content_id: str, file_key: str, gms: str) -> requests.Response:
    return requests.post(
        f"{DEV_API}/seller/v2/content/binary",
        headers=headers,
        json={"contentId": content_id, "gms": gms, "filekey": file_key},
    )


def enter_update_state(headers: dict, content_id: str):
    response = requests.post(
        f"{DEV_API}/seller/contentUpdate",
        headers=headers,
        json={"contentId": content_id},
    )
    if not response.ok:
        fail("contentUpdate", response)
    print(f"contentUpdate ok: {response.text}")


def get_content_info(headers: dict, content_id: str) -> dict:
    response = requests.get(
        f"{DEV_API}/seller/contentInfo",
        headers=headers,
        params={"contentId": content_id},
    )
    if not response.ok:
        fail("contentInfo", response)
    data = response.json()
    return data[0] if isinstance(data, list) else data


def delete_binary(headers: dict, content_id: str, binary_seq: str):
    response = requests.delete(
        f"{DEV_API}/seller/v2/content/binary",
        headers=headers,
        params={"contentId": content_id, "binarySeq": binary_seq},
    )
    if not response.ok:
        fail(f"binary delete (binarySeq={binary_seq})", response)
    print(f"deleted superseded binary binarySeq={binary_seq}")


def submit(headers: dict, content_id: str):
    response = requests.post(
        f"{DEV_API}/seller/contentSubmit",
        headers=headers,
        json={"contentId": content_id},
    )
    if not response.ok:
        fail("contentSubmit", response)
    print(f"contentSubmit ok: {response.text}")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--apk", required=True, help="path to the signed release APK")
    args = parser.parse_args()

    if not os.path.isfile(args.apk):
        sys.exit(f"APK not found: {args.apk}")

    service_account_id = require_env("SAMSUNG_SERVICE_ACCOUNT_ID")
    private_key = require_env("SAMSUNG_PRIVATE_KEY")
    content_id = require_env("SAMSUNG_CONTENT_ID")

    print("Requesting access token...")
    access_token = get_access_token(service_account_id, private_key)
    headers = api_headers(access_token, service_account_id)

    print("Creating upload session...")
    session_id = create_upload_session(headers)

    print(f"Uploading {args.apk}...")
    file_key = upload_apk(headers, session_id, args.apk)
    print(f"Uploaded, fileKey={file_key}")

    gms = os.environ.get("SAMSUNG_GMS", "Y")
    print("Adding binary...")
    response = add_binary(headers, content_id, file_key, gms)
    if not response.ok:
        print(f"binary add returned HTTP {response.status_code}:\n{response.text}")
        print("Moving app into REGISTERING state via contentUpdate, then retrying...")
        enter_update_state(headers, content_id)
        response = add_binary(headers, content_id, file_key, gms)
        if not response.ok:
            fail("binary add (after contentUpdate)", response)
    new_binary_seq = response.json().get("data", {}).get("binarySeq")
    print(f"binary add ok: binarySeq={new_binary_seq}")

    # The new binary replaces all previous ones (single universal APK app);
    # leaving them in the draft makes contentSubmit fail.
    print("Removing superseded binaries...")
    content_info = get_content_info(headers, content_id)
    for binary in content_info.get("binaryList") or []:
        if binary.get("binarySeq") != new_binary_seq:
            delete_binary(headers, content_id, binary["binarySeq"])

    print("Submitting for review...")
    submit(headers, content_id)

    print("Done: submitted to Galaxy Store review.")


if __name__ == "__main__":
    main()
