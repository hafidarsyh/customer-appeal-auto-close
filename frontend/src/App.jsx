import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import StaffIntranetPage from './pages/StaffIntranetPage';
import PublicAppealForm from './pages/PublicAppealForm';

export default function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<StaffIntranetPage />} />
        <Route path="/submit-appeal" element={<PublicAppealForm />} />
      </Routes>
    </Router>
  );
}
