"""
Purpose
Shows how to implement an AWS Lambda function that handles input from direct
invocation.
"""
import base64
import logging
import sys
import time
import json

import cv2
import dlib
import oss2
import numpy as np

logger = logging.getLogger()
logger.setLevel(logging.INFO)

# Define a list of Python lambda functions that are called by this AWS Lambda function.

detector = dlib.get_frontal_face_detector()

endpoint = os.getenv("OSS_ENDPOINT")
auth_key = os.getenv("OSS_AUTH_KEY")
auth_secret = os.getenv("OSS_AUTH_SECRET")
bucket_name = os.getenv("OSS_BUCKET_NAME")
bucket_address = os.getenv("OSS_BUCKET_ADDRESS")


if endpoint is None or auth_key is None or auth_secret is None or bucket_name is None or bucket_address is None:
    print(f"endpoint: {endpoint}, auth_key: {auth_key}, auth_secret: {auth_secret}, bucket_name: {bucket_name}, "
          f"bucket_address: {bucket_address}. something is not set in env.")
    sys.exit(1)

auth = oss2.Auth(auth_key, auth_secret)
bucket = oss2.Bucket(auth, endpoint, bucket_name)


def handler(event, context):
    """
    Accepts an action and a number, performs the specified action on the number,
    and returns the result.
    :param event: The event dict that contains the parameters sent when the function
                  is invoked.
    :param context: The context in which the function is called.
    :return: The result of the specified action.
    """
    logger.info('Event: %s', event)

    data = json.loads(event["body"])
    image_b64 = data["img"]
    if image_b64 is None or len(image_b64) < 1:
        return {"msg": "need img in request body"}

    try:
        image_decode = base64.b64decode(image_b64)
        nparr = np.fromstring(image_decode, np.uint8)
        image = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
        result = check(image)
    except Exception as e:
        logger.error('Check face failed: %s', e)
        return {"msg": "serviceException, check face failed"}

    try:
        bucket.put_object("img_face.jpg", result)
        address = bucket_address + "img_face.jpg"
    except Exception as e:
        logger.error('Upload pic failed: %s', e)
        return {"msg": "serviceException, upload pic failed"}

    msg = {
        'statusCode': 200,
        'body': address
    }

    return msg


def check(img):
    # Dlib 检测器
    faces = detector(img, 1)
    print("人脸数：", len(faces), "\n")

    if len(faces) < 1:
        return {"msg": "no human face found"}

    # 记录人脸矩阵大小
    height_max = 0
    width_sum = 0

    # 计算要生成的图像 img_blank 大小
    for k, d in enumerate(faces):

        # 计算矩形大小
        # (x,y), (宽度width, 高度height)
        pos_start = tuple([d.left(), d.top()])
        pos_end = tuple([d.right(), d.bottom()])

        # 计算矩形框大小
        height = d.bottom() - d.top()
        width = d.right() - d.left()

        # 根据人脸大小生成空的图像
        img_blank = np.zeros((height, width, 3), np.uint8)

        for i in range(height):
            for j in range(width):
                img_blank[i][j] = img[d.top() + i][d.left() + j]

        result = cv2.imencode('.jpg', img_blank)[1].tostring()

        return result

