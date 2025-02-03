#!/bin/bash

# Start Minikube if not running
minikube status || minikube start

# Build the application Docker image
eval $(minikube docker-env)
docker build -t spring-boot-boilerplate:latest .

# Create k8s resources
kubectl apply -f k8s/storage.yml
kubectl apply -f k8s/postgres-deployment.yml
kubectl apply -f k8s/redis-deployment.yml
kubectl apply -f k8s/kafka-deployment.yml
kubectl apply -f k8s/app-deployment.yml

# Wait for deployments
echo "Waiting for deployments to be ready..."
kubectl wait --for=condition=available --timeout=300s deployment/postgres
kubectl wait --for=condition=available --timeout=300s deployment/redis
kubectl wait --for=condition=available --timeout=300s deployment/zookeeper
kubectl wait --for=condition=available --timeout=300s deployment/kafka
kubectl wait --for=condition=available --timeout=300s deployment/spring-app

# Get the application URL
echo "Application URL:"
minikube service spring-app --url 