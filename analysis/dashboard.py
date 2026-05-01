import streamlit as st
import pandas as pd
import plotly.express as px
import plotly.graph_objects as go
from sqlalchemy import create_engine
import os
import time

# 데이터베이스 연결 설정
DB_HOST = os.getenv('ANALYSIS_DB_HOST', 'localhost')
DB_PORT = os.getenv('ANALYSIS_DB_PORT', '3308')
DB_USER = 'root'
DB_PASSWORD = 'password'
DB_NAME = 'analysis_db'

st.set_page_config(page_title="🎓 강의 구매 패턴 분석 대시보드", layout="wide")

st.title("🎓 온라인 강의 실시간 구매 패턴 분석")
st.markdown("Redis Streams를 통해 수집된 사용자 행동 데이터를 실시간으로 시각화합니다.")

# DB 연결 엔진 생성
@st.cache_resource
def get_engine():
    connection_string = f"mysql+mysqlconnector://{DB_USER}:{DB_PASSWORD}@{DB_HOST}:{DB_PORT}/{DB_NAME}"
    return create_engine(connection_string)

def load_data():
    engine = get_engine()
    try:
        query = "SELECT * FROM payment_analysis"
        df = pd.read_sql(query, engine)
        return df
    except Exception as e:
        st.error(f"데이터를 불러오는 데 실패했습니다: {e}")
        return pd.DataFrame()

# 대시보드 리프레시 버튼
if st.button('🔄 데이터 새로고침'):
    st.rerun()

df = load_data()

if not df.empty:
    # 전처리: eventTime을 datetime 객체로 변환
    df['eventTime'] = pd.to_datetime(df['eventTime'])
    
    # 상단 지표 (Metrics)
    col1, col2, col3, col4 = st.columns(4)
    with col1:
        st.metric("총 트래픽", f"{len(df)} 건")
    with col2:
        purchase_count = len(df[df['status'] == 'PURCHASE'])
        st.metric("구매 완료", f"{purchase_count} 건")
    with col3:
        error_count = len(df[df['status'] == 'ERROR'])
        st.metric("결제 오류", f"{error_count} 건", delta=f"{error_count/len(df)*100:.1f}%", delta_color="inverse")
    with col4:
        total_revenue = df[df['status'] == 'PURCHASE']['amount'].sum()
        st.metric("누적 매출액", f"{total_revenue:,.0f} 원")

    st.divider()

    # 1. 결제 퍼널 분석 (Funnel Chart)
    st.subheader("📊 1. 결제 퍼널 분석 (Customer Journey)")
    funnel_order = ['ADD_TO_CART', 'INITIATE_CHECKOUT', 'PURCHASE']
    funnel_counts = df[df['status'].isin(funnel_order)]['status'].value_counts().reindex(funnel_order)
    
    fig_funnel = go.Figure(go.Funnel(
        y=funnel_counts.index,
        x=funnel_counts.values,
        textinfo="value+percent initial"
    ))
    st.plotly_chart(fig_funnel, use_container_width=True)

    col_left, col_right = st.columns(2)

    with col_left:
        # 2. 연령대별 인기 강의 카테고리
        st.subheader("🔥 2. 연령대별 인기 카테고리")
        df['age_group'] = (df['age'] // 10) * 10
        purchase_df = df[df['status'] == 'PURCHASE']
        age_cat_df = purchase_df.groupby(['age_group', 'category']).size().reset_index(name='count')
        
        fig_bar = px.bar(age_cat_df, x='age_group', y='count', color='category', 
                         title="연령대별 구매 완료 건수", barmode='group')
        st.plotly_chart(fig_bar, use_container_width=True)

    with col_right:
        # 3. 플랫폼별 결제 에러 발생 현황
        st.subheader("⚠️ 3. 플랫폼별 결제 에러 현황")
        error_df = df[df['status'] == 'ERROR']
        if not error_df.empty:
            fig_pie = px.pie(error_df, names='platform', title="에러 발생 기기 비중", hole=0.4)
            st.plotly_chart(fig_pie, use_container_width=True)
        else:
            st.info("아직 발생한 에러 데이터가 없습니다.")

    # 4. 실시간 트래픽 추이
    st.subheader("📈 4. 실시간 트래픽 추이 (1분 단위)")
    df_trend = df.resample('1min', on='eventTime').size().reset_index(name='count')
    fig_trend = px.line(df_trend, x='eventTime', y='count', title="시간대별 이벤트 발생 빈도")
    st.plotly_chart(fig_trend, use_container_width=True)

else:
    st.warning("데이터베이스에 아직 데이터가 없습니다. Producer를 실행하여 데이터를 생성해 주세요.")
