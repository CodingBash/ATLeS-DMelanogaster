import picamera
from time import sleep
import sys

def main(argv):
    with picamera.PiCamera() as camera:
        camera.start_recording("../capture/capture.h264")
        sleep(argv[0])
        camera.stop_recording()

if __name__ == "__main__":
    main(sys.argv[1:])
