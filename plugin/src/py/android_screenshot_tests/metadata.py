#!/usr/bin/env python3

from __future__ import absolute_import
from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

import json
import os
import re
import shutil
import tempfile
import unittest

# Given a metadata file locally, this transforms it (in-place), to
# remove any screenshot elements that don't satisfy the given filter
# criteria
def filter_screenshots(metadata_file, name_regex=None):
    with open(metadata_file, "r") as f:
        parsed = json.load(f)
        to_remove = []
        for s in parsed:
            if name_regex and not (re.search(name_regex, s["name"])):
                to_remove.append(s)

        for s in to_remove:
            parsed.remove(s)

    with open(metadata_file, "w") as f:
        f.write(json.dumps(parsed))
