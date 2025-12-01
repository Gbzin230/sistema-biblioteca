# etl_analytics.py
import os
import pandas as pd
from sqlalchemy import create_engine, text
from datetime import datetime
from dotenv import load_dotenv
import sys
import traceback

load_dotenv()

SOURCE_DB_URL = os.getenv("SOURCE_DB_URL")
TARGET_DB_URL = os.getenv("TARGET_DB_URL")

if not SOURCE_DB_URL or not TARGET_DB_URL:
    raise SystemExit("❌ Defina SOURCE_DB_URL e TARGET_DB_URL no .env")

# Create engines
src_engine = create_engine(SOURCE_DB_URL, future=True)
tgt_engine = create_engine(TARGET_DB_URL, future=True)


def load_table(query: str) -> pd.DataFrame:
    """Executa uma query no banco fonte e retorna um DataFrame."""
    try:
        with src_engine.connect() as conn:
            return pd.read_sql_query(text(query), conn)
    except Exception:
        print("Erro ao executar query no banco fonte:")
        print(query)
        traceback.print_exc()
        raise


from sqlalchemy import text
import sqlalchemy

# REQUER: from sqlalchemy import text
import traceback

def upsert_table(df, table_name, if_exists="replace"):
    """
    Upsert seguro e robusto:
    - Cria staging (`table_name`_stg) com colunas alinhadas ao target (se existir)
    - Se target existe: INSERT ... ON DUPLICATE KEY UPDATE (somente colunas não-PK)
    - Se target não existe: cria tabela target diretamente a partir do df
    - Remove staging no final
    """
    staging = f"{table_name}_stg"
    if df is None:
        print(f"-> Aviso: DataFrame ausente para {table_name}")
        return
    cols_df = list(df.columns)
    if not cols_df:
        print(f"-> Aviso: DataFrame vazio para tabela '{table_name}', nada a fazer.")
        return

    try:
        with tgt_engine.begin() as conn:
            # verifica se target existe
            exists_q = text("""
                SELECT COUNT(*) FROM information_schema.tables
                WHERE table_schema = DATABASE() AND table_name = :t
            """)
            exists = conn.execute(exists_q, {"t": table_name}).scalar()

            if exists:
                # pega colunas e PKs da tabela alvo
                col_q = text("""
                    SELECT COLUMN_NAME
                    FROM information_schema.columns
                    WHERE table_schema = DATABASE() AND table_name = :t
                    ORDER BY ORDINAL_POSITION
                """)
                target_cols = [r[0] for r in conn.execute(col_q, {"t": table_name}).all()]

                pk_q = text("""
                    SELECT k.COLUMN_NAME
                    FROM information_schema.table_constraints tc
                    JOIN information_schema.key_column_usage k
                      ON k.constraint_name = tc.constraint_name
                      AND k.table_schema = tc.table_schema
                      AND k.table_name = tc.table_name
                    WHERE tc.table_schema = DATABASE()
                      AND tc.table_name = :t
                      AND tc.constraint_type = 'PRIMARY KEY'
                    ORDER BY k.ORDINAL_POSITION
                """)
                pk_cols = [r[0] for r in conn.execute(pk_q, {"t": table_name}).all()]

                # Interseção e alinhamento: só manter colunas que existem no target
                intersect_cols = [c for c in target_cols if c in cols_df]
                # Preencher colunas que existem no target mas não no df com NULLs
                for c in target_cols:
                    if c not in df.columns:
                        df[c] = None
                # Reordenar DF para a ordem de target_cols
                df = df[[c for c in target_cols if c in df.columns]]

                # cria staging com a estrutura alinhada
                df.to_sql(staging, conn, if_exists="replace", index=False)

                # build insert/upsert SQL: só usar colunas em intersect_cols
                col_list = ", ".join(f"`{c}`" for c in intersect_cols)
                # geramos update_expr apenas para colunas que NÃO são PK
                non_pk_cols = [c for c in intersect_cols if c not in pk_cols]
                if non_pk_cols:
                    update_expr = ", ".join(f"`{c}`=VALUES(`{c}`)" for c in non_pk_cols)
                else:
                    # se não houver colunas não-PK, nada para atualizar; podemos usar INSERT IGNORE
                    update_expr = None

                if update_expr:
                    insert_sql = f"""
                        INSERT INTO `{table_name}` ({col_list})
                        SELECT {col_list} FROM `{staging}`
                        ON DUPLICATE KEY UPDATE {update_expr};
                    """
                else:
                    # sem colunas para atualizar, usa INSERT IGNORE para não duplicar PKs
                    insert_sql = f"""
                        INSERT IGNORE INTO `{table_name}` ({col_list})
                        SELECT {col_list} FROM `{staging}`;
                    """

                conn.execute(text(insert_sql))
                conn.execute(text(f"DROP TABLE IF EXISTS `{staging}`;"))
                print(f"-> Upsert em '{table_name}' concluído (linhas upserted: {len(df)})")
            else:
                # target não existe: cria ela diretamente (primeira execução)
                df.to_sql(table_name, conn, if_exists="replace", index=False)
                # remove staging caso exista (defensivo)
                conn.execute(text(f"DROP TABLE IF EXISTS `{staging}`;"))
                print(f"-> Tabela '{table_name}' criada (linhas: {len(df)})")

    except Exception:
        print(f"Erro ao upsertar a tabela '{table_name}'. Traceback abaixo:")
        traceback.print_exc()
        # tenta dropar staging por limpeza se existir
        try:
            with tgt_engine.begin() as conn2:
                conn2.execute(text(f"DROP TABLE IF EXISTS `{staging}`;"))
        except Exception:
            pass
        raise





