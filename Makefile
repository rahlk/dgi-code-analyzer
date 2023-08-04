.PHONY: build
PLATFORM ?= amd64,arm64
VERSION = quay.io/rkrsn/code-analyzer:1.0.1
LATEST = quay.io/rkrsn/code-analyzer:latest

build:
	$(eval ARGS=$(shell while IFS= read -r line; do \
        name=$$(echo "$$line" | cut -d '=' -f1); \
        value=$$(echo "$$line" | cut -d '=' -f2); \
        echo "--build-arg $$name=$$value "; \
    done < .docker.env))
	docker buildx build --platform linux/$(PLATFORM) $(ARGS) -t $(VERSION) -t $(LATEST) . --push
