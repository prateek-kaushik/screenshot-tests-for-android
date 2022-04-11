#!/usr/bin/env python3

from __future__ import absolute_import
from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

import json
import os
import shutil
import tempfile
import unittest

from . import metadata

# Tests for the metadata package
class TestMetadata(unittest.TestCase):
    def setUp(self):
        fd, self.tmp_metadata = tempfile.mkstemp(prefix="TempMetadataJson")
        os.close(fd)
        os.unlink(self.tmp_metadata)

        self.fixture_metadata = os.path.join(
            os.path.dirname(__file__), "metadata_fixture.json"
        )
        shutil.copyfile(self.fixture_metadata, self.tmp_metadata)

    def tearDown(self):
        if os.path.exists(self.tmp_metadata):
            os.unlink(self.tmp_metadata)

    def test_nothing_removed_for_empty_filter(self):
        metadata.filter_screenshots(self.tmp_metadata)

        self.assertEqual(
            self.get_num_screenshots_in(self.fixture_metadata),
            self.get_num_screenshots_in(self.tmp_metadata),
        )

    def test_exactly_one_result(self):
        metadata.filter_screenshots(
            self.tmp_metadata, name_regex="testAddPlaceIsShowing"
        )

        self.assertEqual(1, self.get_num_screenshots_in(self.tmp_metadata))

    def test_regex(self):
        metadata.filter_screenshots(
            self.tmp_metadata, name_regex=".*testAddPlaceIsShowing.*"
        )

        self.assertEqual(1, self.get_num_screenshots_in(self.tmp_metadata))

    def test_regex(self):
        metadata.filter_screenshots(self.tmp_metadata, name_regex=".*CheckinTitleBar.*")

        self.assertEqual(7, self.get_num_screenshots_in(self.tmp_metadata))

    def get_num_screenshots_in(self, metadata_file):
        with open(metadata_file, "r") as f:
            parsed = json.load(f)
            """Gets the number of screenshots in the given metadata file"""
            return len(parsed)


if __name__ == "__main__":
    unittest.main()