# -----------------------------------------------------
# BUILD DIMENSIONS
# -----------------------------------------------------
def build_dims():
    print("  * build_dims: dim_usuario")
    df_u = load_table("""
        SELECT
            u.cod_username AS usuario_id,
            u.txt_nome AS nome,
            u.txt_email AS email,
            u.char_sexo AS sexo,
            -- dt_nascimento vem como VARCHAR(8) no formato YYYYMMDD; tentamos converter, se já for DATE mantemos
            COALESCE(STR_TO_DATE(u.dt_nascimento, '%Y%m%d'), u.dt_nascimento) AS dt_nascimento,
            NULL AS cidade,  -- coluna cidade não existe no schema fonte
            u.flag_ativo AS ativo,
            r.nome_role AS role,
            u.dt_cadastro
        FROM biblioteca_db1.tb_usuario u
        LEFT JOIN biblioteca_db1.tb_role r ON r.cod_role = u.cod_role
    """)
    upsert_table(df_u, "dim_usuario")

    print("  * build_dims: dim_livro")
    df_l = load_table("""
        SELECT
            l.cod_livro AS livro_id,
            l.txt_titulo AS titulo,
            l.ano_lancamento,
            e.nome_editora AS editora,
            o.nome_obra AS obra,
            l.num_total_licencas,
            l.txt_sinopse AS sinopse,
            l.uri_img_livro AS uri_img
        FROM biblioteca_db1.tb_livro l
        LEFT JOIN biblioteca_db1.tb_editora e ON e.cod_editora = l.cod_editora
        LEFT JOIN biblioteca_db1.tb_obra o ON o.cod_obra = l.cod_obra
    """)
    upsert_table(df_l, "dim_livro")

    print("  * build_dims: dim_autor")
    df_a = load_table("""
        SELECT cod_autor AS autor_id, nome_autor
        FROM biblioteca_db1.tb_autor
    """)
    upsert_table(df_a, "dim_autor")

    print("  * build_dims: dim_tema")
    df_t = load_table("""
        SELECT cod_tema AS tema_id, nome_tema
        FROM biblioteca_db1.tb_tema
    """)
    upsert_table(df_t, "dim_tema")

    # dim_date can be built if needed; kept out of automatic building unless necessary


# -----------------------------------------------------
# BUILD FACTS
# -----------------------------------------------------
def build_facts():
    print("  * build_facts: fact_emprestimo")
    df_emp = load_table("""
        SELECT 
            e.cod_emprestimo AS emprestimo_id,
            e.cod_livro AS livro_id,
            e.cod_username AS usuario_id,
            e.dt_inicio, e.dt_fim, e.num_renovacoes,
            se.cod_status AS status_emprestimo_cod,
            se.nome_status AS status_emprestimo_nome,
            TIMESTAMPDIFF(DAY, e.dt_inicio, e.dt_fim) AS duracao_dias,
            TIMESTAMPDIFF(DAY, e.dt_inicio, NOW()) AS duracao_dias_atual
        FROM biblioteca_db1.tb_emprestimo e
        LEFT JOIN biblioteca_db1.tb_status_emprestimo se 
            ON se.cod_status = e.cod_status
    """)
    upsert_table(df_emp, "fact_emprestimo")

    print("  * build_facts: fact_reserva")
    df_res = load_table("""
        SELECT 
            r.cod_reserva AS reserva_id, 
            r.cod_livro AS livro_id,
            r.cod_username AS usuario_id,
            r.dt_inicio_reserva, 
            r.dt_fim_reserva,
            sr.cod_status AS status_reserva_cod,
            sr.nome_status AS status_reserva_nome
        FROM biblioteca_db1.tb_reserva r
        LEFT JOIN biblioteca_db1.tb_status_reserva sr 
            ON sr.cod_status = r.cod_status_reserva
    """)
    upsert_table(df_res, "fact_reserva")


