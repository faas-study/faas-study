"""
Purpose
Shows how to implement an AWS Lambda function that handles input from direct
invocation.
"""
import base64
import logging
import json

import cv2
import dlib
import numpy as np

logger = logging.getLogger()
logger.setLevel(logging.INFO)

# Define a list of Python lambda functions that are called by this AWS Lambda function.

detector = dlib.get_frontal_face_detector()


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

    # data = json.loads(event)
    # image_b64 = data.get("img")
    data = json.loads(event["body"])
    image_b64 = data["img"]
    if image_b64 is None or len(image_b64) < 1:
        return {"msg": "need img in request body"}

    try:
        image_decode = base64.b64decode(image_b64)
        nparr = np.fromstring(image_decode, np.uint8)
        image = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
        # if normal, result is a path of file, otherwise its a dict of error message.
        result = check(image)
    except Exception as e:
        return {"msg": "serviceException"}

    if type(result) == dict and result.get("msg") is not None:
        return {"msg": "serviceException"}

    msg = {
        'statusCode': 200,
        'body': result
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

        result = base64.b64encode(cv2.imencode('.jpg', img_blank)[1].tostring())

        return result
