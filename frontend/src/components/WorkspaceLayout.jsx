import React from 'react';
import { Link, NavLink, Outlet } from 'react-router-dom';
import AccountNav from '../auth/AccountNav';
import styles from './WorkspaceLayout.module.css';

export default function WorkspaceLayout({ links, label }) {
  return <>
    <header className={styles.header}><div className="shell nav">
      <Link className="brand" to="/">vCraft<span>.</span></Link>
      <nav aria-label="Tài khoản"><AccountNav /></nav>
    </div></header>
    <main className={'shell ' + styles.layout}>
      <nav className={styles.sidebar} aria-label={label}>
        {links.map(([path, title]) => <NavLink key={path} to={path} end>{title}</NavLink>)}
      </nav>
      <div className={styles.content}><Outlet /></div>
    </main>
  </>;
}
