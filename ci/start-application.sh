set -e

docker_username=${docker_username:-sofusalbertsen}
APP_NAME=${APP_NAME:-labs}

export docker_username
export APP_NAME

docker compose up -d