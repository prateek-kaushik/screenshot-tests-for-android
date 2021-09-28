#!/usr/bin/env python

from __future__ import absolute_import
from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

import subprocess
from . import common
from .common import get_adb

class SimplePuller:
    """Pulls a given file from the device"""

    def __init__(self, adb_args=[]):
        self._adb_args = list(adb_args)

    def remote_file_exists(self, src):
        output = common.check_output(
            [get_adb()] + self._adb_args + ["shell",
                                        "ls %s && echo EXISTS || echo DOES_NOT_EXIST" % src])
        return "EXISTS" in output

    def pull(self, src, dest):
        subprocess.check_call(
            [get_adb()] + self._adb_args + ["pull", src, dest],
            stderr=subprocess.STDOUT)

    def get_external_data_dir(self):
        output = common.check_output(
            [get_adb()] + self._adb_args + ["shell", "echo", "$EXTERNAL_STORAGE"])
        return output.strip().split()[-1]
