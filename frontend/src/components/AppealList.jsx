import React, { useEffect, useState } from 'react';
import { fetchAppeals, manualSync } from '../api';

export default function AppealList({ selectedId, onSelectAppeal }) {
  const [appeals, setAppeals] = useState([]);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);
  const [syncing, setSyncing] = useState(false);

  const loadAppeals = async (showLoading = false) => {
    if (showLoading) setLoading(true);
    try {
      const data = await fetchAppeals();
      setAppeals(data);
      setError(null);
    } catch (err) {
      setError(err.message);
    } finally {
      if (showLoading) setLoading(false);
    }
  };

  const handleSyncNow = async () => {
    setSyncing(true);
    setError(null);
    try {
      await manualSync();
      await loadAppeals(false);
    } catch (err) {
      setError(`Sync failed: ${err.message}`);
    } finally {
      setSyncing(false);
    }
  };

  useEffect(() => {
    loadAppeals(true);

    const interval = setInterval(() => {
      loadAppeals(false);
    }, 15000); // Poll every 15 seconds

    return () => clearInterval(interval);
  }, []);

  if (loading) {
    return <div className="loading-state">Loading appeals queue...</div>;
  }

  return (
    <div className="appeal-list-card">
      <div className="card-header">
        <h2>Appeals Queue</h2>
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <button
            className="btn btn-primary"
            onClick={handleSyncNow}
            disabled={syncing}
          >
            {syncing ? 'Syncing...' : 'Sync Now'}
          </button>
          <button className="btn btn-secondary" onClick={() => loadAppeals(true)}>Refresh Now</button>
        </div>
      </div>
      {error && <div className="error-message">Error: {error}</div>}
      <div className="table-wrapper">
        <table className="appeal-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Customer</th>
              <th>Subject</th>
              <th>Status</th>
              <th>Last Updated</th>
            </tr>
          </thead>
          <tbody>
            {appeals.map((appeal) => {
              const isSelected = appeal.id === selectedId;
              return (
                <tr
                  key={appeal.id}
                  className={`appeal-row ${isSelected ? 'active-row' : ''}`}
                  onClick={() => onSelectAppeal(appeal.id)}
                >
                  <td className="col-id">#{appeal.id}</td>
                  <td className="col-customer"><strong>{appeal.customerName}</strong></td>
                  <td className="col-subject">{appeal.subject}</td>
                  <td className="col-status">
                    <span className={`status-badge badge-${appeal.status.toLowerCase()}`}>
                      {appeal.status.replace('_', ' ')}
                    </span>
                  </td>
                  <td className="col-date">{new Date(appeal.lastUpdated).toLocaleString()}</td>
                </tr>
              );
            })}
            {appeals.length === 0 && (
              <tr>
                <td colSpan="5" className="empty-row">No appeals synced yet.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
