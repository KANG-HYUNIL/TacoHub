import React from 'react';


const Header: React.FC = () => (
  <header className="header">
    <h1 className="header-title">TacoHub</h1>
    <nav className="header-nav">
      <ul>
        <li><a href="/">Home</a></li>
        <li><a href="/login">Login</a></li>
        <li><a href="/signup">Sign Up</a></li>
        <li><a href="/desc">Description</a></li>
      </ul>
    </nav>
  </header>
);

export default Header;
