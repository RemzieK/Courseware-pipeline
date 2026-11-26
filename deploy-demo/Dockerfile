FROM nginx:1.27-alpine
ARG VERSION=dev
LABEL org.opencontainers.image.source="https://github.com/RemzieK/Courseware-pipeline"
RUN echo "<h1>UniCloud demo</h1><p>Version: ${VERSION}</p>" > /usr/share/nginx/html/index.html \
 && echo "${VERSION}" > /usr/share/nginx/html/version.txt