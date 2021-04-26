"""
Purpose
Shows how to implement an AWS Lambda function that handles input from direct
invocation.
"""
import base64
import logging
import sys
import json

import oss2
import requests

logger = logging.getLogger()
logger.setLevel(logging.INFO)

# Define a list of Python lambda functions that are called by this AWS Lambda function.

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

    # try:
    #     # request img check
    #     response = requests.post('https://4h009m4nr2.execute-api.us-east-2.amazonaws.com/face-check', json=data,
    #                              verify=False)
    # except Exception as e:
    #     logger.error('Check face failed, %s', e)
    #     return {"msg": "serviceException, check face failed"}

    # if response.status_code != 200:
    #     logger.error('Request face check failed, response is %s', response.text)
    #     return {"msg": "serviceException, request face check failed"}

    # img_data = base64.b64decode(response.text)

    try:
        bucket.put_object("img_face.jpg", img_data)
        address = bucket_address + "img_face.jpg"
    except Exception as e:
        logger.error('Upload pic failed, %s', e)
        return {"msg": "serviceException, upload pic failed"}

    msg = {
        'statusCode': 200,
        'body': address
    }

    return msg
