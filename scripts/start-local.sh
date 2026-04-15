#!/bin/zsh
set -e
cd "$(dirname "$0")/.."
echo "请分别启动前后端:"
echo "后端: cd backend && mvn spring-boot:run"
echo "前端: cd frontend && npm install && npm run dev"
echo "项目管理系统地址:"
echo "前端: http://127.0.0.1:3280"
echo "后端: http://127.0.0.1:9280/api"
