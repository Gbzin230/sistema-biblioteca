FROM python:3.11-slim

RUN apt-get update && apt-get install -y cron && apt-get clean

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# --- MUDANÇA AQUI: Copia ambos os scripts ---
COPY etl_analytics.py .
COPY auto_return.py . 
# --------------------------------------------

COPY crontab.txt /etc/cron.d/etl-cron

RUN chmod 0644 /etc/cron.d/etl-cron
RUN crontab /etc/cron.d/etl-cron
RUN touch /var/log/etl.log

CMD ["cron", "-f"]


