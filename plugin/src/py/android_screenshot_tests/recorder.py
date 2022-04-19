#!/usr/bin/env python3

import json
import os
import shutil
import sys
import tempfile
import xml.etree.ElementTree as ET
from os.path import join

from PIL import Image, ImageChops, ImageDraw, ImageStat

from . import common


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
        tilewidth, tileheight = self._get_image_size(
            join(self._input, common.get_image_file_name(name, 0, 0))
        )

        canvaswidth = 0

        for i in range(w):
            input_file = common.get_image_file_name(name, i, 0)
            canvaswidth += self._get_image_size(join(self._input, input_file))[0]

        canvasheight = 0

        for j in range(h):
            input_file = common.get_image_file_name(name, 0, j)
            canvasheight += self._get_image_size(join(self._input, input_file))[1]

        im = Image.new("RGBA", (canvaswidth, canvasheight))

        for i in range(w):
            for j in range(h):
                input_file = common.get_image_file_name(name, i, j)
                with Image.open(join(self._input, input_file)) as input_image:
                    im.paste(input_image, (i * tilewidth, j * tileheight))
                    input_image.close()

        output_path = join(self._output, classname)
        if not os.path.exists(output_path):
            os.makedirs(output_path)
        im.save(join(output_path, method + ".png"))
        im.close()

    def _get_metadata_json(self):
        with open(join(self._input, "metadata.json"), "r") as f:
            return json.load(f)

    def _record(self):
        metadata = self._get_metadata_json()
        for screenshot in metadata:
            self._copy(
                screenshot["name"],
                screenshot["testClass"],
                screenshot["testName"],
                int(screenshot["tileWidth"]),
                int(screenshot["tileHeight"]),
            )

    def _clean(self):
        if os.path.exists(self._output):
            shutil.rmtree(self._output)
        os.makedirs(self._output)

    def _is_image_same(self, file1, file2, failure_folder, failure_file):
        with Image.open(file1) as im1, Image.open(file2) as im2:
            diff_image = ImageChops.difference(im1.convert("RGB"), im2.convert("RGB"))
            try:
                stat = ImageStat.Stat(diff_image)
                diff_ratio = sum(stat.mean) / (len(stat.mean) * 255)
                difference_percent = diff_ratio * 100.0
                is_passed = not (difference_percent > 1)

                print("difference_percent is %d for image %s" % (difference_percent, file2))

                if is_passed and im1.size == im2.size:
                    return True
                else:
                    if failure_file:
                        if not os.path.exists(failure_folder):
                            os.makedirs(failure_folder)
                        diff = diff_image.getbbox()
                        diff_list = list(diff) if diff else []
                        draw = ImageDraw.Draw(im2)
                        draw.rectangle(diff_list, outline=(255, 0, 0))
                        im2.save(join(failure_folder, failure_file))
                    return False
            finally:
                diff_image.close()

    def record(self):
        self._clean()
        self._record()

    def verify(self):
        self._output = tempfile.mkdtemp()
        self._record()

        screenshots = self._get_metadata_json()
        failures = []
        for screenshot in screenshots:
            test_class = screenshot["testClass"]
            test_method = screenshot["testName"]
            actual = join(join(self._output, test_class), test_method + ".png")
            expected = join(join(self._realoutput, test_class), test_method + ".png")
            if self._failure_output:
                diff_name = screenshot["name"] + "_diff.png"
                diff = join(self._failure_output, test_class)

                if not self._is_image_same(expected, actual, diff, diff_name):
                    expected_name = screenshot["name"] + "_expected.png"
                    actual_name = screenshot["name"] + "_actual.png"

                    shutil.copy(actual, join(diff, actual_name))
                    shutil.copy(expected, join(diff, expected_name))

                    failures.append((expected, actual))
            else:
                if not self._is_image_same(expected, actual, None, None):
                    raise VerifyError("Image %s is not same as %s" % (expected, actual))

        if failures:
            reason = ""
            for expected, actual in failures:
                reason = reason + "\nImage %s is not same as %s" % (expected, actual)
            raise VerifyError(reason)

        shutil.rmtree(self._output)
