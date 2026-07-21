FROM python:3.11-slim

WORKDIR /app

COPY pyproject.toml .
RUN pip install --no-cache-dir .

COPY anti_timpa/ anti_timpa/
COPY .env.example .env

EXPOSE 8000

CMD ["uvicorn", "anti_timpa.app:app", "--host", "0.0.0.0", "--port", "8000"]
