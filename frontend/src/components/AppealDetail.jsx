import React, { useEffect, useState } from 'react';
import { fetchAppealDetail, respondToAppeal } from '../api';

export default function AppealDetail({ appealId, onResponseSubmitted }) {
  const [appeal, setAppeal] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [responseText, setResponseText] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [submitSuccess, setSubmitSuccess] = useState(false);

  useEffect(() => {
    if (!appealId) return;

    const loadDetails = async () => {
      setLoading(true);
      setError(null);
      setSubmitSuccess(false);
      setResponseText('');
      try {
        const data = await fetchAppealDetail(appealId);
        setAppeal(data);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    loadDetails();
  }, [appealId]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!responseText.trim()) return;

    setSubmitting(true);
    setError(null);
    try {
      await respondToAppeal(appealId, responseText);
      setSubmitSuccess(true);
      
      // Reload details to show updated status/response
      const updated = await fetchAppealDetail(appealId);
      setAppeal(updated);

      if (onResponseSubmitted) {
        onResponseSubmitted();
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  if (!appealId) {
    return (
      <div className="appeal-detail-card empty-detail">
        <p>Select an appeal from the queue to view details and respond.</p>
      </div>
    );
  }

  if (loading) {
    return <div className="loading-state">Loading details for appeal #{appealId}...</div>;
  }

  if (!appeal) {
    return (
      <div className="appeal-detail-card error-detail">
        <p className="error-message">{error || 'Could not load appeal.'}</p>
      </div>
    );
  }

  return (
    <div className="appeal-detail-card">
      <div className="detail-header">
        <div className="header-title">
          <span className="detail-id">Appeal #{appeal.id}</span>
          <span className={`status-badge badge-${appeal.status.toLowerCase()}`}>
            {appeal.status.replace('_', ' ')}
          </span>
        </div>
        <div className="detail-meta">
          <span><strong>Submitted:</strong> {new Date(appeal.submittedAt).toLocaleString()}</span>
          <span><strong>Last Updated:</strong> {new Date(appeal.lastUpdated).toLocaleString()}</span>
        </div>
      </div>

      <div className="detail-section">
        <h3>Customer Information</h3>
        <p><strong>Name:</strong> {appeal.customerName}</p>
        <p><strong>Subject:</strong> {appeal.subject}</p>
      </div>

      <div className="detail-section">
        <h3>Appeal Message</h3>
        <blockquote className="message-text">{appeal.message}</blockquote>
      </div>

      {appeal.status === 'CLOSED' && (
        <div className="detail-section closed-banner">
          <h3>Case Closed</h3>
          <p><strong>Closed At:</strong> {appeal.closedAt ? new Date(appeal.closedAt).toLocaleString() : 'N/A'}</p>
          <p><strong>Reason:</strong> {appeal.closeReason || 'No reason specified'}</p>
        </div>
      )}

      <div className="detail-section action-section">
        <h3>Officer Actions</h3>
        
        {error && <div className="error-message">Error: {error}</div>}
        {submitSuccess && <div className="success-message">Response submitted successfully!</div>}

        {appeal.status === 'OPEN' ? (
          <form onSubmit={handleSubmit} className="response-form">
            <label htmlFor="officer-response">Write your response to the customer:</label>
            <textarea
              id="officer-response"
              rows="6"
              placeholder="Type response message here..."
              value={responseText}
              onChange={(e) => setResponseText(e.target.value)}
              required
              disabled={submitting}
            />
            <button
              type="submit"
              className="btn btn-primary"
              disabled={submitting || !responseText.trim()}
            >
              {submitting ? 'Submitting...' : 'Submit Response'}
            </button>
          </form>
        ) : (
          <div className="previous-response">
            <p><strong>Officer Response:</strong></p>
            {appeal.officerResponse ? (
              <blockquote className="response-text">{appeal.officerResponse}</blockquote>
            ) : (
              <p className="no-response-text">No officer response was registered for this case.</p>
            )}
            {appeal.respondedAt && (
              <p className="response-date">
                <strong>Responded At:</strong> {new Date(appeal.respondedAt).toLocaleString()}
              </p>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
