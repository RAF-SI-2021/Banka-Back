FROM docker.io/golang:1.17.5 as builder

ENV CGO_ENABLED=0

WORKDIR /go/src/xmudrii.com/api-cache
COPY . .
RUN mkdir _build && go build -o /go/src/xmudrii.com/api-cache/_build/api-cache

FROM docker.io/alpine:3.15

COPY --from=builder /go/src/xmudrii.com/api-cache/_build/api-cache /usr/local/bin/api-cache
ENTRYPOINT ["/usr/local/bin/api-cache"]
