import React from 'react';
import './DescPage.css';

const DescPage: React.FC = () => {
  return (
    <div className="desc-container">
      <header className="desc-header">
        <h1>TacoHub 사용법 및 정보</h1>
      </header>
      <main className="desc-main">
        {/* 아래 영역에 TacoHub 사용법, FAQ, 안내 등 자유롭게 추가 */}
        <section className="desc-section">
          {/* 예시: <h2>시작하기</h2> <p>내용</p> */}
        </section>
        <section className="desc-section">
          {/* 예시: <h2>주요 기능</h2> <p>내용</p> */}
        </section>
        <section className="desc-section">
          {/* 예시: <h2>자주 묻는 질문</h2> <p>내용</p> */}
        </section>
      </main>
    </div>
  );
};

export default DescPage;
