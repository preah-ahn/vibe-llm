docker run -d --name pg-vector \
  -e POSTGRES_PASSWORD=1234 \
  -p 54320:5432 \
  pgvector/pgvector:pg17
