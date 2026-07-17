import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { submitAppeal } from '../api';

export default function PublicAppealForm() {
  const [formData, setFormData] = useState({
    customerName: '',
    subject: '',
    message: '',
  });

  const [fieldErrors, setFieldErrors] = useState({});
  const [generalError, setGeneralError] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [successData, setSuccessData] = useState(null);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
    // Clear field-level error when user starts typing again
    if (fieldErrors[name]) {
      setFieldErrors((prev) => ({
        ...prev,
        [name]: '',
      }));
    }
  };

  const validateForm = () => {
    const errors = {};
    if (!formData.customerName.trim()) {
      errors.customerName = 'Customer name is required';
    }
    if (!formData.subject.trim()) {
      errors.subject = 'Subject is required';
    }
    if (!formData.message.trim()) {
      errors.message = 'Appeal message is required';
    }
    return errors;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFieldErrors({});
    setGeneralError(null);

    const clientErrors = validateForm();
    if (Object.keys(clientErrors).length > 0) {
      setFieldErrors(clientErrors);
      return;
    }

    setSubmitting(true);
    try {
      const response = await submitAppeal(formData);
      setSuccessData(response);
      setFormData({ customerName: '', subject: '', message: '' });
    } catch (err) {
      if (err.isValidationError) {
        setFieldErrors(err.errors);
      } else {
        setGeneralError(err.message || 'An unexpected error occurred. Please try again.');
      }
    } finally {
      setSubmitting(false);
    }
  };

  if (successData) {
    return (
      <div className="app-container" style={{ justifyContent: 'center', alignItems: 'center', minHeight: '100vh', padding: '2rem' }}>
        <div className="appeal-detail-card" style={{ maxWidth: '600px', height: 'auto', textAlign: 'center', gap: '2rem', padding: '3rem' }}>
          <div>
            <span className="logo-icon" style={{ fontSize: '4rem', display: 'block', marginBottom: '1rem' }}>✅</span>
            <h2 className="detail-id" style={{ fontSize: '2rem', color: '#ffffff', marginBottom: '0.5rem' }}>Submission Received</h2>
            <p style={{ color: 'var(--text-secondary)', fontSize: '1rem' }}>
              Your appeal has been successfully filed with the council.
            </p>
          </div>

          <div className="closed-banner" style={{ background: 'rgba(16, 185, 129, 0.08)', border: '1px solid rgba(16, 185, 129, 0.2)', padding: '1.5rem', borderRadius: '12px' }}>
            <h3 style={{ color: 'var(--success)', marginBottom: '0.5rem', fontSize: '1.1rem' }}>Reference Number</h3>
            <div style={{ fontFamily: 'var(--font-title)', fontSize: '2.2rem', fontWeight: 'bold', color: '#ffffff', letterSpacing: '0.05em' }}>
              #{successData.referenceId}
            </div>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', marginTop: '0.5rem' }}>
              Please quote this number if you need to contact us about this appeal.
            </p>
          </div>

          <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center' }}>
            <button className="btn btn-primary" onClick={() => setSuccessData(null)}>
              Submit Another Appeal
            </button>
            <Link to="/" className="btn btn-secondary">
              Back to Intranet
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="app-container" style={{ minHeight: '100vh' }}>
      <header className="app-header">
        <div className="header-content">
          <div className="logo-section">
            <span className="logo-icon">🏛️</span>
            <h1>Public Appeal Portal</h1>
          </div>
          <Link to="/" className="badge-officer" style={{ textDecoration: 'none', cursor: 'pointer' }}>
            Staff Intranet
          </Link>
        </div>
      </header>

      <main className="app-main" style={{ justifyContent: 'center', alignItems: 'flex-start', overflowY: 'auto' }}>
        <div className="appeal-detail-card" style={{ maxWidth: '700px', width: '100%', height: 'auto', padding: '2.5rem', gap: '1.5rem' }}>
          <div className="detail-header" style={{ paddingBottom: '1rem' }}>
            <h2 style={{ fontFamily: 'var(--font-title)', fontSize: '1.75rem', color: '#ffffff' }}>Submit a New Appeal</h2>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginTop: '0.25rem' }}>
              Please fill out the form below to register your official appeal. All fields are required.
            </p>
          </div>

          {generalError && (
            <div className="error-message">
              <strong>Error:</strong> {generalError}
            </div>
          )}

          <form onSubmit={handleSubmit} className="response-form" style={{ gap: '1.25rem' }}>
            <div className="detail-section">
              <label htmlFor="customerName" style={{ fontWeight: '500' }}>Your Full Name</label>
              <textarea
                id="customerName"
                name="customerName"
                rows="1"
                placeholder="e.g. Grace Kim"
                value={formData.customerName}
                onChange={handleInputChange}
                disabled={submitting}
                style={{ resize: 'none', height: '42px', padding: '0.65rem 1rem' }}
              />
              {fieldErrors.customerName && (
                <span className="error-message" style={{ display: 'block', marginTop: '0.25rem', padding: '0.35rem 0.75rem', fontSize: '0.8rem' }}>
                  {fieldErrors.customerName}
                </span>
              )}
            </div>

            <div className="detail-section">
              <label htmlFor="subject" style={{ fontWeight: '500' }}>Appeal Subject</label>
              <textarea
                id="subject"
                name="subject"
                rows="1"
                placeholder="e.g. Speeding fine dispute"
                value={formData.subject}
                onChange={handleInputChange}
                disabled={submitting}
                style={{ resize: 'none', height: '42px', padding: '0.65rem 1rem' }}
              />
              {fieldErrors.subject && (
                <span className="error-message" style={{ display: 'block', marginTop: '0.25rem', padding: '0.35rem 0.75rem', fontSize: '0.8rem' }}>
                  {fieldErrors.subject}
                </span>
              )}
            </div>

            <div className="detail-section">
              <label htmlFor="message" style={{ fontWeight: '500' }}>Explain Your Appeal Message</label>
              <textarea
                id="message"
                name="message"
                rows="6"
                placeholder="Please describe your appeal in detail here..."
                value={formData.message}
                onChange={handleInputChange}
                disabled={submitting}
              />
              {fieldErrors.message && (
                <span className="error-message" style={{ display: 'block', marginTop: '0.25rem', padding: '0.35rem 0.75rem', fontSize: '0.8rem' }}>
                  {fieldErrors.message}
                </span>
              )}
            </div>

            <div style={{ display: 'flex', gap: '1rem', marginTop: '0.5rem' }}>
              <button
                type="submit"
                className="btn btn-primary"
                disabled={submitting}
                style={{ flex: 1 }}
              >
                {submitting ? 'Submitting Appeal...' : 'Submit Appeal Form'}
              </button>
              <Link to="/" className="btn btn-secondary" style={{ textDecoration: 'none' }}>
                Cancel
              </Link>
            </div>
          </form>
        </div>
      </main>
    </div>
  );
}
