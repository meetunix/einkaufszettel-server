import requests

class Communicator:
    def __init__(self, base_url, endpoint="/r0/ez/"):
        self.base_url = base_url
        self.endpoint = endpoint
        self.url = self.base_url + self.endpoint
        self.set_headers()

    def set_headers(self):
        self._send_headers = {
            "Content-Type": "application/json",
            "Accept": "application/json",
        }

    def store(self, ez):

        r = requests.put(
                self.url + ez.eid,
                headers=self._send_headers,
                data=ez.get_json()
                )

        print(r.status_code, r.text)

