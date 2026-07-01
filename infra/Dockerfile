FROM ubuntu:22.04

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y \
    ansible \
    python3 \
    python3-pip \
    openssh-client \
    wget \
    unzip \
    nikto \
    nuclei \
    && rm -rf /var/lib/apt/lists/*

RUN wget https://github.com/projectdiscovery/nuclei/releases/download/v3.2.0/nuclei_3.2.0_linux_amd64.zip \
    && unzip nuclei_3.2.0_linux_amd64.zip \
    && mv nuclei /usr/local/bin/ \
    && chmod +x /usr/local/bin/nuclei \
    && rm -f nuclei_3.2.0_linux_amd64.zip
    
WORKDIR /app

ENV ANSIBLE_STDOUT_CALLBACK=json
ENV ANSIBLE_HOST_KEY_CHECKING=False

CMD ["echo", "Conteiner pronto para uso!"]
