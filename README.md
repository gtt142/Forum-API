# Forum-API

## Документация к API
https://tech-db-forum.bozaro.ru/

## Требования к проекту
Проект должен включать в себя все необходимое для разворачивания сервиса в Docker-контейнере.

## Контейнер будет собираться из запускаться командами вида:
### Сборка
```
docker build -t t.garifullin https://github.com/gtt142/Forum-API.git
```
Или
```
git clone https://github.com/gtt142/Forum-API.git
cd Forum-API/
docker build -t t.garifullin .
```
### Запуск
```
docker run -p 5000:5000 --name t.garifullin -t t.garifullin
```
