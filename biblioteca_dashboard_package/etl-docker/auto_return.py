import os
import smtplib
from email.message import EmailMessage
from datetime import datetime
from dotenv import load_dotenv
import pandas as pd
from sqlalchemy import create_engine, text

# Carrega variáveis
load_dotenv()

# Usa a mesma variável de conexão do ETL existente
DB_URL = os.getenv("SOURCE_DB_URL") 
SMTP_HOST = os.getenv("SMTP_HOST")
SMTP_PORT = int(os.getenv("SMTP_PORT", "587"))
SMTP_USER = os.getenv("SMTP_USER")
SMTP_PASS = os.getenv("SMTP_PASS")
FROM_EMAIL = os.getenv("FROM_EMAIL", SMTP_USER)

if not DB_URL:
    print("❌ SOURCE_DB_URL não configurada. Abortando.")
    exit(1)

# Conecta no banco OPERACIONAL (biblioteca_db1)
engine = create_engine(DB_URL, future=True)

def send_email(to_email: str, subject: str, body: str):
    if not SMTP_HOST or not SMTP_USER:
        print(f"⚠️ SMTP não configurado. Email para {to_email} ignorado.")
        return False
    
    msg = EmailMessage()
    msg["From"] = FROM_EMAIL
    msg["To"] = to_email
    msg["Subject"] = subject
    msg.set_content(body, subtype="html")

    try:
        with smtplib.SMTP(SMTP_HOST, SMTP_PORT) as smtp:
            smtp.starttls()
            smtp.login(SMTP_USER, SMTP_PASS)
            smtp.send_message(msg)
        return True
    except Exception as e:
        print(f"❌ Erro ao enviar email: {e}")
        return False

def process_returns():
    print(f"[{datetime.now()}] Iniciando verificação de atrasos...")
    
    # Busca empréstimos vencidos (Status 1 = ATIVO)
    query_vencidos = """
    SELECT e.cod_emprestimo, e.cod_livro, e.cod_username, 
           u.txt_email, u.txt_nome, l.txt_titulo
    FROM biblioteca_db1.tb_emprestimo e
    JOIN biblioteca_db1.tb_usuario u ON u.cod_username = e.cod_username
    JOIN biblioteca_db1.tb_livro l ON l.cod_livro = e.cod_livro
    WHERE e.cod_status = 1 
      AND e.dt_fim < NOW()
    """
    
    with engine.connect() as conn:
        df = pd.read_sql_query(text(query_vencidos), conn)

    if df.empty:
        print("✅ Nenhum empréstimo vencido encontrado.")
        return

    print(f"🚨 Encontrados {len(df)} empréstimos vencidos.")

    for _, row in df.iterrows():
        emp_id = row['cod_emprestimo']
        email = row['txt_email']
        nome = row['txt_nome']
        livro = row['txt_titulo']

        # ATUALIZAÇÃO FORÇADA (Aqui reside o risco de negócio)
        # Define status = 2 (Finalizado/Devolvido)
        update_q = text("""
            UPDATE biblioteca_db1.tb_emprestimo 
            SET cod_status = 2
            WHERE cod_emprestimo = :id
        """)
        
        with engine.begin() as conn:
            conn.execute(update_q, {"id": emp_id})
            print(f"🔄 Empréstimo {emp_id} devolvido via batch.")

        # Envio de Email
        assunto = "Biblioteca: Devolução Automática Realizada"
        corpo = f"""
        <p>Olá, <b>{nome}</b>.</p>
        <p>O prazo do livro <i>{livro}</i> expirou e o sistema realizou a devolução automática.</p>
        <p>Caso deseje ler novamente, verifique a disponibilidade no site.</p>
        """
        
        if send_email(email, assunto, corpo):
            print(f"📧 Email enviado para {email}")

if __name__ == "__main__":
    process_returns()