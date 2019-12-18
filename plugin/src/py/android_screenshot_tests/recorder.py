#!/usr/bin/env python
# Copyright 2014-present Facebook, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import xml.etree.ElementTree as ET
import os
import sys

from os.path import join
from PIL import Image, ImageChops, ImageDraw

from . import common
import shutil
import tempfile

class VerifyError(Exception):
    pass

class Recorder:
    def __init__(self, input, output, failure_output):
        self._input = input
        self._output = output
        self._realoutput = output
        self._failure_output = failure_output

    def _get_image_size(self, file_name):
        with Image.open(file_name) as im:
            return im.size

    def _copy(self, name, classname, method, w, h):
        img_path = join(self._input, classname)
        tilewidth, tileheight = self._get_image_size(
            join(img_path,
                 common.get_image_file_name(name, 0, 0)))

        canvaswidth = 0

        for i  in range(w):
            input_file = common.get_image_file_name(name, i, 0)
            canvaswidth += self._get_image_size(join(img_path, input_file))[0]


        canvasheight = 0

        for j in range(h):
            input_file = common.get_image_file_name(name, 0, j)
            canvasheight += self._get_image_size(join(img_path, input_file))[1]

        im = Image.new("RGBA", (canvaswidth, canvasheight))

        for i in range(w):
            for j in range(h):
                input_file = common.get_image_file_name(name, i, j)
                with Image.open(join(img_path, input_file)) as input_image:
                    im.paste(input_image, (i * tilewidth, j * tileheight))
                    input_image.close()

        output_path = join(self._output, classname)
        if not os.path.exists(output_path):
            os.makedirs(output_path)
        im.save(join(output_path, method + ".png"))
        im.close()

    def _get_metadata_root(self):
        return ET.parse(join(self._input, "metadata.xml")).getroot()

    def _record(self):
        root = self._get_metadata_root()
        for screenshot in root.iter("screenshot"):
            test_class = screenshot.find('test_class').text
            test_method = screenshot.find('test_name').text
            self._copy(screenshot.find('name').text,
                       test_class,
                       test_method,
                       int(screenshot.find('tile_width').text),
                       int(screenshot.find('tile_height').text))

    def _clean(self):
        if os.path.exists(self._output):
            shutil.rmtree(self._output)
        os.makedirs(self._output)

    def _is_image_same(self, name, file1, file2, failure_folder, failure_file):
        with Image.open(file1) as im1, Image.open(file2) as im2:
            diff_image = ImageChops.difference(im1, im2)
            try:
                diff = diff_image.getbbox()
                if diff is None:
                    return True
                else:
                    if failure_file:
                        if not os.path.exists(failure_folder):
                            os.makedirs(failure_folder)
                        diff_list = list(diff) if diff else []
                        draw = ImageDraw.Draw(im2)
                        draw.rectangle(diff_list, outline = (255,0,0))
                        im2.save(join(failure_folder, failure_file))
                    return False
            finally:
                diff_image.close()

    def record(self):
        # self._clean()
        self._record()

    def verify(self):
        self._output = tempfile.mkdtemp()
        self._record()

        root = self._get_metadata_root()
        failures = []
        for screenshot in root.iter("screenshot"):
            name = screenshot.find('name').text + ".png"
            test_class = screenshot.find('test_class').text
            test_method = screenshot.find('test_name').text
            actual = join(join(self._output, test_class), test_method + ".png")
            expected = join(join(self._realoutput, test_class), test_method + ".png")
            if self._failure_output:
                diff_name = screenshot.find('name').text + "_diff.png"
                diff = join(self._failure_output, test_class)
                is_passed = self._is_image_same(test_method, expected, actual, diff, diff_name)
                if not is_passed:
                    expected_name = screenshot.find('name').text + "_expected.png"
                    actual_name = screenshot.find('name').text + "_actual.png"

                    shutil.copy(actual, join(diff, actual_name))
                    shutil.copy(expected, join(diff, expected_name))
                    
                    failures.append((expected, actual))
            else:
                if not self._is_image_same("",expected, actual, None, None):
                    raise VerifyError("Image %s is not same as %s" % (expected, actual))                  

        if failures:
            reason = ''
            for expected, actual in failures:
                reason = reason + "\nImage %s is not same as %s" % (expected, actual)
            raise VerifyError(reason)

        shutil.rmtree(self._output)
