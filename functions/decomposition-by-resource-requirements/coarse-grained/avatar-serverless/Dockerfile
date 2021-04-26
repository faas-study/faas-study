FROM public.ecr.aws/lambda/python:3.8


RUN yum update -y
RUN yum -y install gcc-c++
RUN yum install -y make
RUN yum install -y cmake
RUN yum install -y mesa-libGL


ENV PYTHONUNBUFFERED=TRUE

COPY app.py ./
COPY requirements.txt ./

RUN pip install -r requirements.txt

CMD ["app.handler"]
