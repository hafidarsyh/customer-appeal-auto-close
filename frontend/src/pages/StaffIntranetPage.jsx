import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import AppealList from '../components/AppealList';
import AppealDetail from '../components/AppealDetail';

export default function StaffIntranetPage() {
  const [selectedId, setSelectedId] = useState(null);
  const [listVersion, setListVersion] = useState(0);

  const handleResponseSubmitted = () => {
    // Increment version key to force AppealList to refresh immediately
    setListVersion((prev) => prev + 1);
  };

  return (
    <div className="app-container">
      <header className="app-header">
        <div className="header-content">
          <div className="logo-section">
            <span className="logo-icon">🏛️</span>
            <h1>Council Officer Intranet</h1>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
            <Link to="/submit-appeal" className="btn btn-secondary" style={{ textDecoration: 'none' }}>
              Public Appeal Form
            </Link>
            <span className="badge-officer">Active Session: Officer</span>
          </div>
        </div>
      </header>
      <main className="app-main">
        <div className="app-sidebar">
          <AppealList
            key={listVersion}
            selectedId={selectedId}
            onSelectAppeal={setSelectedId}
          />
        </div>
        <div className="app-content">
          <AppealDetail
            appealId={selectedId}
            onResponseSubmitted={handleResponseSubmitted}
          />
        </div>
      </main>
    </div>
  );
}
