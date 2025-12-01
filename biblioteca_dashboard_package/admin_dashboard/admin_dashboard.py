import streamlit as st
import pandas as pd
import plotly.express as px
from sqlalchemy import create_engine, text
from dotenv import load_dotenv
import os

load_dotenv()
DB_URL = os.getenv("ANALYTICS_DB_URL")

if not DB_URL:
    st.error("❌ Variável ANALYTICS_DB_URL não encontrada no .env")
    st.stop()

engine = create_engine(DB_URL, future=True)

def query(q):
    with engine.connect() as conn:
        return pd.read_sql_query(text(q), conn)

st.set_page_config(
    page_title="Dashboard Administrativo - Biblioteca",
    layout="wide",
    initial_sidebar_state="expanded"
)

st.title("📘 Dashboard Administrativo — Biblioteca Virtual")
st.markdown("Base: **biblioteca_analytics**")

# ----------------------------------------------------------
# KPIs
# ----------------------------------------------------------
st.header("📊 Indicadores Gerais")

total_livros = query("SELECT COUNT(*) AS total FROM dim_livro")["total"][0]
total_exemplares = query("SELECT SUM(num_total_licencas) AS total FROM dim_livro")["total"][0]

emprestimos_ativos = query("""
    SELECT COUNT(*) AS total
    FROM fact_emprestimo
    WHERE status_emprestimo_nome = 'ATIVO'
""")["total"][0]

ocupacao = (emprestimos_ativos / total_exemplares) * 100

total_usuarios = query("SELECT COUNT(*) AS total FROM dim_usuario")["total"][0]

metricas = query("""
    SELECT tempo_medio_emprestimo_days, taxa_renovacao
    FROM metric_emprestimo_summary 
    ORDER BY snapshot_dt DESC LIMIT 1
""")

tempo_medio = float(metricas["tempo_medio_emprestimo_days"][0])
taxa_ren = float(metricas["taxa_renovacao"][0])

c1, c2, c3, c4 = st.columns(4)
c1.metric("Total de Livros", total_livros)
c2.metric("Exemplares Disponíveis", total_exemplares)
c3.metric("Empréstimos Ativos", emprestimos_ativos)
c4.metric("Ocupação (%)", f"{ocupacao:.1f}%")

c5, c6 = st.columns(2)
c5.metric("Tempo Médio de Empréstimo", f"{tempo_medio:.1f} dias")
c6.metric("Taxa de Renovação", f"{taxa_ren:.2f}")

# ----------------------------------------------------------
# Top livros
# ----------------------------------------------------------
st.header("🏆 Top 10 Livros Mais Emprestados")

df_top = query("""
    SELECT titulo, total_emprestimos
    FROM agg_livro_popularidade
    ORDER BY total_emprestimos DESC LIMIT 10
""")

st.plotly_chart(px.bar(df_top, x="titulo", y="total_emprestimos"))

# Autores
st.header("✍ Autores Mais Populares")

df_aut = query("""
    SELECT nome_autor, total_emprestimos
    FROM agg_autor_popularidade
    ORDER BY total_emprestimos DESC LIMIT 10
""")

st.plotly_chart(px.bar(df_aut, x="nome_autor", y="total_emprestimos"))

# Temas
st.header("📚 Gêneros Literários Mais Emprestados")

df_tema = query("""
    SELECT nome_tema, total_emprestimos
    FROM agg_tema_popularidade
""")

st.plotly_chart(px.pie(df_tema, names="nome_tema", values="total_emprestimos"))

# ----------------------------------------------------------
# Usuários
# ----------------------------------------------------------
st.header("👥 Distribuição de Usuários")

df_sexo = query("""
    SELECT sexo, COUNT(*) AS total
    FROM dim_usuario
    GROUP BY sexo
""")

st.plotly_chart(px.pie(df_sexo, names="sexo", values="total"))

# st.header("🌍 Usuários por Cidade")

# df_city = query("""
#     SELECT cidade, COUNT(*) AS total
#     FROM dim_usuario
#     WHERE cidade IS NOT NULL
#     GROUP BY cidade
#     ORDER BY total DESC
# """)

# st.plotly_chart(px.bar(df_city, x="cidade", y="total"))

st.header("🔞 Distribuição por Faixa Etária")

df_age = query("""
    SELECT 
        CASE
            WHEN TIMESTAMPDIFF(YEAR, dt_nascimento, CURDATE()) <= 12 THEN 'Crianças'
            WHEN TIMESTAMPDIFF(YEAR, dt_nascimento, CURDATE()) BETWEEN 13 AND 20 THEN 'Jovens'
            WHEN TIMESTAMPDIFF(YEAR, dt_nascimento, CURDATE()) BETWEEN 21 AND 59 THEN 'Adultos'
            ELSE 'Idosos'
        END AS faixa,
        COUNT(*) AS total
    FROM dim_usuario
    GROUP BY faixa
""")

st.plotly_chart(px.pie(df_age, names="faixa", values="total"))

# ----------------------------------------------------------
# Gêneros favoritos por gênero
# ----------------------------------------------------------
st.header("🎭 Gêneros Favoritos por Sexo")

df_fav = query("""SELECT * FROM agg_genero_favorito""")

st.plotly_chart(px.bar(
    df_fav, x="nome_tema", y="total_emprestimos", color="sexo", barmode="group"
))

# ----------------------------------------------------------
# Faixa etária por livro
# ----------------------------------------------------------
st.header("📖 Livros Mais Lidos por Faixa Etária")

df_faixa = query("""SELECT * FROM agg_faixa_etaria_livros""")

st.plotly_chart(px.bar(
    df_faixa, x="titulo", y="total_emprestimos", color="faixa"
))

# ----------------------------------------------------------
# Reservas
# ----------------------------------------------------------
st.header("📑 Indicadores de Reservas")

df_res = query("""
    SELECT * FROM agg_reservas_summary
    ORDER BY snapshot_dt DESC LIMIT 1
""")

total_res = int(df_res["total_reservas"][0])
canceladas = int(df_res["total_canceladas"][0])
taxa_cancel = (canceladas / total_res) * 100 if total_res > 0 else 0

c1, c2, c3 = st.columns(3)
c1.metric("Total de Reservas", total_res)
c2.metric("Canceladas", canceladas)
c3.metric("Taxa de Cancelamento (%)", f"{taxa_cancel:.1f}%")

st.header("📌 Livros Mais Reservados")

df_liv = query("""SELECT titulo, total_reservas FROM agg_livros_reservados""")

st.plotly_chart(px.bar(df_liv, x="titulo", y="total_reservas"))

