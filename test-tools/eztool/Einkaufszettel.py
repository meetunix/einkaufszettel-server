""" Module representing the Einkaufszettel. """

import json
import random
import uuid
from datetime import datetime


class Category:
    def __init__(self, description, color):
        self.descr = description
        self.color = color

    @staticmethod
    def random():
        return Category(
            "description " + str(random.randint(100, 999)),
            "A0B8FF",
        )


class Item:
    def __init__(self, iid, name, ordinal, size, amount, unit, category):
        self.iid = iid
        self.itemName = name
        self.ordinal = ordinal
        self.size = size
        self.amount = amount
        self.unit = unit
        # category
        self.catDescription = category.descr
        self.catColor = category.color

    @staticmethod
    def random(category):
        return Item(
            str(uuid.uuid4()),
            f"some Item {random.randint(1000,9999)}",
            random.randint(1, 10),
            round(random.uniform(1, 200), 2),
            random.randint(1, 10),
            "Kg",
            category,
        )


class Einkaufszettel:
    def __init__(self, eid, created, modified, name, version, items):
        self.eid = eid
        self.created = created
        self.modified = modified
        self.name = name
        self.version = version
        self.items = items

    @staticmethod
    def random(amount_items=1):
        cs = [Category.random() for i in range(3)]
        items = [Item.random(cs[random.randint(0, 2)]) for i in range(amount_items)]
        curr_epoch = int(datetime.now().timestamp()) * 1000

        return Einkaufszettel(
            str(uuid.uuid4()),
            curr_epoch,
            curr_epoch,
            "ez-tool EZ " + str(random.randint(1000, 9999)),
            1,
            items,
        )

    def increment_version(self):
        self.version += 1
        return self

    def add_item(self, item):
        self.items.append(item)

    def get_json(self):
        return json.dumps(self, indent=True, default=encode_einkaufszettel)


def encode_einkaufszettel(ez):
    if isinstance(ez, Einkaufszettel):
        return {
            "eid": ez.eid,
            "created": ez.created,
            "modified": ez.modified,
            "name": ez.name,
            "version": ez.version,
            "items": [i.__dict__ for i in ez.items],
        }
    else:
        type_name = ez.__class__.__name__
        raise TypeError(f"Object of type '{type_name}' is not JSON serializable")
