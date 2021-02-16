"""Doing threaded CRUD actions on a ez-server."""

import requests
import concurrent.futures
import threading

thread_local = threading.local()

class Communicator:
    def __init__(self, base_url, endpoint="/r0/ez/", worker=8):
        self.base_url = base_url
        self.endpoint = endpoint
        self.url = self.base_url + self.endpoint
        self._worker = worker
        self.set_headers()

    def set_headers(self):
        # used for put method
        self._send_headers = {
            "Content-Type": "application/json",
            "Accept": "application/json",
        }
        # used for get and delete methods
        self._headers = {"Accept": "application/json"}

    # storing/writing to server
    def _dump_store(self, ezl):
        """Upload a list of Einkaufszettel to the server synchronously and without
        requests session-feature. For testing only, because it is very slow."""
        for ez in ezl:
            response = requests.put(
                    self.url + ez.eid,
                    headers=self._send_headers,
                    data=ez.get_json()
                    )
            print(response.text, response.status_code)

    def _get_session(self):
        """Handles the session inside the local thread_local instance."""
        if not hasattr(thread_local, "session"):
            thread_local.session = requests.Session()
        return thread_local.session


    def _store_ez(self, ez):
        """Upload a Einkaufszettel to the server using the requests session feature."""
        session = self._get_session()
        with session.put(
                self.url + ez.eid,
                headers=self._send_headers,
                data=ez.get_json()
                ) as response:

            if response.status_code == 200:
                pass
            elif response.status_code == 404:
                print("EZ does not exist")
            else:
                print(f"unable to write/update ez ({response.status_code} - {response.text}")


    def store(self, ezl):
        """Upload a list of Einkaufszettel to the Server using miltiple threads."""
        with concurrent.futures.ThreadPoolExecutor(max_workers=self._worker) as executor:
            executor.map(self._store_ez, ezl)

    # reading from server
    def _read_ez(self, eid):
        """Read single Einkaufszettel from the server using requests session feature."""
        session = self._get_session();
        with session.get(self.url + eid, headers=self._headers) as response:

            if response.status_code == 200:
                pass
            elif response.status_code == 404:
                print("EZ does not exist")
            else:
                print(f"unable to read ez ({response.status_code} - {response.text}")

    def read(self, eids):
        """Read Einkaufszettel from the Server using multiple threads.
         eids is a list of eids."""
        with concurrent.futures.ThreadPoolExecutor(max_workers=self._worker) as executor:
            executor.map(self._read_ez, eids)

    #delete from server
    def _delete_ez(self, eid):
        """Delete single Einkaufszettel from the server using requests session feature."""
        session = self._get_session();
        with session.delete(self.url + eid, headers=self._headers) as response:

            if response.status_code == 200:
                pass
            elif response.status_code == 404:
                print("EZ does not exist")
            else:
                print(f"unable to delete ({response.status_code} - {response.text}")

    def delete(self, eids):
        """Delete Einkaufszettel from the Server using multiple threads.
         eids is a list of eids."""
        with concurrent.futures.ThreadPoolExecutor(max_workers=self._worker) as executor:
            executor.map(self._delete_ez, eids)


