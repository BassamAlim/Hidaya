#!/usr/bin/env python3
"""Publish an APK to Samsung Galaxy Store via the Galaxy Store Developer API.

Flow (per https://developer.samsung.com/galaxy-store/galaxy-store-develop-api.html):
  1. Mint a JWT with the Seller Portal service account and exchange it for an
     access token.
  2. Create an upload session and upload the APK.
  3. Attach the uploaded binary to the app's content via contentUpdate.
  4. Submit the app for review via contentSubmit.

NOTE: This is scaffolding written against Samsung's documented API. Validate the
request/response shapes against the current docs on the first real run — Samsung
occasionally revises this API, and error responses are printed verbatim to help.

Environment variables:
  SAMSUNG_SERVICE_ACCOUNT_ID  service account id from Seller Portal (API Service)
  SAMSUNG_PRIVATE_KEY         PEM private key for that service account
  SAMSUNG_CONTENT_ID          the app's content id in Seller Portal
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


def attach_binary(headers: dict, content_id: str, file_key: str, apk_path: str):
    response = requests.post(
        f"{DEV_API}/seller/contentUpdate",
        headers=headers,
        json={
            "contentId": content_id,
            "binaryList": [
                {
                    "fileName": os.path.basename(apk_path),
                    "filekey": file_key,
                }
            ],
        },
    )
    if not response.ok:
        fail("contentUpdate", response)
    print(f"contentUpdate ok: {response.text}")


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

    print("Attaching binary to content...")
    attach_binary(headers, content_id, file_key, args.apk)

    print("Submitting for review...")
    submit(headers, content_id)

    print("Done: submitted to Galaxy Store review.")


if __name__ == "__main__":
    main()
