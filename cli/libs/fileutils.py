import errno
import os
import shutil
import subprocess

__author__ = 'Davide Caroselli'


def makedirs(name, mode=0777, exist_ok=False):
    try:
        os.makedirs(name, mode)
    except OSError as exception:
        if not exist_ok or exception.errno != errno.EEXIST:
            raise


def linecount(f):
    blank_line = False

    with open(f) as stream:
        count = 0
        for _, line in enumerate(stream):
            if blank_line:
                count += 1
                blank_line = False

            if len(line.rstrip('\n')) == 0:
                blank_line = True
            else:
                count += 1

    return count


def wordcount(f):
    wc = 0

    with open(f, 'r+') as stream:
        for _ in stream.read().split():
            wc += 1

    return wc


def df(f=None):
    if f is None:
        f = '.'

    output = subprocess.Popen(['df', f], stdout=subprocess.PIPE).communicate()[0]
    _, size, used, available, _, _ = output.split('\n')[1].split()

    return int(size) * 1024, int(used) * 1024, int(available) * 1024


def du(f=None):
    if f is None:
        f = '.'

    total_size = 0

    for dirpath, dirnames, filenames in os.walk(f):
        for f in filenames:
            fp = os.path.join(dirpath, f)
            total_size += os.path.getsize(fp)

    return int(total_size)


def free():
    output = subprocess.Popen(['free', '-t', '-b'], stdout=subprocess.PIPE).communicate()[0]
    _, total, used, available = output.split('\n')[4].split()

    return int(available)


def merge(srcs, dest, buffer_size=10 * 1024 * 1024, delimiter=None):
    with open(dest, 'wb') as blob:
        for src in srcs:
            with open(src, 'rb') as source:
                shutil.copyfileobj(source, blob, buffer_size)
            if delimiter is not None:
                blob.write(delimiter)