# -----------------------------------------------------
# BUILD AGGREGATES
# -----------------------------------------------------
def build_aggs():
    print("  * build_aggs: agg_livro_popularidade")
    df_pop = load_table("""
        SELECT l.cod_livro AS livro_id, l.txt_titulo AS titulo,
               COALESCE(emp.cnt,0) AS total_emprestimos,
               COALESCE(res.cnt,0) AS total_reservas,
               l.num_total_licencas AS total_ativos
        FROM biblioteca_db1.tb_livro l
        LEFT JOIN (
            SELECT cod_livro, COUNT(*) AS cnt 
            FROM biblioteca_db1.tb_emprestimo
            GROUP BY cod_livro
        ) emp ON emp.cod_livro = l.cod_livro
        LEFT JOIN (
            SELECT cod_livro, COUNT(*) AS cnt 
            FROM biblioteca_db1.tb_reserva
            GROUP BY cod_livro
        ) res ON res.cod_livro = l.cod_livro
    """)
    upsert_table(df_pop, "agg_livro_popularidade")

    print("  * build_aggs: agg_autor_popularidade")
    df_aut = load_table("""
        SELECT a.cod_autor AS autor_id, a.nome_autor,
               COALESCE(SUM(x.cnt),0) AS total_emprestimos
        FROM biblioteca_db1.tb_autor a
        LEFT JOIN biblioteca_db1.tb_livro_autor la 
            ON la.cod_autor = a.cod_autor
        LEFT JOIN (
            SELECT cod_livro, COUNT(*) AS cnt
            FROM biblioteca_db1.tb_emprestimo
            GROUP BY cod_livro
        ) x ON x.cod_livro = la.cod_livro
        GROUP BY a.cod_autor, a.nome_autor
    """)
    upsert_table(df_aut, "agg_autor_popularidade")

    print("  * build_aggs: agg_tema_popularidade")
    df_tema = load_table("""
        SELECT t.cod_tema AS tema_id, t.nome_tema,
               COALESCE(SUM(x.cnt),0) AS total_emprestimos
        FROM biblioteca_db1.tb_tema t
        LEFT JOIN biblioteca_db1.tb_livro_tema lt 
            ON lt.cod_tema = t.cod_tema
        LEFT JOIN (
            SELECT cod_livro, COUNT(*) AS cnt
            FROM biblioteca_db1.tb_emprestimo
            GROUP BY cod_livro
        ) x ON x.cod_livro = lt.cod_livro
        GROUP BY t.cod_tema, t.nome_tema
    """)
    upsert_table(df_tema, "agg_tema_popularidade")

    print("  * build_aggs: metric_emprestimo_summary")
    df_m = load_table("""
        SELECT
            COUNT(CASE WHEN cod_status = 1 THEN 1 END) AS total_emp_ativos,
            AVG(TIMESTAMPDIFF(DAY, dt_inicio, dt_fim)) AS tempo_medio_emprestimo_days,
            (SUM(num_renovacoes) / NULLIF(COUNT(*),0)) AS taxa_renovacao
        FROM biblioteca_db1.tb_emprestimo;
    """)
    # ensure columns exist
    if df_m.empty:
        print("  ! metric_emprestimo_summary: query retornou vazio — inserindo snapshot vazio")
    df_m["snapshot_dt"] = datetime.utcnow()
    upsert_table(df_m, "metric_emprestimo_summary", if_exists="append")

    print("  * build_aggs: metric_usuario_summary")
    df_u = load_table("""
        SELECT
            (SELECT COUNT(*) FROM biblioteca_db1.tb_usuario) AS total_usuarios,
            SUM(CASE WHEN char_sexo='M' THEN 1 ELSE 0 END) AS distrib_masc,
            SUM(CASE WHEN char_sexo='F' THEN 1 ELSE 0 END) AS distrib_fem,
            SUM(CASE WHEN char_sexo NOT IN ('M','F') THEN 1 ELSE 0 END) AS distrib_outros
        FROM biblioteca_db1.tb_usuario;
    """)
    df_u["snapshot_dt"] = datetime.utcnow()
    upsert_table(df_u, "metric_usuario_summary", if_exists="append")


