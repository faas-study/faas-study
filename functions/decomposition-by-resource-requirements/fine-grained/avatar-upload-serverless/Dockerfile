FROM public.ecr.aws/lambda/python:3.8

ENV PYTHONUNBUFFERED=TRUE

COPY app.py ./
COPY requirements.txt ./

RUN pip install -r requirements.txt

CMD ["app.handler"]
