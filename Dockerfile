FROM continuumio/miniconda

RUN apt-get update

RUN conda install -c dlr-sc freeimageplus
RUN conda install -c DLR-SC gl2ps
RUN  conda install -c glotzer tbb
RUN conda install -c oce -c pythonocc pythonocc-core