# -----------------------------------------------------
# EXTRA AGGREGATES (FAIXA ETÁRIA + RESERVAS)
# -----------------------------------------------------
def build_extra_aggs():
    print("  * build_extra_aggs: agg_faixa_etaria_livros")
    df_faixa = load_table("""
        SELECT 
            CASE
                WHEN TIMESTAMPDIFF(YEAR, STR_TO_DATE(u.dt_nascimento, '%Y%m%d'), CURDATE()) <= 12 THEN 'Crianças'
                WHEN TIMESTAMPDIFF(YEAR, STR_TO_DATE(u.dt_nascimento, '%Y%m%d'), CURDATE()) BETWEEN 13 AND 20 THEN 'Jovens'
                WHEN TIMESTAMPDIFF(YEAR, STR_TO_DATE(u.dt_nascimento, '%Y%m%d'), CURDATE()) BETWEEN 21 AND 59 THEN 'Adultos'
                ELSE 'Idosos'
            END AS faixa,
            e.cod_livro AS livro_id,
            l.txt_titulo AS titulo,
            COUNT(*) AS total_emprestimos
        FROM biblioteca_db1.tb_emprestimo e
        JOIN biblioteca_db1.tb_livro l ON l.cod_livro = e.cod_livro
        JOIN biblioteca_db1.tb_usuario u ON u.cod_username = e.cod_username
        GROUP BY faixa, livro_id, l.txt_titulo
    """)
    upsert_table(df_faixa, "agg_faixa_etaria_livros")

    print("  * build_extra_aggs: agg_genero_favorito")
    df_gen = load_table("""
        SELECT 
            u.char_sexo AS sexo,
            t.cod_tema AS tema_id,
            t.nome_tema,
            COUNT(*) AS total_emprestimos
        FROM biblioteca_db1.tb_emprestimo e
        JOIN biblioteca_db1.tb_usuario u ON u.cod_username = e.cod_username
        JOIN biblioteca_db1.tb_livro_tema lt ON lt.cod_livro = e.cod_livro
        JOIN biblioteca_db1.tb_tema t ON t.cod_tema = lt.cod_tema
        GROUP BY sexo, tema_id, t.nome_tema
    """)
    upsert_table(df_gen, "agg_genero_favorito")

    print("  * build_extra_aggs: agg_reservas_summary")
    df_res = load_table("""
        SELECT
            (SELECT COUNT(*) FROM biblioteca_db1.tb_reserva) AS total_reservas,
            (SELECT COUNT(*) FROM biblioteca_db1.tb_reserva WHERE cod_status_reserva = 3) 
            AS total_canceladas
    """)
    df_res["snapshot_dt"] = datetime.utcnow()
    upsert_table(df_res, "agg_reservas_summary", if_exists="append")

    print("  * build_extra_aggs: agg_livros_reservados")
    df_liv = load_table("""
        SELECT l.cod_livro AS livro_id,
               l.txt_titulo AS titulo,
               COUNT(r.cod_reserva) AS total_reservas
        FROM biblioteca_db1.tb_reserva r
        JOIN biblioteca_db1.tb_livro l ON l.cod_livro = r.cod_livro
        GROUP BY l.cod_livro, l.txt_titulo
    """)
    upsert_table(df_liv, "agg_livros_reservados")


# -----------------------------------------------------
# MAIN PIPELINE
# -----------------------------------------------------
def main():
    try:
        print("Building Dimensions...")
        build_dims()

        print("Building Facts...")
        build_facts()

        print("Building Aggregates...")
        build_aggs()

        print("Building Extra Aggregates...")
        build_extra_aggs()

        print("ETL Completed!")
    except Exception as e:
        print("ETL falhou com erro:")
        traceback.print_exc()
        sys.exit(1)


if __name__ == "__main__":
    main()
